/*
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
package org.jboss.pnc.cleaner.orchApi;

import io.quarkus.oidc.client.OidcClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.pnc.client.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Configuration for the Orchestrator client configurable using properties
 *
 * @author Jakub Bartecek
 */
@ApplicationScoped
public class OrchClientConfiguration {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @ConfigProperty(name = "orch.protocol")
    protected String protocol;

    @ConfigProperty(name = "orch.host")
    protected String host;

    @ConfigProperty(name = "orch.port")
    protected Integer port;

    @ConfigProperty(name = "orch.pageSize", defaultValue = "50")
    protected Integer pageSize;

    @Inject
    OidcClient oidcClient;

    public Configuration getConfiguration(boolean authenticated) {
        Configuration.ConfigurationBuilder configurationBuilder = Configuration.builder()
                .addDefaultMdcToHeadersMappings();

        configurationBuilder.protocol(protocol);
        configurationBuilder.host(host);
        configurationBuilder.port(port);
        configurationBuilder.pageSize(pageSize);
        if (authenticated) {
            configurationBuilder
                    .bearerTokenSupplier(() -> oidcClient.getTokens().await().indefinitely().getAccessToken());
        }

        return configurationBuilder.build();
    }

    /**
     * By default, we don't want authentication
     *
     * @return
     */
    public Configuration getConfiguration() {
        return getConfiguration(false);
    }
}
