package com.wychesterso.transit.brisbane_bus.dto;

public interface StopArrivalDTO {
    String getTripId();
    Integer getArrivalTime();
    Integer getDepartureTime();
}