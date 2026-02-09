package com.wychesterso.transit.brisbane_bus.api.cache.dto;

import java.util.List;
import java.util.Map;

public record CanonicalStopSequenceAndShape(
        Map<String, Integer> stopIdToSequenceMap,
        List<ShapePoint> shape
) {}
