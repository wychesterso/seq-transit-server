package com.wychesterso.transit.seq_transit.api.cache.dto;

import com.wychesterso.transit.seq_transit.api.repository.dto.Shape;

public record ShapePoint(
        Double shapePtLat,
        Double shapePtLon,
        Integer shapePtSequence
) {
    public static ShapePoint from(Shape s) {
        if (s == null) return null;
        return new ShapePoint(
                s.getShapePtLat(),
                s.getShapePtLon(),
                s.getShapePtSequence()
        );
    }
}
