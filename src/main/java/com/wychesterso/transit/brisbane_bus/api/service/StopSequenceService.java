package com.wychesterso.transit.brisbane_bus.api.service;

import com.wychesterso.transit.brisbane_bus.api.cache.StopSequenceCache;
import com.wychesterso.transit.brisbane_bus.api.cache.dto.CanonicalStopSequence;
import com.wychesterso.transit.brisbane_bus.api.cache.dto.CanonicalStopSequenceAndShape;
import com.wychesterso.transit.brisbane_bus.api.cache.dto.ShapePoint;
import com.wychesterso.transit.brisbane_bus.api.repository.dto.Shape;
import com.wychesterso.transit.brisbane_bus.api.repository.dto.StopList;
import com.wychesterso.transit.brisbane_bus.api.repository.StopSequenceRepository;
import com.wychesterso.transit.brisbane_bus.api.service.dto.StopSequence;
import org.springframework.stereotype.Service;

import java.util.*;
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
        CanonicalStopSequenceAndShape cachedResult =
                cache.getCanonicalStopSequenceAndShape(routeShortName, tripHeadsign, directionId);
        if (cachedResult != null) return cachedResult;

        CanonicalStopSequence canonicalStopSequence = getCanonicalStopSequenceWithTrips(
                routeShortName, tripHeadsign, directionId);
        List<String> tripIds = canonicalStopSequence.tripIds();

        // build shape_signature -> shapes map
        Map<String, List<List<Shape>>> groupedShapes =
                tripIds.stream()
                        .map(repository::getShape)
                        .filter(Objects::nonNull)
                        .collect(Collectors.groupingBy(
                                StopSequenceService::shapeSignature
                        ));

        // find most frequent shape
        List<Shape> canonicalShape =
                groupedShapes.entrySet().stream()
                        .max(Comparator.comparingInt(e -> e.getValue().size()))
                        .orElseThrow()
                        .getValue().get(0);

        // result
        CanonicalStopSequenceAndShape result = new CanonicalStopSequenceAndShape(
                canonicalStopSequence.stopIdToSequenceMap(), canonicalShape.stream().map(ShapePoint::from).toList());

        // cache it
        cache.cacheCanonicalStopSequenceAndShape(routeShortName, tripHeadsign, directionId, result);

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
