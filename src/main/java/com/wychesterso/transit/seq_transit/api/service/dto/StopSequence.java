package com.wychesterso.transit.seq_transit.api.service.dto;

import java.util.Map;

public record StopSequence(
        String tripId,
        Map<String, Integer> stopIdToSequence
) {}
