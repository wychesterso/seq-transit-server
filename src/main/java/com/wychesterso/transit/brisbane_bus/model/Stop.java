package com.wychesterso.transit.brisbane_bus.model;

import jakarta.persistence.*;

@Entity
@Table(name = "stops")
public class Stop {

    @Id
    @Column(name = "stop_id")
    private String stopId;

    @Column(name = "stop_code")
    private String stopCode;

    @Column(name = "stop_name")
    private String stopName;

    @Column(name = "stop_desc")
    private String stopDesc;

    @Column(name = "stop_lat")
    private Double stopLat;

    @Column(name = "stop_lon")
    private Double stopLon;

    @Column(name = "zone_id")
    private String zoneId;

    @Column(name = "stop_url")
    private String stopUrl;

    @Column(name = "location_type")
    private Integer locationType;

    @Column(name = "parent_station")
    private String parentStation;

    @Column(name = "platform_code")
    private String platformCode;

    protected Stop() {};

    public Stop(
            String stopId,
            String stopCode,
            String stopName,
            String stopDesc,
            Double stopLat,
            Double stopLon,
            String zoneId,
            String stopUrl,
            Integer locationType,
            String parentStation,
            String platformCode
    ) {
        this.stopId = stopId;
        this.stopCode = stopCode;
        this.stopName = stopName;
        this.stopDesc = stopDesc;
        this.stopLat = stopLat;
        this.stopLon = stopLon;
        this.zoneId = zoneId;
        this.stopUrl = stopUrl;
        this.locationType = locationType;
        this.parentStation = parentStation;
        this.platformCode = platformCode;
    }

    public String getStopId() { return stopId; }
    public String getStopCode() { return stopCode; }
    public String getStopName() { return stopName; }
    public String getStopDesc() { return stopDesc; }
    public Double getStopLat() { return stopLat; }
    public Double getStopLon() { return stopLon; }
    public String getZoneId() { return zoneId; }
    public String getStopUrl() { return stopUrl; }
    public Integer getLocationType() { return locationType; }
    public String getParentStation() { return parentStation; }
    public String getPlatformCode() { return platformCode; }

}