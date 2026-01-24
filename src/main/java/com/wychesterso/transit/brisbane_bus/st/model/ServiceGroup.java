package com.wychesterso.transit.brisbane_bus.st.model;

public interface ServiceGroup {
    String getRouteShortName();
    String getRouteLongName();
    String getTripHeadsign();
    Integer getDirectionId();
    String getRouteColor();
    String getRouteTextColor();
}
