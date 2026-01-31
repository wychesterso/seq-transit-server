package com.wychesterso.transit.brisbane_bus.api.cache.dto;

import java.util.List;

public record ServiceGroupAtStopList(
        List<ServiceGroupAtStopDTO> serviceGroupAtStopList
) {}
