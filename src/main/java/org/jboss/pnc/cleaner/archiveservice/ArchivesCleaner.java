/*
 * SPDX-FileCopyrightText: Copyright © 2019 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.cleaner.archiveservice;

/**
 * Provides methods to clean the historical archives stored in the Indy Archive Service
 *
 * @author Andrea Vibelli
 */
public interface ArchivesCleaner {

    /**
     * Deletes a historical archive given a build configuration id. The method is not blocking.
     *
     * @param buildConfigurationId ID of the build configuration whose archive is meant to be deleted
     */
    void deleteArchive(String buildConfigurationId);
}
