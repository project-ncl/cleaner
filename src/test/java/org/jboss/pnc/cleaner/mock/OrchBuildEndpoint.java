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
package org.jboss.pnc.cleaner.mock;

import io.quarkus.test.Mock;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.jboss.pnc.api.causeway.dto.push.BuildPushCompleted;
import org.jboss.pnc.dto.Artifact;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.BuildConfigurationRevision;
import org.jboss.pnc.dto.BuildPushOperation;
import org.jboss.pnc.dto.BuildPushReport;
import org.jboss.pnc.dto.insights.BuildRecordInsights;
import org.jboss.pnc.dto.requests.BuildPushParameters;
import org.jboss.pnc.dto.response.Graph;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.dto.response.RunningBuildCount;
import org.jboss.pnc.dto.response.SSHCredentials;
import org.jboss.pnc.rest.api.endpoints.BuildEndpoint;
import org.jboss.pnc.rest.api.parameters.BuildsFilterParameters;
import org.jboss.pnc.rest.api.parameters.PageParameters;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import java.util.Collection;
import java.util.List;

/**
 * @author <a href="mailto:matejonnet@gmail.opecom">Matej Lazar</a>
 */
@Mock
@Path("/pnc-rest/v2/builds")
public class OrchBuildEndpoint implements BuildEndpoint {

    @Inject
    OrchBuildProvider buildProvider;

    @Override
    public Page<Build> getAll(
            @Valid PageParameters pageParameters,
            BuildsFilterParameters buildsFilterParameters,
            List<String> list) {
        Collection<Build> builds = buildProvider.getBuilds();
        return new Page<>(0, builds.size(), builds.size(), builds);
    }

    @Override
    public Build getSpecific(String id) {
        return buildProvider.getById(id);
    }

    @Override
    public void delete(String id, String callback) {
        throw new UnsupportedOperationException("Not yet implemented in mock.");
    }

    @Override
    public void update(String id, @NotNull Build build) {
        throw new UnsupportedOperationException("Not yet implemented in mock.");
    }

    @Override
    public Page<Artifact> getBuiltArtifacts(String id, @Valid PageParameters pageParameters) {
        throw new UnsupportedOperationException("Not yet implemented in mock.");
    }

    @Override
    public void setBuiltArtifacts(String id, List<String> artifactIds) {
        throw new UnsupportedOperationException("Not yet implemented in mock.");
    }

    @Override
    public Page<Artifact> getDependencyArtifacts(String id, @Valid PageParameters pageParameters) {
        throw new UnsupportedOperationException("Not yet implemented in mock.");
    }

    @Override
    public void setDependentArtifacts(String id, List<String> artifactIds) {
        throw new UnsupportedOperationException("Not yet implemented in mock.");
    }

    @Override
    public Response getInternalScmArchiveLink(String id) {
        throw new UnsupportedOperationException("Not yet implemented in mock.");
    }

    @Override
    public void addAttribute(String id, String key, String value) {
        buildProvider.getById(id).getAttributes().put(key, value);
    }

    @Override
    public void removeAttribute(String id, String key) {
        buildProvider.getById(id).getAttributes().remove(key);
    }

    @Override
    public BuildPushReport getPushResult(String id) {
        throw new UnsupportedOperationException("Not yet implemented in mock.");
    }

    @Override
    public Page<BuildPushOperation> getPushOperations(String s, @Valid PageParameters pageParameters) {
        throw new UnsupportedOperationException("Not yet implemented in mock.");
    }

    @Override
    public BuildPushOperation push(String id, BuildPushParameters buildPushParameters) {
        throw new UnsupportedOperationException("Not yet implemented in mock.");
    }

    @Override
    public void cancelPush(String id) {
        throw new UnsupportedOperationException("Not yet implemented in mock.");
    }

    @Override
    public void completePush(String s, BuildPushCompleted buildPushCompleted) {
        throw new UnsupportedOperationException("Not yet implemented in mock.");
    }

    @Override
    public BuildConfigurationRevision getBuildConfigRevision(String id) {
        throw new UnsupportedOperationException("Not yet implemented in mock.");
    }

    @Override
    public void cancel(String id) {
        throw new UnsupportedOperationException("Not yet implemented in mock.");
    }

    @Override
    public Graph<Build> getDependencyGraph(String id) {
        throw new UnsupportedOperationException("Not yet implemented in mock.");
    }

    @Override
    public StreamingOutput getAlignLogs(String id) {
        throw new UnsupportedOperationException("Not yet implemented in mock.");
    }

    @Override
    public StreamingOutput getBuildLogs(String id) {
        throw new UnsupportedOperationException("Not yet implemented in mock.");
    }

    @Override
    public SSHCredentials getSshCredentials(String id) {
        throw new UnsupportedOperationException("Not yet implemented in mock.");
    }

    @Override
    public RunningBuildCount getCount() {
        throw new UnsupportedOperationException("Not yet implemented in mock.");
    }

    @Override
    public Page<Build> getAllIndependentTempBuildsOlderThanTimestamp(@Valid PageParameters pageParams, long timestamp) {
        throw new UnsupportedOperationException("Not yet implemented in mock.");
    }

    @Override
    public void createBuiltArtifactsQualityLevelRevisions(String id, String quality, String reason) {
        throw new UnsupportedOperationException("Not yet implemented in mock.");
    }

    @Override
    public Page<BuildRecordInsights> getAllBuildRecordInsightsNewerThanTimestamp(
            int pageSize,
            int pageIndex,
            long timestamp) {
        throw new UnsupportedOperationException("Not yet implemented in mock.");
    }

    @Override
    public Graph<Build> getImplicitDependencyGraph(String s, @Min(0L) @Max(5L) Integer integer) {
        throw new UnsupportedOperationException("Not yet implemented in mock.");
    }
}
