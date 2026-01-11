package com.wychesterso.transit.brisbane_bus.gtfs;

import org.slf4j.Logger;

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

    public static Integer parseGtfsTime(String t) {
        String[] p = t.split(":");
        return Integer.parseInt(p[0]) * 3600
                + Integer.parseInt(p[1]) * 60
                + Integer.parseInt(p[2]);
    }

    public static void printLog(Logger log, String s) {
        log.info("CalendarLoader: " + s);
    }
}
