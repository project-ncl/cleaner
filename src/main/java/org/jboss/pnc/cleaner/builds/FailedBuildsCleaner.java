/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2019-2022 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.cleaner.builds;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Scope;
import jakarta.annotation.PostConstruct;

import io.quarkus.oidc.client.OidcClient;
import io.quarkus.scheduler.Scheduled;

import org.commonjava.indy.client.core.Indy;
import org.commonjava.indy.client.core.IndyClientException;
import org.commonjava.indy.client.core.IndyClientModule;
import org.commonjava.indy.client.core.auth.IndyClientAuthenticator;
import org.commonjava.indy.client.core.auth.OAuth20BearerTokenAuthenticator;
import org.commonjava.indy.client.core.module.IndyStoresClientModule;
import org.commonjava.indy.folo.client.IndyFoloAdminClientModule;
import org.commonjava.indy.folo.client.IndyFoloContentClientModule;
import org.commonjava.indy.model.core.Group;
import org.commonjava.indy.model.core.StoreKey;
import org.commonjava.indy.model.core.StoreType;
import org.commonjava.indy.model.core.dto.StoreListingDTO;
import org.commonjava.indy.model.core.io.IndyObjectMapper;
import org.commonjava.util.jhttpc.model.SiteConfig;
import org.commonjava.util.jhttpc.model.SiteConfigBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.pnc.api.constants.MDCHeaderKeys;
import org.jboss.pnc.client.BuildClient;
import org.jboss.pnc.client.RemoteCollection;
import org.jboss.pnc.client.RemoteResourceException;
import org.jboss.pnc.client.RemoteResourceNotFoundException;
import org.jboss.pnc.common.Strings;
import org.jboss.pnc.common.otel.OtelUtils;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.enums.BuildStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.text.MessageFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.commonjava.indy.pkg.maven.model.MavenPackageTypeDescriptor.MAVEN_PKG_KEY;
import static org.commonjava.indy.pkg.npm.model.NPMPackageTypeDescriptor.NPM_PKG_KEY;

@SuppressWarnings("deprecation")
@ApplicationScoped
public class FailedBuildsCleaner {

    /**
     * Builds have format with build-{build-id} where {build-id} is a 13 characters long base32 number.
     */
    private static final Pattern INDY_BUILD_GROUP_PATTERN = Pattern.compile("build-([A-Z0-9]{13})");
    private static final String className = FailedBuildsCleaner.class.getName();

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    OidcClient oidcClient;

    @Inject
    BuildClient buildClient;

    /** Retention time in hours. */
    @ConfigProperty(name = "failedbuildscleaner.retention")
    Integer retention;

    /** Base URL of Indy. */
    @ConfigProperty(name = "failedbuildscleaner.indyurl")
    String indyUrl;

    @ConfigProperty(name = "failedbuildscleaner.indy.requesttimeout")
    int indyRequestTimeout;

    @ConfigProperty(name = "pnc-cleaner.indy-client.metrics.enabled", defaultValue = "false")
    Boolean indyClientMetricsEnabled;

    @ConfigProperty(name = "pnc-cleaner.indy-client.metrics.honeycombDataset")
    Optional<String> indyClientMetricsHoneycombDataset;

    @ConfigProperty(name = "pnc-cleaner.indy-client.metrics.honeycombWriteKey")
    Optional<String> indyClientMetricsHoneycombWriteKey;

    @ConfigProperty(name = "pnc-cleaner.indy-client.metrics.baseSampleRate")
    Optional<Integer> indyClientMetricsBaseSampleRate;

    private static List<BuildStatus> failedStatuses;

    static {
        failedStatuses = new ArrayList<>(5);
        failedStatuses.add(BuildStatus.CANCELLED);
        failedStatuses.add(BuildStatus.SYSTEM_ERROR);
        failedStatuses.add(BuildStatus.FAILED);
    }

    @Inject
    MeterRegistry registry;

    private Counter errCounter;
    private Counter warnCounter;

    @PostConstruct
    void initMetrics() {
        errCounter = registry.counter(className + ".error.count");
        warnCounter = registry.counter(className + ".warning.count");
    }

