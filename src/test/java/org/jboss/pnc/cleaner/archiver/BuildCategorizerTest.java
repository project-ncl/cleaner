/*
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

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.StringReader;

import static org.jboss.pnc.cleaner.archiver.ArchivedBuildRecord.ErrorGroup.PSI;
import static org.junit.jupiter.api.Assertions.*;

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