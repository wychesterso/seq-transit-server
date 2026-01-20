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
                    ORDER BY stop_id
            """,
            nativeQuery = true
    )
    List<Stop> getStops();

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
}
