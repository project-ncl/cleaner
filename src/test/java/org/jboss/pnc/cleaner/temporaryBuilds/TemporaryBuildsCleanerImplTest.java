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
package org.jboss.pnc.cleaner.temporaryBuilds;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.quarkus.test.junit.QuarkusTest;
import org.jboss.pnc.cleaner.temporaryBuilds.TemporaryBuildsCleaner;
import org.jboss.pnc.common.util.TimeUtils;
import org.jboss.pnc.spi.exception.ValidationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.head;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

/**
 * @author Jakub Bartecek
 */
@QuarkusTest
public class TemporaryBuildsCleanerImplTest {

    static final String BUILD_RECORD_ENDPOINT = "/build-records/";

    static final String TEMPORARY_OLDER_THAN_ENDPOINT = "/build-records/temporary-older-than-timestamp";

    static final String SINGLE_TEMPORARY_BUILD_FILE = "singleTemporaryBuild.json";

    private WireMockServer wireMockServer = new WireMockServer(options().port(8082));

    @Inject
    TemporaryBuildsCleanerImpl temporaryBuildsCleaner;

    @BeforeEach
    public void beforeEach() {
        wireMockServer.start();
    }

    @AfterEach
    public void afterEach() {
        wireMockServer.stop();
    }

    @Test
    public void shouldDeleteATemporaryBuild() throws ValidationException {
        // given
        final int buildId = 100;
        wireMockServer.stubFor(get(urlMatching(TEMPORARY_OLDER_THAN_ENDPOINT + ".*")).willReturn(aResponse()
                .withStatus(200)
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .withBodyFile(SINGLE_TEMPORARY_BUILD_FILE)));

        wireMockServer.stubFor(delete(BUILD_RECORD_ENDPOINT + buildId)
        .willReturn(aResponse().withStatus(200)));

        // when
        temporaryBuildsCleaner.deleteExpiredBuildRecords(TimeUtils.getDateXDaysAgo(14));

        // then
        wireMockServer.verify(deleteRequestedFor(urlEqualTo(BUILD_RECORD_ENDPOINT + buildId)));
        wireMockServer.verify(1, deleteRequestedFor(urlMatching(BUILD_RECORD_ENDPOINT + ".*")));
    }

}
