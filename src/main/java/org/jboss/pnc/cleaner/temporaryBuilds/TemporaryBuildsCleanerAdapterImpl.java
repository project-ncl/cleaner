package org.jboss.pnc.cleaner.temporaryBuilds;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.pnc.cleaner.orchapi.BuildRecordEndpoint;
import org.jboss.pnc.cleaner.orchapi.BuildRecordPage;
import org.jboss.pnc.rest.restmodel.BuildConfigSetRecordRest;
import org.jboss.pnc.rest.restmodel.BuildRecordRest;
import org.jboss.pnc.rest.validation.exceptions.RepositoryViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

/**
 * Implementation of an adapter providing high-level operations on Orchestrator REST API
 *
 * @author Jakub Bartecek
 */
@ApplicationScoped
public class TemporaryBuildsCleanerAdapterImpl implements TemporaryBuildsCleanerAdapter {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    @RestClient
    BuildRecordEndpoint buildRecordService;

    @Override
    public Collection<BuildRecordRest> findTemporaryBuildsOlderThan(Date expirationDate) {
        final int pageSize = 50;

        Collection<BuildRecordRest> buildRecordRests = new HashSet<>();
        int currentPage = 0;
        boolean condition;

        do {
            BuildRecordPage buildRecordPage = buildRecordService.getAllTemporaryOlderThanTimestamp(currentPage, pageSize, null, null, expirationDate.getTime());
            buildRecordRests.addAll(buildRecordPage.getContent());

            currentPage++;
            condition = currentPage < buildRecordPage.getTotalPages();
        } while (condition);

        return buildRecordRests;
    }

    @Override
    public void deleteTemporaryBuild(Integer id) throws RepositoryViolationException {
        buildRecordService.delete(id);
    }

    @Override
    public List<BuildConfigSetRecordRest> findTemporaryBuildConfigSetRecordsOlderThan(Date expirationDate) {
        return null;
    }

    @Override
    public void deleteTemporaryBuildConfigSetRecord(Integer id) throws RepositoryViolationException {
        throw new RepositoryViolationException("");
    }
}
