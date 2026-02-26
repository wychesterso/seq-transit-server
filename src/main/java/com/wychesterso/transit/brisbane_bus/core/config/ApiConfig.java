package com.wychesterso.transit.brisbane_bus.core.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("api")
@ComponentScan(basePackages = {
        "com.wychesterso.transit.brisbane_bus.core",
        "com.wychesterso.transit.brisbane_bus.api",
        "com.wychesterso.transit.brisbane_bus.rt_loader"
})
public class ApiConfig {}
