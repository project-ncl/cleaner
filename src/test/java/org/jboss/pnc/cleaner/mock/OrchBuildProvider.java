package org.jboss.pnc.cleaner.mock;

import lombok.Getter;
import lombok.Setter;
import org.jboss.pnc.dto.Build;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author <a href="mailto:matejonnet@gmail.opecom">Matej Lazar</a>
 */
@Getter
@Setter
@ApplicationScoped
public class OrchBuildProvider {
    private Collection<Build> builds = new ArrayList<>();

    public boolean addBuild(Build build) {
        return builds.add(build);
    }

    public Build getById(String id) {
        return builds.stream().filter(b -> b.getId().equals(id)).findAny().orElse(null);
    }
}
