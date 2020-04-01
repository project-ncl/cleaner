package org.jboss.pnc.cleaner.logverifier;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.pnc.api.bifrost.rest.Bifrost;

/**
 * @author <a href="mailto:matejonnet@gmail.opecom">Matej Lazar</a>
 */
@RegisterRestClient
public interface BifrostClient extends Bifrost {
}
