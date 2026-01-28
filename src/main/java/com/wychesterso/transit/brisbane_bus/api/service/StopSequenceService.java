package com.wychesterso.transit.brisbane_bus.api.service;

import com.wychesterso.transit.brisbane_bus.api.service.cache.CanonicalStopSequence;
import com.wychesterso.transit.brisbane_bus.st.model.ServiceGroup;
import com.wychesterso.transit.brisbane_bus.st.model.StopList;
import com.wychesterso.transit.brisbane_bus.st.repository.TripRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class StopSequenceService {

    private final TripRepository tripRepository;
    private final RedisTemplate<String, Object> redis;

    public StopSequenceService(
            TripRepository tripRepository,
            RedisTemplate<String, Object> redis) {
        this.tripRepository = tripRepository;
        this.redis = redis;
    }

    // map stop_id to stop_sequence
    public Map<String, Integer> getCanonicalStopSequence(
            String routeShortName,
            String tripHeadsign,
            int directionId) {

        String key = "service:%s:stoplist:%s:%s"
                .formatted(routeShortName, tripHeadsign, directionId);

        @SuppressWarnings("unchecked")
        CanonicalStopSequence cached = (CanonicalStopSequence) redis.opsForValue().get(key);
        if (cached != null) {
            System.out.println("Using cached Redis result: " + key);
            return cached.stopIdToSequenceMap();
        }

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
        Map<String, Integer> result = frequency.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(Map.of());

        redis.opsForValue().set(
                key,
                new CanonicalStopSequence(result),
                Duration.ofSeconds(86400) // TTL
        );

        return result;
    }

    public Map<String, Integer> getCanonicalStopSequence(ServiceGroup serviceGroup) {
        return getCanonicalStopSequence(
                serviceGroup.getRouteShortName(),
                serviceGroup.getTripHeadsign(),
                serviceGroup.getDirectionId()
        );
    }
}
