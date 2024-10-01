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
package org.jboss.pnc.cleaner.builds;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.commonjava.indy.client.core.Indy;
import org.commonjava.indy.client.core.IndyClientException;
import org.commonjava.indy.client.core.module.IndyStoresClientModule;
import org.commonjava.indy.folo.client.IndyFoloAdminClientModule;
import org.commonjava.indy.model.core.Group;
import org.commonjava.indy.model.core.dto.StoreListingDTO;

import jakarta.inject.Inject;
import java.time.Instant;
import java.util.List;

import static org.commonjava.indy.model.core.GenericPackageTypeDescriptor.GENERIC_PKG_KEY;

public class FailedBuildsCleanerSession {

    private static final String className = FailedBuildsCleanerSession.class.getName();

    private IndyFoloAdminClientModule foloAdmin;
    private IndyStoresClientModule stores;

    private List<Group> genericGroups;

    private Instant to;

    public FailedBuildsCleanerSession(Indy indyClient, Instant to) {
        try {
            this.stores = indyClient.stores();
            this.foloAdmin = indyClient.module(IndyFoloAdminClientModule.class);
        } catch (IndyClientException e) {
            throw new RuntimeException("Unable to retrieve Indy client module: " + e, e);
        }
        this.to = to;
    }

    @Timed
    public List<Group> getGenericGroups() {
        if (genericGroups == null) {
            try {
                StoreListingDTO<Group> groupListing = stores.listGroups(GENERIC_PKG_KEY);
                genericGroups = groupListing.getItems();
            } catch (IndyClientException e) {
                throw new RuntimeException("Error in loading generic http groups: " + e, e);
            }
        }
        return genericGroups;
    }

    public IndyFoloAdminClientModule getFoloAdmin() {
        return foloAdmin;
    }

    public IndyStoresClientModule getStores() {
        return stores;
    }

    public Instant getTo() {
        return to;
    }

}
