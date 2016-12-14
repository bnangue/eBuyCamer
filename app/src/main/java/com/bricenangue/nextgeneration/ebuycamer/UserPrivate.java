package com.bricenangue.nextgeneration.ebuycamer;

import android.net.Uri;

import java.util.Map;

import retrofit2.http.Url;

/**
 * Created by bricenangue on 06/12/2016.
 */

public class UserPrivate {
    private Map<String,String> contacts;
    private Map<String, Boolean> chatrooms;

    public UserPrivate() {
    }

    public Map<String, String> getContacts() {
        return contacts;
    }

    public void setContacts(Map<String, String> contacts) {
        this.contacts = contacts;
    }

    public Map<String, Boolean> getChatrooms() {
        return chatrooms;
    }

    public void setChatrooms(Map<String, Boolean> chatrooms) {
        this.chatrooms = chatrooms;
    }
}
