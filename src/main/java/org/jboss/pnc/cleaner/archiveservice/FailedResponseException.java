/*
 * SPDX-FileCopyrightText: Copyright © 2019 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.cleaner.archiveservice;

/**
 * @author Andrea Vibelli
 */
public class FailedResponseException extends RuntimeException {

    public FailedResponseException(String message) {
        super(message);
    }
}