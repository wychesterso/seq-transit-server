package com.wychesterso.transit.brisbane_bus.api.repository.dto;

public interface ServiceGroup {
    String getRouteShortName();
    String getRouteLongName();
    String getTripHeadsign();
    Integer getDirectionId();
    Integer getRouteType();
    String getRouteColor();
    String getRouteTextColor();
    String getShapeId();
}
