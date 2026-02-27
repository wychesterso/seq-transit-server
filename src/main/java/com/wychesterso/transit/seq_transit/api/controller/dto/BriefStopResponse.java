package com.wychesterso.transit.seq_transit.api.controller.dto;

import com.wychesterso.transit.seq_transit.api.repository.dto.Stop;

public record BriefStopResponse(
        String stopId,
        String stopCode,
        String stopName,
        Double stopLat,
        Double stopLon,
        String zoneId
) {
    public static BriefStopResponse from(Stop stop) {
        if (stop == null) return null;
        return new BriefStopResponse(
                stop.getStopId(),
                stop.getStopCode(),
                stop.getStopName(),
                stop.getStopLat(),
                stop.getStopLon(),
                stop.getZoneId()
        );
    }
}
