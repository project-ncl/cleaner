/*
 * SPDX-FileCopyrightText: Copyright © 2019 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.cleaner.archiver;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.StringReader;

import org.junit.jupiter.api.Test;

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