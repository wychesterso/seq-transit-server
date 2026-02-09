package com.wychesterso.transit.brisbane_bus.api.controller.dto.model;

public enum AdjacentRadius {

    SMALL(100),
    MEDIUM(250),
    LARGE(500),
    XL(1000);

    private final int meters;

    AdjacentRadius(int meters) {
        this.meters = meters;
    }

    public int getMeters() {
        return meters;
    }

    public double toLatDelta() {
        return meters / 111_320.0;
    }
}
