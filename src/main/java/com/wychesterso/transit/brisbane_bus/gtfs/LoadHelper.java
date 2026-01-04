package com.wychesterso.transit.brisbane_bus.gtfs;

public class LoadHelper {
    public static Integer parseInteger(String s) {
        if (s == null || s.isBlank()) return null;
        return Integer.parseInt(s);
    }

    public static Double parseDouble(String s) {
        if (s == null || s.isBlank()) return null;
        return Double.parseDouble(s);
    }

    public static Boolean parseBoolean(String s) {
        if (s == null || s.isBlank()) return null;
        return s.strip().equals("1");
    }
}
