package com.wychesterso.transit.brisbane_bus.st.repository;

import com.wychesterso.transit.brisbane_bus.st.model.ServiceGroup;
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
                        r.route_text_color AS routeTextColor
                    FROM routes r
                    JOIN trips t ON r.route_id = t.route_id
                    JOIN stop_times st ON t.trip_id = st.trip_id
                    WHERE st.stop_id = :stopId
                    ORDER BY r.route_short_name;
                    """,
            nativeQuery = true
    )
    List<ServiceGroup> getServicesAtStop(
            @Param("stopId") String stopId
    );
}
