package com.wychesterso.transit.brisbane_bus.api.service;

import com.wychesterso.transit.brisbane_bus.st.model.StopList;
import com.wychesterso.transit.brisbane_bus.st.repository.TripRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RouteService {

    private final TripRepository tripRepository;

    public RouteService(TripRepository tripRepository) {
        this.tripRepository = tripRepository;
    }

    private List<String> getCanonicalStopList(
            String routeShortName,
            int directionId,
            String tripHeadsign) {

        List<StopList> rows = tripRepository.getStopSequences(
                        routeShortName,
                        directionId,
                        tripHeadsign
                );

        // group by trip_id
        Map<String, List<StopList>> byTrip = rows.stream()
                .collect(Collectors.groupingBy(StopList::getTripId));

        // build stop lists
        Map<List<String>, Long> frequency = byTrip.values().stream()
                .map(stops -> stops.stream()
                        .sorted(Comparator.comparingInt(StopList::getStopSequence))
                        .map(StopList::getStopId)
                        .toList()
                )
                .collect(Collectors.groupingBy(
                        Function.identity(),
                        Collectors.counting()
                ));

        // return most frequently occurring stop list
        return frequency.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(List.of());
    }
}
