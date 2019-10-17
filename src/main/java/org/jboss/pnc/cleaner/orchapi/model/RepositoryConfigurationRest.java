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
package org.jboss.pnc.cleaner.orchapi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The REST entity that contains configuration of the SCM repositories.
 *
 * @author Jakub Bartecek
 */
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
public class RepositoryConfigurationRest {

    @Getter
    @Setter
    private Integer id;

    @Getter
    @Setter
    private String internalUrl;

    @Getter
    @Setter
    private String externalUrl;

    @Getter
    @Setter
    private Boolean preBuildSyncEnabled;

    public RepositoryConfigurationRest() {
    }

}
