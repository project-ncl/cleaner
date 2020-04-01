/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.cleaner.logverifier;

import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.jboss.pnc.api.bifrost.dto.MetaData;
import org.jboss.pnc.cleaner.mock.BifrostProvider;
import org.jboss.pnc.cleaner.mock.OrchBuildProvider;
import org.jboss.pnc.dto.Build;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import javax.inject.Inject;
import java.util.HashMap;

import static org.jboss.pnc.cleaner.logverifier.BuildLogVerifier.BUILD_OUTPUT_OK_KEY;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTest
public class LogVerifierTest {

    @Inject
    BuildLogVerifier buildLogVerifier;

    @Inject
    OrchBuildProvider orchBuildProvider;

    @Inject
    BifrostProvider bifrostProvider;

    @Inject
    OrchClientConfigurationMock orchClientConfiguration;

    @Test
    @Order(-1) // Before all methods
    public void prepare() {
        log.info("Overriding configuration ...");
        orchClientConfiguration.setPort(8081); //Quarkus test server port
    }

    @Test
    @Order(Integer.MAX_VALUE) // After all methods
    public void cleanup() {
        log.info("Restoring configuration ...");
        orchClientConfiguration.setPort(8082); //wiremock server used by other tests
    }

    @Test
    public void shouldMarkMatchedChecksum() {
        // given
        Build build1 = Build.builder().id("1").buildOutputChecksum("match").attributes(new HashMap<>()).build();
        orchBuildProvider.addBuild(build1);
        Build build2 = Build.builder().id("2").buildOutputChecksum("dont-match").attributes(new HashMap<>()).build();
        orchBuildProvider.addBuild(build2);

        bifrostProvider.addMetaData("build-1", new MetaData("match"));
        bifrostProvider.addMetaData("build-2", new MetaData("wrong"));

        // when
        buildLogVerifier.verifyUnflaggedBuilds();

        // then
        Build build1Updated = orchBuildProvider.getById("1");
        Assertions.assertEquals(Boolean.TRUE.toString(), build1Updated.getAttributes().get(BUILD_OUTPUT_OK_KEY));

        Build build2Updated = orchBuildProvider.getById("2");
        Assertions.assertEquals(Boolean.FALSE.toString(), build2Updated.getAttributes().get(BUILD_OUTPUT_OK_KEY));
    }
}
