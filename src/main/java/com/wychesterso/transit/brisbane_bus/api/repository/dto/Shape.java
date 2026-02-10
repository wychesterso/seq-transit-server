package com.wychesterso.transit.brisbane_bus.api.repository.dto;

public interface Shape {
    String getShapeId();
    Double getShapePtLat();
    Double getShapePtLon();
    Integer getShapePtSequence();
}
