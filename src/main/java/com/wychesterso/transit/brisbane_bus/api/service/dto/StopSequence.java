package com.wychesterso.transit.brisbane_bus.api.service.dto;

import java.util.Map;

public record StopSequence(
        String tripId,
        Map<String, Integer> stopIdToSequence
) {}
