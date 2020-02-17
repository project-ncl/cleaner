package org.jboss.pnc.cleaner.logverifier;

import io.quarkus.scheduler.Scheduled;

import javax.inject.Inject;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class BuildLogVerifierScheduler {

    @Inject
    BuildLogVerifier buildLogVerifier;

    @Scheduled(cron = "{buildLogVerifierScheduler.cron}")
    public void verifyBuildLogs() {
        buildLogVerifier.verifyUnflaggedBuilds();
    }


}
