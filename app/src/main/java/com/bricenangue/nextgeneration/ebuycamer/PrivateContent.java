package com.bricenangue.nextgeneration.ebuycamer;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by bricenangue on 06/12/2016.
 */

public class PrivateContent {
    private String title;
    private String description;
    private String creatorid;
    private ArrayList<PublicationPhotos> publictionPhotos;
    private String price;
    private String uniquefirebaseId;
    private long timeofCreation;
    private Categories categorie;
    private Locations location;
    private String currency;


    public PrivateContent() {
    }



    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreatorid() {
        return creatorid;
    }

    public void setCreatorid(String creatorid) {
        this.creatorid = creatorid;
    }

    public ArrayList<PublicationPhotos> getPublictionPhotos() {
        return publictionPhotos;
    }

    public void setPublictionPhotos(ArrayList<PublicationPhotos> publictionPhotos) {
        this.publictionPhotos = publictionPhotos;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getUniquefirebaseId() {
        return uniquefirebaseId;
    }

    public void setUniquefirebaseId(String uniquefirebaseId) {
        this.uniquefirebaseId = uniquefirebaseId;
    }

    public long getTimeofCreation() {
        return timeofCreation;
    }

    public void setTimeofCreation(long timeofCreation) {
        this.timeofCreation = timeofCreation;
    }

    public Categories getCategorie() {
        return categorie;
    }

    public void setCategorie(Categories categorie) {
        this.categorie = categorie;
    }

    public Locations getLocation() {
        return location;
    }

    public void setLocation(Locations location) {
        this.location = location;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
