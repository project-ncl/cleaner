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
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import io.quarkus.test.junit.QuarkusTest;
import org.jboss.pnc.cleaner.orchapi.model.DeleteOperationResult;
import org.jboss.pnc.common.util.HttpUtils;
import org.jboss.pnc.common.util.TimeUtils;
import org.jboss.pnc.spi.coordinator.Result;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static java.time.Duration.ofSeconds;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

/**
 * @author Jakub Bartecek
 */
@QuarkusTest
public class TemporaryBuildsCleanerImplTest {

    static final String BUILDS_ENDPOINT = "/build-records/";

    static final String BUILDS_TEMPORARY_OLDER_THAN_ENDPOINT = "/build-records/temporary-older-than-timestamp";

    static final String SINGLE_TEMPORARY_BUILD_FILE = "singleTemporaryBuild.json";

    static final String BUILDS_GROUPS_ENDPOINT = "/build-config-set-records/";

    static final String BUILD_GROUPS_TEMPORARY_OLDER_THAN_ENDPOINT = "/build-config-set-records/temporary-older-than-timestamp";

    static final String SINGLE_TEMPORARY_BUILD_GROUP_FILE = "singleTemporaryBuildGroup.json";

    private static WireMockServer keycloakServer = new WireMockServer(
            options().port(8084).withRootDirectory("src/test/resources/wiremock/keycloak"));

    private WireMockServer wireMockServer = new WireMockServer(
            options().port(8082).withRootDirectory("src/test/resources/wiremock/general"));

    @Inject
    TemporaryBuildsCleanerImpl temporaryBuildsCleaner;

    @BeforeAll
    public static void beforeAll() {
        keycloakServer.start();
    }

    @AfterAll
    public static void afterAll() {
        keycloakServer.stop();
    }

    @BeforeEach
    public void beforeEach() {
        wireMockServer.start();
    }

    @AfterEach
    public void afterEach() {
        wireMockServer.stop();
    }

    @Test
    public void shouldDeleteATemporaryBuild() {
        // given
        final int buildId = 100;
        wireMockServer.stubFor(get(urlMatching(BUILDS_TEMPORARY_OLDER_THAN_ENDPOINT + ".*"))
                .willReturn(aResponse().withStatus(200).withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile(SINGLE_TEMPORARY_BUILD_FILE)));

        wireMockServer.stubFor(delete(urlMatching(
                BUILDS_ENDPOINT + buildId + "\\?callback=0\\.0\\.0\\.0%3A8080%2Fcallbacks%2Fbuild-record-delete%2F100"))
                        .willReturn(aResponse().withStatus(200)));

        startCallbackThread("http://0.0.0.0:8081/callbacks/build-record-delete/100");

        // when
        assertTimeoutPreemptively(ofSeconds(15),
                () -> temporaryBuildsCleaner.deleteExpiredBuildRecords(TimeUtils.getDateXDaysAgo(14)));

        // then
        wireMockServer.verify(1, deleteRequestedFor(urlMatching(BUILDS_ENDPOINT + ".*")));
    }

