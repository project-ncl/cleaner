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

import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.Config;
import org.jboss.pnc.client.BuildClient;
import org.jboss.pnc.client.GroupBuildClient;
import org.jboss.pnc.client.RemoteCollection;
import org.jboss.pnc.client.RemoteResourceException;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.response.DeleteOperationResult;
import org.jboss.pnc.dto.GroupBuild;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;

/**
 * Implementation of an adapter providing high-level operations on Orchestrator REST API
 *
 * @author Jakub Bartecek
 */
@ApplicationScoped
@Slf4j
public class TemporaryBuildsCleanerAdapterImpl implements TemporaryBuildsCleanerAdapter {

    private String BASE_DELETE_BUILD_CALLBACK_URL;

    private String BASE_DELETE_BUILD_GROUP_CALLBACK_URL;

    @Inject
    Config config;

    @Inject
    BuildClient buildClient;

    @Inject
    GroupBuildClient groupBuildClient;

    @Inject
    BuildDeleteCallbackManager buildDeleteCallbackManager;

    @Inject
    BuildGroupDeleteCallbackManager buildGroupDeleteCallbackManager;

    @PostConstruct
    void init() {
        final String host = config.getValue("applicationUri", String.class);

        BASE_DELETE_BUILD_CALLBACK_URL = host + "/callbacks/delete/builds/";
        BASE_DELETE_BUILD_GROUP_CALLBACK_URL = host + "/callbacks/delete/group-builds/";
    }

    @Override
    public Collection<Build> findTemporaryBuildsOlderThan(Date expirationDate) {
        Collection<Build> buildsRest = new HashSet<>();

        try {
            RemoteCollection<Build> remoteCollection = buildClient
                    .getAllIndependentTempBuildsOlderThanTimestamp(expirationDate.getTime());
            remoteCollection.forEach(buildsRest::add);
        } catch (RemoteResourceException e) {
            log.warn(
                    "Querying of temporary builds from Orchestrator failed with [status: {}, errorResponse: {}]",
                    e.getStatus(),
                    e.getResponse().orElse(null));
            return buildsRest;
        }

        return buildsRest;
    }

    @Override
    public void deleteTemporaryBuild(String id) throws OrchInteractionException {
        buildDeleteCallbackManager.initializeHandler(id);
        try {
            buildClient.delete(id, BASE_DELETE_BUILD_CALLBACK_URL + id);
            DeleteOperationResult result = buildDeleteCallbackManager.await(id);

            if (result != null && result.getStatus() != null && result.getStatus().isSuccess()) {
                return;
            } else {
                throw new OrchInteractionException(
                        String.format(
                                "Deletion of a build %s failed! " + "Orchestrator"
                                        + " reported a failure: [status={}, message={}].",
                                result == null ? null : result.getStatus(),
                                result == null ? null : result.getMessage()));
            }

        } catch (RemoteResourceException e) {
            buildDeleteCallbackManager.cancel(id);
            throw new OrchInteractionException(
                    String.format(
                            "Deletion of a build %s failed! The operation " + "failed with errorStatus=%s.",
                            id,
                            e.getStatus()),
                    e);
        } catch (InterruptedException e) {
            buildDeleteCallbackManager.cancel(id);
            throw new OrchInteractionException(
                    String.format("Deletion of a build %s failed! Wait operation " + "failed with an exception.", id),
                    e);
        }

    }

    @Override
    public Collection<GroupBuild> findTemporaryGroupBuildsOlderThan(Date expirationDate) {
        Collection<GroupBuild> groupBuilds = new HashSet<>();
        try {
            RemoteCollection<GroupBuild> remoteCollection = groupBuildClient.getAll(
                    Optional.empty(),
                    Optional.of("temporaryBuild==TRUE;endTime<" + formatTimestampForRsql(expirationDate)));
            remoteCollection.forEach(build -> groupBuilds.add(build));

        } catch (RemoteResourceException e) {
            log.warn(
                    "Querying of temporary group builds from Orchestrator failed with [status: {}, errorResponse: "
                            + "{}]",
                    e.getStatus(),
                    e.getResponse().orElse(null));
        }

        return groupBuilds;
    }

    @Override
    public void deleteTemporaryGroupBuild(String id) throws OrchInteractionException {
        buildGroupDeleteCallbackManager.initializeHandler(id);

        try {
            groupBuildClient.delete(id, BASE_DELETE_BUILD_GROUP_CALLBACK_URL + id);
            DeleteOperationResult result = buildGroupDeleteCallbackManager.await(id);

            if (result != null && result.getStatus() != null && result.getStatus().isSuccess()) {
                return;
            } else {
                throw new OrchInteractionException(
                        String.format(
                                "Deletion of a group build %s failed! " + "Orchestrator"
                                        + " reported a failure: [status={}, message={}].",
                                result == null ? null : result.getStatus(),
                                result == null ? null : result.getMessage()));
            }

        } catch (RemoteResourceException e) {
            buildDeleteCallbackManager.cancel(id);
            throw new OrchInteractionException(
                    String.format(
                            "Deletion of a group build %s failed! The operation " + "failed with errorMessage=%s.",
                            id,
                            e.getStatus()),
                    e);
        } catch (InterruptedException e) {
            buildDeleteCallbackManager.cancel(id);
            throw new OrchInteractionException(
                    String.format(
                            "Deletion of a group build %s failed! Wait operation " + "failed with an exception.",
                            id),
                    e);
        }
    }

    private String formatTimestampForRsql(Date expirationDate) {
        return DateTimeFormatter.ISO_DATE_TIME.withLocale(Locale.ROOT)
                .withZone(ZoneId.of("UTC"))
                .format(Instant.ofEpochMilli(expirationDate.getTime()));
    }
}
