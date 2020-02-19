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
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import org.jboss.pnc.model.TargetRepository.Type;

import java.util.Set;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@JsonDeserialize(builder = TargetRepositoryRest.TargetRepositoryRestBuilder.class)
@AllArgsConstructor
@Builder
@ToString
public class TargetRepositoryRest {

    @Getter
    @Setter
    private Integer id;

    /**
     * Flag that the repository is temporary.
     */
    @Getter
    private Boolean temporaryRepo;

    @Getter
    private String identifier;

    @Getter
    private Type repositoryType;

    @Getter
    private String repositoryPath;

    @Getter
    private Set<Integer> artifactIds;

    @JsonPOJOBuilder(withPrefix = "")
    public static final class TargetRepositoryRestBuilder {
    }
}
