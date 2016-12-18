package com.bricenangue.nextgeneration.ebuycamer;

import android.app.Application;

import com.facebook.FacebookSdk;
import com.firebase.client.Firebase;
import com.google.firebase.FirebaseApp;

/**
 * Created by bricenangue on 29/11/2016.
 */

public class APP extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
        FacebookSdk.sdkInitialize(this);

    }
}
