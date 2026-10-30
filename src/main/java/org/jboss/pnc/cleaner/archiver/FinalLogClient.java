/*
 * SPDX-FileCopyrightText: Copyright © 2019 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.cleaner.archiver;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.pnc.api.bifrost.rest.FinalLogRest;

import jakarta.ws.rs.Path;

/**
 * @author <a href="mailto:matejonnet@gmail.opecom">Matej Lazar</a>
 */
@Path("/final-log")
@RegisterRestClient
public interface FinalLogClient extends FinalLogRest {
}
