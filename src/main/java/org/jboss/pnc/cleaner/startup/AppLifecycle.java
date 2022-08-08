package org.jboss.pnc.cleaner.startup;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import io.quarkus.runtime.StartupEvent;
import lombok.extern.slf4j.Slf4j;
import org.jboss.pnc.cleaner.common.AppInfo;

@ApplicationScoped
@Slf4j
public class AppLifecycle {

    void onStart(@Observes StartupEvent ev) {
        log.info("The application is starting: {}", AppInfo.getAppInfoString());
    }
}
