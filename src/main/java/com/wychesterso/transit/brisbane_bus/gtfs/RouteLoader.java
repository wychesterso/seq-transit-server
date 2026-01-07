package com.wychesterso.transit.brisbane_bus.gtfs;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import com.wychesterso.transit.brisbane_bus.model.Route;
import com.wychesterso.transit.brisbane_bus.repository.RouteRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;

@Component
public class RouteLoader {

    private final RouteRepository routeRepository;

    public RouteLoader(RouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    @Transactional
    public void loadRoutes() throws IOException, CsvValidationException {

        try (CSVReader reader = new CSVReader(
                new FileReader("src/main/resources/static/SEQ_GTFS/routes.txt"))) {

            reader.readNext(); // header
            String[] row;

            while ((row = reader.readNext()) != null) {
                routeRepository.save(
                        new Route(
                                row[0],
                                row[1],
                                row[2],
                                row[3],
                                LoadHelper.parseInteger(row[4]),
                                row[5],
                                row[6],
                                row[7]
                        )
                );
            }
        }
    }

    @GetMapping("/routes")
    public List<Route> routes() {
        return routeRepository.findAll();
    }
}