package com.bricenangue.nextgeneration.ebuycamer;

import android.net.Uri;

import java.util.Map;

import retrofit2.http.Url;

/**
 * Created by bricenangue on 29/11/2016.
 */

public class User {
   private UserPrivate userPrivate;
    private UserPublic userPublic;



    public User() {
    }

    public UserPrivate getUserPrivate() {
        return userPrivate;
    }

    public void setUserPrivate(UserPrivate userPrivate) {
        this.userPrivate = userPrivate;
    }

    public UserPublic getUserPublic() {
        return userPublic;
    }

    public void setUserPublic(UserPublic userPublic) {
        this.userPublic = userPublic;
    }
}
