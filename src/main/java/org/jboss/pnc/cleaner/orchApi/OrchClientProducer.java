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
