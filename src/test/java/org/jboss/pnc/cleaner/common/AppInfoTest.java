/*
 * SPDX-FileCopyrightText: Copyright © 2019 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.cleaner.common;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class AppInfoTest {

    @Test
    void testGetVersion() {

        String version = AppInfo.getVersion();
        assertThat(version).isNotNull().doesNotContain("${");
    }

    @Test
    void testGetRevision() {

        String revision = AppInfo.getRevision();
        assertThat(revision).isNotNull().doesNotContain("${");
    }

    @Test
    void testGetBuildTime() {

        String buildTime = AppInfo.getBuildTime();
        assertThat(buildTime).isNotNull().doesNotContain("${");
    }

    @Test
    void testGetAppInfoString() {

        String appInfoString = AppInfo.getAppInfoString();
        assertThat(appInfoString).contains(AppInfo.getBuildTime())
                .contains(AppInfo.getRevision())
                .contains(AppInfo.getVersion())
                .doesNotContain("${");
    }
}