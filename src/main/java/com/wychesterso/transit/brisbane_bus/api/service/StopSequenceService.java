package com.wychesterso.transit.brisbane_bus.api.service;

import com.wychesterso.transit.brisbane_bus.api.cache.StopSequenceCache;
import com.wychesterso.transit.brisbane_bus.st.model.StopList;
import com.wychesterso.transit.brisbane_bus.api.repository.StopSequenceRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class StopSequenceService {

    private final StopSequenceRepository repository;
    private final StopSequenceCache cache;

    public StopSequenceService(
            StopSequenceRepository repository,
            StopSequenceCache cache) {
        this.repository = repository;
        this.cache = cache;
    }

    /**
     * Get the canonical (most frequently occurring) stop sequence for a service group
     * @param routeShortName service group's route name
     * @param tripHeadsign service group's headsign
     * @param directionId service group's direction identifier
     * @return map of stopId to stopSequence
     */
    public Map<String, Integer> getCanonicalStopSequence(
            String routeShortName,
            String tripHeadsign,
            int directionId
    ) {
        Map<String, Integer> cachedResult =
                cache.getCanonicalStopSequence(routeShortName, tripHeadsign, directionId);
        if (cachedResult != null) return cachedResult;

        // repo query
        List<StopList> rows = repository.getStopSequences(
                routeShortName,
                directionId,
                tripHeadsign
        );

        // group by trip_id
        Map<String, List<StopList>> byTrip =
                rows.stream().collect(Collectors.groupingBy(StopList::getTripId));

        // build stop_id -> stop_sequence maps and count frequency
        Map<Map<String, Integer>, Long> frequency =
                byTrip.values().stream()
                        .map(stops ->
                                stops.stream()
                                        .sorted(Comparator.comparingInt(StopList::getStopSequence))
                                        .collect(Collectors.toMap(
                                                StopList::getStopId,
                                                StopList::getStopSequence,
                                                (a, b) -> a, // defensive merge
                                                LinkedHashMap::new // preserve traversal order
                                        ))
                        )
                        .collect(Collectors.groupingBy(
                                Function.identity(),
                                Collectors.counting()
                        ));

        // return most frequently occurring map
        Map<String, Integer> result = frequency.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(Map.of());

        // cache it
        cache.cacheCanonicalStopSequence(routeShortName, tripHeadsign, directionId, result);

        return result;
    }
}
