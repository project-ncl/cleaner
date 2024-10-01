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
package org.jboss.pnc.cleaner.temporaryBuilds;

import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.GroupBuild;

import java.util.Collection;
import java.util.Date;

/**
 * Adapter, which provides high level operations on Orchestrator REST API
 *
 * @author Jakub Bartecek
 */
public interface TemporaryBuildsCleanerAdapter {

    /**
     * Finds all temporary builds, which are older than a timestamp set by the expirationDate parameter
     *
     * @param expirationDate Timestamp defining expiration date of temporary builds
     * @return List of expired builds
     */
    Collection<Build> findTemporaryBuildsOlderThan(Date expirationDate);

    /**
     * Deletes a temporary build and waits for the operation completion. The method is blocking.
     *
     * @param id ID of a temporary build, which is meant to be deleted
     * @throws OrchInteractionException Thrown if deletion fails with an error
     */
    void deleteTemporaryBuild(String id) throws OrchInteractionException;

    /**
     * Finds all temporary BuildConfigSetRecords, which are older than a timestamp set by the expirationDate parameter
     *
     * @param expirationDate Timestamp defining expiration date of BuildConfigSetRecords
     * @return List of expired BuildConfigSetRecords
     */
    Collection<GroupBuild> findTemporaryGroupBuildsOlderThan(Date expirationDate);

    /**
     * Deletes a temporary BuildConfigSetRecord
     *
     * @param id ID of a temporary BuildConfigSetRecord, which is meant to be deleted
     * @throws OrchInteractionException Thrown if deletion fails with an error
     */
    void deleteTemporaryGroupBuild(String id) throws OrchInteractionException;
}
