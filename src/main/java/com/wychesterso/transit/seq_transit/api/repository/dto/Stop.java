package com.wychesterso.transit.seq_transit.api.repository.dto;

public interface Stop {
    String getStopId();
    String getStopCode();
    String getStopName();
    Double getStopLat();
    Double getStopLon();
    String getZoneId();
}
