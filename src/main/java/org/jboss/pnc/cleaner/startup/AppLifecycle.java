/*
 * SPDX-FileCopyrightText: Copyright © 2019 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.cleaner.startup;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

import org.jboss.pnc.cleaner.common.AppInfo;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class AppLifecycle {

    void onStart(@Observes StartupEvent ev) {
        log.info("The application is starting: {}", AppInfo.getAppInfoString());
    }

    void onShutdown(@Observes ShutdownEvent ev) {
        log.info("The application is shutting down: {}", AppInfo.getAppInfoString());
    }
}
