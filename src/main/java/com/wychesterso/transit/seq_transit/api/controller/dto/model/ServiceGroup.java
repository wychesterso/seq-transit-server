package com.wychesterso.transit.seq_transit.api.controller.dto.model;

public record ServiceGroup(
        String routeShortName,
        String tripHeadsign,
        int directionId
) {}
