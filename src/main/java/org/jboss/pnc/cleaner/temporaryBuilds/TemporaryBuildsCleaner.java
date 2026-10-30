/*
 * SPDX-FileCopyrightText: Copyright © 2019 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.cleaner.temporaryBuilds;

/**
 * Interface for schedulers to delete builds without a transaction context
 *
 * @author Jakub Bartecek
 */
public interface TemporaryBuildsCleaner {

    /**
     * Cleanup old temporary builds
     */
    void cleanupExpiredTemporaryBuilds();
}
