package com.bricenangue.nextgeneration.ebuycamer;

import android.net.Uri;

import java.util.Map;

import retrofit2.http.Url;

/**
 * Created by bricenangue on 06/12/2016.
 */

public class UserPublic {
    private String name;
    private String email;
    private Locations Location;
    private String chatId;
    private String uniquefirebasebId;
    private String profilePhotoUri;
    private Map<String,Url> userLinks;
    private long numberOfAds;
    private String phoneNumber;

    public UserPublic() {


    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Locations getLocation() {
        return Location;
    }

    public void setLocation(Locations location) {
        Location = location;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getUniquefirebasebId() {
        return uniquefirebasebId;
    }

    public void setUniquefirebasebId(String uniquefirebasebId) {
        this.uniquefirebasebId = uniquefirebasebId;
    }

    public String getProfilePhotoUri() {
        return profilePhotoUri;
    }

    public void setProfilePhotoUri(String profilePhotoUri) {
        this.profilePhotoUri = profilePhotoUri;
    }

    public Map<String, Url> getUserLinks() {
        return userLinks;
    }

    public void setUserLinks(Map<String, Url> userLinks) {
        this.userLinks = userLinks;
    }

    public long getNumberOfAds() {
        return numberOfAds;
    }

    public void setNumberOfAds(long numberOfAds) {
        this.numberOfAds = numberOfAds;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}

