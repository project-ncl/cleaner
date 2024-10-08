/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2019-2022 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.cleaner.rest;

import org.jboss.pnc.api.dto.ComponentVersion;
import org.jboss.pnc.cleaner.common.AppInfo;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

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
