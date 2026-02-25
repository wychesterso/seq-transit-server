package com.wychesterso.transit.brisbane_bus.api.repository;

import com.wychesterso.transit.brisbane_bus.api.repository.dto.StopDelay;
import com.wychesterso.transit.brisbane_bus.api.repository.dto.StopTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GtfsRtRepository extends JpaRepository<StopTime, String> {

    @Query(
            value = """
                SELECT
                    rt.trip_id                     AS tripId,
                    rt.stop_id                     AS stopId,
                    rt.effective_arrival_seconds   AS effectiveArrivalSeconds,
                    rt.effective_departure_seconds AS effectiveDepartureSeconds,
                    rt.cancelled                   AS cancelled,
                    rt.skipped                     AS skipped
                FROM gtfs_rt_stop_delay rt
            """,
            nativeQuery = true
    )
    List<StopDelay> findAllDelays();

    @Query(
            value = """
                SELECT
                    rt.trip_id                     AS tripId,
                    rt.stop_id                     AS stopId,
                    rt.effective_arrival_seconds   AS effectiveArrivalSeconds,
                    rt.effective_departure_seconds AS effectiveDepartureSeconds,
                    rt.cancelled                   AS cancelled,
                    rt.skipped                     AS skipped
                FROM gtfs_rt_stop_delay rt
                WHERE rt.trip_id = :tripId
                AND rt.stop_id = :stopId
            """,
            nativeQuery = true
    )
    StopDelay findDelay(
            @Param("tripId") String tripId,
            @Param("stopId") String stopId
    );
}
