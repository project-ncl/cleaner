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

        InputStream inputStream = AppInfo.class.getClassLoader().getResourceAsStream("git.properties");

        try {
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
