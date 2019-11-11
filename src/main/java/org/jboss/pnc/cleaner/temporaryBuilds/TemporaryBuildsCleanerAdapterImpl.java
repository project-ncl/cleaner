package org.jboss.pnc.cleaner.temporaryBuilds;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.pnc.cleaner.orchapi.BuildConfigSetRecordEndpoint;
import org.jboss.pnc.cleaner.orchapi.model.BuildConfigurationSetRecordPage;
import org.jboss.pnc.cleaner.orchapi.BuildRecordEndpoint;
import org.jboss.pnc.cleaner.orchapi.model.BuildRecordPage;
import org.jboss.pnc.cleaner.orchapi.model.BuildConfigSetRecordRest;
import org.jboss.pnc.cleaner.orchapi.model.BuildRecordRest;
import org.jboss.pnc.cleaner.orchapi.validation.exceptions.RepositoryViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;

/**
 * Implementation of an adapter providing high-level operations on Orchestrator REST API
 *
 * @author Jakub Bartecek
 */
@ApplicationScoped
@Slf4j
public class TemporaryBuildsCleanerAdapterImpl implements TemporaryBuildsCleanerAdapter {

    @Inject
    @RestClient
    BuildRecordEndpoint buildRecordService;

    @Inject
    @RestClient
    BuildConfigSetRecordEndpoint buildConfigSetRecordEndpoint;

    @Override
    public Collection<BuildRecordRest> findTemporaryBuildsOlderThan(Date expirationDate) {
        final int pageSize = 50;

        Collection<BuildRecordRest> buildRecordRests = new HashSet<>();
        int currentPage = 0;
        boolean condition;

        do {
            Response response;
            try {
                response = buildRecordService.getAllTemporaryOlderThanTimestamp(currentPage, pageSize, null, null,
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
                            response.getStatus(), response.readEntity(String.class));
                    return buildRecordRests;
            }
        } while (condition);

        return buildRecordRests;
    }

    @Override
    public void deleteTemporaryBuild(Integer id) throws RepositoryViolationException {
        buildRecordService.delete(id); // TODO wait for async operation completion and report results
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
                response = buildConfigSetRecordEndpoint.getAllTemporaryOlderThanTimestamp(currentPage, pageSize, null, null,
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
                            response.getStatus(), response.readEntity(String.class));
                    return buildConfigSetRecords;
            }


        } while (condition);

        return buildConfigSetRecords;
    }

    @Override
    public void deleteTemporaryBuildConfigSetRecord(Integer id) throws RepositoryViolationException {
        buildConfigSetRecordEndpoint.delete(id); // TODO wait for async operation completion and report results
    }
}
