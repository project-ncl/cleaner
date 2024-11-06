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
package org.jboss.pnc.cleaner.archiver;

import io.micrometer.core.annotation.Timed;
import io.quarkus.scheduler.Scheduled;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.pnc.cleaner.orchApi.OrchClientProducer;
import org.jboss.pnc.client.BuildClient;
import org.jboss.pnc.client.ProductMilestoneClient;
import org.jboss.pnc.client.ProductVersionClient;
import org.jboss.pnc.client.RemoteCollection;
import org.jboss.pnc.client.RemoteResourceException;
import org.jboss.pnc.common.pnc.LongBase32IdConverter;
import org.jboss.pnc.constants.Attributes;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.DTOEntity;
import org.jboss.pnc.dto.ProductMilestone;
import org.jboss.pnc.dto.ProductMilestoneRef;
import org.jboss.pnc.dto.ProductRef;
import org.jboss.pnc.dto.ProductVersion;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.rest.api.parameters.BuildsFilterParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.Reader;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.time.temporal.IsoFields;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.jboss.pnc.cleaner.archiver.ArchivedBuildRecord.ErrorGroup.INDY;
import static org.jboss.pnc.cleaner.archiver.BuildCategorizer.*;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@ApplicationScoped
public class BuildArchiver {
    private final Logger logger = LoggerFactory.getLogger(BuildArchiver.class);
    public static final String BUILD_ARCHIVED = "BUILD_ARCHIVED";
    @Inject
    @RestClient
    FinalLogClient finalLogs;
    @Inject
    BuildClient buildClient;
    @Inject
    ProductMilestoneClient productMilestoneClient;
    @Inject
    ProductVersionClient productVersionClient;
    @Inject
    BuildArchiver self;
    @ConfigProperty(name = "buildArchiver.trimmedLogMaxSize", defaultValue = "1000000")
    Integer trimmedLogMaxSize;

    @Inject
    OrchClientProducer orchClientProducer;

