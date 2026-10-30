/*
 * SPDX-FileCopyrightText: Copyright © 2019 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.cleaner.archiver;

import static org.jboss.pnc.cleaner.archiver.ArchivedBuildRecord.ErrorGroup.PSI;
import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.StringReader;

import org.junit.jupiter.api.Test;

class BuildCategorizerTest {

    @Test
    void testCategorizeErrors() {
        LogParser buildLogParser = BuildCategorizer.getLogParser(0);
        String buildLog = """
                ==== BUILD ALMOST SUCCEEDED====
                Exception trying to GET https://paas.example.com/healthz/ready
                EverythingExcplodedException""";
        buildLogParser.findMatches(new BufferedReader(new StringReader(buildLog)));

        LogParser alignmentLogParser = BuildCategorizer.getLogParser(0);
        String alignmentLog = "";
        alignmentLogParser.findMatches(new BufferedReader(new StringReader(alignmentLog)));

        BuildCategorizer.DetectedCategory detectedCategory = BuildCategorizer
                .categorizeErrors(buildLogParser, alignmentLogParser);
        assertEquals(PSI, detectedCategory.getCategory());
        assertEquals("Exception trying to GET https://paas.example.com/healthz/ready", detectedCategory.getMessage());
    }
}