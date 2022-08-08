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
