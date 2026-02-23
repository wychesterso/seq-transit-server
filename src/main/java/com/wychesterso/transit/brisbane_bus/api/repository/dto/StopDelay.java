package com.wychesterso.transit.brisbane_bus.api.repository.dto;

public interface StopDelay {
    String getTripId();
    String getStopId();
    Integer getEffectiveArrivalSeconds();
    Integer getEffectiveDepartureSeconds();
    boolean getCancelled();
    boolean getSkipped();
}
