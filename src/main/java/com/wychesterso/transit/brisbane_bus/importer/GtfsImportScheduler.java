package com.wychesterso.transit.brisbane_bus.importer;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class GtfsImportScheduler {

    private final GtfsImportRunner runner;

    public GtfsImportScheduler(GtfsImportRunner runner) {
        this.runner = runner;
    }

    @Scheduled(cron = "${gtfs.static.refresh-cron}")
    public void refreshStaticGtfs() throws Exception {
        runner.runImport();
    }
}