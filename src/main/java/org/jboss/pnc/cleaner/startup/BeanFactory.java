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
package org.jboss.pnc.cleaner.startup;

import org.commonjava.cdi.util.weft.config.DefaultWeftConfig;
import org.commonjava.cdi.util.weft.config.WeftConfig;
import org.commonjava.indy.client.core.metric.ClientGoldenSignalsMetricSet;
import org.commonjava.indy.client.core.metric.ClientTrafficClassifier;
import org.commonjava.o11yphant.metrics.TrafficClassifier;
import org.commonjava.o11yphant.metrics.conf.DefaultMetricsConfig;
import org.commonjava.o11yphant.metrics.conf.MetricsConfig;
import org.commonjava.o11yphant.metrics.sli.GoldenSignalsMetricSet;
import org.commonjava.o11yphant.metrics.system.StoragePathProvider;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

@ApplicationScoped
public class BeanFactory {

    // >>> Indy client required beans - start

    @Produces
    public GoldenSignalsMetricSet clientMetricSet() {
        return new ClientGoldenSignalsMetricSet();
    }

    @Produces
    public MetricsConfig metricsConfig() {
        return new DefaultMetricsConfig();
    }

    @Produces
    public StoragePathProvider storagePathProvider() {
        return () -> null;
    }

    @Produces
    public TrafficClassifier trafficClassifier() {
        return new ClientTrafficClassifier();
    }

    @Produces
    public WeftConfig weftConfig() {
        return new DefaultWeftConfig();
    }

    // <<< Indy client required beans - end

}
