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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import io.quarkus.test.junit.QuarkusTest;
import org.jboss.pnc.cleaner.common.TestConstants;
import org.jboss.pnc.common.util.HttpUtils;
import org.jboss.pnc.common.util.TimeUtils;
import org.jboss.pnc.dto.response.DeleteOperationResult;
import org.jboss.pnc.enums.ResultStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.config.Config;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static java.time.Duration.ofSeconds;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

/**
 * @author Jakub Bartecek
 */
@QuarkusTest
public class TemporaryBuildsCleanerImplTest {

    static final String BUILDS_ENDPOINT = TestConstants.ROOT_PATH + "/builds";

    static final String GROUP_BUILDS_ENDPOINT = TestConstants.ROOT_PATH + "/group-builds";

    static final String GET_TEMP_BUILDS_ENDPOINT = "/independent-temporary-older-than-timestamp";

    static final String EMPTY_RESPONSE_FILE = "emptyResponse.json";

    static final String SINGLE_TEMPORARY_BUILD_FILE = "singleTemporaryBuild.json";

    static final String SINGLE_TEMPORARY_BUILD_GROUP_FILE = "singleTemporaryBuildGroup.json";

    private WireMockServer wireMockServer = new WireMockServer(
            options().port(8082).withRootDirectory("src/test/resources/wiremock/general"));

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

    @Inject
    Config config;

    @Test
    public void shouldDeleteATemporaryBuild() {
        // given
        final String buildId = "684";
        wireMockServer.stubFor(
                get(urlMatching(BUILDS_ENDPOINT + GET_TEMP_BUILDS_ENDPOINT + "\\?.*")).inScenario("scenario")
                        .whenScenarioStateIs(Scenario.STARTED)
                        .willReturn(
                                aResponse().withStatus(200)
                                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                        .withBodyFile(SINGLE_TEMPORARY_BUILD_FILE))
                        .willSetStateTo("Return nothing"));
        wireMockServer.stubFor(
                get(urlMatching(BUILDS_ENDPOINT + GET_TEMP_BUILDS_ENDPOINT + "\\?.*")).inScenario("scenario")
                        .whenScenarioStateIs("Return nothing")
                        .willReturn(
                                aResponse().withStatus(200)
                                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                        .withBodyFile(EMPTY_RESPONSE_FILE)));

        String host = config.getValue("applicationUri", String.class);
        assertEquals("0.0.0.0:8080", host);
        String deleteRequestRegex = BUILDS_ENDPOINT + "/" + buildId
                + "\\?callback=0\\.0\\.0\\.0%3A8080%2Fcallbacks%2Fdelete%2Fbuilds%2F684";
        wireMockServer.stubFor(delete(urlMatching(deleteRequestRegex)).willReturn(aResponse().withStatus(200)));

        startCallbackThread("http://localhost:8081/callbacks/delete/builds/684");

        // when
        assertTimeoutPreemptively(
                ofSeconds(15),
                () -> temporaryBuildsCleaner.deleteExpiredBuildRecords(TimeUtils.getDateXDaysAgo(14)));

        // then
        wireMockServer.verify(1, deleteRequestedFor(urlMatching(deleteRequestRegex)));
    }

    private void startCallbackThread(String callbackUrl) {
        Thread callbackThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                    performCallback(callbackUrl);
                } catch (Exception e) {
                    System.err.println("Callback operation failed! URL=" + callbackUrl + "; " + e);
                }
            }
        });
        callbackThread.start();
    }

    private void performCallback(String callbackUrl) throws JsonProcessingException {
        DeleteOperationResult deleteOperationResult = new DeleteOperationResult();
        deleteOperationResult.setId("100");
        deleteOperationResult.setStatus(ResultStatus.SUCCESS);
        deleteOperationResult.setMessage("Build 100 was deleted successfully!");

        HttpUtils.performHttpPostRequest(callbackUrl, deleteOperationResult);
    }

    @Test
    public void shouldSucceedIfNoBuildToBeDeleted() {
        // given
        wireMockServer.stubFor(
                get(urlMatching(BUILDS_ENDPOINT + ".*")).willReturn(
                        aResponse().withStatus(200)
                                .withBodyFile(EMPTY_RESPONSE_FILE)
                                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)));

        // when
        temporaryBuildsCleaner.deleteExpiredBuildRecords(TimeUtils.getDateXDaysAgo(14));

        // then
        wireMockServer.verify(0, deleteRequestedFor(urlMatching(BUILDS_ENDPOINT + ".*")));
    }

    @Test
    public void shouldFailSafeBuildDeletionIfRemoteServerIsNotWorking() {
        // given
        wireMockServer.stubFor(
                get(urlMatching(BUILDS_ENDPOINT + ".*")).willReturn(
                        aResponse().withStatus(500).withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)));

        // when
        temporaryBuildsCleaner.deleteExpiredBuildRecords(TimeUtils.getDateXDaysAgo(14));

        // then - nothing should happen
    }

    @Test
    public void shouldDeleteATemporaryBuildGroup() {
        // given
        final String buildId = "166";
        wireMockServer.stubFor(
                get(urlMatching(GROUP_BUILDS_ENDPOINT + "\\?.*")).willReturn(
                        aResponse().withStatus(200)
                                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBodyFile(SINGLE_TEMPORARY_BUILD_GROUP_FILE)));

        String deleteRequestRegex = GROUP_BUILDS_ENDPOINT + "/" + buildId
                + "\\?callback=0\\.0\\.0\\.0%3A8080%2Fcallbacks%2Fdelete%2Fgroup-builds%2F166";
        wireMockServer.stubFor(delete(urlMatching(deleteRequestRegex)).willReturn(aResponse().withStatus(200)));

        startCallbackThread("http://localhost:8081/callbacks/delete/group-builds/166");

        // when
        assertTimeoutPreemptively(
                ofSeconds(15),
                () -> temporaryBuildsCleaner.deleteExpiredBuildConfigSetRecords(TimeUtils.getDateXDaysAgo(14)));

        // then
        wireMockServer.verify(1, deleteRequestedFor(urlMatching(deleteRequestRegex)));
    }

    @Test
    public void shouldSucceedIfNoBuildGroupToBeDeleted() {
        // given
        wireMockServer.stubFor(
                get(urlMatching(GROUP_BUILDS_ENDPOINT + ".*")).willReturn(
                        aResponse().withStatus(200)
                                .withBodyFile(EMPTY_RESPONSE_FILE)
                                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)));

        // when
        temporaryBuildsCleaner.deleteExpiredBuildConfigSetRecords(TimeUtils.getDateXDaysAgo(14));

        // then
        wireMockServer.verify(0, deleteRequestedFor(urlMatching(GROUP_BUILDS_ENDPOINT + ".*")));
    }

    @Test
    public void shouldFailSafeBuildGroupDeletionIfRemoteServerIsNotWorking() {
        // given
        wireMockServer.stubFor(
                get(urlMatching(GROUP_BUILDS_ENDPOINT + ".*")).willReturn(
                        aResponse().withStatus(500).withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)));

        // when
        temporaryBuildsCleaner.deleteExpiredBuildConfigSetRecords(TimeUtils.getDateXDaysAgo(14));

        // then - nothing should happen
    }
}
