/*
 * SPDX-FileCopyrightText: Copyright © 2019 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.cleaner.rest;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.jboss.pnc.api.dto.ComponentVersion;
import org.jboss.pnc.cleaner.common.AppInfo;

@Path("/version")
public class Version {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ComponentVersion getVersion() {
        return ComponentVersion.builder()
                .name("PNC-Cleaner")
                .version(AppInfo.getVersion())
                .commit(AppInfo.getRevision())
                .builtOn(
                        ZonedDateTime
                                .parse(AppInfo.getBuildTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ")))
                .build();
    }
}
