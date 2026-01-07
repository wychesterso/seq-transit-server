package com.wychesterso.transit.brisbane_bus.model;

import jakarta.persistence.*;

@Entity
@Table(name = "routes")
public class Route {

    @Id
    @Column(name = "route_id")
    private String routeId;

    @Column(name = "route_short_name")
    private String routeShortName;

    @Column(name = "route_long_name")
    private String routeLongName;

    @Column(name = "route_desc")
    private String routeDesc;

    @Column(name = "route_type")
    private Integer routeType;

    @Column(name = "route_url")
    private String routeUrl;

    @Column(name = "route_color")
    private String routeColor;

    @Column(name = "route_text_color")
    private String routeTextColor;

    protected Route() {}

    public Route(
            String routeId,
            String routeShortName,
            String routeLongName,
            String routeDesc,
            Integer routeType,
            String routeUrl,
            String routeColor,
            String routeTextColor
    ) {
        this.routeId = routeId;
        this.routeShortName = routeShortName;
        this.routeLongName = routeLongName;
        this.routeDesc = routeDesc;
        this.routeType = routeType;
        this.routeUrl = routeUrl;
        this.routeColor = routeColor;
        this.routeTextColor = routeTextColor;
    }

    public String getRouteId() { return routeId; }
    public String getRouteShortName() { return routeShortName; }
    public String getRouteLongName() { return routeLongName; }
    public String getRouteDesc() { return routeDesc; }
    public Integer getRouteType() { return routeType; }
    public String getRouteUrl() { return routeUrl; }
    public String getRouteColor() { return routeColor; }
    public String getRouteTextColor() { return routeTextColor; }

}