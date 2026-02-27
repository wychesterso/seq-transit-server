package com.wychesterso.transit.seq_transit.api.repository.dto;

public record ServiceGroupKey(
        String routeShortName,
        String tripHeadsign,
        Integer directionId
) {}
