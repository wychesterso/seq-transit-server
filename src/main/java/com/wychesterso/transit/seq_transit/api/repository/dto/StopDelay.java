package com.wychesterso.transit.seq_transit.api.repository.dto;

public interface StopDelay {
    String getTripId();
    String getStopId();
    Integer getEffectiveArrivalSeconds();
    Integer getEffectiveDepartureSeconds();
    boolean getCancelled();
    boolean getSkipped();
}
