package com.wychesterso.transit.brisbane_bus.importer;

import com.wychesterso.transit.brisbane_bus.gtfs.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;

@Component
public class GtfsImportOrchestrator {

    private final RouteLoader routeLoader;
    private final StopLoader stopLoader;
    private final CalendarLoader calendarLoader;
    private final CalendarDateLoader calendarDateLoader;
    private final TripLoader tripLoader;
    private final StopTimeLoader stopTimeLoader;

    public GtfsImportOrchestrator(
            RouteLoader routeLoader,
            StopLoader stopLoader,
            CalendarLoader calendarLoader,
            CalendarDateLoader calendarDateLoader,
            TripLoader tripLoader,
            StopTimeLoader stopTimeLoader) {
        this.routeLoader = routeLoader;
        this.stopLoader = stopLoader;
        this.calendarLoader = calendarLoader;
        this.calendarDateLoader = calendarDateLoader;
        this.tripLoader = tripLoader;
        this.stopTimeLoader = stopTimeLoader;
    }

    @Transactional
    public void importGtfs(Path gtfsDir) throws Exception {
        routeLoader.loadRoutes(gtfsDir);
        stopLoader.loadStops(gtfsDir);
        calendarLoader.loadCalendar(gtfsDir);
        calendarDateLoader.loadCalendarDates(gtfsDir);
        tripLoader.loadTrips(gtfsDir);
        stopTimeLoader.loadStopTimes(gtfsDir);
    }
}