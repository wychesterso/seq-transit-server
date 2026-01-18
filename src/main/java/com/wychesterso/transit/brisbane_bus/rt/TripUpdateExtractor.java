package com.wychesterso.transit.brisbane_bus.rt;

import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.TripDescriptor;
import com.google.transit.realtime.GtfsRealtime.TripUpdate;
import com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeUpdate;
import com.wychesterso.transit.brisbane_bus.rt.model.RtStopDelay;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TripUpdateExtractor {

    public List<RtStopDelay> extract(FeedMessage feed) {
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

                Integer arrivalDelay = stu.hasArrival()
                        && stu.getArrival().hasDelay()
                        ? stu.getArrival().getDelay()
                        : null;

                Integer departureDelay = stu.hasDeparture()
                        && stu.getDeparture().hasDelay()
                        ? stu.getDeparture().getDelay()
                        : null;

                result.add(new RtStopDelay(
                        tripId,
                        stu.getStopId(),
                        arrivalDelay,
                        departureDelay,
                        false,
                        skipped
                ));
            }
        }

        return result;
    }
}