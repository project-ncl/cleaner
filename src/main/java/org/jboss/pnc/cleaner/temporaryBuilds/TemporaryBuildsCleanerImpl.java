/**
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
package org.jboss.pnc.cleaner.temporaryBuilds;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Scope;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.pnc.cleaner.archiveservice.ArchivesCleaner;
import org.jboss.pnc.common.otel.OtelUtils;
import org.jboss.pnc.common.util.TimeUtils;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.GroupBuild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Deletes temporary builds via Orchestrator REST API
 *
 * @author Jakub Bartecek
 */
@ApplicationScoped
public class TemporaryBuildsCleanerImpl implements TemporaryBuildsCleaner {

    private static final String className = TemporaryBuildsCleanerImpl.class.getName();

    private final Logger log = LoggerFactory.getLogger(TemporaryBuildsCleanerImpl.class);

    @ConfigProperty(name = "temporaryBuildsCleaner.lifespan")
    Integer TEMPORARY_BUILD_LIFESPAN;

    @Inject
    TemporaryBuildsCleanerAdapter temporaryBuildsCleanerAdapter;

    @Inject
    ArchivesCleaner archivesCleaner;

    @Inject
    MeterRegistry registry;

    private Counter warnCounter;

    @PostConstruct
    void initMetrics() {
        warnCounter = registry.counter(className + ".warning.count");
    }

    @Timed
    @Override
    public void cleanupExpiredTemporaryBuilds() {
        log.info(
                "Regular cleanup of expired temporary builds started. Removing builds older than "
                        + TEMPORARY_BUILD_LIFESPAN + " days.");
        Date expirationThreshold = new Date(
                Instant.now().minus(TEMPORARY_BUILD_LIFESPAN, ChronoUnit.DAYS).toEpochMilli());

        // Create a parent child span with values from MDC
        SpanBuilder spanBuilder = OtelUtils.buildChildSpan(
                GlobalOpenTelemetry.get().getTracer(""),
                "TemporaryBuildsCleanerImpl.cleanupExpiredTemporaryBuilds",
                SpanKind.CLIENT,
                null,
                null,
                null,
                null,
                Span.current().getSpanContext(),
                Map.of(
                        "expirationThreshold",
                        String.format("%1$tY/%1$tm/%1$te %1$tH:%1$tM:%1$tS,%1$tL", expirationThreshold)));
        Span span = spanBuilder.startSpan();
        log.debug("Started a new span :{}", span);

        // put the span into the current Context
        try (Scope scope = span.makeCurrent()) {

            deleteExpiredBuildConfigSetRecords(expirationThreshold);
            deleteExpiredBuildRecords(expirationThreshold);

            log.info("Regular cleanup of expired temporary builds finished.");
        } finally {
            span.end(); // closing the scope does not end the span, this has to be done manually
        }
    }

    @Timed
    void deleteExpiredBuildConfigSetRecords(Date expirationThreshold) {
        Collection<GroupBuild> expiredBCSRecords = temporaryBuildsCleanerAdapter
                .findTemporaryGroupBuildsOlderThan(expirationThreshold);

        for (GroupBuild groupBuild : expiredBCSRecords) {
            try {
                log.info("Deleting temporary BuildConfigSetRecord {}", groupBuild);
                temporaryBuildsCleanerAdapter.deleteTemporaryGroupBuild(groupBuild.getId());
                log.info("Temporary BuildConfigSetRecord {} was deleted successfully", groupBuild);
            } catch (OrchInteractionException ex) {
                warnCounter.increment();
                log.warn("Deletion of temporary BuildConfigSetRecord {} failed!", groupBuild);
            }
        }
    }

    @Timed
    void deleteExpiredBuildRecords(Date expirationThreshold) {
        Set<Build> failedBuilds = new HashSet<>();
        Collection<Build> expiredBuilds = null;
        do {
            log.info("Doing an iteration of Temporary Builds deletion.");
            expiredBuilds = temporaryBuildsCleanerAdapter.findTemporaryBuildsOlderThan(expirationThreshold);
            expiredBuilds.removeAll(failedBuilds);
            for (Build build : expiredBuilds) {
                try {
                    log.info("Deleting temporary build {}", build);
                    temporaryBuildsCleanerAdapter.deleteTemporaryBuild(build.getId());
                    log.info("Temporary build {} was deleted successfully", build);

                    log.info(
                            "Deleting archive of temporary build {} with build config id {}",
                            build,
                            build.getBuildConfigRevision().getId());
                    archivesCleaner.deleteArchive(build.getBuildConfigRevision().getId());

                } catch (OrchInteractionException ex) {
                    warnCounter.increment();
                    log.warn("Deletion of temporary build {} failed! Cause: {}", build, ex);
                    failedBuilds.add(build);
                }
            }
        } while (!expiredBuilds.isEmpty());
    }
}
