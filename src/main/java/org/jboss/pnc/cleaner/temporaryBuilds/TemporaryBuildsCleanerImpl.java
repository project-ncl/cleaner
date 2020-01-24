/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.cleaner.temporaryBuilds;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.pnc.cleaner.orchapi.model.BuildConfigSetRecordRest;
import org.jboss.pnc.cleaner.orchapi.model.BuildRecordRest;
import org.jboss.pnc.common.util.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collection;
import java.util.Date;

/**
 * Deletes temporary builds via Orchestrator REST API
 *
 * @author Jakub Bartecek
 */
@ApplicationScoped
public class TemporaryBuildsCleanerImpl implements TemporaryBuildsCleaner {

    private final Logger log = LoggerFactory.getLogger(TemporaryBuildsCleanerImpl.class);

    @ConfigProperty(name = "temporaryBuildsCleaner.lifespan")
    Integer TEMPORARY_BUILD_LIFESPAN;

    @Inject
    TemporaryBuildsCleanerAdapter temporaryBuildsCleanerAdapter;

    @Override
    public void cleanupExpiredTemporaryBuilds() {
        log.info("Regular cleanup of expired temporary builds started. Removing builds older than " +
                TEMPORARY_BUILD_LIFESPAN + " days.");
        Date expirationThreshold = TimeUtils.getDateXDaysAgo(TEMPORARY_BUILD_LIFESPAN);

        deleteExpiredBuildConfigSetRecords(expirationThreshold);
        deleteExpiredBuildRecords(expirationThreshold);

        log.info("Regular cleanup of expired temporary builds finished.");
    }

    void deleteExpiredBuildConfigSetRecords(Date expirationThreshold) {
        Collection<BuildConfigSetRecordRest> expiredBCSRecords = temporaryBuildsCleanerAdapter
                .findTemporaryBuildConfigSetRecordsOlderThan(expirationThreshold);

        for (BuildConfigSetRecordRest buildSetRecord : expiredBCSRecords) {
            try {
                log.info("Deleting temporary BuildConfigSetRecord {}", buildSetRecord);
                temporaryBuildsCleanerAdapter.deleteTemporaryBuildConfigSetRecord(buildSetRecord.getId());
                log.info("Temporary BuildConfigSetRecord {} was deleted successfully", buildSetRecord);
            } catch (OrchInteractionException ex) {
                log.warn("Deletion of temporary BuildConfigSetRecord {} failed!", buildSetRecord);
            }
        }
    }

    void deleteExpiredBuildRecords(Date expirationThreshold) {
        Collection<BuildRecordRest> expiredBuilds = temporaryBuildsCleanerAdapter.findTemporaryBuildsOlderThan
                (expirationThreshold);

        for (BuildRecordRest buildRecord : expiredBuilds) {
            try {
                log.info("Deleting temporary build {}", buildRecord);
                temporaryBuildsCleanerAdapter.deleteTemporaryBuild(buildRecord.getId());
                log.info("Temporary build {} was deleted successfully", buildRecord);
            } catch (OrchInteractionException ex) {
                log.warn("Deletion of temporary build {} failed! Cause: {}", buildRecord, ex);
            }
        }
    }
}
