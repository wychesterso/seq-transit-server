package com.wychesterso.transit.brisbane_bus.api.repository.dto;

public interface Stop {
    String getStopId();
    String getStopCode();
    String getStopName();
    Double getStopLat();
    Double getStopLon();
    String getZoneId();
}
