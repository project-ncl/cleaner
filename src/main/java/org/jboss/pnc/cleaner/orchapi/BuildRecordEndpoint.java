/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.cleaner.orchapi;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.pnc.rest.restmodel.BuildRecordRest;
import org.jboss.pnc.rest.validation.exceptions.RepositoryViolationException;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import java.util.List;

import static org.jboss.pnc.cleaner.orchapi.SwaggerConstants.PAGE_INDEX_DEFAULT_VALUE;
import static org.jboss.pnc.cleaner.orchapi.SwaggerConstants.PAGE_INDEX_QUERY_PARAM;
import static org.jboss.pnc.cleaner.orchapi.SwaggerConstants.PAGE_SIZE_DEFAULT_VALUE;
import static org.jboss.pnc.cleaner.orchapi.SwaggerConstants.PAGE_SIZE_QUERY_PARAM;
import static org.jboss.pnc.cleaner.orchapi.SwaggerConstants.QUERY_QUERY_PARAM;
import static org.jboss.pnc.cleaner.orchapi.SwaggerConstants.SORTING_QUERY_PARAM;

@Path("/build-records")
@RegisterRestClient
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface BuildRecordEndpoint {

    @GET
    BuildRecordPage getAll(
            @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @QueryParam(SORTING_QUERY_PARAM) String sort,
            @QueryParam(QUERY_QUERY_PARAM) String q);

    @GET
    @Path("/with-status-and-log")
    List<BuildRecordRest> getAllByStatusAndLogContaining(
            @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @QueryParam(SORTING_QUERY_PARAM) String sort,
            @QueryParam(QUERY_QUERY_PARAM) String q,
            @QueryParam("status") BuildStatus status,
            @QueryParam("search") String search);

    @GET
    @Path("/{id}")
    BuildRecordSingleton getSpecific(@PathParam("id") Integer id);

    @DELETE
    @Path("/{id}")
    void delete(@PathParam("id") Integer id)
            throws RepositoryViolationException;

    @GET
    @Path("/{id}/log")
    @Produces(MediaType.TEXT_PLAIN)
    String getLogs(@PathParam("id") Integer id);

    @GET
    @Path("/{id}/repour-log")
    @Produces(MediaType.TEXT_PLAIN)
    String getRepourLogs(@PathParam("id") Integer id);

    @GET
    @Path("/{id}/built-artifacts")
    ArtifactPage getBuiltArtifacts(@PathParam("id") Integer id,
            @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @QueryParam(SORTING_QUERY_PARAM) String sort,
            @QueryParam(QUERY_QUERY_PARAM) String q);

    @PUT
    @Path("/{id}/built-artifacts")
    void setArtifacts(@PathParam("id") Integer id,
            List<Integer> artifactIds);

    @GET
    @Path("/{id}/dependency-artifacts")
    ArtifactPage getDependencyArtifacts(@PathParam("id") Integer id,
            @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @QueryParam(SORTING_QUERY_PARAM) String sort,
            @QueryParam(QUERY_QUERY_PARAM) String q);

    @GET
    @Path("/projects/{projectId}")
    BuildRecordPage getAllForProject(@QueryParam("pageIndex") @DefaultValue("0") int pageIndex,
            @DefaultValue("50") @QueryParam("pageSize") int pageSize,
            @QueryParam("sort") String sortingRsql,
            @PathParam("projectId") Integer projectId,
            @QueryParam("q") String rsql);

    @GET
    @Path("/build-configuration-or-project-name/{name}")
    BuildRecordPage getAllForProject(@QueryParam("pageIndex") @DefaultValue("0") int pageIndex,
            @DefaultValue("50") @QueryParam("pageSize") int pageSize,
            @QueryParam("sort") String sortingRsql,
            @PathParam("name") String name,
            @QueryParam("q") String rsql);

    @GET
    @Path("/{id}/build-configuration-audited")
    BuildConfigurationAuditedSingleton getBuildConfigurationAudited(@PathParam("id") Integer id);

    @POST
    @Path("/{id}/put-attribute")
    void putAttribute(@PathParam("id") Integer id,
                              @QueryParam("key") String key,
                              @QueryParam("value") String value);

    @DELETE
    @Path("/{id}/remove-attribute")
    void removeAttribute(@PathParam("id") Integer id,
                              @QueryParam("key") String key);

    @GET
    @Path("/{id}/get-attributes")
    AttributeSingleton getAttributes(@PathParam("id") Integer id);

    @GET
    @Path("/get-by-attribute")
    BuildRecordPage queryByAttribute(
            @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @QueryParam(SORTING_QUERY_PARAM) String sort,
            @QueryParam(QUERY_QUERY_PARAM) String q,
            @QueryParam("key") String key,
            @QueryParam("value") String value);

}
