package org.jboss.pnc.cleaner.builds;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
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
import org.jboss.pnc.cleaner.auth.KeycloakServiceClient;
import org.jboss.pnc.client.BuildClient;
import org.jboss.pnc.client.RemoteCollection;
import org.jboss.pnc.client.RemoteResourceException;
import org.jboss.pnc.client.RemoteResourceNotFoundException;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.enums.BuildStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

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
    KeycloakServiceClient serviceClient;

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
        failedStatuses.add(BuildStatus.CANCELLED); // added for old data, now cancellation cleans up the Indy data
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

    @Scheduled(cron = "{failedbuildscleaner.cron}")
    void cleanRegularly() {
        logger.info("Starting regular failed builds cleanup job.");
        Instant limit = Instant.now().minus(retention, ChronoUnit.HOURS);
        logger.debug("Cleaning up failed builds older than {}.", limit);
        cleanOlder(limit);
    }

    /**
     * Cleans builds started before the provided point in time.
     *
     * @param limit point in time marking the line which builds should be deleted
     */
    @Timed
    public void cleanOlder(Instant limit) {
        logger.info("Retrieving service account auth token.");
        String serviceAccountToken = serviceClient.getAuthToken();

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
            logger.debug("Creating Indy authenticator.");
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
        logger.debug("Loading build record for group {}.", groupName);
        try {
            boolean clean = shouldClean(packageType, groupName, session);

            if (clean) {
                logger.info("Cleaning {} repositories for {}.", packageType, groupName);
                IndyStoresClientModule stores = session.getStores();
                try {
                    // delete the content
                    logger.debug("Cleaning {} group and hosted repository {}.", packageType, groupName);
                    deleteGroupAndHostedRepo(packageType, groupName, stores);

                    logger.debug("Searching for generic-http stores for {}.", groupName);
                    List<StoreKey> genericRepos = findGenericRepos(groupName, session);
                    for (StoreKey genericRepo : genericRepos) {
                        stores.delete(genericRepo, "Scheduled cleanup of failed builds.");
                    }

                    // delete the tracking record - mostly not needed, only in case the build failed in
                    // promotion phase and the tracking report was already sealed
                    IndyFoloAdminClientModule foloAdmin = session.getFoloAdmin();
                    logger.debug("Cleaning tracking record {} (if present).", groupName);
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
                logger.debug(
                        "Build record for {} group {} is older than the limit. Cleaning...",
                        packageType,
                        groupName);
                clean = true;
            } else {
                logger.debug(
                        "Build record for {} group {} is younger than the limit. Skipping.",
                        packageType,
                        groupName);
            }
        } else {
            logger.debug(
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
    private Build getBuildRecord(String buildContentId) throws CleanerException {
        logger.debug("Looking for build record with query \"buildContentId==" + buildContentId + "\"");

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
                    logger.debug("Attempting to find build record by id {}", id);
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
                logger.debug("Build with buildContentId = {} found.", buildContentId);
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
    private void deleteGroupAndHostedRepo(String pkgKey, String repoName, IndyStoresClientModule stores)
            throws IndyClientException {
        StoreKey groupKey = new StoreKey(pkgKey, StoreType.group, repoName);
        if (stores.exists(groupKey)) {
            logger.trace("{} group {} exists - deleting...", pkgKey, repoName);
            stores.delete(groupKey, "Scheduled cleanup of failed builds.");
        }

        StoreKey storeKey = new StoreKey(pkgKey, StoreType.hosted, repoName);
        if (stores.exists(storeKey)) {
            logger.trace("{} hosted repo {} exists - deleting...", pkgKey, repoName);
            stores.delete(storeKey, "Scheduled cleanup of failed builds.");
        }
    }
}
