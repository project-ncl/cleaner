/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.cleaner.auth;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.keycloak.representations.AccessTokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.CDI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@ApplicationScoped
public class DefaultKeycloakServiceClient implements KeycloakServiceClient {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @ConfigProperty(name = "serviceaccount.realm")
    String realm;
    @ConfigProperty(name = "serviceaccount.realmpublickey")
    String realmPublicKey;
    @ConfigProperty(name = "serviceaccount.authserverurl")
    String authServerUrl;
    @ConfigProperty(name = "serviceaccount.sslrequired")
    Boolean sslRequired;
    @ConfigProperty(name = "serviceaccount.resource")
    String resource;
    @ConfigProperty(name = "serviceaccount.secret")
    String secret;

    @ConfigProperty(defaultValue = "86400", name = "keycloak.refreshifexpiresinseconds")
    long serviceTokenRefreshIfExpiresInSeconds;

    private AccessTokenResponse keycloakToken;

    private Instant expiresAt;

    public DefaultKeycloakServiceClient() {
    }

    /**
     * A static method providing functionality to get authentication token for static methods using manual CDI lookup of the
     * KeycloakServiceClient bean
     *
     * @return A valid token
     */
    public static String getAuthTokenStatic() {
        KeycloakServiceClient serviceClient = CDI.current().select(KeycloakServiceClient.class).get();
        return serviceClient.getAuthToken();
    }

    @Override
    public String getAuthToken() {
        if (keycloakToken == null || refreshRequired()) {
            logger.debug(
                    "Requesting new service account auth token using values:\n" + "authServerUrl {}\n" + "realm {}\n"
                            + "resource {}\n" + "secret {}\n" + "sslRequired {}",
                    authServerUrl,
                    realm,
                    resource,
                    secret.replaceAll(".", "*"),
                    sslRequired);
            keycloakToken = KeycloakClient.getAuthTokensBySecret(authServerUrl, realm, resource, secret, sslRequired);
            expiresAt = Instant.now().plus(keycloakToken.getExpiresIn(), ChronoUnit.SECONDS);
        }
        return keycloakToken.getToken();
    }

    private boolean refreshRequired() {
        if (expiresAt.isAfter(Instant.now().plus(serviceTokenRefreshIfExpiresInSeconds, ChronoUnit.SECONDS))) {
            return true;
        }
        return false;
    }
}
