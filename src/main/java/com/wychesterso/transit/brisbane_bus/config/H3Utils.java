package com.wychesterso.transit.brisbane_bus.config;

import com.uber.h3core.H3Core;
import java.io.IOException;

public class H3Utils {

    private static final H3Core h3;

    static {
        try {
            h3 = H3Core.newInstance();
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize H3", e);
        }
    }

    public static String latLonToCell(double lat, double lon, int resolution) {
        long cell = h3.geoToH3(lat, lon, resolution);
        return Long.toHexString(cell);
    }
}
