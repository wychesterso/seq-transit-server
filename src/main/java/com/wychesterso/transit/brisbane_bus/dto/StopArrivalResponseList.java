package com.wychesterso.transit.brisbane_bus.dto;

import java.util.List;

public class StopArrivalResponseList {
    private List<StopArrivalResponse> arrivals;

    public StopArrivalResponseList() {}
    public StopArrivalResponseList(List<StopArrivalResponse> arrivals) {
        this.arrivals = arrivals;
    }

    public List<StopArrivalResponse> getArrivals() { return arrivals; }
    public void setArrivals(List<StopArrivalResponse> arrivals) { this.arrivals = arrivals; }
}
