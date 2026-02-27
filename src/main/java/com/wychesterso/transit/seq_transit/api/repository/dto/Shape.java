package com.wychesterso.transit.seq_transit.api.repository.dto;

public interface Shape {
    String getShapeId();
    Double getShapePtLat();
    Double getShapePtLon();
    Integer getShapePtSequence();
}
