package com.wychesterso.transit.brisbane_bus.api.service;

import com.wychesterso.transit.brisbane_bus.api.dto.ArrivalsAtStopResponse;
import com.wychesterso.transit.brisbane_bus.api.dto.BriefServiceResponse;
import com.wychesterso.transit.brisbane_bus.api.dto.BriefStopResponse;
import com.wychesterso.transit.brisbane_bus.api.dto.ServiceId;
import com.wychesterso.transit.brisbane_bus.api.service.cache.ServiceGroupDTO;
import com.wychesterso.transit.brisbane_bus.api.service.cache.ServiceGroupList;
import com.wychesterso.transit.brisbane_bus.st.model.ServiceGroup;
import com.wychesterso.transit.brisbane_bus.st.repository.ServiceGroupRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class ServiceGroupService {

    private final ServiceGroupRepository serviceGroupRepository;
    private final ServiceGroupStopLocator serviceGroupStopLocator;
    private final ArrivalsService arrivalsService;
    private final StopSequenceService stopSequenceService;

    private final RedisTemplate<String, Object> redis;

    public ServiceGroupService(
            ServiceGroupRepository serviceGroupRepository,
            ServiceGroupStopLocator serviceGroupStopLocator,
            ArrivalsService arrivalsService,
            StopSequenceService stopSequenceService,
            RedisTemplate<String, Object> redis) {
        this.serviceGroupRepository = serviceGroupRepository;
        this.serviceGroupStopLocator = serviceGroupStopLocator;
        this.arrivalsService = arrivalsService;
        this.stopSequenceService = stopSequenceService;
        this.redis = redis;
    }

    public List<BriefServiceResponse> getServicesByPrefix(String prefix, double lat, double lon) {
        if (prefix == null || prefix.isBlank()) return List.of();

        List<ServiceGroupDTO> cachedDTOs;
        String key = "services:prefix:%s".formatted(prefix);

        @SuppressWarnings("unchecked")
        ServiceGroupList cached = (ServiceGroupList) redis.opsForValue().get(key);
        if (cached != null) {
            System.out.println("Using cached Redis result: " + key);
            cachedDTOs = cached.serviceGroupList();
        } else {
            cachedDTOs = serviceGroupRepository.getServicesByPrefix(prefix)
                    .stream()
                    .map(sg -> new ServiceGroupDTO(
                            sg.getRouteShortName(),
                            sg.getRouteLongName(),
                            sg.getTripHeadsign(),
                            sg.getDirectionId(),
                            sg.getRouteColor(),
                            sg.getRouteTextColor()
                    ))
                    .toList();
            redis.opsForValue().set(
                    key,
                    new ServiceGroupList(cachedDTOs),
                    Duration.ofHours(24) // TTL
            );
        }

        return cachedDTOs.stream()
                .map(dto -> DTOtoResponse(dto, lat, lon))
                .toList();
    }

    public List<BriefServiceResponse> getServicesAtStop(String stopId, double lat, double lon) {

        List<ServiceGroupDTO> cachedDTOs;
        String key = "stop:%s:services".formatted(stopId);

        @SuppressWarnings("unchecked")
        ServiceGroupList cached = (ServiceGroupList) redis.opsForValue().get(key);
        if (cached != null) {
            System.out.println("Using cached Redis result: " + key);
            cachedDTOs = cached.serviceGroupList();
        } else {
            cachedDTOs = serviceGroupRepository.getServicesAtStop(stopId)
                    .stream()
                    .map(sg -> new ServiceGroupDTO(
                            sg.getRouteShortName(),
                            sg.getRouteLongName(),
                            sg.getTripHeadsign(),
                            sg.getDirectionId(),
                            sg.getRouteColor(),
                            sg.getRouteTextColor()
                    ))
                    .toList();
            redis.opsForValue().set(
                    key,
                    new ServiceGroupList(cachedDTOs),
                    Duration.ofHours(24) // TTL
            );
        }

        return cachedDTOs.stream()
                .map(dto -> DTOtoResponse(dto, lat, lon))
                .toList();
    }

    public List<BriefServiceResponse> getServicesAtStops(List<String> stopIds, double lat, double lon) {
        if (stopIds == null || stopIds.isEmpty()) return List.of();

        List<String> sortedStopIds = new ArrayList<>(stopIds);
        Collections.sort(sortedStopIds);
        String raw = String.join(",", sortedStopIds);
        String hash = DigestUtils.sha256Hex(raw);
        String key = "stops:services:%s:%.5f:%.5f".formatted(hash, lat, lon);

        List<ServiceGroupDTO> cachedDTOs;

        @SuppressWarnings("unchecked")
        ServiceGroupList cached = (ServiceGroupList) redis.opsForValue().get(key);
        if (cached != null) {
            System.out.println("Using cached Redis result: " + key);
            cachedDTOs = cached.serviceGroupList();
        } else {
            cachedDTOs = serviceGroupRepository.getServicesAtStops(stopIds.toArray(new String[0]), lat, lon)
                    .stream()
                    .map(sg -> new ServiceGroupDTO(
                            sg.getRouteShortName(),
                            sg.getRouteLongName(),
                            sg.getTripHeadsign(),
                            sg.getDirectionId(),
                            sg.getRouteColor(),
                            sg.getRouteTextColor()
                    ))
                    .toList();
            redis.opsForValue().set(
                    key,
                    new ServiceGroupList(cachedDTOs),
                    Duration.ofHours(24) // TTL
            );
        }

        return cachedDTOs.stream()
                .map(dto -> DTOtoResponse(dto, lat, lon))
                .toList();
    }

    private BriefServiceResponse toResponse(
            ServiceGroup serviceGroup,
            double lat,
            double lon) {

        BriefStopResponse adjacentStop = serviceGroupStopLocator.getAdjacentStopForService(
                serviceGroup.getRouteShortName(),
                serviceGroup.getTripHeadsign(),
                serviceGroup.getDirectionId(),
                lat,
                lon
        );
        ArrivalsAtStopResponse arrivalsAtStopResponse = arrivalsService.getNextArrivalsForServiceAtStop(
                adjacentStop.stopId(),
                serviceGroup.getRouteShortName(),
                serviceGroup.getTripHeadsign(),
                serviceGroup.getDirectionId()
        );

        return new BriefServiceResponse(
                new ServiceId(
                        serviceGroup.getRouteShortName(),
                        serviceGroup.getTripHeadsign(),
                        serviceGroup.getDirectionId()
                ),
                serviceGroup.getRouteShortName(),
                serviceGroup.getRouteLongName(),
                arrivalsAtStopResponse
        );
    }

    private BriefServiceResponse DTOtoResponse(
            ServiceGroupDTO dto,
            double lat,
            double lon) {

        BriefStopResponse adjacentStop = serviceGroupStopLocator.getAdjacentStopForService(
                dto.routeShortName(),
                dto.tripHeadsign(),
                dto.directionId(),
                lat,
                lon
        );
        ArrivalsAtStopResponse arrivalsAtStopResponse = arrivalsService.getNextArrivalsForServiceAtStop(
                adjacentStop.stopId(),
                dto.routeShortName(),
                dto.tripHeadsign(),
                dto.directionId()
        );

        return new BriefServiceResponse(
                new ServiceId(
                        dto.routeShortName(),
                        dto.tripHeadsign(),
                        dto.directionId()
                ),
                dto.routeShortName(),
                dto.routeLongName(),
                arrivalsAtStopResponse
        );
    }
}