    @Timed
    @Scheduled(cron = "{buildArchiverScheduler.cron}", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    public void archiveBuilds() {
        logger.info("Archiving Builds ...");
        Collection<Build> unverifiedBuilds = getUnarchivedBuilds().getAll();
        logger.info("Found {} unverified builds.", unverifiedBuilds.size());
        unverifiedBuilds.forEach(b -> {
            try {
                archiveBuild(b);
            } catch (Exception ex) {
                logger.error("Failed to archive build " + b.getId(), ex);
            }
        });
    }

    @Timed
    RemoteCollection<Build> getUnarchivedBuilds() {
        BuildsFilterParameters buildsFilterParameters = new BuildsFilterParameters();
        buildsFilterParameters.setRunning(false);
        List<String> attributes = Collections.singletonList("!" + BUILD_ARCHIVED);
        try {
            return buildClient.getAll(buildsFilterParameters, attributes);
        } catch (RemoteResourceException e) {
            logger.error("Cannot read remote builds.", e);
            return RemoteCollection.empty();
        }
    }

    @Timed
    public void archiveBuild(Build build) {
        logger.info("Archiving build {}", build.getId());
        long trimLogSize = 0;
        if (build.getTemporaryBuild() && build.getStatus() != BuildStatus.SUCCESS) {
            trimLogSize = trimmedLogMaxSize;
        }

        LogParser alignmentLog;
        LogParser buildLog;
        if (build.getStatus() == BuildStatus.NO_REBUILD_REQUIRED
                || build.getStatus() == BuildStatus.REJECTED_FAILED_DEPENDENCIES) {
            alignmentLog = BuildCategorizer.getLogParser(0);
            buildLog = BuildCategorizer.getLogParser(0);
        } else {
            alignmentLog = getLogParser("alignment-log", trimLogSize, build.getId());
            buildLog = getLogParser("build-log", trimLogSize, build.getId());
        }

        self.archiveBuildRecord(build, buildLog, alignmentLog);

        try (BuildClient buildClientAuthenticated = orchClientProducer.getAuthenticatedBuildClient()) {
            buildClientAuthenticated.addAttribute(build.getId(), BUILD_ARCHIVED, "true");
        } catch (RemoteResourceException ex) {
            logger.error("Failed to mark build as archived in PNC", ex);
        }
    }

    private LogParser getLogParser(String tag, long trimLogSize, String buildID) {
        logger.info("Reading " + tag + " of build " + buildID);
        LogParser buildLog = BuildCategorizer.getLogParser(trimLogSize);
        try (Response response = finalLogs.getFinalLog(buildID, tag)) {
            if (response.getStatus() == 200) {
                buildLog.findMatches(new BufferedReader(response.readEntity(Reader.class)));
            }
        }
        return buildLog;
    }

    @Transactional
    public void archiveBuildRecord(Build build, LogParser buildLog, LogParser alignmentLog) {
        ArchivedBuildRecord archivedBuildRecord;
        try {
            archivedBuildRecord = mapBuild(build);
        } catch (RemoteResourceException ex) {
            throw new RuntimeException("Failed to read build info from PNC", ex);
        }

        archivedBuildRecord.autoAlign = !alignmentLog.contains(DMANIPULATION_DISABLE_TRUE)
                && !alignmentLog.contains(DMANIPULATION_DISABLE_TRUE1);
        archivedBuildRecord.brewPullActive = alignmentLog.contains(DBREW_PULL_ACTIVE_TRUE);

        String buildType = build.getBuildConfigRevision().getBuildType().toString();
        if (buildLog.contains(FRONTEND_MAVEN_PLUGIN)) {
            buildType = "MVN-WRAPPED-NPM";
        }
        archivedBuildRecord.buildType = buildType;

        archivedBuildRecord.trimmedBuildLog = buildLog.getTrimmedLog();
        archivedBuildRecord.trimmedAlignLog = alignmentLog.getTrimmedLog();

        archivedBuildRecord.status = processErrors(archivedBuildRecord, build.getStatus(), buildLog, alignmentLog);

        archivedBuildRecord.persist();
    }

    private ArchivedBuildRecord mapBuild(Build build) throws RemoteResourceException {
        long dbID = parseBuildID(build.getId());
        ArchivedBuildRecord archived = ArchivedBuildRecord.findById(dbID);
        if (archived == null) {
            archived = new ArchivedBuildRecord();
            archived.buildRecordId = dbID;
        }

        archived.submitTime = build.getSubmitTime();
        archived.startTime = build.getStartTime();
        archived.endTime = build.getEndTime();
        archived.submitYear = build.getSubmitTime().atZone(ZoneOffset.UTC).get(ChronoField.YEAR);
        archived.submitMonth = build.getSubmitTime().atZone(ZoneOffset.UTC).get(ChronoField.MONTH_OF_YEAR);
        archived.submitQuarter = build.getSubmitTime().atZone(ZoneOffset.UTC).get(IsoFields.QUARTER_OF_YEAR);
        archived.lastUpdate = build.getLastUpdateTime();

        archived.status = build.getStatus();
        archived.temporaryBuild = build.getTemporaryBuild();
        archived.brewPullActive = build.getBuildConfigRevision().isBrewPullActive();
        archived.buildcontentID = build.getBuildContentId();

        archived.executionRootName = build.getAttributes().get(Attributes.BUILD_BREW_NAME);
        archived.executionRootVersion = build.getAttributes().get(Attributes.BUILD_BREW_VERSION);

        archived.buildConfigID = parseBuildConfigID(build.getBuildConfigRevision().getId());
        archived.buildConfigRev = build.getBuildConfigRevision().getRev();
        archived.buildConfigName = build.getBuildConfigRevision().getName();

        archived.projectID = parseProjectID(build.getProject().getId());
        archived.projectName = build.getProject().getName();

        archived.userId = parseUserID(build.getUser().getId());
        archived.username = build.getUser().getUsername();

        archived.buildEnvironmentID = parseEnvironmentID(build.getEnvironment().getId());

        archived.groupBuildID = parseGroupBuildID(getNullableID(build.getGroupBuild()));

        if (build.getProductMilestone() != null) {
            ProductMilestoneRef productMilestone = build.getProductMilestone();
            archived.productMilestoneID = parseProductMilestoneID(productMilestone.getId());
            archived.productMilestoneVersion = productMilestone.getVersion();

            ProductVersion productVersion = getProductVersion(productMilestone.getId());
            archived.productVersionID = parseProductVersionID(productVersion.getId());
            archived.productVersion = productVersion.getVersion();

            ProductRef product = productVersion.getProduct();
            archived.productID = parseProductID(product.getId());
            archived.productName = product.getName();
        }

        return archived;
    }

    private long parseBuildID(String id) {
        return LongBase32IdConverter.toLong(id);
    }

    private long parseProductID(String id) {
        return Long.parseLong(id);
    }

    private long parseProductVersionID(String id) {
        return Long.parseLong(id);
    }

    private long parseProductMilestoneID(String id) {
        return Long.parseLong(id);
    }

    private Long parseGroupBuildID(String nullableID) {
        if (nullableID == null) {
            return null;
        }
        return LongBase32IdConverter.toLong(nullableID);
    }

    private long parseEnvironmentID(String id) {
        return Long.parseLong(id);
    }

    private long parseUserID(String id) {
        return Long.parseLong(id);
    }

    private long parseProjectID(String id) {
        return Long.parseLong(id);
    }

    private long parseBuildConfigID(String id) {
        return Long.parseLong(id);
    }

    private ProductVersion getProductVersion(String productMilestoneID) throws RemoteResourceException {
        ProductMilestone productMilestone = productMilestoneClient.getSpecific(productMilestoneID);
        String productVersionID = productMilestone.getProductVersion().getId();
        ProductVersion productVersion = productVersionClient.getSpecific(productVersionID);
        return productVersion;
    }

    private String getNullableID(DTOEntity dtoEntity) {
        if (dtoEntity == null) {
            return null;
        }
        return dtoEntity.getId();
    }

    private BuildStatus processErrors(
            ArchivedBuildRecord archviedBuildRecord,
            BuildStatus finalStatus,
            LogParser buildLog,
            LogParser alignmentLog) {
        if (finalStatus == BuildStatus.SYSTEM_ERROR) {
            DetectedCategory catMsg = categorizeErrors(buildLog, alignmentLog);
            archviedBuildRecord.categorizedErrorMessage = catMsg.getMessage();
            archviedBuildRecord.categorizedErrorGroup = catMsg.getCategory();

            if (catMsg.isPreviouslyMarkedSystemError()) {
                finalStatus = BuildStatus.FAILED;
            }
        } else if (finalStatus == BuildStatus.FAILED) {
            DetectedCategory catMsg = categorizeErrors(buildLog, alignmentLog);
            if (catMsg.getCategory() == INDY) {
                archviedBuildRecord.categorizedErrorMessage = catMsg.getMessage();
                archviedBuildRecord.categorizedErrorGroup = catMsg.getCategory();
                finalStatus = BuildStatus.SYSTEM_ERROR;
            }
        }
        return finalStatus;
    }

}
