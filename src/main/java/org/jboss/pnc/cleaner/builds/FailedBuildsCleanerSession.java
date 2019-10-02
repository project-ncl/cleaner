package org.jboss.pnc.cleaner.builds;

import org.commonjava.indy.client.core.Indy;
import org.commonjava.indy.client.core.IndyClientException;
import org.commonjava.indy.client.core.module.IndyStoresClientModule;
import org.commonjava.indy.folo.client.IndyFoloAdminClientModule;
import org.commonjava.indy.model.core.Group;
import org.commonjava.indy.model.core.dto.StoreListingDTO;

import java.time.Instant;
import java.util.List;

import static org.commonjava.indy.model.core.GenericPackageTypeDescriptor.GENERIC_PKG_KEY;

public class FailedBuildsCleanerSession {

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
