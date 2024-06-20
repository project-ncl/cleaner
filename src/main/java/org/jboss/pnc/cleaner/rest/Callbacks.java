/**
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

import io.micrometer.core.annotation.Timed;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;

import org.jboss.pnc.cleaner.temporaryBuilds.BuildDeleteCallbackManager;
import org.jboss.pnc.cleaner.temporaryBuilds.BuildGroupDeleteCallbackManager;
import org.jboss.pnc.dto.response.DeleteOperationResult;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * @author Jakub Bartecek
 */
@Path("/callbacks")
public class Callbacks {

    @Inject
    BuildDeleteCallbackManager buildDeleteCallbackManager;

    @Inject
    BuildGroupDeleteCallbackManager buildGroupDeleteCallbackManager;

    @Path("/delete/builds/{buildId}")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed
    @WithSpan
    public Response buildRecordDeleteCallback(
            @SpanAttribute(value = "buildId") @PathParam("buildId") String buildId,
            @SpanAttribute(value = "deleteOperation") DeleteOperationResult deleteOperation) {
        buildDeleteCallbackManager.callback(buildId, deleteOperation);
        return Response.ok().build();
    }

    @Path("/delete/group-builds/{buildId}")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed
    @WithSpan
    public Response buildGroupRecordDeleteCallback(
            @SpanAttribute(value = "buildId") @PathParam("buildId") String buildId,
            @SpanAttribute(value = "deleteOperation") DeleteOperationResult deleteOperation) {
        buildGroupDeleteCallbackManager.callback(buildId, deleteOperation);
        return Response.ok().build();
    }
}
