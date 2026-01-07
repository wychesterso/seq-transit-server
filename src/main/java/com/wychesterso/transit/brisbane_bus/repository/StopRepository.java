package com.wychesterso.transit.brisbane_bus.repository;

import com.wychesterso.transit.brisbane_bus.model.Stop;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StopRepository extends JpaRepository<Stop, String> {
}