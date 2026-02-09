package com.wychesterso.transit.brisbane_bus.api.repository.dto;

public interface ServiceGroupAtStop {
    String getRouteShortName();
    String getRouteLongName();
    String getTripHeadsign();
    Integer getDirectionId();
    Integer getRouteType();
    String getRouteColor();
    String getRouteTextColor();
    String getStopId();
    Double getStopLat();
    Double getStopLon();
}
