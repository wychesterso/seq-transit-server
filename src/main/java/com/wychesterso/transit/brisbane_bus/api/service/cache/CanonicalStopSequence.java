package com.wychesterso.transit.brisbane_bus.api.service.cache;

import java.util.Map;

public record CanonicalStopSequence(
        Map<String, Integer> stopIdToSequenceMap
) {}
