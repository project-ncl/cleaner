package org.jboss.pnc.cleaner.temporaryBuilds;

import org.jboss.pnc.rest.restmodel.BuildConfigSetRecordRest;
import org.jboss.pnc.rest.restmodel.BuildRecordRest;
import org.jboss.pnc.rest.validation.exceptions.RepositoryViolationException;

import java.util.Collection;
import java.util.Date;

/**
 * Adapter, which provides high level operations on Orchestrator REST API
 *
 * @author Jakub Bartecek
 */
public interface TemporaryBuildsCleanerAdapter {

    /**
     * Finds all temporary builds, which are older than a timestamp set by the expirationDate parameter
     *
     * @param expirationDate Timestamp defining expiration date of temporary builds
     * @return List of expired builds
     */
    Collection<BuildRecordRest> findTemporaryBuildsOlderThan(Date expirationDate);

    /**
     * Deletes a temporary build
     *
     * @param id ID of a temporary build, which is meant to be deleted
     * @throws RepositoryViolationException Thrown if deletion fails with an error
     */
    void deleteTemporaryBuild(Integer id) throws RepositoryViolationException;

    /**
     * Finds all temporary BuildConfigSetRecords, which are older than a timestamp set by the expirationDate parameter
     *
     * @param expirationDate Timestamp defining expiration date of BuildConfigSetRecords
     * @return List of expired BuildConfigSetRecords
     */
    Collection<BuildConfigSetRecordRest> findTemporaryBuildConfigSetRecordsOlderThan(Date expirationDate);

    /**
     * Deletes a temporary BuildConfigSetRecord
     *
     * @param id ID of a temporary BuildConfigSetRecord, which is meant to be deleted
     * @throws RepositoryViolationException Thrown if deletion fails with an error
     */
    void deleteTemporaryBuildConfigSetRecord(Integer id) throws RepositoryViolationException;
}