    @Scheduled(cron = "{failedbuildscleaner.cron}", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    void cleanRegularly() {
        logger.info("Starting regular failed builds cleanup job.");
        Instant limit = Instant.now().minus(retention, ChronoUnit.HOURS);
        logger.info("Cleaning up failed builds older than {}.", limit);
        cleanOlder(limit);
    }

    /**
     * Cleans builds started before the provided point in time.
     *
     * @param limit point in time marking the line which builds should be deleted
     */
    @Timed
    public void cleanOlder(Instant limit) {

        // Create a parent child span with values from MDC
        SpanBuilder spanBuilder = OtelUtils.buildChildSpan(
                GlobalOpenTelemetry.get().getTracer(""),
                "FailedBuildsCleaner.cleanOlder",
                SpanKind.CLIENT,
                null,
                null,
                null,
                null,
                Span.current().getSpanContext(),
                Map.of());

        Span span = spanBuilder.startSpan();
        logger.debug("Started a new span :{}", span);

        // put the span into the current Context
        try (Scope scope = span.makeCurrent()) {
            logger.info("Retrieving service account auth token.");
            String serviceAccountToken = oidcClient.getTokens().await().indefinitely().getAccessToken();

            logger.info("Initializing Indy client.");
            Indy indyClient = initIndy(serviceAccountToken);
            FailedBuildsCleanerSession session = new FailedBuildsCleanerSession(indyClient, limit);

            // get list of build group names from Indy
            String[] packageTypes = new String[] { MAVEN_PKG_KEY, NPM_PKG_KEY };
            for (String packageType : packageTypes) {
                logger.info("Loading list of existing {} repository groups from Indy.", packageType);
                List<String> groupNames = getGroupNames(packageType, session);

                // cycle through them and clean one by one
                for (String groupName : groupNames) {
                    cleanBuildIfNeeded(packageType, groupName, session);
                }
            }
        } finally {
            span.end(); // closing the scope does not end the span, this has to be done manually
        }
    }

    /**
     * Initializes Indy client with given access token.
     *
     * @param accessToken
     * @return
     */
    @Timed
    Indy initIndy(String accessToken) {
        IndyClientAuthenticator authenticator = null;
        if (accessToken != null) {
            logger.info("Creating Indy authenticator.");
            authenticator = new OAuth20BearerTokenAuthenticator(accessToken);
        }
        try {
            SiteConfigBuilder siteConfigBuilder = new SiteConfigBuilder("indy", indyUrl)
                    .withRequestTimeoutSeconds(indyRequestTimeout)
                    // this client is used in single thread, we don't need more than 1 connection at a time
                    .withMaxConnections(1)
                    .withMetricEnabled(indyClientMetricsEnabled);
            if (indyClientMetricsEnabled) {
                if (indyClientMetricsHoneycombDataset.isPresent()) {
                    siteConfigBuilder.withHoneycombDataset(indyClientMetricsHoneycombDataset.get());
                }
                if (indyClientMetricsHoneycombWriteKey.isPresent()) {
                    siteConfigBuilder.withHoneycombWriteKey(indyClientMetricsHoneycombWriteKey.get());
                }
                if (indyClientMetricsBaseSampleRate.isPresent()) {
                    siteConfigBuilder.withBaseSampleRate(indyClientMetricsBaseSampleRate.get());
                }
            }
            SiteConfig siteConfig = siteConfigBuilder.build();

            IndyClientModule[] modules = new IndyClientModule[] {
                    new IndyFoloAdminClientModule(),
                    new IndyFoloContentClientModule() };

            Map<String, String> mdcCopyMappings = new HashMap<>(); // TODO fill in these if needed
            SpanContext spanContext = Span.current().getSpanContext();
            mdcCopyMappings.put(MDCHeaderKeys.TRACE_ID.getHeaderName(), spanContext.getTraceId());
            mdcCopyMappings.put(MDCHeaderKeys.SPAN_ID.getHeaderName(), spanContext.getSpanId());
            OtelUtils.createTraceStateHeader(spanContext).forEach((k, v) -> {
                if (!Strings.isEmpty(v)) {
                    mdcCopyMappings.put(k, v);
                }
            });
            OtelUtils.createTraceParentHeader(spanContext).forEach((k, v) -> {
                if (!Strings.isEmpty(v)) {
                    mdcCopyMappings.put(k, v);
                }
            });
            return new Indy(siteConfig, authenticator, new IndyObjectMapper(true), mdcCopyMappings, modules);
        } catch (IndyClientException e) {
            errCounter.increment();
            throw new IllegalStateException("Failed to create Indy client: " + e.getMessage(), e);
        }
    }

    /**
     * Loads Maven build group names from Indy.
     *
     * @param session initialized Indy client, cannot be <code>null</code>
     *
     * @return the loaded list of group names, can be empty, never <code>null</code>
     */
    @Timed
    List<String> getGroupNames(String packageType, FailedBuildsCleanerSession session) {
        IndyStoresClientModule indyStores = session.getStores();

        List<Group> groups;
        try {
            StoreListingDTO<Group> groupsListing = indyStores.listGroups(packageType);
            if (groupsListing == null) {
                errCounter.increment();
                throw new RuntimeException(
                        "Error getting Maven group list from Indy. The result " + "was empty. Check Indy URL.");
            }
            groups = groupsListing.getItems();
        } catch (IndyClientException e) {
            errCounter.increment();
            throw new RuntimeException("Error getting Maven group list from Indy: " + e.toString(), e);
        }
        List<String> result = groups.stream()
                .map(g -> g.getName())
                .filter(n -> INDY_BUILD_GROUP_PATTERN.matcher(n).matches())
                .collect(Collectors.toList());
        return result;
    }

    /**
     * Checks if the given group name matches an old enough failed build and if so it cleans everything produced by the
     * build. The cleaned data include tracking record, build group, build hosted repo and any generic http repos from
     * Indy.
     *
     * @param groupName the potentially cleaned group name
     * @param session cleaner session
     */
    @Timed
    void cleanBuildIfNeeded(String packageType, String groupName, FailedBuildsCleanerSession session) {
        logger.info("Loading build record for group {}.", groupName);
        try {
            boolean clean = shouldClean(packageType, groupName, session);

            if (clean) {
                logger.info("Cleaning {} repositories for {}.", packageType, groupName);
                IndyStoresClientModule stores = session.getStores();
                try {
                    // delete the content
                    logger.info("Cleaning {} group and hosted repository {}.", packageType, groupName);
                    deleteGroupAndHostedRepo(packageType, groupName, stores);

                    logger.info("Searching for generic-http stores for {}.", groupName);
                    List<StoreKey> genericRepos = findGenericRepos(groupName, session);
                    for (StoreKey genericRepo : genericRepos) {
                        stores.delete(genericRepo, "Scheduled cleanup of failed builds.");
                    }

                    // delete the tracking record - mostly not needed, only in case the build failed in
                    // promotion phase and the tracking report was already sealed
                    IndyFoloAdminClientModule foloAdmin = session.getFoloAdmin();
                    logger.info("Cleaning tracking record {} (if present).", groupName);
                    foloAdmin.clearTrackingRecord(groupName);
                } catch (IndyClientException e) {
                    errCounter.increment();
                    String description = MessageFormat.format("Failed to perform cleanups in Indy for %s", groupName);
                    logger.error(description, e);
                }
            }
        } catch (CleanerException ex) {
            errCounter.increment();
            logger.error("Error loading build record for group " + groupName + ". Skipping.", ex);
            ;
        }
    }

    /**
     * Checks if repo group with given name should be cleaned. It says so if the build record with matching
     * buildContentId could not be found (probably dropped before by temporary builds cleaner) or if the loaded build
     * record has one of the statuses listed in failedStatuses and
     *
     * @param groupName
     * @param session
     * @return
     * @throws CleanerException in case of an error when loading the build record
     */
    @Timed
    boolean shouldClean(String packageType, String groupName, FailedBuildsCleanerSession session)
            throws CleanerException {
        Build build = getBuildRecord(groupName);
        boolean clean = false;
        if (build == null) {
            warnCounter.increment();
            logger.warn(
                    "Build record for {} group {} not found. Assuming it was removed by "
                            + "temporary builds cleaner before failed builds cleaner got to it. Cleaning...",
                    packageType,
                    groupName);
            clean = true;
        } else if (failedStatuses.contains(build.getStatus())) {
            if (build.getEndTime().isBefore(session.getTo())) {
                logger.info(
                        "Build record for {} group {} is older than the limit. Cleaning...",
                        packageType,
                        groupName);
                clean = true;
            } else {
                logger.info(
                        "Build record for {} group {} is younger than the limit. Skipping.",
                        packageType,
                        groupName);
            }
        } else {
            logger.info(
                    "Build record's status for {} group {} is {}, which is not one of statuses to be cleaned.",
                    packageType,
                    groupName,
                    build.getStatus());
        }
        return clean;
    }

    /**
     * Finds storeKeys of repos matching the pattern used to store repos for generic http downloads. It finds groups
     * matching the pattern for given buildContentId and collects their keys along with keys of their constituents,
     * which are always a source remote repo and hosted repo to backup the downloaded binaries.
     *
     * @param buildContentId the build content ID
     * @return the list of matching store keys, might be empty, never null
     */
    @Timed
    List<StoreKey> findGenericRepos(String buildContentId, FailedBuildsCleanerSession session) {
        List<StoreKey> result = new ArrayList<>();
        for (Group genericGroup : session.getGenericGroups()) {
            if (genericGroup.getName().startsWith("g-") && genericGroup.getName().endsWith("-" + buildContentId)) {
                result.add(genericGroup.getKey());
                result.addAll(genericGroup.getConstituents());
            }
        }

        return result;
    }

    /**
     * Loads build record from PNC identified by given buildContentId. In case multiple build records match the id it
     * logs an error and returns null.
     *
     * @param buildContentId id of the wanted build
     * @return found build record or null
     */
    @Timed
    Build getBuildRecord(String buildContentId) throws CleanerException {
        logger.info("Looking for build record with query \"buildContentId==" + buildContentId + "\"");

        try {
            RemoteCollection<Build> builds = buildClient
                    .getAll(null, null, Optional.empty(), Optional.of("buildContentId==" + buildContentId));

            if (builds.size() > 1) {
                errCounter.increment();
                logger.error("Multiple build records found for buildContentId = {}", buildContentId);
                return null;

            } else if (builds.size() == 0) {
                warnCounter.increment();
                logger.warn("Build record NOT found for buildContentId = {}", buildContentId);

                Matcher matcher = INDY_BUILD_GROUP_PATTERN.matcher(buildContentId);
                if (matcher.matches()) {
                    String id = matcher.group(1);
                    logger.info("Attempting to find build record by id {}", id);
                    try {
                        return buildClient.getSpecific(id);
                    } catch (RemoteResourceNotFoundException e) {
                        warnCounter.increment();
                        logger.warn("Build record NOT found even by ID = {}", id);
                        return null;
                    }
                } else {
                    errCounter.increment();
                    logger.error("Unable to parse buildContentId \"{}\"", buildContentId);
                    return null;
                }
            } else {
                logger.info("Build with buildContentId = {} found.", buildContentId);
                return builds.iterator().next();
            }
        } catch (RemoteResourceException e) {
            errCounter.increment();
            throw new CleanerException(
                    "Error when getting build record [buildContentId=%s, status=%d].",
                    e,
                    buildContentId,
                    e.getStatus());
        }
    }

    /**
     * Deletes the build hosted repository and repo group from Indy if it exists.
     *
     * @param pkgKey package key
     * @param repoName repository name
     * @param stores Indy stores client module
     * @throws IndyClientException in case of an error happening in Indy
     */
    @Timed
    void deleteGroupAndHostedRepo(String pkgKey, String repoName, IndyStoresClientModule stores)
            throws IndyClientException {
        StoreKey groupKey = new StoreKey(pkgKey, StoreType.group, repoName);
        if (stores.exists(groupKey)) {
            logger.debug("{} group {} exists - deleting...", pkgKey, repoName);
            stores.delete(groupKey, "Scheduled cleanup of failed builds.");
        }

        StoreKey storeKey = new StoreKey(pkgKey, StoreType.hosted, repoName);
        if (stores.exists(storeKey)) {
            logger.debug("{} hosted repo {} exists - deleting...", pkgKey, repoName);
            stores.delete(storeKey, "Scheduled cleanup of failed builds.");
        }
    }
}