    private void startCallbackThread(String callbackUrl) {
        Thread callbackThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                    performCallback(callbackUrl);
                } catch (Exception e) {
                    System.err.println("Callback operation failed! " + e);
                }
            }
        });
        callbackThread.start();
    }

    private void performCallback(String callbackUrl) throws JsonProcessingException {
        DeleteOperationResult deleteOperationResult = new DeleteOperationResult();
        deleteOperationResult.setId("100");
        deleteOperationResult.setStatus(Result.Status.SUCCESS);
        deleteOperationResult.setMessage("Build 100 was deleted successfully!");

        HttpUtils.performHttpPostRequest(callbackUrl, deleteOperationResult);
    }

    @Test
    public void shouldAddAuthorizationHeadersWhenDeleting() {
        // given
        final int buildId = 100;
        wireMockServer.stubFor(get(urlMatching(BUILDS_TEMPORARY_OLDER_THAN_ENDPOINT + ".*"))
                .willReturn(aResponse().withStatus(200).withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile(SINGLE_TEMPORARY_BUILD_FILE)));

        wireMockServer.stubFor(delete(urlMatching(
                BUILDS_ENDPOINT + buildId + "\\?callback=0\\.0\\.0\\.0%3A8080%2Fcallbacks%2Fbuild-record-delete%2F100"))
                        .willReturn(aResponse().withStatus(200)));
        startCallbackThread("http://0.0.0.0:8081/callbacks/build-record-delete/100");

        // when
        temporaryBuildsCleaner.deleteExpiredBuildRecords(TimeUtils.getDateXDaysAgo(14));

        // then
        wireMockServer.verify(1,
                deleteRequestedFor(urlMatching(BUILDS_ENDPOINT + ".*")).withHeader("Authorization", new EqualToPattern("Bearer "
                        + "fyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJ5emxOa0tBUmZlMUVzcHZJbU9rdkVTeUttVGl6N05MTWp2Z3lSSGJEZHVBIn0.eyJqdGkiOiJjNDNhMGE5OS1lZmQ3LTQ4NDUtOTliNS0yN2VjNmJiN2IyZGQiLCJleHAiOjE1NzM5MDM4NjUsIm5iZiI6MCwiaWF0IjoxNTczNzMxMDY1LCJpc3MiOiJodHRwczovL3NlY3VyZS1zc28tbmV3Y2FzdGxlLWRldmVsLnBzaS5yZWRoYXQuY29tL2F1dGgvcmVhbG1zL3BuY3JlZGhhdCIsImF1ZCI6WyJwbmNpbmR5dWkiLCJwbmNpbmR5IiwiYWNjb3VudCJdLCJzdWIiOiIyODI0Yzk1ZS0zZjM2LTQyYjktYTljNy1hZjhkNWMyZjhjOTciLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJwbmMtb3JjaGVzdHJhdG9yIiwiYXV0aF90aW1lIjowLCJzZXNzaW9uX3N0YXRlIjoiYjc1NDcwZWMtY2E3Ny00MTYzLThiNTktMGFjZThmMGEwM2Y1IiwiYWNyIjoiMSIsInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJzeXN0ZW0tdXNlciIsIm9mZmxpbmVfYWNjZXNzIiwicG93ZXItdXNlciIsInVtYV9hdXRob3JpemF0aW9uIiwidXNlciJdfSwicmVzb3VyY2VfYWNjZXNzIjp7InBuY2luZHl1aSI6eyJyb2xlcyI6WyJwbmNpbmR5YWRtaW4iLCJwbmNpbmR5dXNlciJdfSwicG5jaW5keSI6eyJyb2xlcyI6WyJwbmNpbmR5YWRtaW4iLCJwb3dlci11c2VyIiwicG5jaW5keXVzZXIiXX0sImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoiIiwiY2xpZW50SWQiOiJwbmMtb3JjaGVzdHJhdG9yIiwiY2xpZW50SG9zdCI6IjE3Mi41NC4xMC4xIiwicHJlZmVycmVkX3VzZXJuYW1lIjoic2VydmljZS1hY2NvdW50LXBuYy1vcmNoZXN0cmF0b3IiLCJjbGllbnRBZGRyZXNzIjoiMTcyLjU0LjEwLjEiLCJlbWFpbCI6InNlcnZpY2UtYWNjb3VudC1wbmMtb3JjaGVzdHJhdG9yQHBsYWNlaG9sZGVyLm9yZyJ9.b8GT6tWnlOuQpoamNnIOOvLwJgwMYrhx9p6ynDx5iOmn2FM02NXK5DGuxofFznGX3kfk1YUfD3T6Bvb234GVDwgCojk_h9_uiVjdZ-f0sBWOt9Zpa_m1p1TpC8pca4kz605oYhEZ8po9Zx6wxuvFegsK1XsnuMi2saDK0dMnWT9AdjkoFsASPdrR25LLmy53KC29hxMqrJgmQc4yYOE8f4r8eHS53u2-D5JaywyGrGiTpMH5NGbqPpKM7aPbnaPcFfmzcDTT4_U3V0vBKblZy9bn5iB-K1FOFIeegqTo4OnhDfJ5Ay_qogGUO3aBH5FmiB-RStC8vuvuzS5QsiFeKw")));
    }

    @Test
    public void shouldSucceedIfNoBuildToBeDeleted() {
        // given
        wireMockServer.stubFor(get(urlMatching(BUILDS_TEMPORARY_OLDER_THAN_ENDPOINT + ".*"))
                .willReturn(aResponse().withStatus(204).withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)));

        // when
        temporaryBuildsCleaner.deleteExpiredBuildRecords(TimeUtils.getDateXDaysAgo(14));

        // then
        wireMockServer.verify(0, deleteRequestedFor(urlMatching(BUILDS_ENDPOINT + ".*")));
    }

    @Test
    public void shouldFailSafeBuildDeletionIfRemoteServerIsNotWorking() {
        // given
        wireMockServer.stubFor(get(urlMatching(BUILDS_TEMPORARY_OLDER_THAN_ENDPOINT + ".*"))
                .willReturn(aResponse().withStatus(500).withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)));

        // when
        temporaryBuildsCleaner.deleteExpiredBuildRecords(TimeUtils.getDateXDaysAgo(14));

        // then - nothing should happen
    }

    @Test
    public void shouldDeleteATemporaryBuildGroup() {
        // given
        final int buildId = 102;
        wireMockServer.stubFor(get(urlMatching(BUILD_GROUPS_TEMPORARY_OLDER_THAN_ENDPOINT + ".*"))
                .willReturn(aResponse().withStatus(200).withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile(SINGLE_TEMPORARY_BUILD_GROUP_FILE)));

        wireMockServer.stubFor(delete(urlMatching(BUILDS_GROUPS_ENDPOINT + buildId
                + "\\?callback=0\\.0\\.0\\.0%3A8080%2Fcallbacks%2Fbuild-group-record-delete%2F102"))
                        .willReturn(aResponse().withStatus(200)));

        startCallbackThread("http://0.0.0.0:8081/callbacks/build-group-record-delete/102");

        // when
        assertTimeoutPreemptively(ofSeconds(15),
                () -> temporaryBuildsCleaner.deleteExpiredBuildConfigSetRecords(TimeUtils.getDateXDaysAgo(14)));

        // then
        wireMockServer.verify(1, deleteRequestedFor(urlMatching(BUILDS_GROUPS_ENDPOINT + ".*")));
    }

    @Test
    public void shouldSucceedIfNoBuildGroupToBeDeleted() {
        // given
        wireMockServer.stubFor(get(urlMatching(BUILD_GROUPS_TEMPORARY_OLDER_THAN_ENDPOINT + ".*"))
                .willReturn(aResponse().withStatus(204).withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)));

        // when
        temporaryBuildsCleaner.deleteExpiredBuildConfigSetRecords(TimeUtils.getDateXDaysAgo(14));

        // then
        wireMockServer.verify(0, deleteRequestedFor(urlMatching(BUILDS_GROUPS_ENDPOINT + ".*")));
    }

    @Test
    public void shouldFailSafeBuildGroupDeletionIfRemoteServerIsNotWorking() {
        // given
        wireMockServer.stubFor(get(urlMatching(BUILD_GROUPS_TEMPORARY_OLDER_THAN_ENDPOINT + ".*"))
                .willReturn(aResponse().withStatus(500).withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)));

        // when
        temporaryBuildsCleaner.deleteExpiredBuildConfigSetRecords(TimeUtils.getDateXDaysAgo(14));

        // then - nothing should happen
    }
}
