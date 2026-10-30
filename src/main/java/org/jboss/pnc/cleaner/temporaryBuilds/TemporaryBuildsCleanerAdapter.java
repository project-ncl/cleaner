/*
 * SPDX-FileCopyrightText: Copyright © 2019 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.cleaner.temporaryBuilds;

import java.util.Collection;
import java.util.Date;

import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.GroupBuild;

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
    Collection<Build> findTemporaryBuildsOlderThan(Date expirationDate);

    /**
     * Deletes a temporary build and waits for the operation completion. The method is blocking.
     *
     * @param id ID of a temporary build, which is meant to be deleted
     * @throws OrchInteractionException Thrown if deletion fails with an error
     */
    void deleteTemporaryBuild(String id) throws OrchInteractionException;

    /**
     * Finds all temporary BuildConfigSetRecords, which are older than a timestamp set by the expirationDate parameter
     *
     * @param expirationDate Timestamp defining expiration date of BuildConfigSetRecords
     * @return List of expired BuildConfigSetRecords
     */
    Collection<GroupBuild> findTemporaryGroupBuildsOlderThan(Date expirationDate);

    /**
     * Deletes a temporary BuildConfigSetRecord
     *
     * @param id ID of a temporary BuildConfigSetRecord, which is meant to be deleted
     * @throws OrchInteractionException Thrown if deletion fails with an error
     */
    void deleteTemporaryGroupBuild(String id) throws OrchInteractionException;
}
