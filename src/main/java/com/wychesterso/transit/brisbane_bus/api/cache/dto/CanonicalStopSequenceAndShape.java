package com.wychesterso.transit.brisbane_bus.api.cache.dto;

import com.wychesterso.transit.brisbane_bus.api.repository.dto.ShapePoint;

import java.util.List;
import java.util.Map;

public record CanonicalStopSequenceAndShape(
        Map<String, Integer> stopIdToSequenceMap,
        List<ShapePoint> shape
) {}
