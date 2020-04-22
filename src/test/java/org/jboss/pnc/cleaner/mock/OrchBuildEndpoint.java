package org.jboss.pnc.cleaner.mock;

import io.quarkus.test.Mock;
import org.jboss.pnc.dto.Artifact;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.BuildConfigurationRevision;
import org.jboss.pnc.dto.BuildPushResult;
import org.jboss.pnc.dto.requests.BuildPushParameters;
import org.jboss.pnc.dto.response.Graph;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.dto.response.SSHCredentials;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.rest.api.endpoints.BuildEndpoint;
import org.jboss.pnc.rest.api.parameters.BuildsFilterParameters;
import org.jboss.pnc.rest.api.parameters.PageParameters;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.util.Collection;
import java.util.List;

/**
 * @author <a href="mailto:matejonnet@gmail.opecom">Matej Lazar</a>
 */
@Mock
@Path("/pnc-rest-new/rest-new/builds")
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
    public BuildPushResult getPushResult(String id) {
        throw new UnsupportedOperationException("Not yet implemented in mock.");
    }

    @Override
    public BuildPushResult push(String id, BuildPushParameters buildPushParameters) {
        throw new UnsupportedOperationException("Not yet implemented in mock.");
    }

    @Override
    public void cancelPush(String id) {
        throw new UnsupportedOperationException("Not yet implemented in mock.");
    }

    @Override
    public BuildPushResult completePush(String id, BuildPushResult buildPushResult) {
        throw new UnsupportedOperationException("Not yet implemented in mock.");
    }

    @Override
    public BuildConfigurationRevision getBuildConfigurationRevision(String id) {
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
    public Page<Build> getAllByStatusAndLogContaining(
            BuildStatus status,
            String search,
            @Valid PageParameters pageParameters) {
        throw new UnsupportedOperationException("Not yet implemented in mock.");
    }
}
