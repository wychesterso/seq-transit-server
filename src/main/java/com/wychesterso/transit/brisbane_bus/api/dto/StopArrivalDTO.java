package com.wychesterso.transit.brisbane_bus.api.dto;

public interface StopArrivalDTO {
    String getTripId();
    String getStopId();
    Integer getArrivalTimeSeconds();
    Integer getDepartureTimeSeconds();
}