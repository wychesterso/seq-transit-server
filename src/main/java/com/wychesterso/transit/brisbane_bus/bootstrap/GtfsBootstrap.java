package com.wychesterso.transit.brisbane_bus.bootstrap;

import com.wychesterso.transit.brisbane_bus.gtfs.RouteLoader;
import com.wychesterso.transit.brisbane_bus.gtfs.StopLoader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class GtfsBootstrap implements CommandLineRunner {

    private final RouteLoader routeLoader;
    private final StopLoader stopLoader;

    @Value("${gtfs.load-on-startup:true}")
    private boolean loadOnStartup;

    public GtfsBootstrap(RouteLoader routeLoader, StopLoader stopLoader) {
        this.routeLoader = routeLoader;
        this.stopLoader = stopLoader;
    }

    @Override
    public void run(String... args) throws Exception {
        if (!loadOnStartup) return;
        routeLoader.loadRoutes();
        stopLoader.loadStops();
    }
}