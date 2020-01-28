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

import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.pnc.cleaner.auth.DefaultKeycloakServiceClient;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.jboss.pnc.cleaner.orchapi.SwaggerConstants.PAGE_INDEX_QUERY_PARAM;
import static org.jboss.pnc.cleaner.orchapi.SwaggerConstants.PAGE_SIZE_QUERY_PARAM;
import static org.jboss.pnc.cleaner.orchapi.SwaggerConstants.QUERY_QUERY_PARAM;
import static org.jboss.pnc.cleaner.orchapi.SwaggerConstants.SORTING_QUERY_PARAM;

@Path("/build-config-set-records")
@RegisterRestClient
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface BuildConfigSetRecordEndpoint {

    default String[] getAccessToken() {
        return new String[] { "Bearer " + DefaultKeycloakServiceClient.getAuthTokenStatic() };
    }

    @DELETE
    @Path("/{id}")
    @ClientHeaderParam(name = "Authorization",
                       value = "{getAccessToken}")
    Response delete(@PathParam("id") Integer id, @QueryParam("callback") String callbackUrl);

    @GET
    @Path("/temporary-older-than-timestamp")
    Response getAllTemporaryOlderThanTimestamp(@QueryParam(PAGE_INDEX_QUERY_PARAM) int pageIndex,
                                               @QueryParam(PAGE_SIZE_QUERY_PARAM) int pageSize,
                                               @QueryParam(SORTING_QUERY_PARAM) String sort,
                                               @QueryParam(QUERY_QUERY_PARAM) String q,
                                               @QueryParam("timestamp") long timestamp);
}
