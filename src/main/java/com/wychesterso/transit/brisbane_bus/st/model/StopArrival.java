package com.wychesterso.transit.brisbane_bus.st.model;

public interface StopArrival {
    String getTripId();
    String getStopId();
    Integer getArrivalTimeSeconds();
    Integer getDepartureTimeSeconds();
}