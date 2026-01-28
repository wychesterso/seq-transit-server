package com.wychesterso.transit.brisbane_bus.api.service;

import com.wychesterso.transit.brisbane_bus.st.model.ServiceGroup;
import com.wychesterso.transit.brisbane_bus.st.model.StopList;
import com.wychesterso.transit.brisbane_bus.st.repository.TripRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class StopListService {

    private final TripRepository tripRepository;
    private final RedisTemplate<String, Object> redis;

    public StopListService(
            TripRepository tripRepository,
            RedisTemplate<String, Object> redis) {
        this.tripRepository = tripRepository;
        this.redis = redis;
    }

    // map stop_sequence to stop_id
    public Map<String, Integer> getCanonicalStopList(
            String routeShortName,
            String tripHeadsign,
            int directionId) {

        String key = "service:%s:%s:%s:stoplist"
                .formatted(routeShortName, tripHeadsign, directionId);

        List<StopList> rows = tripRepository.getStopSequences(
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
                                                (a, b) -> a,          // defensive merge
                                                LinkedHashMap::new   // preserves traversal order
                                        ))
                        )
                        .collect(Collectors.groupingBy(
                                Function.identity(),
                                Collectors.counting()
                        ));

        // return most frequently occurring map
        return frequency.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(Map.of());
    }

    public Map<String, Integer> getCanonicalStopList(ServiceGroup serviceGroup) {
        return getCanonicalStopList(
                serviceGroup.getRouteShortName(),
                serviceGroup.getTripHeadsign(),
                serviceGroup.getDirectionId()
        );
    }
}
