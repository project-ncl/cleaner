/*
 * SPDX-FileCopyrightText: Copyright © 2019 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.cleaner.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.jboss.pnc.cleaner.temporaryBuilds.BuildDeleteCallbackManager;
import org.jboss.pnc.cleaner.temporaryBuilds.BuildGroupDeleteCallbackManager;
import org.jboss.pnc.dto.response.DeleteOperationResult;

import io.micrometer.core.annotation.Timed;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;

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
