/*
 * SPDX-FileCopyrightText: Copyright © 2019 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.cleaner.temporaryBuilds;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.scheduler.Scheduled;

/**
 * Executes regular cleanup of old temporary builds after expiration.
 *
 * @author Jakub Bartecek
 */
@ApplicationScoped
public class TemporaryBuildsCleanupScheduler {

    private final Logger log = LoggerFactory.getLogger(TemporaryBuildsCleanupScheduler.class);

    @Inject
    TemporaryBuildsCleaner temporaryBuildsCleanupScheduleWorker;

    /**
     * Schedules cleanup of old temporary builds
     */
    @Scheduled(cron = "{temporaryBuildsCleaner.cron}", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    @WithSpan
    public void cleanupExpiredTemporaryBuilds() {
        log.info("Regular deletion of temporary builds triggered by clock.");
        temporaryBuildsCleanupScheduleWorker.cleanupExpiredTemporaryBuilds();
        log.info("Regular deletion of temporary builds successfully finished.");
    }
}
