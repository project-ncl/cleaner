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
package org.jboss.pnc.cleaner.common;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
public class AppInfo {

    static Properties appProperties;

    static {

        appProperties = new Properties();

        try (InputStream inputStream = AppInfo.class.getClassLoader().getResourceAsStream("git.properties")) {
            appProperties.load(inputStream);
        } catch (IOException | NullPointerException e) {
            log.warn("AppInfo initialization failed. Read of git.properties file failed!", e);
        }
    }

    public static String getVersion() {
        return appProperties.getProperty("git.build.version");
    }

    public static String getRevision() {
        return appProperties.getProperty("git.commit.id.describe-short");
    }

    public static String getBuildTime() {
        return appProperties.getProperty("git.build.time");
    }

    public static String getAppInfoString() {

        StringBuilder builder = new StringBuilder();

        builder.append(getVersion()).append(" (").append(getRevision()).append(") Built on: ").append(getBuildTime());

        return builder.toString();
    }
}
