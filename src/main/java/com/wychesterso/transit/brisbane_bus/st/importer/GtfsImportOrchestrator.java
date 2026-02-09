package com.wychesterso.transit.brisbane_bus.st.importer;

import com.wychesterso.transit.brisbane_bus.st.loader.*;
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
    private final ShapeLoader shapeLoader;
    private final StopTimeLoader stopTimeLoader;

    public GtfsImportOrchestrator(
            RouteLoader routeLoader,
            StopLoader stopLoader,
            CalendarLoader calendarLoader,
            CalendarDateLoader calendarDateLoader,
            TripLoader tripLoader,
            ShapeLoader shapeLoader,
            StopTimeLoader stopTimeLoader) {
        this.routeLoader = routeLoader;
        this.stopLoader = stopLoader;
        this.calendarLoader = calendarLoader;
        this.calendarDateLoader = calendarDateLoader;
        this.tripLoader = tripLoader;
        this.shapeLoader = shapeLoader;
        this.stopTimeLoader = stopTimeLoader;
    }

    @Transactional
    public void importGtfs(Path gtfsDir) throws Exception {
        routeLoader.loadRoutes(gtfsDir);
        stopLoader.loadStops(gtfsDir);
        calendarLoader.loadCalendar(gtfsDir);
        calendarDateLoader.loadCalendarDates(gtfsDir);
        tripLoader.loadTrips(gtfsDir);
        shapeLoader.loadShapes(gtfsDir);
        stopTimeLoader.loadStopTimes(gtfsDir);
    }
}