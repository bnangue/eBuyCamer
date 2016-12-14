package com.bricenangue.nextgeneration.ebuycamer;

/**
 * Created by bricenangue on 29/11/2016.
 */

public class Locations {
    private String name;
    private String geoCoordonne;

    public Locations() {

    }

    public Locations(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGeoCoordonne() {
        return geoCoordonne;
    }

    public void setGeoCoordonne(String geoCoordonne) {
        this.geoCoordonne = geoCoordonne;
    }
}
