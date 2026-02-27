package com.wychesterso.transit.seq_transit.api.cache.dto;

import java.util.List;
import java.util.Map;

public record CanonicalStopSequenceAndShape(
        Map<String, Integer> stopIdToSequenceMap,
        List<ShapePoint> shape
) {}
