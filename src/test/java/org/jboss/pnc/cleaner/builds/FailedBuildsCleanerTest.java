package org.jboss.pnc.cleaner.builds;

import io.quarkus.test.junit.QuarkusTest;

import com.github.tomakehurst.wiremock.WireMockServer;

import org.commonjava.indy.client.core.Indy;
import org.commonjava.indy.model.core.StoreKey;
import org.commonjava.indy.model.core.StoreType;
import org.junit.jupiter.api.AfterEach;
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

    private static final String ORCH_BUILD_RECORD = "/build-records";

    private static final String BUILD_RECORD_FAILED_OLD_NO_CONTENTID_FILE = "buildRecordFailedOldNoContentId.json";

    private static final String BUILD_RECORDS_FAILED_FILE = "buildRecordsFailed.json";

    private static final String BUILD_RECORDS_NOT_FAILED_FILE = "buildRecordsNotFailed.json";

    private static final String BUILD_RECORDS_TOO_YOUNG_FILE = "buildRecordsTooYoung.json";


    private WireMockServer orchWireMockServer = new WireMockServer(options().port(8082));

    private WireMockServer indyWireMockServer = new WireMockServer(options().port(8083));

    @Inject
    private FailedBuildsCleaner failedBuildsCleaner;


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
     * Tests parsing of build group names from the list of all maven groups that are usually in Indy.
     * It contains 1 old format ("build_&lt;some string&gt;") build group and 2 new format
     * ("build-&lt;number&gt;"). Then checks if the resulting list contains 3 entries and if the
     * expected names are in there.
     */
    @Test
    public void getGroupNamesOk() {
        indyWireMockServer.stubFor(get(urlMatching(INDY_STORE_MAVEN_GROUP)).willReturn(aResponse()
                .withStatus(200)
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .withBodyFile(INDY_MAVEN_GROUPS_FILE)));

        // limit nor auth token is not important for getting group names
        Instant limit = Instant.now();
        Indy indyClient = failedBuildsCleaner.initIndy("");
        FailedBuildsCleanerSession session = new FailedBuildsCleanerSession(indyClient, limit);


        List<String> groupNames = failedBuildsCleaner.getGroupNames(session);

        assertEquals(3, groupNames.size());
        assertTrue(groupNames.contains("build_wildfly-swarm-keycloak-config-api_20170310.1332"));
        assertTrue(groupNames.contains("build-32064"));
        assertTrue(groupNames.contains("build-30573"));
    }

    /**
     * Tests parsing of build group names from the list containing only maven groups that are
     * usually in Indy with no build groups. Then checks, if the resulting list is empty.
     */
    @Test
    public void getGroupNamesNoBuildGroup() {
        indyWireMockServer.stubFor(get(urlMatching(INDY_STORE_MAVEN_GROUP)).willReturn(aResponse()
                .withStatus(200)
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .withBodyFile(INDY_MAVEN_GROUPS_NO_BUILD_GROUP_FILE)));

        // limit nor auth token is not important for getting group names
        Instant limit = Instant.now();
        Indy indyClient = failedBuildsCleaner.initIndy("");
        FailedBuildsCleanerSession session = new FailedBuildsCleanerSession(indyClient, limit);


        List<String> groupNames = failedBuildsCleaner.getGroupNames(session);

        assertEquals(0, groupNames.size());
    }

    @Test
    public void shouldCleanOk() throws CleanerException {
        orchWireMockServer.stubFor(get(urlMatching(ORCH_BUILD_RECORD + "?.*q=buildContentId%3D%3Dbuild-36000")).willReturn(aResponse()
                .withStatus(200)
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .withBodyFile(BUILD_RECORDS_FAILED_FILE)));

        // limit is set to be after the build record end time
        Instant limit = Instant.ofEpochMilli(1573174847816L);
        // auth token is not important for the test
        Indy indyClient = failedBuildsCleaner.initIndy("");
        FailedBuildsCleanerSession session = new FailedBuildsCleanerSession(indyClient, limit);

        boolean clean = failedBuildsCleaner.shouldClean("build-36000", session);

        assertTrue(clean);
    }

    @Test
    public void shouldCleanNoContentId() throws CleanerException {
        orchWireMockServer.stubFor(get(urlMatching(ORCH_BUILD_RECORD + "?.*q=buildContentId%3D%3Dbuild-36000")).willReturn(aResponse()
                .withStatus(204)));

        orchWireMockServer.stubFor(get(urlMatching(ORCH_BUILD_RECORD + "/36000")).willReturn(aResponse()
                .withStatus(200)
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .withBodyFile(BUILD_RECORD_FAILED_OLD_NO_CONTENTID_FILE)));

        // limit is set to be after the build record end time
        Instant limit = Instant.ofEpochMilli(1573174847816L);
        // auth token is not important for the test
        Indy indyClient = failedBuildsCleaner.initIndy("");
        FailedBuildsCleanerSession session = new FailedBuildsCleanerSession(indyClient, limit);

        boolean clean = failedBuildsCleaner.shouldClean("build-36000", session);

        assertTrue(clean);
    }

    @Test
    public void shouldCleanNotFound() throws CleanerException {
        orchWireMockServer.stubFor(get(urlMatching(ORCH_BUILD_RECORD + "?.*q=buildContentId%3D%3Dbuild-36002")).willReturn(aResponse()
                .withStatus(204)));

        orchWireMockServer.stubFor(get(urlMatching(ORCH_BUILD_RECORD + "/36002")).willReturn(aResponse()
                .withStatus(404)));

        // limit is set to be after the build record end time
        Instant limit = Instant.ofEpochMilli(1573174847816L);
        // auth token is not important for the test
        Indy indyClient = failedBuildsCleaner.initIndy("");
        FailedBuildsCleanerSession session = new FailedBuildsCleanerSession(indyClient, limit);

        boolean clean = failedBuildsCleaner.shouldClean("build-36002", session);

        assertTrue(clean);
    }

    @Test
    public void shouldCleanNotFailed() throws CleanerException {
        orchWireMockServer.stubFor(get(urlMatching(ORCH_BUILD_RECORD + "?.*q=buildContentId%3D%3Dbuild-36001")).willReturn(aResponse()
                .withStatus(200)
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .withBodyFile(BUILD_RECORDS_NOT_FAILED_FILE)));

        // limit is set to be after the build record end time
        Instant limit = Instant.ofEpochMilli(1573175614256L);
        // auth token is not important for the test
        Indy indyClient = failedBuildsCleaner.initIndy("");
        FailedBuildsCleanerSession session = new FailedBuildsCleanerSession(indyClient, limit);

        boolean clean = failedBuildsCleaner.shouldClean("build-36001", session);

        assertFalse(clean);
    }

    @Test
    public void shouldCleanTooYoung() throws CleanerException {
        orchWireMockServer.stubFor(get(urlMatching(ORCH_BUILD_RECORD + "?.*q=buildContentId%3D%3Dbuild-36000")).willReturn(aResponse()
                .withStatus(200)
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .withBodyFile(BUILD_RECORDS_FAILED_FILE)));

        // limit is set to be before the build record end time
        Instant limit = Instant.ofEpochMilli(1573174447816L);
        // auth token is not important for the test
        Indy indyClient = failedBuildsCleaner.initIndy("");
        FailedBuildsCleanerSession session = new FailedBuildsCleanerSession(indyClient, limit);

        boolean clean = failedBuildsCleaner.shouldClean("build-36000", session);

        assertFalse(clean);
    }

    @Test
    public void findGenericReposOk() {
        indyWireMockServer.stubFor(get(urlMatching(INDY_STORE_GENERIC_GROUP)).willReturn(aResponse()
                .withStatus(200)
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .withBodyFile(INDY_GENERIC_GROUPS_FILE)));

        // limit nor auth token is not important for getting group names
        Instant limit = Instant.now();
        Indy indyClient = failedBuildsCleaner.initIndy("");
        FailedBuildsCleanerSession session = new FailedBuildsCleanerSession(indyClient, limit);

        List<StoreKey> groupNames = failedBuildsCleaner.findGenericRepos("build-23013", session);

        assertEquals(9, groupNames.size());
        assertTrue(groupNames.contains(new StoreKey(GENERIC_PKG_KEY, StoreType.group, "g-glassfish-java-net-build-23013")));
        assertTrue(groupNames.contains(new StoreKey(GENERIC_PKG_KEY, StoreType.remote, "r-glassfish-java-net-build-23013")));
        assertTrue(groupNames.contains(new StoreKey(GENERIC_PKG_KEY, StoreType.hosted, "h-glassfish-java-net-build-23013")));
        assertTrue(groupNames.contains(new StoreKey(GENERIC_PKG_KEY, StoreType.group, "g-www-apache-org-build-23013")));
        assertTrue(groupNames.contains(new StoreKey(GENERIC_PKG_KEY, StoreType.remote, "r-www-apache-org-build-23013")));
        assertTrue(groupNames.contains(new StoreKey(GENERIC_PKG_KEY, StoreType.hosted, "h-www-apache-org-build-23013")));
        assertTrue(groupNames.contains(new StoreKey(GENERIC_PKG_KEY, StoreType.group, "g-nodejs-org-build-23013")));
        assertTrue(groupNames.contains(new StoreKey(GENERIC_PKG_KEY, StoreType.remote, "r-nodejs-org-build-23013")));
        assertTrue(groupNames.contains(new StoreKey(GENERIC_PKG_KEY, StoreType.hosted, "h-nodejs-org-build-23013")));
    }

    @Test
    public void findGenericReposOldContentId() {
        indyWireMockServer.stubFor(get(urlMatching(INDY_STORE_GENERIC_GROUP)).willReturn(aResponse()
                .withStatus(200)
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .withBodyFile(INDY_GENERIC_GROUPS_FILE)));

        // limit nor auth token is not important for getting group names
        Instant limit = Instant.now();
        Indy indyClient = failedBuildsCleaner.initIndy("");
        FailedBuildsCleanerSession session = new FailedBuildsCleanerSession(indyClient, limit);


        List<StoreKey> groupNames = failedBuildsCleaner
                .findGenericRepos("build_org-keycloak-keycloak-connect-4-x_20181109.2045", session);

        assertEquals(3, groupNames.size());
        assertTrue(groupNames.contains(new StoreKey(GENERIC_PKG_KEY, StoreType.group,
                "g-mwnodereg-hosts-mwqe-eng-bos-redhat-com-build_org-keycloak-keycloak-connect-4-x_20181109.2045")));
        assertTrue(groupNames.contains(new StoreKey(GENERIC_PKG_KEY, StoreType.remote,
                "r-mwnodereg-hosts-mwqe-eng-bos-redhat-com-build_org-keycloak-keycloak-connect-4-x_20181109.2045")));
        assertTrue(groupNames.contains(new StoreKey(GENERIC_PKG_KEY, StoreType.hosted,
                "h-mwnodereg-hosts-mwqe-eng-bos-redhat-com-build_org-keycloak-keycloak-connect-4-x_20181109.2045")));
    }

    @Test
    public void findGenericReposNoGroups() {
        indyWireMockServer.stubFor(get(urlMatching(INDY_STORE_GENERIC_GROUP)).willReturn(aResponse()
                .withStatus(200)
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .withBodyFile(INDY_GENERIC_GROUPS_FILE)));

        // limit nor auth token is not important for getting group names
        Instant limit = Instant.now();
        Indy indyClient = failedBuildsCleaner.initIndy("");
        FailedBuildsCleanerSession session = new FailedBuildsCleanerSession(indyClient, limit);


        List<StoreKey> groupNames = failedBuildsCleaner.findGenericRepos("build-23014", session);

        assertEquals(0, groupNames.size());
    }

    @Test
    public void cleanBuildIfNeededOk() {
        orchWireMockServer.stubFor(get(urlMatching(ORCH_BUILD_RECORD + "?.*q=buildContentId%3D%3Dbuild-36000")).willReturn(aResponse()
                .withStatus(200)
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .withBodyFile(BUILD_RECORDS_FAILED_FILE)));

        indyWireMockServer.stubFor(get(INDY_STORE_GENERIC_GROUP).willReturn(aResponse()
                .withStatus(200)
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .withBodyFile(INDY_GENERIC_GROUPS_FILE)));

        indyWireMockServer.stubFor(head(urlMatching(INDY_STORE_MAVEN_GROUP + "/.*")).willReturn(aResponse()
                .withStatus(200)));
        indyWireMockServer.stubFor(head(urlMatching(INDY_STORE_MAVEN_HOSTED + "/.*")).willReturn(aResponse()
                .withStatus(200)));

        indyWireMockServer.stubFor(delete(urlMatching(INDY_STORE_ENDPOINT + "/.*")).willReturn(aResponse()
                .withStatus(204)));
        indyWireMockServer.stubFor(delete(INDY_FOLO_ADMIN_ENDPOINT + "/build-36000/record").willReturn(aResponse()
                .withStatus(204)));

        // limit is set to be after the build record end time
        Instant limit = Instant.ofEpochMilli(1573174847816L);
        // auth token is not important for getting group names
        Indy indyClient = failedBuildsCleaner.initIndy("");
        FailedBuildsCleanerSession session = new FailedBuildsCleanerSession(indyClient, limit);

        failedBuildsCleaner.cleanBuildIfNeeded("build-36000", session);

        indyWireMockServer.verify(deleteRequestedFor(urlEqualTo(INDY_STORE_MAVEN_GROUP + "/build-36000")));
        indyWireMockServer.verify(deleteRequestedFor(urlEqualTo(INDY_STORE_MAVEN_HOSTED + "/build-36000")));
        indyWireMockServer.verify(deleteRequestedFor(urlEqualTo(INDY_FOLO_ADMIN_ENDPOINT + "/build-36000/record")));
    }

}
