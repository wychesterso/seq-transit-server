package com.wychesterso.transit.brisbane_bus.core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@Profile("rt-loader")
@EnableScheduling
public class RtSchedulingConfig {}