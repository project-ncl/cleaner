/*
 * SPDX-FileCopyrightText: Copyright © 2019 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.cleaner.orchApi;

import java.io.IOException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.pnc.client.Configuration;
import org.jboss.pnc.quarkus.client.auth.runtime.PNCClientAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    PNCClientAuth pncClientAuth;

    @ConfigProperty(name = "pnc_client_auth.type")
    PNCClientAuth.ClientAuthType clientAuthType;

    public Configuration getConfiguration(boolean authenticated) {
        Configuration.ConfigurationBuilder configurationBuilder = Configuration.builder()
                .addDefaultMdcToHeadersMappings();

        configurationBuilder.protocol(protocol);
        configurationBuilder.host(host);
        configurationBuilder.port(port);
        configurationBuilder.pageSize(pageSize);
        if (authenticated) {
            switch (clientAuthType) {
                case OIDC -> configurationBuilder.bearerTokenSupplier(() -> pncClientAuth.getAuthToken());
                case LDAP -> {
                    try {
                        PNCClientAuth.LDAPCredentials ldapCredentials = pncClientAuth.getLDAPCredentials();
                        configurationBuilder.basicAuth(
                                new Configuration.BasicAuth(ldapCredentials.username(), ldapCredentials.password()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
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
