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
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.pnc.cleaner.orchapi.BuildConfigSetRecordEndpoint;
import org.jboss.pnc.cleaner.orchapi.BuildRecordEndpoint;
import org.jboss.pnc.cleaner.orchapi.model.BuildConfigSetRecordRest;
import org.jboss.pnc.cleaner.orchapi.model.BuildConfigurationSetRecordPage;
import org.jboss.pnc.cleaner.orchapi.model.BuildRecordPage;
import org.jboss.pnc.cleaner.orchapi.model.BuildRecordRest;
import org.jboss.pnc.cleaner.orchapi.model.DeleteOperationResult;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

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
    @RestClient
    BuildRecordEndpoint buildRecordService;

    @Inject
    @RestClient
    BuildConfigSetRecordEndpoint buildConfigSetRecordEndpoint;

    @Inject
    BuildDeleteCallbackManager buildDeleteCallbackManager;

    @Inject
    BuildGroupDeleteCallbackManager buildGroupDeleteCallbackManager;

    @PostConstruct
    private void init() {
        final String host = config.getValue("applicationUri", String.class);

        BASE_DELETE_BUILD_CALLBACK_URL = host + "/callbacks/build-record-delete/";
        BASE_DELETE_BUILD_GROUP_CALLBACK_URL = host + "/callbacks/build-group-record-delete/";
    }

    @Override
    public Collection<BuildRecordRest> findTemporaryBuildsOlderThan(Date expirationDate) {
        final int pageSize = 50;

        Collection<BuildRecordRest> buildRecordRests = new HashSet<>();
        int currentPage = 0;
        boolean condition;

        do {
            Response response;
            try {
                response = buildRecordService.getAllTemporaryOlderThanTimestamp(currentPage,
                                                                                pageSize,
                                                                                null,
                                                                                null,
                                                                                expirationDate.getTime());
            } catch (Exception e) {
                log.warn("Querying of temporary builds from Orchestrator failed with exception", e);
                return buildRecordRests;
            }

            switch (response.getStatus()) {
                case 200:
                    BuildRecordPage buildRecordPage = response.readEntity(BuildRecordPage.class);
                    buildRecordRests.addAll(buildRecordPage.getContent());

                    currentPage++;
                    condition = currentPage < buildRecordPage.getTotalPages();
                    break;
                case 204:
                    return buildRecordRests;
                default:
                    log.warn("Querying of temporary builds from Orchestrator failed with [status: {}, message: {}]",
                             response.getStatus(),
                             response.readEntity(String.class));
                    return buildRecordRests;
            }
        } while (condition);

        return buildRecordRests;
    }

    @Override
    public void deleteTemporaryBuild(Integer id) throws OrchInteractionException {
        buildDeleteCallbackManager.initializeHandler(id);
        Response deleteResponse = buildRecordService.delete(id, BASE_DELETE_BUILD_CALLBACK_URL + id);

        switch (deleteResponse.getStatus()) {
            case 200:
                // Deletion was initiated. Wait for callback, which confirms end of the operation.
                try {
                    DeleteOperationResult result = buildDeleteCallbackManager.await(id);
                    if (result != null && result.getStatus() != null && result.getStatus().isSuccess()) {
                        return;
                    } else {
                        throw new OrchInteractionException(String.format("Deletion of a build %s failed! " + "Orchestrator"
                                + " reported a failure: [status={}, message={}].",
                                                                         result == null ? null : result.getStatus(),
                                                                         result == null ? null : result.getMessage()));
                    }
                } catch (InterruptedException e) {
                    buildDeleteCallbackManager.cancel(id);
                    throw new OrchInteractionException(
                            String.format("Deletion of a build %s failed! Wait operation " + "failed with an exception.", id),
                            e);
                }

            case 404:
                buildDeleteCallbackManager.cancel(id);
                throw new OrchInteractionException(
                        String.format("Deletion of a build %s failed! The build was not " + "found.", id));

            default:
                buildDeleteCallbackManager.cancel(id);
                throw new OrchInteractionException(
                        String.format("Deletion of a build %s failed! The operation " + "failed" + " with status code %s.",
                                      id,
                                      deleteResponse.getStatus()));
        }
    }

    @Override
    public Collection<BuildConfigSetRecordRest> findTemporaryBuildConfigSetRecordsOlderThan(Date expirationDate) {
        final int pageSize = 50;

        Collection<BuildConfigSetRecordRest> buildConfigSetRecords = new HashSet<>();
        int currentPage = 0;
        boolean condition;

        do {

            Response response;
            try {
                response = buildConfigSetRecordEndpoint.getAllTemporaryOlderThanTimestamp(currentPage,
                                                                                          pageSize,
                                                                                          null,
                                                                                          null,
                                                                                          expirationDate.getTime());
            } catch (Exception e) {
                log.warn("Querying of temporary builds from Orchestrator failed with exception", e);
                return buildConfigSetRecords;
            }

            switch (response.getStatus()) {
                case 200:
                    BuildConfigurationSetRecordPage buildConfigurationSetRecordPage = response.readEntity(BuildConfigurationSetRecordPage.class);
                    buildConfigSetRecords.addAll(buildConfigurationSetRecordPage.getContent());

                    currentPage++;
                    condition = currentPage < buildConfigurationSetRecordPage.getTotalPages();
                    break;
                case 204:
                    return buildConfigSetRecords;
                default:
                    log.warn("Querying of temporary build groups from Orchestrator failed with [status: {}, message: {}]",
                             response.getStatus(),
                             response.readEntity(String.class));
                    return buildConfigSetRecords;
            }

        } while (condition);

        return buildConfigSetRecords;
    }

    @Override
    public void deleteTemporaryBuildConfigSetRecord(Integer id) throws OrchInteractionException {
        buildGroupDeleteCallbackManager.initializeHandler(id);
        Response deleteResponse = buildConfigSetRecordEndpoint.delete(id, BASE_DELETE_BUILD_GROUP_CALLBACK_URL + id);

        switch (deleteResponse.getStatus()) {
            case 200:
                // Deletion was initiated. Wait for callback, which confirms end of the operation.
                try {
                    DeleteOperationResult result = buildGroupDeleteCallbackManager.await(id);
                    if (result != null && result.getStatus() != null && result.getStatus().isSuccess()) {
                        return;
                    } else {
                        throw new OrchInteractionException(String.format("Deletion of a build %s failed! " + "Orchestrator"
                                + " reported a failure: [status={}, message={}].",
                                                                         result == null ? null : result.getStatus(),
                                                                         result == null ? null : result.getMessage()));
                    }
                } catch (InterruptedException e) {
                    buildGroupDeleteCallbackManager.cancel(id);
                    throw new OrchInteractionException(
                            String.format("Deletion of a build %s failed! Wait operation " + "failed with an exception.", id),
                            e);
                }

            case 404:
                buildGroupDeleteCallbackManager.cancel(id);
                throw new OrchInteractionException(
                        String.format("Deletion of a build %s failed! The build was not " + "found.", id));

            default:
                buildGroupDeleteCallbackManager.cancel(id);
                throw new OrchInteractionException(
                        String.format("Deletion of a build %s failed! The operation " + "failed" + " with status code %s.",
                                      id,
                                      deleteResponse.getStatus()));
        }
    }
}
