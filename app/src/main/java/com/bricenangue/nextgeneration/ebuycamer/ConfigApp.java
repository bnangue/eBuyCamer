package com.bricenangue.nextgeneration.ebuycamer;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by bricenangue on 19/11/2016.
 */

public class ConfigApp {

    public static final String FIREBASE_APP_URL_USERS_POSTS = "Posts";
    public static final String FIREBASE_APP_URL_USERS_POSTS_USER = "User-Posts";
    public static final String FIREBASE_APP_URL_USERS_FAVORITES_USER = "User-Favorites";
    public static final String FIREBASE_APP_URL_USERS_FAVORITES = "Favorites";
    public static final String FIREBASE_APP_URL_POSTS_EXIST = "Posts-Exits";
    static final String FIREBASE_APP_URL_REGIONS = "Cities";
    static final String FIREBASE_APP_URL_USERS = "Users";



    private Context context;
    static final String FIREBASE_APP_URL_STORAGE_USER_PROFILE_PICTURE = "/Photo/profilePicture";

    static  final String FIREBASE_APP_URL_STORAGE_USER_PROFILES= "USERS";

    public ConfigApp(Context context){
        this.context=context;
    }
    public boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if ( "WIFI".equals(ni.getTypeName()))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if ("MOBILE".equals(ni.getTypeName()))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }


}
