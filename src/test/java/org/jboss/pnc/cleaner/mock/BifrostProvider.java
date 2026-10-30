/*
 * SPDX-FileCopyrightText: Copyright © 2019 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.cleaner.mock;

import java.util.HashMap;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;

import org.jboss.pnc.api.bifrost.dto.MetaData;

import lombok.Getter;
import lombok.Setter;

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
