package com.wychesterso.transit.brisbane_bus.rt;

import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.TripDescriptor;
import com.google.transit.realtime.GtfsRealtime.TripUpdate;
import com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeUpdate;
import com.wychesterso.transit.brisbane_bus.rt.model.RtStopDelay;
import org.springframework.stereotype.Component;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class TripUpdateExtractor {

    private static final ZoneId BRISBANE = ZoneId.of("Australia/Brisbane");

    public List<RtStopDelay> extract(
            FeedMessage feed,
            LocalDate serviceDate
    ) {
        List<RtStopDelay> result = new ArrayList<>();

        for (FeedEntity entity : feed.getEntityList()) {
            if (!entity.hasTripUpdate()) continue;

            TripUpdate tu = entity.getTripUpdate();
            TripDescriptor trip = tu.getTrip();

            String tripId = trip.getTripId();

            // trip cancelled
            if (trip.getScheduleRelationship() ==
                    TripDescriptor.ScheduleRelationship.CANCELED) {

                for (StopTimeUpdate stu : tu.getStopTimeUpdateList()) {
                    result.add(new RtStopDelay(
                            tripId,
                            stu.getStopId(),
                            null,
                            null,
                            true,
                            false
                    ));
                }
                continue;
            }

            // per-stop updates
            for (StopTimeUpdate stu : tu.getStopTimeUpdateList()) {
                boolean skipped =
                        stu.getScheduleRelationship() ==
                                StopTimeUpdate.ScheduleRelationship.SKIPPED;

                Integer effectiveArr = getEffectiveSeconds(
                        stu.hasArrival() ? stu.getArrival().getTime() : null,
                        serviceDate
                );

                Integer effectiveDep = getEffectiveSeconds(
                        stu.hasDeparture() ? stu.getDeparture().getTime() : null,
                        serviceDate
                );

                result.add(new RtStopDelay(
                        tripId,
                        stu.getStopId(),
                        effectiveArr,
                        effectiveDep,
                        false,
                        skipped
                ));
            }
        }

        return result;
    }

    private Integer getEffectiveSeconds(
            Long epochSeconds,
            LocalDate serviceDate
    ) {
        if (epochSeconds == null || epochSeconds == 0) return null;

        ZonedDateTime localTime = Instant.ofEpochSecond(epochSeconds)
                        .atZone(BRISBANE);

        return Math.toIntExact(
                Duration.between(
                        serviceDate.atStartOfDay(),
                        localTime.toLocalDateTime()
                ).getSeconds()
        );
    }
}