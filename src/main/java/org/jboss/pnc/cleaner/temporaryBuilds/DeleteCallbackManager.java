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
package org.jboss.pnc.cleaner.temporaryBuilds;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.pnc.dto.response.DeleteOperationResult;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Manages delete operation callbacks and provides a blocking was of waiting fot the operation completion. First the
 * wait operation must be initiated using a method #initializeHandler and then at any time a blocking method #await can
 * be called.
 *
 * @author Jakub Bartecek
 */
@Slf4j
public class DeleteCallbackManager {

    private static final String className = DeleteCallbackManager.class.getName();

    private Map<String, CallbackData> buildsMap = new ConcurrentHashMap<>();

    @ConfigProperty(name = "simpleCallbackHandler.max-delete-wait-time", defaultValue = "600")
    long MAX_WAIT_TIME;

    @Inject
    MeterRegistry registry;

    private Counter errCounter;
    private Counter warnCounter;

    @PostConstruct
    void initMetrics() {
        errCounter = registry.counter(className + ".error.count");
        warnCounter = registry.counter(className + ".warning.count");
        registry.gauge(className + ".builds.map.size", this, DeleteCallbackManager::getBuildsMapSize);
    }

    private int getBuildsMapSize() {
        return buildsMap.size();
    }

    /**
     * Initialize data to wait for a completion of a deletion of a specific build
     *
     * @param buildId ID of a build to wait for
     * @return True if succeeds. False if this build is already registered.
     */
    public boolean initializeHandler(String buildId) {
        if (buildsMap.containsKey(buildId)) {
            // Delete operation is already in progress and waiting for that build deletion
            return false;
        }

        buildsMap.put(buildId, new CallbackData());
        return true;
    }

    /**
     * Registers a response to a delete operation completion
     *
     * @param buildId ID of a build, which deletion completed
     * @param result Result of the operation
     */
    public void callback(String buildId, DeleteOperationResult result) {
        CallbackData callbackData = buildsMap.get(buildId);
        if (callbackData != null) {
            callbackData.setCallbackResponse(result);
            callbackData.getCountDownLatch().countDown();
        } else {
            warnCounter.increment();
            log.warn(
                    "Delete operation callback called for a delete operation, which was not initialized. BuildId: "
                            + "{}",
                    buildId);
        }
    }

    /**
     * Blocking wait operation for completion of delete operation. It waits for a configurable maximum time.
     *
     * @param buildId Build ID
     * @return Result of the operation or null if the callback was not triggered
     * @throws InterruptedException Thrown if an error occurs while waiting for callback
     */
    @Timed
    public DeleteOperationResult await(String buildId) throws InterruptedException {
        CallbackData callbackData = buildsMap.get(buildId);
        if (callbackData == null) {
            errCounter.increment();
            throw new IllegalArgumentException(
                    "Await operation triggered for a build, which was not initiated using "
                            + "method initializeHandler. This method must be called first!");
        }

        callbackData.getCountDownLatch().await(MAX_WAIT_TIME, TimeUnit.SECONDS);
        buildsMap.remove(buildId);
        return callbackData.getCallbackResponse();
    }

    public void cancel(String buildId) {
        buildsMap.remove(buildId);
    }

    @Data
    public class CallbackData {

        private DeleteOperationResult callbackResponse = null;

        private final CountDownLatch countDownLatch = new CountDownLatch(1);
    }
}
