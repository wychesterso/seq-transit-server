package com.wychesterso.transit.brisbane_bus.api.service.cache;

import java.util.List;

public record ServiceGroupList(
        List<ServiceGroupDTO> serviceGroupList
) {}
