/*
 * SPDX-FileCopyrightText: Copyright © 2019 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.cleaner.temporaryBuilds;

import lombok.ToString;

/**
 * Exception informing about problems with communication with Orchestrator
 *
 * @author Jakub Bartecek
 */
@ToString
public class OrchInteractionException extends Exception {

    public OrchInteractionException(String message) {
        super(message);
    }

    public OrchInteractionException(String message, Throwable cause) {
        super(message, cause);
    }

}
