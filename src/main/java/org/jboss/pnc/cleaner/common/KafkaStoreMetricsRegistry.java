package org.jboss.pnc.cleaner.common;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

public class KafkaStoreMetricsRegistry {
    private static KafkaStoreMetricsRegistry instance = null;
    private static MeterRegistry registry;

    private KafkaStoreMetricsRegistry() {

        registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
    }

    public static KafkaStoreMetricsRegistry getInstance() {
        if (instance == null) {
            instance = new KafkaStoreMetricsRegistry();
        }
        return instance;
    }

    public MeterRegistry getRegistry() {
        return registry;
    }
}
