package org.jboss.pnc.cleaner.mock;

import io.quarkus.test.Mock;
import org.jboss.pnc.api.bifrost.dto.Line;
import org.jboss.pnc.api.bifrost.dto.MetaData;
import org.jboss.pnc.api.bifrost.enums.Direction;
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
            Integer maxLines,
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
            Integer maxLines) throws IOException {
        throw new UnsupportedOperationException("Not yet implemented in mock.");
    }

    @Override
    public MetaData getMetaData(
            String matchFilters,
            String prefixFilters,
            Line afterLine,
            Direction direction,
            Integer maxLines) throws IOException {
        String processContext = Strings.toMap(matchFilters).get("mdc.processContext.keyword").get(0);
        return provider.getMetaDataForContext(processContext);
    }
}
