package org.jboss.pnc.cleaner.orchApi;

import static jakarta.ws.rs.core.HttpHeaders.AUTHORIZATION;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;

import org.jboss.pnc.quarkus.client.auth.runtime.PNCClientAuth;

@ApplicationScoped
public class PNCAuthClientRequestFilter implements ClientRequestFilter {

    @Inject
    PNCClientAuth pncClientAuth;

    @Override
    public void filter(ClientRequestContext requestContext) {
        String authHeader = pncClientAuth.getHttpAuthorizationHeaderValueWithCachedToken();
        if (authHeader != null && !authHeader.isBlank()) {
            requestContext.getHeaders().putSingle(AUTHORIZATION, authHeader);
        }
    }
}
