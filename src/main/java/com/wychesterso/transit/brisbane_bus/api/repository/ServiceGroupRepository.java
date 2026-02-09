package com.wychesterso.transit.brisbane_bus.api.repository;

import com.wychesterso.transit.brisbane_bus.api.repository.dto.ServiceGroup;
import com.wychesterso.transit.brisbane_bus.api.repository.dto.ServiceGroupAtStop;
import com.wychesterso.transit.brisbane_bus.api.repository.dto.StopTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceGroupRepository extends JpaRepository<StopTime, String> {

    @Query(
            value = """
                    SELECT
                        r.route_short_name AS routeShortName,
                        r.route_long_name AS routeLongName,
                        t.trip_headsign AS tripHeadsign,
                        t.direction_id AS directionId,
                        r.route_type AS routeType,
                        r.route_color AS routeColor,
                        r.route_text_color AS routeTextColor,
                        t.shape_id AS shapeId
                    FROM routes r
                    JOIN trips t ON r.route_id = t.route_id
                    WHERE r.route_short_name = :routeShortName
                        AND t.trip_headsign = :tripHeadsign
                        AND t.direction_id = :directionId
                    LIMIT 1;
                    """,
            nativeQuery = true
    )
    ServiceGroup getServiceInfo(
            @Param("routeShortName") String routeShortName,
            @Param("tripHeadsign") String tripHeadsign,
            @Param("directionId") Integer directionId
    );

    @Query(
            value = """
                    SELECT
                        sg.route_short_name AS routeShortName,
                        sg.route_long_name AS routeLongName,
                        sg.trip_headsign AS tripHeadsign,
                        sg.direction_id AS directionId,
                        sg.route_type AS routeType,
                        sg.route_color AS routeColor,
                        sg.route_text_color AS routeTextColor,
                        sg.shape_id AS shapeId
                    FROM (
                        SELECT
                            r.route_short_name,
                            r.route_long_name,
                            t.trip_headsign,
                            t.direction_id,
                            r.route_type,
                            r.route_color,
                            r.route_text_color,
                            MIN(t.shape_id) AS shape_id
                        FROM routes r
                        JOIN trips t ON r.route_id = t.route_id
                        WHERE r.route_short_name ILIKE CONCAT(:prefix, '%')
                        GROUP BY
                            r.route_short_name,
                            r.route_long_name,
                            t.trip_headsign,
                            t.direction_id,
                            r.route_type,
                            r.route_color,
                            r.route_text_color
                    ) sg
                    ORDER BY
                        CASE
                            WHEN sg.route_short_name ~ '^[0-9]+$' THEN 0
                            ELSE 1
                        END,
                        CASE
                            WHEN sg.route_short_name ~ '^[0-9]+$'
                            THEN sg.route_short_name::int
                        END,
                        sg.route_short_name;
                    """,
            nativeQuery = true
    )
    List<ServiceGroup> getServicesByPrefix(
            @Param("prefix") String prefix
    );

    @Query(
            value = """
                    SELECT DISTINCT
                        r.route_short_name AS routeShortName,
                        r.route_long_name AS routeLongName,
                        t.trip_headsign AS tripHeadsign,
                        t.direction_id AS directionId,
                        r.route_type AS routeType,
                        r.route_color AS routeColor,
                        r.route_text_color AS routeTextColor,
                        st.stop_id AS stopId,
                        s.stop_lat AS stopLat,
                        s.stop_lon AS stopLon
                    FROM routes r
                    JOIN trips t ON r.route_id = t.route_id
                    JOIN stop_times st ON t.trip_id = st.trip_id
                    JOIN stops s ON st.stop_id = s.stop_id
                    WHERE st.stop_id = :stopId
                    ORDER BY r.route_short_name;
                    """,
            nativeQuery = true
    )
    List<ServiceGroupAtStop> getServicesAtStop(
            @Param("stopId") String stopId
    );

    @Query(
            value = """
                    SELECT DISTINCT
                        r.route_short_name AS routeShortName,
                        r.route_long_name AS routeLongName,
                        t.trip_headsign AS tripHeadsign,
                        t.direction_id AS directionId,
                        r.route_type AS routeType,
                        r.route_color AS routeColor,
                        r.route_text_color AS routeTextColor,
                        st.stop_id AS stopId,
                        s.stop_lat AS stopLat,
                        s.stop_lon AS stopLon
                    FROM routes r
                    JOIN trips t ON r.route_id = t.route_id
                    JOIN stop_times st ON t.trip_id = st.trip_id
                    JOIN stops s ON st.stop_id = s.stop_id
                    WHERE st.stop_id = :stopId
                        AND st.pickup_type = 0
                    ORDER BY r.route_short_name;
                    """,
            nativeQuery = true
    )
    List<ServiceGroupAtStop> getPickupServicesAtStop(
            @Param("stopId") String stopId
    );
}
