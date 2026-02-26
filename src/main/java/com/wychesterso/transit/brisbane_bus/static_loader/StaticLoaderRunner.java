package com.wychesterso.transit.brisbane_bus.static_loader;

import com.wychesterso.transit.brisbane_bus.static_loader.importer.GtfsImportRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("static-loader")
public class StaticLoaderRunner implements CommandLineRunner {

    private final GtfsImportRunner runner;

    public StaticLoaderRunner(GtfsImportRunner runner) {
        this.runner = runner;
    }

    @Override
    public void run(String... args) {
        try {
            runner.runImport();
            System.exit(0);
        } catch (Exception e) {
            System.exit(1);
        }
    }
}
