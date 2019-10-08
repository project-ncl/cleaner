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

import lombok.Getter;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Getter
@ApplicationScoped
public class KeycloakClientConfig {
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

}
