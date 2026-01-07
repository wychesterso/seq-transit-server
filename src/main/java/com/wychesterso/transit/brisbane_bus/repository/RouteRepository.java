package com.wychesterso.transit.brisbane_bus.repository;

import com.wychesterso.transit.brisbane_bus.model.Route;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RouteRepository extends JpaRepository<Route, String> {
}