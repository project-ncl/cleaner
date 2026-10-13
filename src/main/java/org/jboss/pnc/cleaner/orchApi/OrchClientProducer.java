/*
 * SPDX-FileCopyrightText: Copyright © 2019 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.cleaner.orchApi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import org.jboss.pnc.client.BuildClient;
import org.jboss.pnc.client.GroupBuildClient;
import org.jboss.pnc.client.ProductMilestoneClient;
import org.jboss.pnc.client.ProductVersionClient;

/**
 * Producer for Orchestrator clients
 *
 * @author Jakub Bartecek
 */
@ApplicationScoped
public class OrchClientProducer {

    @Inject
    OrchClientConfiguration orchClientConfiguration;

    @Produces
    public BuildClient getBuildClient() {
        return new BuildClient(orchClientConfiguration.getConfiguration());
    }

    @Produces
    public GroupBuildClient getBuildGroupClient() {
        return new GroupBuildClient(orchClientConfiguration.getConfiguration());
    }

    @Produces
    public ProductVersionClient getProductVersionClient() {
        return new ProductVersionClient(orchClientConfiguration.getConfiguration());
    }

    @Produces
    public ProductMilestoneClient getProductMilestoneClient() {
        return new ProductMilestoneClient(orchClientConfiguration.getConfiguration());
    }

}
