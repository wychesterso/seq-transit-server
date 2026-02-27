package com.wychesterso.transit.seq_transit.api.repository.dto;

public interface StopArrival {
    String getTripId();
    String getStopId();
    Integer getArrivalTimeSeconds();
    Integer getDepartureTimeSeconds();
}