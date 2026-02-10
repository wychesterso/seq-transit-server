package com.wychesterso.transit.brisbane_bus.api.repository;

import com.wychesterso.transit.brisbane_bus.api.repository.dto.Shape;
import com.wychesterso.transit.brisbane_bus.api.repository.dto.StopList;
import com.wychesterso.transit.brisbane_bus.api.repository.dto.StopTime;
import com.wychesterso.transit.brisbane_bus.api.repository.dto.TripAndShape;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StopSequenceRepository extends JpaRepository<StopTime, String> {

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

    @Query(
            value = """
                    SELECT
                        sh.shape_id AS shapeId,
                        sh.shape_pt_lat AS shapePtLat,
                        sh.shape_pt_lon AS shapePtLon,
                        sh.shape_pt_sequence AS shapePtSequence
                    FROM shapes sh
                    JOIN trips t ON sh.shape_id = t.shape_id
                    WHERE t.shape_id = :shapeId
                    ORDER BY sh.shape_pt_sequence
                    """,
            nativeQuery = true
    )
    List<Shape> getShape(
            @Param("shapeId") String shapeId
    );

    @Query(
            value = """
                    SELECT
                        t.trip_id AS tripId,
                        t.shape_id AS shapeId
                    FROM trips t
                    WHERE t.trip_id IN (:tripIds)
                    """,
            nativeQuery = true
    )
    List<TripAndShape> getShapeIdsForTrips(
            @Param("tripIds") List<String> tripIds
    );

    @Query(
            value = """
                    SELECT
                        sh.shape_id AS shapeId,
                        sh.shape_pt_lat AS shapePtLat,
                        sh.shape_pt_lon AS shapePtLon,
                        sh.shape_pt_sequence AS shapePtSequence
                    FROM shapes sh
                    WHERE sh.shape_id IN (:shapeIds)
                    ORDER BY sh.shape_id, sh.shape_pt_sequence
                    """,
            nativeQuery = true
    )
    List<Shape> getShapesForShapeIds(List<String> shapeIds);
}
