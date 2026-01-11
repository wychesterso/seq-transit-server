package com.wychesterso.transit.brisbane_bus.repository;

import com.wychesterso.transit.brisbane_bus.dto.StopArrivalDTO;
import com.wychesterso.transit.brisbane_bus.model.StopTime;
import com.wychesterso.transit.brisbane_bus.model.StopTimeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StopArrivalRepository extends JpaRepository<StopTime, StopTimeId> {

    @Query(
        value = """
            SELECT
                st.trip_id        AS tripId,
                st.arrival_time   AS arrivalTime,
                st.departure_time AS departureTime
            FROM stop_times st
            WHERE st.stop_id = :stopId
                AND st.arrival_time >= :now
            ORDER BY st.arrival_time
            LIMIT 10
        """,
        nativeQuery = true
    )
    List<StopArrivalDTO> findNextArrivalsForStop(
            @Param("stopId") String stopId,
            @Param("now") int now
    );

    @Query(
        value = """
            WITH active_services AS (
                SELECT service_id
                FROM calendar
                WHERE
                    start_date <= CURRENT_DATE
                    AND end_date >= CURRENT_DATE
                    AND CASE EXTRACT(DOW FROM CURRENT_DATE)
                        WHEN 0 THEN sunday
                        WHEN 1 THEN monday
                        WHEN 2 THEN tuesday
                        WHEN 3 THEN wednesday
                        WHEN 4 THEN thursday
                        WHEN 5 THEN friday
                        WHEN 6 THEN saturday
                    END IS TRUE
        
                UNION
        
                SELECT service_id
                FROM calendar_dates
                WHERE date = CURRENT_DATE
                    AND exception_type = 1
        
                EXCEPT
        
                SELECT service_id
                FROM calendar_dates
                WHERE date = CURRENT_DATE
                AND exception_type = 2
            )
        
            SELECT
                st.trip_id        AS tripId,
                st.arrival_time   AS arrivalTimeSeconds,
                st.departure_time AS departureTimeSeconds
            FROM stop_times st
            JOIN trips t ON t.trip_id = st.trip_id
            WHERE st.stop_id = :stopId
                AND t.route_id = :routeId
                AND t.service_id IN (SELECT service_id FROM active_services)
                AND st.arrival_time >= :now
            ORDER BY st.arrival_time
            LIMIT 3
        """,
        nativeQuery = true
    )
    List<StopArrivalDTO> findNextArrivalsForRouteAtStop(
            @Param("stopId") String stopId,
            @Param("routeId") String routeId,
            @Param("now") int now
    );
}