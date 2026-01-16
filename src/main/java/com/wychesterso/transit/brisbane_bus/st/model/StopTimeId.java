package com.wychesterso.transit.brisbane_bus.st.model;

import java.io.Serializable;
import java.util.Objects;

public class StopTimeId implements Serializable {

    private String tripId;
    private Integer stopSequence;

    public StopTimeId() {}

    public StopTimeId(String tripId, Integer stopSequence) {
        this.tripId = tripId;
        this.stopSequence = stopSequence;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StopTimeId that)) return false;
        return Objects.equals(tripId, that.tripId)
                && Objects.equals(stopSequence, that.stopSequence);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tripId, stopSequence);
    }
}