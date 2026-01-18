package com.wychesterso.transit.brisbane_bus.rt;

import com.google.transit.realtime.GtfsRealtime;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Responsible for holding the most recent, consistent GTFS-RT feed
 */
@Component
public class GtfsRtSnapshot {

    private final AtomicReference<FeedMessage> snapshot =
            new AtomicReference<>();

    public void update(FeedMessage feed) {
        snapshot.set(feed);
    }

    public Optional<FeedMessage> get() {
        return Optional.ofNullable(snapshot.get());
    }
}