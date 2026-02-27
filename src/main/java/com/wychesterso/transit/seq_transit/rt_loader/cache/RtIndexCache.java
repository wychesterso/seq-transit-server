package com.wychesterso.transit.seq_transit.rt_loader.cache;

import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.wychesterso.transit.seq_transit.api.service.dto.TripStopKey;
import com.wychesterso.transit.seq_transit.core.model.RtStopDelay;
import com.wychesterso.transit.seq_transit.rt_loader.importer.RtFetcher;
import com.wychesterso.transit.seq_transit.rt_loader.service.RtIndexBuilder;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class RtIndexCache {

    private static final Duration TTL = Duration.ofSeconds(60);

    private final RtFetcher fetcher;
    private final RtIndexBuilder builder;

    private volatile Map<TripStopKey, RtStopDelay> index = Map.of();
    private volatile Instant lastUpdate = Instant.EPOCH;

    private final AtomicBoolean refreshInProgress = new AtomicBoolean(false);
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final Object lock = new Object();

    public RtIndexCache(
            RtFetcher fetcher,
            RtIndexBuilder builder) {
        this.fetcher = fetcher;
        this.builder = builder;
    }

    public Map<TripStopKey, RtStopDelay> getIndex() {

        Instant now = Instant.now();

        boolean stale = now.isAfter(lastUpdate.plus(TTL));

        // block on first startup
        if (index.isEmpty()) {
            refreshBlocking();
            return index;
        }

        if (stale) triggerAsyncRefresh();

        // ensure immediate return
        return index;
    }

    private void triggerAsyncRefresh() {
        // only one refresh allowed
        if (!refreshInProgress.compareAndSet(false, true)) return;

        executor.submit(() -> {
            try {
                refreshBlocking();
            } finally {
                refreshInProgress.set(false);
            }
        });
    }

    private void refreshBlocking() {
        try {
            FeedMessage feed = fetcher.fetchTripUpdates();
            index = builder.build(feed);
            lastUpdate = Instant.now();
        } catch (Exception e) {
            // keep old cache if refresh fails
            LoggerFactory.getLogger(getClass()).warn("RT refresh failed, keeping stale cache", e);
        }
    }
}
