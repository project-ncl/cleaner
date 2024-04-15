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

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;

class LogParserTest {

    @Test
    public void testNoEmptyLog() {
        LogParser logParser = new LogParser(0);
        String inputText = "line1\nline2\nline3\nline4\nline5\nline6";
        StringReader reader = new StringReader(inputText);
        logParser.findMatches(new BufferedReader(reader));
        assertFalse(logParser.isEmpty());
    }

    @Test
    public void testEmptyLog() {
        LogParser logParser = new LogParser(0);
        String inputText = "";
        StringReader reader = new StringReader(inputText);
        logParser.findMatches(new BufferedReader(reader));
        assertTrue(logParser.isEmpty());
    }

    @Test
    public void testNoTrimmedLog() {
        LogParser logParser = new LogParser(0);
        String inputText = "line1\nline2\nline3\nline4\nline5\nline6";
        StringReader reader = new StringReader(inputText);
        logParser.findMatches(new BufferedReader(reader));
        String trimmedLog = logParser.getTrimmedLog();
        assertEquals("", trimmedLog);
    }

    @Test
    public void testTrimmingToSize() {
        LogParser logParser = new LogParser(20);
        String inputText = "line1\nline2\nline3\nline4\nline5\nline6";
        StringReader reader = new StringReader(inputText);
        logParser.findMatches(new BufferedReader(reader));
        String trimmedLog = logParser.getTrimmedLog();
        assertEquals("line3\nline4\nline5\nline6", trimmedLog);
    }

    @Test
    public void testTrimmingFromError() {
        LogParser logParser = new LogParser(200000);
        String inputText = "line1\nline2\nline3\nline4\nCaught exception: line5\nline6";
        StringReader reader = new StringReader(inputText);
        logParser.findMatches(new BufferedReader(reader));
        String trimmedLog = logParser.getTrimmedLog();
        assertEquals("Caught exception: line5\nline6", trimmedLog);
    }

    @Test
    public void testLiteralFound() {
        LogParser logParser = new LogParser(0);
        String yourError = "your error";
        String myError = "my error";
        logParser.addLiteralLines(yourError, myError);
        String inputText = "line1\nline2\nlong my error line\nline4";
        StringReader reader = new StringReader(inputText);
        logParser.findMatches(new BufferedReader(reader));

        assertFalse(logParser.contains(yourError));
        assertNull(logParser.get(yourError));
        assertTrue(logParser.contains(myError));
        assertEquals("my error", logParser.get(myError));
    }

    @Test
    public void testRegExpFound() {
        LogParser logParser = new LogParser(0);
        String yourError = "your .* error";
        String myError = "my .* error";
        logParser.addRegExpLines(yourError, myError);
        String inputText = "line1\nline2\nlong my shiny error line\nline4";
        StringReader reader = new StringReader(inputText);
        logParser.findMatches(new BufferedReader(reader));

        assertFalse(logParser.contains(yourError));
        assertNull(logParser.get(yourError));
        assertTrue(logParser.contains(myError));
        assertEquals("my shiny error", logParser.get(myError));
    }

}