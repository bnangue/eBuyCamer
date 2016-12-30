package com.bricenangue.nextgeneration.ebuycamer;

import android.app.Application;

import com.facebook.FacebookSdk;
import com.firebase.client.Firebase;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

/**
 * Created by bricenangue on 29/11/2016.
 */

public class APP extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
        FacebookSdk.sdkInitialize(this);
      //  FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        //register for app notification topic to receive updates about app
        FirebaseMessaging.getInstance().subscribeToTopic("com.bricenangue.nextgeneration.ebuycamer");

        FirebaseInstanceId.getInstance().getToken();
    }
}
