package com.wychesterso.transit.brisbane_bus.api.repository.dto;

public interface StopArrival {
    String getTripId();
    String getStopId();
    Integer getArrivalTimeSeconds();
    Integer getDepartureTimeSeconds();
}