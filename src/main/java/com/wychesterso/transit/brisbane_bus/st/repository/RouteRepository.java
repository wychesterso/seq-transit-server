package com.wychesterso.transit.brisbane_bus.st.repository;

import com.wychesterso.transit.brisbane_bus.st.model.Route;
import com.wychesterso.transit.brisbane_bus.st.model.StopTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RouteRepository extends JpaRepository<StopTime, String> {

    @Query(
            value = """
                    SELECT
                        route_id AS routeId,
                        route_short_name AS routeShortName,
                        route_long_name AS routeLongName,
                        route_color AS routeColor,
                        route_text_color AS routeTextColor
                    FROM routes
                    WHERE route_short_name = :routeShortName
                    ORDER BY route_short_name;
            """,
            nativeQuery = true
    )
    List<Route> findRoutesByShortName(
            @Param("routeShortName") String routeShortName
    );
}
