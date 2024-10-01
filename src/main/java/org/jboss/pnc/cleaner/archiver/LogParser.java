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

import lombok.Getter;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LogParser {

    private Map<String, LineMatcher> matchers = new HashMap<>();

    private LinkedList<String> trimmedLog = new LinkedList<>();

    private long trimmedLogSize;
    private long trimLogsSizeLimit;

    private boolean exceptionFound = false;

    @Getter
    private boolean empty = true;

    public LogParser(long trimLogsSizeLimit) {
        this.trimLogsSizeLimit = trimLogsSizeLimit;
    }

    public boolean contains(String pattern) {
        return matchers.get(pattern).isFound();
    }

    public String get(String pattern) {
        return matchers.get(pattern).matchedString();
    }

    public void addRegExpLines(String... patterns) {
        for (String pattern : patterns) {
            matchers.put(pattern, new LineRegExpMatcher(pattern));
        }
    }

    public void addLiteralLines(String... patterns) {
        for (String pattern : patterns) {
            matchers.put(pattern, new LineLiteralMatcher(pattern));
        }
    }

    public void findMatches(BufferedReader reader) {
        reader.lines().forEach(this::processLine);
    }

    private void processLine(String line) {
        if (empty && !line.isEmpty()) {
            empty = false;
        }
        if (trimLogsSizeLimit > 0) {
            if (!exceptionFound && line.contains("Caught exception:")) {
                trimmedLog.clear();
                trimmedLogSize = 0;
                exceptionFound = true;
            }
            trimmedLogSize += line.length();
            trimmedLog.add(line);
            while (trimmedLogSize > trimLogsSizeLimit) {
                String poll = trimmedLog.poll();
                trimmedLogSize -= poll.length();
            }
        }
        for (LineMatcher matcher : matchers.values()) {
            if (!matcher.isFound()) {
                matcher.lineMatches(line);
            }
        }
    }

    public String getTrimmedLog() {
        return trimmedLog.stream().collect(Collectors.joining("\n"));
    }

    private interface LineMatcher {

        boolean lineMatches(String line);

        String matchedString();

        boolean isFound();
    }

    private static class LineRegExpMatcher implements LineMatcher {
        final Matcher matcher;
        String lineFound;

        private LineRegExpMatcher(String regex) {
            matcher = Pattern.compile(".*(" + regex + ").*").matcher("");
        }

        @Override
        public boolean lineMatches(String line) {
            matcher.reset(line);
            if (matcher.matches()) {
                lineFound = matcher.group(1);
                return true;
            }
            return false;
        }

        @Override
        public String matchedString() {
            return lineFound;
        }

        @Override
        public boolean isFound() {
            return lineFound != null;
        }
    }

    private static class LineLiteralMatcher implements LineMatcher {
        private final String pattern;
        private boolean found = false;

        private LineLiteralMatcher(String pattern) {
            this.pattern = pattern;
        }

        @Override
        public boolean lineMatches(String line) {
            if (line.contains(pattern)) {
                found = true;
                return true;
            }
            return false;
        }

        @Override
        public String matchedString() {
            return found ? pattern : null;
        }

        @Override
        public boolean isFound() {
            return found;
        }
    }
}
