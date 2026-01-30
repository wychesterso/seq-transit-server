package com.wychesterso.transit.brisbane_bus.api.dto;

import com.wychesterso.transit.brisbane_bus.st.model.Stop;

public record BriefStopResponse(
        String stopId,
        String stopCode,
        String stopName,
        Double stopLat,
        Double stopLon,
        String zoneId
) {
    public static BriefStopResponse from(Stop stop) {
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
