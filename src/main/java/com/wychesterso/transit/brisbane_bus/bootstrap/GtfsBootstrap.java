package com.wychesterso.transit.brisbane_bus.bootstrap;

import com.wychesterso.transit.brisbane_bus.importer.GtfsImportOrchestrator;
import com.wychesterso.transit.brisbane_bus.importer.GtfsImportRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class GtfsBootstrap {

    private final GtfsImportRunner runner;

    @Value("${gtfs.load-on-startup:true}")
    private boolean loadOnStartup;

    public GtfsBootstrap(GtfsImportRunner runner) {
        this.runner = runner;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void bootstrap() throws Exception {
        if (!loadOnStartup) return;
        runner.runImport();
    }
}