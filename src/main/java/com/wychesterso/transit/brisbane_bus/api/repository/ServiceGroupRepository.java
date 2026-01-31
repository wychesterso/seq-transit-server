package com.wychesterso.transit.brisbane_bus.api.repository;

import com.wychesterso.transit.brisbane_bus.st.model.ServiceGroup;
import com.wychesterso.transit.brisbane_bus.st.model.ServiceGroupAtStop;
import com.wychesterso.transit.brisbane_bus.st.model.StopTime;
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
                        r.route_color AS routeColor,
                        r.route_text_color AS routeTextColor
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
                    SELECT DISTINCT
                        r.route_short_name AS routeShortName,
                        r.route_long_name AS routeLongName,
                        t.trip_headsign AS tripHeadsign,
                        t.direction_id AS directionId,
                        r.route_color AS routeColor,
                        r.route_text_color AS routeTextColor
                    FROM routes r
                    JOIN trips t ON r.route_id = t.route_id
                    WHERE r.route_short_name ILIKE CONCAT(:prefix, '%')
                    ORDER BY r.route_short_name;
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
}
