/*
 * SPDX-FileCopyrightText: Copyright © 2019 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.cleaner.mock;

import java.io.IOException;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.ws.rs.core.Response;

import org.jboss.pnc.api.bifrost.dto.Line;
import org.jboss.pnc.api.bifrost.dto.MetaData;
import org.jboss.pnc.api.bifrost.enums.Direction;
import org.jboss.pnc.api.bifrost.enums.Format;
import org.jboss.pnc.api.bifrost.rest.Bifrost;
import org.jboss.pnc.api.dto.ComponentVersion;
import org.jboss.pnc.common.Strings;

import io.quarkus.test.Mock;

/**
 * @author <a href="mailto:matejonnet@gmail.opecom">Matej Lazar</a>
 */
@Mock
public class BifrostEndpoint implements Bifrost {

    @Inject
    BifrostProvider provider;

    @Override
    public Response getAllLines(
            String matchFilters,
            String prefixFilters,
            Line afterLine,
            Direction direction,
            Format format,
            Integer maxLines,
            @Positive Integer tailLines,
            @Positive Integer batchSize,
            @Min(200L) Integer batchDelay,
            boolean follow,
            String timeoutProbeString) {
        throw new UnsupportedOperationException("Not yet implemented in mock.");
    }

    @Override
    public List<Line> getLines(
            String matchFilters,
            String prefixFilters,
            Line afterLine,
            Direction direction,
            Integer maxLines,
            Integer batchSize) throws IOException {
        throw new UnsupportedOperationException("Not yet implemented in mock.");
    }

    @Override
    public MetaData getMetaData(
            String matchFilters,
            String prefixFilters,
            Line afterLine,
            Direction direction,
            Integer maxLines,
            Integer batchSize) throws IOException {
        String processContext = Strings.toMap(matchFilters).get("mdc.processContext.keyword").get(0);
        return provider.getMetaDataForContext(processContext);
    }

    @Override
    public ComponentVersion getVersion() {
        return ComponentVersion.builder().build();
    }
}