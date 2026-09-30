/*
 * SPDX-FileCopyrightText: Copyright © 2019 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.cleaner.mock;

import java.util.ArrayList;
import java.util.Collection;

import jakarta.enterprise.context.ApplicationScoped;

import org.jboss.pnc.dto.Build;

import lombok.Getter;
import lombok.Setter;

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
