package com.wychesterso.transit.brisbane_bus.api.repository.dto;

import jakarta.persistence.*;

@Entity
@Table(name = "stop_times")
@IdClass(StopTimeId.class)
public class StopTime {

    @Id
    @Column(name = "trip_id")
    private String tripId;

    @Column(name = "arrival_time")
    private Integer arrivalTime;

    @Column(name = "departure_time")
    private Integer departureTime;

    @Column(name = "stop_id")
    private String stopId;

    @Id
    @Column(name = "stop_sequence")
    private Integer stopSequence;

    @Column(name = "pickup_type")
    private Integer pickupType;

    @Column(name = "dropoff_type")
    private Integer dropoffType;

    protected StopTime() {}

    public StopTime(
            String tripId,
            Integer arrivalTime,
            Integer departureTime,
            String stopId,
            Integer stopSequence,
            Integer pickupType,
            Integer dropoffType
    ) {
        this.tripId = tripId;
        this.arrivalTime = arrivalTime;
        this.departureTime = departureTime;
        this.stopId = stopId;
        this.stopSequence = stopSequence;
        this.pickupType = pickupType;
        this.dropoffType = dropoffType;
    }

    public String getTripId() { return tripId; }
    public Integer getArrivalTime() { return arrivalTime; }
    public Integer getDepartureTime() { return departureTime; }
    public String getStopId() { return stopId; }
    public Integer getStopSequence() { return stopSequence; }
    public Integer getPickupType() { return pickupType; }
    public Integer getDropoffType() { return dropoffType; }
}