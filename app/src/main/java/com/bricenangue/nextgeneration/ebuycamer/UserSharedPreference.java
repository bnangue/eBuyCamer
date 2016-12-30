package com.bricenangue.nextgeneration.ebuycamer;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by bricenangue on 19/11/2016.
 */

public class UserSharedPreference {
    Context context;
    public static final String SP_NAME="userDetails";
    private SharedPreferences userLocalDataBase;

    public UserSharedPreference(Context context){
        this.context=context;
        userLocalDataBase=context.getSharedPreferences(SP_NAME,0);
    }
    public void storeUserLocation(String location, int locationnumber){
        SharedPreferences.Editor spEditor=userLocalDataBase.edit();
        spEditor.putString("location",location);
        spEditor.putInt("locationnumber",locationnumber);

        spEditor.apply();
    }

    public Locations getUserLocation(){
        String location=userLocalDataBase.getString("location", "");
        int number=userLocalDataBase.getInt("locationnumber",-1);

        return new Locations(location,number);

    }


    public void setUserNumberofAds(long numberAds){
        SharedPreferences.Editor spEditor=userLocalDataBase.edit();
        if(numberAds<0){
            spEditor.putLong("numberAds",0);

        }else {
            spEditor.putLong("numberAds",numberAds);

        }

        spEditor.apply();
    }
    public Long getUserNumberofAds(){
        return userLocalDataBase.getLong("numberAds", 0);
    }

    public void reduceNumberofAds(){
        if(getUserNumberofAds()<=0){
            setUserNumberofAds(0);
        }else {
            setUserNumberofAds(getUserNumberofAds() - 1);
        }

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


    public void storeUserData(UserPublic user){
        SharedPreferences.Editor spEditor=userLocalDataBase.edit();
        spEditor.putString("email",user.getEmail());
        if (user.getPhoneNumber()!=null){
            spEditor.putString("code",user.getPhoneNumber().getCode());
            spEditor.putString("phonenumber",user.getPhoneNumber().getPhoneNumber());
        }
        spEditor.putString("fullname",user.getName());
        spEditor.putString("pictureuri",user.getProfilePhotoUri());
        spEditor.apply();
    }

    public User getLoggedInUser(){
        String email=userLocalDataBase.getString("email", "");
        String phonenumber=userLocalDataBase.getString("phonenumber","");
        String code=userLocalDataBase.getString("code","");
        String fullname=userLocalDataBase.getString("fullname", "");
        String pictureuri=userLocalDataBase.getString("pictureuri","");

        User user=new User();
        UserPublic userPublic=new UserPublic();
        userPublic.setEmail(email);
        userPublic.setPhoneNumber(new PhoneNumber(code,phonenumber));
        userPublic.setName(fullname);
        userPublic.setProfilePhotoUri(pictureuri);
        user.setUserPublic(userPublic);

        return user;
    }

    public void clearUserData(){
        SharedPreferences.Editor spEditor=userLocalDataBase.edit();
        spEditor.clear();
        spEditor.apply();
    }

    //call with true if logged in
    public void setUserLoggedIn(boolean loggedIn){
        SharedPreferences.Editor spEditor=userLocalDataBase.edit();
        spEditor.putBoolean("loggedIn", loggedIn);
        spEditor.apply();

    }

    public boolean getUserLoggedIn(){
        return userLocalDataBase.getBoolean("loggedIn", false);
    }
}
