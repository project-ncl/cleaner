/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2019-2022 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.cleaner.archiver;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import org.jboss.pnc.api.enums.AlignmentPreference;
import org.jboss.pnc.cleaner.orchApi.OrchClientProducer;
import org.jboss.pnc.client.ProductMilestoneClient;
import org.jboss.pnc.client.ProductVersionClient;
import org.jboss.pnc.client.RemoteResourceException;
import org.jboss.pnc.common.pnc.LongBase32IdConverter;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.BuildConfigurationRevisionRef;
import org.jboss.pnc.dto.Environment;
import org.jboss.pnc.dto.ProductMilestone;
import org.jboss.pnc.dto.ProductRef;
import org.jboss.pnc.dto.ProductVersion;
import org.jboss.pnc.dto.ProductVersionRef;
import org.jboss.pnc.dto.ProjectRef;
import org.jboss.pnc.dto.SCMRepository;
import org.jboss.pnc.dto.User;
import org.jboss.pnc.enums.BuildProgress;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.enums.BuildType;
import org.jboss.pnc.enums.SystemImageType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import jakarta.inject.Inject;
import java.io.BufferedReader;
import java.io.StringReader;
import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
class BuildArchiverTest {

    public static final String MILESTONE_ID = "147369";
    public static final String VERSION_ID = "987123";
    public static final String BUILD_ID = "A7RC57IR7KYAA";
    @Inject
    BuildArchiver buildArchiver;

    @BeforeAll
    public static void setup() throws RemoteResourceException {
        OrchClientProducer producer = Mockito.mock(OrchClientProducer.class);
        QuarkusMock.installMockForType(producer, OrchClientProducer.class);

        ProductMilestoneClient milestoneClient = Mockito.mock(ProductMilestoneClient.class);
        Mockito.when(milestoneClient.getSpecific(MILESTONE_ID)).thenReturn(prepareMilestone());
        Mockito.when(producer.getProductMilestoneClient()).thenReturn(milestoneClient);

        ProductVersionClient productVersionClient = Mockito.mock(ProductVersionClient.class);
        Mockito.when(productVersionClient.getSpecific(VERSION_ID)).thenReturn(prepareProductVersion());
        Mockito.when(producer.getProductVersionClient()).thenReturn(productVersionClient);
    }

    private static Build prepareBuild() {
        return Build.builder()
                .id(BUILD_ID)
                .submitTime(Instant.parse("2024-04-16T13:40:05.319Z"))
                .startTime(Instant.parse("2024-04-16T13:40:05.352Z"))
                .endTime(Instant.parse("2024-04-16T13:45:51.462Z"))
                .progress(BuildProgress.FINISHED)
                .status(BuildStatus.SUCCESS)
                .buildContentId("build-A7RC57IR7KYAA")
                .temporaryBuild(true)
                .alignmentPreference(AlignmentPreference.PREFER_TEMPORARY)
                .scmUrl("https://git.example.com/apache/commons-lang.git")
                .scmRevision("5891b5b522d5df086d0ff0b110fbd9d21bb4fc71")
                .scmTag("1.2.3.temporary-redhat-00001")
                .buildOutputChecksum("b1946ac92492d2347c6235b4d2611184")
                .lastUpdateTime(Instant.parse("2024-04-16T13:45:53.500Z"))
                .scmBuildConfigRevision("98ea6e4f216f2fb4b69fff9b3a44842c38686ca6")
                .scmBuildConfigRevisionInternal(true)
                .project(
                        ProjectRef.refBuilder()
                                .id("123")
                                .name("apache/commons-lang")
                                .description("Apache Commons Lang, a package of Java utility classes.")
                                .projectUrl("https://example.com/apache/commons-lang")
                                .build())
                .scmRepository(
                        SCMRepository.builder()
                                .id("45")
                                .internalUrl("git+ssh://git.example.com/apache/commons-lang.git")
                                .externalUrl("https://example.com/apache/commons-lang.git")
                                .preBuildSyncEnabled(false)
                                .build())
                .environment(
                        Environment.builder()
                                .id("678")
                                .name("OpenJDK 1.8; Mvn 3.6.3")
                                .description("OpenJDK 1.8; Mvn 3.6.3 [builder-rhel-7-j8-mvn3.6.3:latest]")
                                .systemImageRepositoryUrl("example.com/repository")
                                .systemImageId("builder-rhel-7-j8-mvn3.6.3:latest")
                                .attributes(Map.of("JDK", "1.8.0", "MAVEN", "3.6.3", "OS", "LINUX"))
                                .systemImageType(SystemImageType.DOCKER_IMAGE)
                                .deprecated(false)
                                .hidden(false)
                                .build())
                .attributes(
                        Map.of(
                                "BREW_BUILD_VERSION",
                                "1.2.3.temporary-redhat-00001",
                                "BREW_BUILD_NAME",
                                "org.apache.commons:commons-lang"))
                .user(User.builder().id("9").username("megauser").build())
                .buildConfigRevision(
                        BuildConfigurationRevisionRef.refBuilder()
                                .id("1234567")
                                .rev(987654)
                                .name("org.apache.commons-commons-lang-1.2.3")
                                .buildScript("mvn clean deploy")
                                .scmRevision("commons-lang-1.2.3")
                                .creationTime(Instant.parse("2024-03-11T07:44:03.278Z"))
                                .modificationTime(Instant.parse("2024-03-11T07:44:03.257Z"))
                                .buildType(BuildType.MVN)
                                .defaultAlignmentParams(
                                        "-DdependencySource=REST -DrepoRemovalBackup=repositories-backup.xml -DversionSuffixStrip= -DreportNonAligned=true -DstrictPropertyValidation=true")
                                .brewPullActive(false)
                                .build())
                .productMilestone(prepareMilestone())
                .build();
    }

