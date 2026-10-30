/*
 * SPDX-FileCopyrightText: Copyright © 2019 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.cleaner.temporaryBuilds;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * CDI bean, which manages delete operation callbacks for BUILDS and provides a blocking was of waiting fot the
 * operation completion. First the wait operation must be initiated using a method #initializeHandler and then at any
 * time a blocking method #await can be called.
 *
 * @author Jakub Bartecek
 */
@ApplicationScoped
public class BuildDeleteCallbackManager extends DeleteCallbackManager {
}
