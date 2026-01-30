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
                    LIMIT 1;
                    """,
            nativeQuery = true
    )
    Stop findStopById(
            @Param("stopId") String stopId
    );

    @Query(
            value = """
                    SELECT
                        stop_id AS stopId,
                        stop_code AS stopCode,
                        stop_name AS stopName,
                        stop_lat AS stopLat,
                        stop_lon AS stopLon,
                        zone_id as zoneId
                    FROM stops
                    WHERE
                        stop_lat BETWEEN :minLat AND :maxLat
                        AND stop_lon BETWEEN :minLon AND :maxLon
                    ORDER BY
                        POWER(stop_lat - :userLat, 2)
                        + POWER(stop_lon - :userLon, 2)
                    LIMIT 50;
                    """,
            nativeQuery = true
    )
    List<Stop> findAdjacentStops(
            @Param("userLat") Double userLat,
            @Param("userLon") Double userLon,
            @Param("minLat") Double minLat,
            @Param("maxLat") Double maxLat,
            @Param("minLon") Double minLon,
            @Param("maxLon") Double maxLon
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
                    JOIN trips t ON st.trip_id = t.trip_id
                    JOIN routes r ON t.route_id = r.route_id
                    WHERE
                        r.route_short_name = :routeShortName
                        AND t.trip_headsign = :tripHeadsign
                        AND t.direction_id = :directionId
                        AND s.stop_lat BETWEEN :minLat AND :maxLat
                        AND s.stop_lon BETWEEN :minLon AND :maxLon
                    ORDER BY
                        POWER(s.stop_lat - :userLat, 2)
                        + POWER(s.stop_lon - :userLon, 2)
                    LIMIT 1;
                    """,
            nativeQuery = true
    )
    Stop findMostAdjacentStopForService(
            @Param("routeShortName") String routeShortName,
            @Param("tripHeadsign") String tripHeadsign,
            @Param("directionId") Integer directionId,
            @Param("userLat") Double userLat,
            @Param("userLon") Double userLon,
            @Param("minLat") Double minLat,
            @Param("maxLat") Double maxLat,
            @Param("minLon") Double minLon,
            @Param("maxLon") Double maxLon
    );
}
