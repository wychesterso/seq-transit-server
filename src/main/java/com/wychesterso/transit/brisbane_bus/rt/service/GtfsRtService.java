package com.wychesterso.transit.brisbane_bus.rt.service;

import com.wychesterso.transit.brisbane_bus.rt.GtfsRtSnapshot;
import com.wychesterso.transit.brisbane_bus.rt.TripUpdateExtractor;
import com.wychesterso.transit.brisbane_bus.rt.model.RtStopDelay;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GtfsRtService {

    private final GtfsRtSnapshot snapshot;
    private final TripUpdateExtractor extractor;

    public GtfsRtService(
            GtfsRtSnapshot snapshot,
            TripUpdateExtractor extractor) {
        this.snapshot = snapshot;
        this.extractor = extractor;
    }

    public List<RtStopDelay> getCurrentDelays() {
        return snapshot.get()
                .map(extractor::extract)
                .orElse(List.of());
    }
}
