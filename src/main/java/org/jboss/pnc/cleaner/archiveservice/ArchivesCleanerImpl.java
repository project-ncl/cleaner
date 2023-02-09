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
package org.jboss.pnc.cleaner.archiveservice;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.function.Function;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.jboss.pnc.api.constants.MDCKeys;
import org.jboss.pnc.cleaner.auth.KeycloakServiceClient;
import org.jboss.pnc.common.Strings;
import org.jboss.pnc.common.otel.OtelUtils;
import org.slf4j.MDC;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Scope;

import lombok.extern.slf4j.Slf4j;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;

import static org.jboss.pnc.api.constants.HttpHeaders.AUTHORIZATION_STRING;
import static org.jboss.pnc.api.constants.HttpHeaders.CONTENT_TYPE_STRING;

/**
 * Implementation of the ArchivesCleaner interface which provides methods to clean the historical archives stored in the
 * Indy Archive Service
 *
 * @author Andrea Vibelli
 */

@ApplicationScoped
@Slf4j
public class ArchivesCleanerImpl implements ArchivesCleaner {

    @Inject
    KeycloakServiceClient serviceClient;

    @Inject
    Config config;

    @Inject
    ManagedExecutor executor;

    @Inject
    java.net.http.HttpClient httpClient;

    @Override
    public void deleteArchive(String buildConfigurationId) {
        if (!Strings.isEmpty(config.getValue("archive-service.delete-api-url", String.class))) {
            // Create a parent child span with values from MDC
            SpanBuilder spanBuilder = OtelUtils.buildChildSpan(
                    GlobalOpenTelemetry.get().getTracer(""),
                    "ArchivesCleanerImpl.deleteTemporaryBuild",
                    SpanKind.CLIENT,
                    MDC.get(MDCKeys.TRACE_ID_KEY),
                    MDC.get(MDCKeys.SPAN_ID_KEY),
                    MDC.get(MDCKeys.TRACE_FLAGS_KEY),
                    MDC.get(MDCKeys.TRACE_STATE_KEY),
                    Span.current().getSpanContext(),
                    Map.of("buildConfigurationId", buildConfigurationId));
            Span span = spanBuilder.startSpan();
            log.debug("Started a new span :{}", span);

            // put the span into the current Context
            try (Scope scope = span.makeCurrent()) {
                doDeleteArchive(buildConfigurationId);
            } finally {
                span.end(); // closing the scope does not end the span, this has to be done manually
            }
        }
    }

    private HttpResponse<String> doDeleteArchive(String buildConfigurationId) {
        log.debug("Deleting historical archive of build config id {} ...", buildConfigurationId);

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(config.getValue("archive-service.delete-api-url", String.class) + buildConfigurationId))
                .DELETE()
                .timeout(Duration.ofSeconds(config.getValue("archive-service.http-client.request-timeout", Long.class)))
                .header(AUTHORIZATION_STRING, "Bearer " + serviceClient.getAuthToken())
                .header(CONTENT_TYPE_STRING, "application/json");

        HttpRequest request = builder.build();
        RetryPolicy<HttpResponse<String>> retryPolicy = new RetryPolicy<HttpResponse<String>>()
                .withMaxDuration(Duration.ofSeconds(config.getValue("archive-service.running-wait-for", Long.class)))
                .withMaxRetries(Integer.MAX_VALUE) // retry until maxDuration is reached
                .withBackoff(
                        config.getValue("archive-service.running-retry-delay-msec", Long.class),
                        config.getValue("archive-service.running-retry-max-delay-msec", Long.class),
                        ChronoUnit.MILLIS)
                .onSuccess(
                        ctx -> log
                                .info("Archival service responded, response status: {}.", ctx.getResult().statusCode()))
                .onRetry(ctx -> {
                    String lastError;
                    if (ctx.getLastFailure() != null) {
                        lastError = ctx.getLastFailure().getMessage();
                    } else {
                        lastError = "";
                    }
                    Integer lastStatus;
                    if (ctx.getLastResult() != null) {
                        lastStatus = ctx.getLastResult().statusCode();
                    } else {
                        lastStatus = null;
                    }
                    log.warn(
                            "Archival service call retry attempt #{}, last error: [{}], last status: [{}].",
                            ctx.getAttemptCount(),
                            lastError,
                            lastStatus);
                })
                .onFailure(ctx -> log.error("Unable to call archival service: {}.", ctx.getFailure().getMessage()))
                .onAbort(e -> log.warn("Archival service call aborted: {}.", e.getFailure().getMessage()));

        log.info("About to call archival service {}.", request.uri());
        return Failsafe.with(retryPolicy)
                .with(executor)
                .getStageAsync(
                        () -> httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                                .thenApply(validateResponse()))
                .join();
    }

    private Function<HttpResponse<String>, HttpResponse<String>> validateResponse() {
        return response -> {
            if (response.statusCode() == 202) {
                return response;
            } else {
                log.error(
                        "Call to archival service failed with status code {} and message {}",
                        response.statusCode(),
                        response.body());
                throw new FailedResponseException("Response status code: " + response.statusCode());
            }
        };
    }

}
