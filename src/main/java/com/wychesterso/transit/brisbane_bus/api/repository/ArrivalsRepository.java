package com.wychesterso.transit.brisbane_bus.api.repository;

import com.wychesterso.transit.brisbane_bus.st.model.StopArrival;
import com.wychesterso.transit.brisbane_bus.st.model.StopTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArrivalsRepository extends JpaRepository<StopTime, String> {

    @Query(
            value = """
                SELECT
                    st.trip_id        AS tripId,
                    st.stop_id        AS stopId,
                    st.arrival_time   AS arrivalTimeSeconds,
                    st.departure_time AS departureTimeSeconds
                FROM stop_times st
                WHERE st.stop_id = :stopId
                    AND st.arrival_time >= :now
                ORDER BY st.arrival_time
                LIMIT 10
            """,
            nativeQuery = true
    )
    List<StopArrival> findNextArrivalsForStop(
            @Param("stopId") String stopId,
            @Param("now") int now
    );

    @Query(
            value = """
                WITH active_services AS (
                    SELECT service_id
                    FROM calendar
                    WHERE
                        start_date <= TO_DATE(CAST(:serviceDateInt AS text), 'YYYYMMDD')
                        AND end_date >= TO_DATE(CAST(:serviceDateInt AS text), 'YYYYMMDD')
                        AND CASE EXTRACT(DOW FROM TO_DATE(CAST(:serviceDateInt AS text), 'YYYYMMDD'))
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
                    WHERE date = TO_DATE(CAST(:serviceDateInt AS text), 'YYYYMMDD')
                        AND exception_type = 1
        
                    EXCEPT
        
                    SELECT service_id
                    FROM calendar_dates
                    WHERE date = TO_DATE(CAST(:serviceDateInt AS text), 'YYYYMMDD')
                    AND exception_type = 2
                )
        
                SELECT
                    st.trip_id        AS tripId,
                    st.stop_id        AS stopId,
                    st.arrival_time   AS arrivalTimeSeconds,
                    st.departure_time AS departureTimeSeconds
                FROM stop_times st
                JOIN trips t ON t.trip_id = st.trip_id
                JOIN routes r ON t.route_id = r.route_id
                WHERE st.stop_id = :stopId
                    AND r.route_short_name = :routeShortName
                    AND t.trip_headsign = :tripHeadsign
                    AND t.direction_id = :directionId
                    AND t.service_id IN (SELECT service_id FROM active_services)
                    AND st.arrival_time >= :now
                ORDER BY st.arrival_time
                LIMIT 3
            """,
            nativeQuery = true
    )
    List<StopArrival> findNextArrivalsForServiceAtStop(
            @Param("stopId") String stopId,
            @Param("routeShortName") String routeShortName,
            @Param("tripHeadsign") String tripHeadsign,
            @Param("directionId") Integer directionId,
            @Param("serviceDateInt") int serviceDateInt,
            @Param("now") int now
    );
}