    private static ProductMilestone prepareMilestone() {
        return ProductMilestone.builder()
                .id(MILESTONE_ID)
                .plannedEndDate(Instant.parse("2124-03-11T07:44:03.278Z"))
                .startingDate(Instant.parse("2024-01-11T07:44:03.278Z"))
                .version("1.0.0.RC1")
                .productVersion(prepareProductVersion())
                .build();
    }

    private static ProductVersion prepareProductVersion() {
        return ProductVersion.builder()
                .id(VERSION_ID)
                .version("1.0")
                .product(
                        ProductRef.refBuilder()
                                .id("258741")
                                .name("Best Product")
                                .abbreviation("BP")
                                .description("The best product ever")
                                .build())
                .build();
    }

    @Test
    void testArchiveBuildRecord() {

        Build build = prepareBuild();
        LogParser buildLog = prepareLog("""
                [INFO] --- frontend-maven-plugin:4.9.10:revision (default) @ cleaner ---
                [INFO]\s
                [INFO] --- formatter:2.23.0:format (java-format) @ cleaner ---
                [INFO] Processed 45 files in 867ms (Formatted: 0, Skipped: 0, Unchanged: 45, Failed: 0, Readonly: 0)
                [INFO]\s
                [INFO] --- resources:3.3.0:resources (default-resources) @ cleaner ---
                """);
        LogParser alignmentLog = prepareLog(
                """
                        2024-04-17 10:19:34,461 [INFO] repour.adjust.pme_provider:132 Executing "MVN" using "pme" adjust provider (delegating to "process" provider). Command is "['java', '-jar', '/opt/repour/pom-manipulation-cli.jar', '-s', '/opt/repour/temporary-settings.xml', '-DdependencySource=REST', '-DrepoRemovalBackup=repositories-backup.xml', '-DversionSuffixStrip=', '-DreportNonAligned=true', '-DstrictPropertyValidation=true', '-Dzookeeper-version=3.8.4', '-DrestURL=http://da.example.com/da/rest/v-1', '-DrestSocketTimeout=3600', '-DversionIncrementalSuffix=redhat', '-DversionIncrementalSuffixPadding=5', '-DbrewPullActive=true', '-DrepoReportingRemoval=true', '-DrestMode=TEMPORARY_PREFER_PERSISTENT', '-DversionIncrementalSuffix=temporary-redhat']".
                        """);
        buildArchiver.archiveBuildRecord(build, buildLog, alignmentLog);

        ArchivedBuildRecord archivedBuild = ArchivedBuildRecord.findById(LongBase32IdConverter.toLong(BUILD_ID));

        assertEquals("Best Product", archivedBuild.productName);
        assertEquals(true, archivedBuild.brewPullActive);
        assertEquals(true, archivedBuild.autoAlign);
        assertEquals(2024, archivedBuild.submitYear);
        assertEquals(4, archivedBuild.submitMonth);
        assertEquals(2, archivedBuild.submitQuarter);
        assertEquals("MVN-WRAPPED-NPM", archivedBuild.buildType);
    }

    private LogParser prepareLog(String log) {
        LogParser logParser = BuildCategorizer.getLogParser(200);
        logParser.findMatches(new BufferedReader(new StringReader(log)));
        return logParser;
    }
}