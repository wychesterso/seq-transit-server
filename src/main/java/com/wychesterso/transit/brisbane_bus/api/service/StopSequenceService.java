package com.wychesterso.transit.brisbane_bus.api.service;

import com.wychesterso.transit.brisbane_bus.api.cache.StopSequenceCache;
import com.wychesterso.transit.brisbane_bus.api.cache.dto.CanonicalStopSequence;
import com.wychesterso.transit.brisbane_bus.api.cache.dto.CanonicalStopSequenceAndShape;
import com.wychesterso.transit.brisbane_bus.api.cache.dto.ShapePoint;
import com.wychesterso.transit.brisbane_bus.api.repository.dto.Shape;
import com.wychesterso.transit.brisbane_bus.api.repository.dto.StopList;
import com.wychesterso.transit.brisbane_bus.api.repository.StopSequenceRepository;
import com.wychesterso.transit.brisbane_bus.api.repository.dto.TripAndShape;
import com.wychesterso.transit.brisbane_bus.api.service.dto.StopSequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class StopSequenceService {

    private final StopSequenceRepository repository;
    private final StopSequenceCache cache;

    private static final Logger log = LoggerFactory.getLogger(StopSequenceService.class);

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
        return getCanonicalStopSequenceWithTrips(routeShortName, tripHeadsign, directionId)
                .stopIdToSequenceMap();
    }

    /**
     * Get the canonical (most frequently occurring) stop sequence and shape for a service group
     * @param routeShortName service group's route name
     * @param tripHeadsign service group's headsign
     * @param directionId service group's direction identifier
     * @return the canonical stop sequence and shape
     */
    public CanonicalStopSequenceAndShape getCanonicalStopSequenceAndShape(
            String routeShortName,
            String tripHeadsign,
            int directionId
    ) {
        long start = System.currentTimeMillis();
        log.info("Starting getCanonicalStopSequenceAndShape...");

        CanonicalStopSequenceAndShape cachedResult =
                cache.getCanonicalStopSequenceAndShape(routeShortName, tripHeadsign, directionId);
        if (cachedResult != null) {
            log.info("{} ms: Returning cached result...",
                    System.currentTimeMillis() - start);
            return cachedResult;
        }

        log.info("{} ms: Getting canonicalStopSequence with related trips...",
                System.currentTimeMillis() - start);
        CanonicalStopSequence canonicalStopSequence = getCanonicalStopSequenceWithTrips(
                routeShortName, tripHeadsign, directionId);
        List<String> tripIds = canonicalStopSequence.tripIds();

        log.info("{} ms: Getting tripAndShapeList...",
                System.currentTimeMillis() - start);
        List<TripAndShape> tripAndShapeList = repository.getShapeIdsForTrips(tripIds);
        Map<String, Long> counts =
                tripAndShapeList.stream()
                        .collect(Collectors.groupingBy(
                                TripAndShape::getShapeId,
                                Collectors.counting()
                        ));

        log.info("{} ms: Determining canonicalShape...",
                System.currentTimeMillis() - start);
        String canonicalShapeId =
                counts.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .orElseThrow()
                        .getKey();

        log.info("{} ms: Retrieving shape points...",
                System.currentTimeMillis() - start);
        List<Shape> canonicalShape = repository.getShape(canonicalShapeId);

        // result
        CanonicalStopSequenceAndShape result = new CanonicalStopSequenceAndShape(
                canonicalStopSequence.stopIdToSequenceMap(), canonicalShape.stream().map(ShapePoint::from).toList());

        // cache it
        cache.cacheCanonicalStopSequenceAndShape(routeShortName, tripHeadsign, directionId, result);

        log.info("getCanonicalStopSequenceAndShape finished in {} ms",
                System.currentTimeMillis() - start);
        return result;
    }

    private CanonicalStopSequence getCanonicalStopSequenceWithTrips(
            String routeShortName,
            String tripHeadsign,
            int directionId
    ) {
        CanonicalStopSequence cachedResult =
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

        // build stop sequences
        List<StopSequence> patterns =
                byTrip.entrySet().stream()
                        .map(entry -> new StopSequence(
                                entry.getKey(),
                                entry.getValue().stream()
                                        .sorted(Comparator.comparingInt(StopList::getStopSequence))
                                        .collect(Collectors.toMap(
                                                StopList::getStopId,
                                                StopList::getStopSequence,
                                                (a, b) -> a, // defensive merge
                                                LinkedHashMap::new // preserve traversal order
                                        ))
                        ))
                        .toList();

        // group by sequence map, and collect tripIds
        Map<Map<String, Integer>, List<String>> grouped =
                patterns.stream()
                        .collect(Collectors.groupingBy(
                                StopSequence::stopIdToSequence,
                                Collectors.mapping(StopSequence::tripId, Collectors.toList())
                        ));

        // find most frequent pattern
        Map.Entry<Map<String, Integer>, List<String>> mostFrequent =
                grouped.entrySet().stream()
                        .max(Comparator.comparingInt(e -> e.getValue().size()))
                        .orElseThrow();

        // result
        CanonicalStopSequence result = new CanonicalStopSequence(
                mostFrequent.getKey(),
                mostFrequent.getValue());

        // cache it
        cache.cacheCanonicalStopSequence(routeShortName, tripHeadsign, directionId, result);

        return result;
    }

    private static String shapeSignature(List<Shape> shape) {
        return shape.stream()
                .sorted(Comparator.comparingInt(Shape::getShapePtSequence))
                .map(p -> p.getShapePtLat() + "," + p.getShapePtLon())
                .collect(Collectors.joining("|"));
    }
}
