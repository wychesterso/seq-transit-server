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
                st.trip_id AS tripId,
                st.arrival_time AS arrivalTime,
                st.departure_time AS departureTime
            FROM stop_times st
            WHERE st.stop_id = :stopId
              AND st.arrival_time >= :now
            ORDER BY st.arrival_time
            LIMIT 10
        """,
            nativeQuery = true
    )
    List<StopArrivalDTO> findNextArrivals(
            @Param("stopId") String stopId,
            @Param("now") int now
    );
}