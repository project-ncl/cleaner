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

import io.quarkus.test.Mock;
import org.jboss.pnc.api.bifrost.dto.Line;
import org.jboss.pnc.api.bifrost.dto.MetaData;
import org.jboss.pnc.api.bifrost.enums.Direction;
import org.jboss.pnc.api.bifrost.enums.Format;
import org.jboss.pnc.api.bifrost.rest.Bifrost;
import org.jboss.pnc.common.Strings;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;

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
            Integer batchSize,
            Integer batchDelay,
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
}