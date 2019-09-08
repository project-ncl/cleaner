package org.jboss.pnc.cleaner.rest;

import org.jboss.pnc.cleaner.common.AppInfo;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/version")
public class Version {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getVersion() {
        return AppInfo.getAppInfoString();
    }
}
