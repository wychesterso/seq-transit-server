package com.wychesterso.transit.brisbane_bus.st.repository;

import com.wychesterso.transit.brisbane_bus.st.model.StopList;
import com.wychesterso.transit.brisbane_bus.st.model.StopTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<StopTime, String> {

    @Query(
            value = """
                    SELECT
                        st.trip_id AS tripId,
                        st.stop_id AS stopId,
                        st.stop_sequence AS stopSequence
                    FROM stop_times st
                    JOIN trips t ON st.trip_id = t.trip_id
                    JOIN routes r ON t.route_id = r.route_id
                    WHERE
                        r.route_short_name = :routeShortName
                        AND t.direction_id = :directionId
                        AND t.trip_headsign = :tripHeadsign
                    """,
            nativeQuery = true
    )
    List<StopList> getStopSequences(
            @Param("routeShortName") String routeShortName,
            @Param("directionId") int directionId,
            @Param("tripHeadsign") String tripHeadsign
    );
}
