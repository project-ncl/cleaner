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
package org.jboss.pnc.cleaner.mock;

import lombok.Getter;
import lombok.Setter;
import org.jboss.pnc.dto.Build;

import jakarta.enterprise.context.ApplicationScoped;
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
