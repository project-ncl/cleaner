package org.jboss.pnc.cleaner.mock;

import lombok.Getter;
import lombok.Setter;
import org.jboss.pnc.api.bifrost.dto.MetaData;

import javax.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:matejonnet@gmail.opecom">Matej Lazar</a>
 */
@Getter
@Setter
@ApplicationScoped
public class BifrostProvider {
    private Map<String, MetaData> metaDatas = new HashMap<>();

    public MetaData addMetaData(String context, MetaData metaData) {
        return metaDatas.put(context, metaData);
    }

    public MetaData getMetaDataForContext(String context) {
        return metaDatas.get(context);
    }
}
