package com.wychesterso.transit.brisbane_bus.st.repository;

import com.wychesterso.transit.brisbane_bus.st.model.Stop;
import com.wychesterso.transit.brisbane_bus.st.model.StopTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StopRepository extends JpaRepository<StopTime, String> {

    @Query(
            value = """
                SELECT
                    stop_id AS stopId,
                    stop_code AS stopCode,
                    stop_name AS stopName,
                    stop_lat AS stopLat,
                    stop_lon AS stopLon,
                    zone_id AS zoneId
                FROM stops
                WHERE stop_id = :stopId
            """,
            nativeQuery = true
    )
    List<Stop> findStopById(
            @Param("stopId") String stopId
    );

    @Query(
            value = """
                SELECT
                    s.stop_id AS stopId,
                    s.stop_code AS stopCode,
                    s.stop_name AS stopName,
                    s.stop_lat AS stopLat,
                    s.stop_lon AS stopLon,
                    s.zone_id AS zoneId
                FROM stops s
                JOIN stop_times st ON s.stop_id = st.stop_id
                JOIN trips t ON t.trip_id = st.trip_id
                WHERE t.route_id = :routeId
                GROUP BY
                    s.stop_id,
                    s.stop_code,
                    s.stop_name,
                    s.stop_lat,
                    s.stop_lon,
                    s.zone_id
                ORDER BY MIN(st.stop_sequence)
        """,
            nativeQuery = true
    )
    List<Stop> findStopsForRoute(
            @Param("routeId") String routeId
    );
}
