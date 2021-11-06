package org.jboss.pnc.cleaner.builds;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import io.quarkus.test.junit.QuarkusTest;

import com.github.tomakehurst.wiremock.WireMockServer;

import org.commonjava.indy.client.core.Indy;
import org.commonjava.indy.model.core.StoreKey;
import org.commonjava.indy.model.core.StoreType;
import org.jboss.pnc.cleaner.common.TestConstants;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import java.time.Instant;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.head;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.commonjava.indy.pkg.maven.model.MavenPackageTypeDescriptor.MAVEN_PKG_KEY;
import static org.commonjava.indy.model.core.GenericPackageTypeDescriptor.GENERIC_PKG_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class FailedBuildsCleanerTest {

    private static final String INDY_FOLO_ADMIN_ENDPOINT = "/api/folo/admin";

    private static final String INDY_STORE_ENDPOINT = "/api/admin/stores";

    private static final String INDY_STORE_MAVEN_GROUP = INDY_STORE_ENDPOINT + "/maven/group";

    private static final String INDY_STORE_GENERIC_GROUP = INDY_STORE_ENDPOINT + "/generic-http/group";

    private static final String INDY_STORE_MAVEN_HOSTED = INDY_STORE_ENDPOINT + "/maven/hosted";

    private static final String INDY_GENERIC_GROUPS_FILE = "indyGenericGroups.json";

    private static final String INDY_MAVEN_GROUPS_FILE = "indyMavenGroups.json";

    private static final String INDY_MAVEN_GROUPS_NO_BUILD_GROUP_FILE = "indyMavenGroupsNoBuildGroup.json";

    private static final String ORCH_BUILDS = TestConstants.ROOT_PATH + "/builds";

    private static final String BUILD_RECORD_FAILED_NO_CONTENTID_FILE = "buildRecordFailedNoContentId.json";

    private static final String BUILD_RECORDS_BUILDING_FILE = "buildRecordsBuilding.json";

    private static final String BUILD_RECORDS_FAILED_FILE = "buildRecordsFailed.json";

    private static final String BUILD_RECORDS_NOT_FAILED_FILE = "buildRecordsNotFailed.json";

    private static final String EMPTY_RESPONSE_FILE = "emptyResponse.json";

    private WireMockServer orchWireMockServer = new WireMockServer(options().port(8082));

    private WireMockServer indyWireMockServer = new WireMockServer(options().port(8083));

    @Inject
    private FailedBuildsCleaner failedBuildsCleaner;

    private ResponseDefinitionBuilder EMPTY_RESPONSE = aResponse().withStatus(200)
            .withBodyFile(EMPTY_RESPONSE_FILE)
            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);

    @BeforeEach
    public void beforeEach() {
        indyWireMockServer.start();
        orchWireMockServer.start();
    }

    @AfterEach
    public void afterEach() {
        indyWireMockServer.stop();
        orchWireMockServer.stop();
    }

    /**
     * Tests parsing of build group names from the list of all maven groups that are usually in Indy. It contains 1 old
     * format ("build_&lt;some string&gt;") build group and 2 new format ("build-&lt;number&gt;"). Then checks if the
     * resulting list contains 3 entries and if the expected names are in there.
     */
    @Test
    public void getGroupNamesOk() {
        indyWireMockServer.stubFor(
                get(urlMatching(INDY_STORE_MAVEN_GROUP)).willReturn(
                        aResponse().withStatus(200)
                                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBodyFile(INDY_MAVEN_GROUPS_FILE)));

        // limit nor auth token is not important for getting group names
        Instant limit = Instant.now();
        Indy indyClient = failedBuildsCleaner.initIndy("");
        FailedBuildsCleanerSession session = new FailedBuildsCleanerSession(indyClient, limit);

        List<String> groupNames = failedBuildsCleaner.getGroupNames(MAVEN_PKG_KEY, session);

        assertEquals(3, groupNames.size());
        assertTrue(groupNames.contains("build_wildfly-swarm-keycloak-config-api_20170310.1332"));
        assertTrue(groupNames.contains("build-32064"));
        assertTrue(groupNames.contains("build-30573"));
    }

    /**
     * Tests parsing of build group names from the list containing only maven groups that are usually in Indy with no
     * build groups. Then checks, if the resulting list is empty.
     */
    @Test
    public void getGroupNamesNoBuildGroup() {
        indyWireMockServer.stubFor(
                get(urlMatching(INDY_STORE_MAVEN_GROUP)).willReturn(
                        aResponse().withStatus(200)
                                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBodyFile(INDY_MAVEN_GROUPS_NO_BUILD_GROUP_FILE)));

        // limit nor auth token is not important for getting group names
        Instant limit = Instant.now();
        Indy indyClient = failedBuildsCleaner.initIndy("");
        FailedBuildsCleanerSession session = new FailedBuildsCleanerSession(indyClient, limit);

        List<String> groupNames = failedBuildsCleaner.getGroupNames(MAVEN_PKG_KEY, session);

        assertEquals(0, groupNames.size());
    }

    /**
     * Tests the logic saying if a build's repos should be cleaned or not. This test checks a failed build that ended
     * before the limit timestamp and expects it should be cleaned.
     */
    @Test
    public void shouldCleanOk() throws CleanerException {
        orchWireMockServer.stubFor(
                get(urlMatching(ORCH_BUILDS + "?.*q=buildContentId%3D%3Dbuild-36000")).willReturn(
                        aResponse().withStatus(200)
                                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBodyFile(BUILD_RECORDS_FAILED_FILE)));

        // limit is set to be after the build record end time
        Instant limit = Instant.ofEpochMilli(1581174847000L);
        // auth token is not important for the test
        Indy indyClient = failedBuildsCleaner.initIndy("");
        FailedBuildsCleanerSession session = new FailedBuildsCleanerSession(indyClient, limit);

        boolean clean = failedBuildsCleaner.shouldClean(MAVEN_PKG_KEY, "build-36000", session);

        assertTrue(clean);
    }

    /**
     * Tests the logic saying if a build's repos should be cleaned or not. This test checks a failed build that ended
     * before the limit timestamp but it is missing a build content ID, so it should be found by parsed numeric ID and
     * expects it should be cleaned.
     */
    @Test
    public void shouldCleanNoContentId() throws CleanerException {
        orchWireMockServer.stubFor(
                get(urlMatching(ORCH_BUILDS + "?.*q=buildContentId%3D%3Dbuild-36000")).willReturn(EMPTY_RESPONSE));

        orchWireMockServer.stubFor(
                get(urlMatching(ORCH_BUILDS + "/36000")).willReturn(
                        aResponse().withStatus(200)
                                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBodyFile(BUILD_RECORD_FAILED_NO_CONTENTID_FILE)));

        // limit is set to be after the build record end time
        Instant limit = Instant.ofEpochMilli(1581174847000L);
        // auth token is not important for the test
        Indy indyClient = failedBuildsCleaner.initIndy("");
        FailedBuildsCleanerSession session = new FailedBuildsCleanerSession(indyClient, limit);

        boolean clean = failedBuildsCleaner.shouldClean(MAVEN_PKG_KEY, "build-36000", session);

        assertTrue(clean);
    }

    /**
     * Tests the logic saying if a build's repos should be cleaned or not. This test checks a failed build that ended
     * after the limit timestamp but it is missing a build content ID, so it should be found by parsed numeric ID and
     * expects it should NOT be cleaned.
     */
    @Test
    public void shouldCleanNoContentIdTooYoung() throws CleanerException {
        orchWireMockServer.stubFor(
                get(urlMatching(ORCH_BUILDS + "?.*q=buildContentId%3D%3Dbuild-36000")).willReturn(EMPTY_RESPONSE));

        orchWireMockServer.stubFor(
                get(urlMatching(ORCH_BUILDS + "/36000")).willReturn(
                        aResponse().withStatus(200)
                                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBodyFile(BUILD_RECORD_FAILED_NO_CONTENTID_FILE)));

        // limit is set to be before the build record end time
        Instant limit = Instant.ofEpochMilli(1573174447816L);
        // auth token is not important for the test
        Indy indyClient = failedBuildsCleaner.initIndy("");
        FailedBuildsCleanerSession session = new FailedBuildsCleanerSession(indyClient, limit);

        boolean clean = failedBuildsCleaner.shouldClean(MAVEN_PKG_KEY, "build-36000", session);

        assertFalse(clean);
    }

    /**
     * Tests the logic saying if a build's repos should be cleaned or not. This test checks a non-existing build and
     * expects it should be cleaned.
     */
    @Test
    public void shouldCleanNotFound() throws CleanerException {
        orchWireMockServer.stubFor(
                get(urlMatching(ORCH_BUILDS + "?.*q=buildContentId%3D%3Dbuild-36002")).willReturn(EMPTY_RESPONSE));

        orchWireMockServer.stubFor(get(urlMatching(ORCH_BUILDS + "/36002")).willReturn(aResponse().withStatus(404)));

        // limit is set to be after the build record end time
        Instant limit = Instant.ofEpochMilli(1573174847816L);
        // auth token is not important for the test
        Indy indyClient = failedBuildsCleaner.initIndy("");
        FailedBuildsCleanerSession session = new FailedBuildsCleanerSession(indyClient, limit);

        boolean clean = failedBuildsCleaner.shouldClean(MAVEN_PKG_KEY, "build-36002", session);

        assertTrue(clean);
    }

    /**
     * Tests the logic saying if a build's repos should be cleaned or not. This test checks a running build without an
     * endTime value and expects it should NOT be cleaned.
     */
    @Test
    public void shouldCleanBuilding() throws CleanerException {
        orchWireMockServer.stubFor(
                get(urlMatching(ORCH_BUILDS + "?.*q=buildContentId%3D%3Dbuild-36735")).willReturn(
                        aResponse().withStatus(200)
                                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBodyFile(BUILD_RECORDS_BUILDING_FILE)));

        // limit is set to be 6.6 days before the build start time
        Instant limit = Instant.ofEpochMilli(1573756869934L);
        // auth token is not important for the test
        Indy indyClient = failedBuildsCleaner.initIndy("");
        FailedBuildsCleanerSession session = new FailedBuildsCleanerSession(indyClient, limit);

        boolean clean = failedBuildsCleaner.shouldClean(MAVEN_PKG_KEY, "build-36735", session);

        assertFalse(clean);
    }

    /**
     * Tests the logic saying if a build's repos should be cleaned or not. This test checks a successful build which is
     * old enough and expects it should NOT be cleaned.
     */
    @Test
    public void shouldCleanNotFailed() throws CleanerException {
        orchWireMockServer.stubFor(
                get(urlMatching(ORCH_BUILDS + "?.*q=buildContentId%3D%3Dbuild-36001")).willReturn(
                        aResponse().withStatus(200)
                                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBodyFile(BUILD_RECORDS_NOT_FAILED_FILE)));

        // limit is set to be after the build record end time
        Instant limit = Instant.ofEpochMilli(1573175914256L);
        // auth token is not important for the test
        Indy indyClient = failedBuildsCleaner.initIndy("");
        FailedBuildsCleanerSession session = new FailedBuildsCleanerSession(indyClient, limit);

        boolean clean = failedBuildsCleaner.shouldClean(MAVEN_PKG_KEY, "build-36001", session);

        assertFalse(clean);
    }

    /**
     * Tests the logic saying if a build's repos should be cleaned or not. This test checks a failed build which is too
     * young to be cleaned and expects it should NOT be cleaned.
     */
    @Test
    public void shouldCleanTooYoung() throws CleanerException {
        orchWireMockServer.stubFor(
                get(urlMatching(ORCH_BUILDS + "?.*q=buildContentId%3D%3Dbuild-36000")).willReturn(
                        aResponse().withStatus(200)
                                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBodyFile(BUILD_RECORDS_FAILED_FILE)));

        // limit is set to be before the build record end time
        Instant limit = Instant.ofEpochMilli(1573174447816L);
        // auth token is not important for the test
        Indy indyClient = failedBuildsCleaner.initIndy("");
        FailedBuildsCleanerSession session = new FailedBuildsCleanerSession(indyClient, limit);

        boolean clean = failedBuildsCleaner.shouldClean(MAVEN_PKG_KEY, "build-36000", session);

        assertFalse(clean);
    }

    /**
     * Tests finding of build-related generic-http repositories. It reads a list of generic-http groups which contains 3
     * groups matching the requested build content id. It expects to get those 3 groups in the result along with the
     * respective remote and hosted repositories.
     */
    @Test
    public void findGenericReposOk() {
        indyWireMockServer.stubFor(
                get(urlMatching(INDY_STORE_GENERIC_GROUP)).willReturn(
                        aResponse().withStatus(200)
                                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBodyFile(INDY_GENERIC_GROUPS_FILE)));

        // limit nor auth token is not important for getting group names
        Instant limit = Instant.now();
        Indy indyClient = failedBuildsCleaner.initIndy("");
        FailedBuildsCleanerSession session = new FailedBuildsCleanerSession(indyClient, limit);

        List<StoreKey> groupNames = failedBuildsCleaner.findGenericRepos("build-23013", session);

        assertEquals(9, groupNames.size());
        assertTrue(
                groupNames
                        .contains(new StoreKey(GENERIC_PKG_KEY, StoreType.group, "g-glassfish-java-net-build-23013")));
        assertTrue(
                groupNames
                        .contains(new StoreKey(GENERIC_PKG_KEY, StoreType.remote, "r-glassfish-java-net-build-23013")));
        assertTrue(
                groupNames
                        .contains(new StoreKey(GENERIC_PKG_KEY, StoreType.hosted, "h-glassfish-java-net-build-23013")));
        assertTrue(groupNames.contains(new StoreKey(GENERIC_PKG_KEY, StoreType.group, "g-www-apache-org-build-23013")));
        assertTrue(
                groupNames.contains(new StoreKey(GENERIC_PKG_KEY, StoreType.remote, "r-www-apache-org-build-23013")));
        assertTrue(
                groupNames.contains(new StoreKey(GENERIC_PKG_KEY, StoreType.hosted, "h-www-apache-org-build-23013")));
        assertTrue(groupNames.contains(new StoreKey(GENERIC_PKG_KEY, StoreType.group, "g-nodejs-org-build-23013")));
        assertTrue(groupNames.contains(new StoreKey(GENERIC_PKG_KEY, StoreType.remote, "r-nodejs-org-build-23013")));
        assertTrue(groupNames.contains(new StoreKey(GENERIC_PKG_KEY, StoreType.hosted, "h-nodejs-org-build-23013")));
    }

    /**
     * Tests finding of build-related generic-http repositories. It reads a list of generic-http groups which contains 1
     * group matching the requested build content id in old format. It expects to get the group in the result along with
     * its remote and hosted repository.
     */
    @Test
    public void findGenericReposOldContentId() {
        indyWireMockServer.stubFor(
                get(urlMatching(INDY_STORE_GENERIC_GROUP)).willReturn(
                        aResponse().withStatus(200)
                                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBodyFile(INDY_GENERIC_GROUPS_FILE)));

        // limit nor auth token is not important for getting group names
        Instant limit = Instant.now();
        Indy indyClient = failedBuildsCleaner.initIndy("");
        FailedBuildsCleanerSession session = new FailedBuildsCleanerSession(indyClient, limit);

        List<StoreKey> groupNames = failedBuildsCleaner
                .findGenericRepos("build_org-keycloak-keycloak-connect-4-x_20181109.2045", session);

        assertEquals(3, groupNames.size());
        assertTrue(
                groupNames.contains(
                        new StoreKey(
                                GENERIC_PKG_KEY,
                                StoreType.group,
                                "g-nodereg-com-build_org-keycloak-keycloak-connect-4-x_20181109.2045")));
        assertTrue(
                groupNames.contains(
                        new StoreKey(
                                GENERIC_PKG_KEY,
                                StoreType.remote,
                                "r-nodereg-com-build_org-keycloak-keycloak-connect-4-x_20181109.2045")));
        assertTrue(
                groupNames.contains(
                        new StoreKey(
                                GENERIC_PKG_KEY,
                                StoreType.hosted,
                                "h-nodereg-com-build_org-keycloak-keycloak-connect-4-x_20181109.2045")));
    }

    /**
     * Tests finding of build-related generic-http repositories. It reads a list of generic-http groups which does not
     * contain any groups matching the requested build content id. It expects to get an empty list back.
     */
    @Test
    public void findGenericReposNoGroups() {
        indyWireMockServer.stubFor(
                get(urlMatching(INDY_STORE_GENERIC_GROUP)).willReturn(
                        aResponse().withStatus(200)
                                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBodyFile(INDY_GENERIC_GROUPS_FILE)));

        // limit nor auth token is not important for getting group names
        Instant limit = Instant.now();
        Indy indyClient = failedBuildsCleaner.initIndy("");
        FailedBuildsCleanerSession session = new FailedBuildsCleanerSession(indyClient, limit);

        List<StoreKey> groupNames = failedBuildsCleaner.findGenericRepos("build-23014", session);

        assertEquals(0, groupNames.size());
    }

    /**
     * Tests the whole cleanup logic for 1 build. The build failed and is old enough. It does not have any generic repos
     * and expects that delete for build group, hosted repo and tracking report were called.
     */
    @Test
    public void cleanBuildIfNeededOk() {
        orchWireMockServer.stubFor(
                get(urlMatching(ORCH_BUILDS + "?.*q=buildContentId%3D%3Dbuild-36000")).willReturn(
                        aResponse().withStatus(200)
                                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBodyFile(BUILD_RECORDS_FAILED_FILE)));

        indyWireMockServer.stubFor(
                get(INDY_STORE_GENERIC_GROUP).willReturn(
                        aResponse().withStatus(200)
                                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBodyFile(INDY_GENERIC_GROUPS_FILE)));

        indyWireMockServer
                .stubFor(head(urlMatching(INDY_STORE_MAVEN_GROUP + "/.*")).willReturn(aResponse().withStatus(200)));
        indyWireMockServer
                .stubFor(head(urlMatching(INDY_STORE_MAVEN_HOSTED + "/.*")).willReturn(aResponse().withStatus(200)));

        indyWireMockServer
                .stubFor(delete(urlMatching(INDY_STORE_ENDPOINT + "/.*")).willReturn(aResponse().withStatus(204)));
        indyWireMockServer.stubFor(
                delete(INDY_FOLO_ADMIN_ENDPOINT + "/build-36000/record").willReturn(aResponse().withStatus(204)));

        // limit is set to be after the build record end time
        Instant limit = Instant.ofEpochMilli(1581174847000L);
        // auth token is not important for getting group names
        Indy indyClient = failedBuildsCleaner.initIndy("");
        FailedBuildsCleanerSession session = new FailedBuildsCleanerSession(indyClient, limit);

        failedBuildsCleaner.cleanBuildIfNeeded(MAVEN_PKG_KEY, "build-36000", session);

        indyWireMockServer.verify(deleteRequestedFor(urlEqualTo(INDY_STORE_MAVEN_GROUP + "/build-36000")));
        indyWireMockServer.verify(deleteRequestedFor(urlEqualTo(INDY_STORE_MAVEN_HOSTED + "/build-36000")));
        indyWireMockServer.verify(deleteRequestedFor(urlEqualTo(INDY_FOLO_ADMIN_ENDPOINT + "/build-36000/record")));
    }

}
