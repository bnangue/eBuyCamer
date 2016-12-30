package com.bricenangue.nextgeneration.ebuycamer;

/**
 * Created by bricenangue on 29/11/2016.
 */

public class Locations {
    private String name;
    private String geoCoordonne;
    private int numberLocation;
    private String nameCountry;
    private String geoCoordonneCountry;
    private int numberCountry;


    public Locations() {

    }

    public Locations(String name, int numberLocation) {
        this.name = name;
        this.numberLocation=numberLocation;
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

    public int getNumberLocation() {
        return numberLocation;
    }

    public void setNumberLocation(int numberLocation) {
        this.numberLocation = numberLocation;
    }

    public String getNameCountry() {
        return nameCountry;
    }

    public void setNameCountry(String nameCountry) {
        this.nameCountry = nameCountry;
    }

    public String getGeoCoordonneCountry() {
        return geoCoordonneCountry;
    }

    public void setGeoCoordonneCountry(String geoCoordonneCountry) {
        this.geoCoordonneCountry = geoCoordonneCountry;
    }

    public int getNumberCountry() {
        return numberCountry;
    }

    public void setNumberCountry(int numberCountry) {
        this.numberCountry = numberCountry;
    }
}
