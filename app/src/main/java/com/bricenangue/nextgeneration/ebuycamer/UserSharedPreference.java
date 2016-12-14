package com.bricenangue.nextgeneration.ebuycamer;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by bricenangue on 19/11/2016.
 */

public class UserSharedPreference {
    Context context;
    public static final String SP_NAME="userDetails";
    SharedPreferences userLocalDataBase;

    public UserSharedPreference(Context context){
        this.context=context;
        userLocalDataBase=context.getSharedPreferences(SP_NAME,0);
    }
    public void storeUserLocation(String location){
        SharedPreferences.Editor spEditor=userLocalDataBase.edit();
        spEditor.putString("location",location);

        spEditor.apply();
    }

    public String getUserLocation(){
        return userLocalDataBase.getString("location", "");
    }

    public void setUserNumberofAds(long numberAds){
        SharedPreferences.Editor spEditor=userLocalDataBase.edit();
        spEditor.putLong("numberAds",numberAds);

        spEditor.apply();
    }
    public Long getUserNumberofAds(){
        return userLocalDataBase.getLong("numberAds", 0);
    }

    public void reduceNumberofAds(){
        setUserNumberofAds(getUserNumberofAds() - 1);
    }

    public void addNumberofAds(){
        setUserNumberofAds(getUserNumberofAds() + 1);
    }

    public void setEmailVerified(boolean verified){
        SharedPreferences.Editor spEditor=userLocalDataBase.edit();
        spEditor.putBoolean("verified",verified);

        spEditor.apply();
    }


    public boolean getEmailVerified(){
        return userLocalDataBase.getBoolean("verified", false);
    }
}
