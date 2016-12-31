package com.bricenangue.nextgeneration.ebuycamer;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.login.widget.ProfilePictureView;
import com.firebase.ui.FirebaseUI;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ui.ResultCodes;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private static final int FB_SIGN_IN=0;
    private DatabaseReference root;
    private StorageReference storageRoot;
    private UserSharedPreference userSharedPreference;
    private ProgressBar progressBar;
    private FirebaseUser user;


    public boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
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




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        userSharedPreference=new UserSharedPreference(this);

        progressBar=(ProgressBar)findViewById(R.id.progress_bar_main_activity);

        userSharedPreference.setUserDataRefreshed(haveNetworkConnection());

            if (haveNetworkConnection()){

                showProgressbar();
                GoogleApiAvailability api = GoogleApiAvailability.getInstance();
                int code = api.isGooglePlayServicesAvailable(this);

                if (code == ConnectionResult.SUCCESS) {
                    auth =FirebaseAuth.getInstance();
                    root=FirebaseDatabase.getInstance().getReference();
                    storageRoot=FirebaseStorage.getInstance().getReference();


                    if(auth!=null){
                        showProgressbar();
                        user=auth.getCurrentUser();
                        if(user != null ){
                            //user logged in

                            List<? extends UserInfo> list=user.getProviderData();
                            String providerId=list.get(1).getProviderId();

                            if(providerId.equals(getString(R.string.facebook_provider_id))) {

                                procide(user);

                            }else if (providerId.equals(getString(R.string.password_provider_id))
                                    && user.isEmailVerified()){

                                procide(user);

                            }else if (providerId.equals(getString(R.string.password_provider_id))
                                    && !user.isEmailVerified()) {

                                verifyMail(user);
                            }else {
                                auth.signOut();

                                dismissProgressbar();
                                Toast.makeText(this,getString(R.string.problem_while_loading_user_data_not_identify),Toast.LENGTH_LONG).show();
                                prepareFirebaseUI();
                            }


                        }else {

                            dismissProgressbar();
                            Toast.makeText(this,getString(R.string.problem_while_loading_user_data),Toast.LENGTH_LONG).show();
                            prepareFirebaseUI();
                        }
                    }else {
                        dismissProgressbar();

                        Toast.makeText(this,getString(R.string.problem_while_loading_user_data_auth_null),Toast.LENGTH_LONG).show();
                        prepareFirebaseUI();
                    }

                }else {

                    Dialog dialog = GooglePlayServicesUtil.getErrorDialog(code, this, 0);
                    if (dialog != null) {
                        //This dialog will help the user update to the latest GooglePlayServices
                        dialog.show();
                    }
                    if (new ConfigApp(this).haveNetworkConnection()){
                        if (dialog!=null){
                            dialog.dismiss();
                        }
                    }
                }

            }else {

               if(userSharedPreference.getUserLoggedIn()){
                   noConnectionContinue();
               }else {
                   dismissProgressbar();

                   prepareFirebaseUI();
               }
            }



    }

    private void noConnectionContinue() {

        final AlertDialog alertDialog =
                new AlertDialog.Builder(MainActivity.this).setTitle(
                        getString(R.string.alertDialog_no_internet_connection))
                        .setIcon(getResources().getDrawable(R.drawable.ic_warning_black_24dp))
                        .setMessage(getString(R.string.alertDialog_no_internet_connection_message))
                        .create();

        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.button_continue)
                , new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {


                        Locations user_location=userSharedPreference.getUserLocation();
                        if(user_location!=null){
                            if(!user_location.getName().isEmpty() && user_location.getNumberLocation()!=-1){
                                startActivity(new Intent(MainActivity.this,CategoryActivity.class)
                                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                alertDialog.dismiss();
                                dismissProgressbar();
                                finish();
                            }else {
                                startActivity(new Intent(MainActivity.this,LocationsActivity.class)
                                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                alertDialog.dismiss();
                                dismissProgressbar();
                                finish();
                            }

                        }else {
                            startActivity(new Intent(MainActivity.this,LocationsActivity.class)
                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                            alertDialog.dismiss();
                            dismissProgressbar();
                            finish();
                        }

                    }
                });
        alertDialog.setCancelable(false);
        alertDialog.show();

    }

    private void procide(final FirebaseUser user) {
        final DatabaseReference ref=root.child(ConfigApp.FIREBASE_APP_URL_USERS).
                child(user.getUid()).child("userPublic")
                ;
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                userSharedPreference.setUserNumberofAds(dataSnapshot
                        .child("numberOfAds").getValue(long.class));

                userSharedPreference.storeUserData(dataSnapshot.getValue(UserPublic.class));
                userSharedPreference.setUserLoggedIn(true);
                userSharedPreference.setUserDataRefreshed(true);

                userSharedPreference.setEmailVerified(user.isEmailVerified());


                if (dataSnapshot.hasChild("Location")){

                    userSharedPreference.storeUserLocation(dataSnapshot.child("Location")
                            .getValue(Locations.class).getName()
                            , dataSnapshot.child("Location").getValue(Locations.class).getNumberLocation());
                    startActivity(new Intent(MainActivity.this,CategoryActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                    finish();
                    dismissProgressbar();

                }else {

                    startActivity(new Intent(MainActivity.this,LocationsActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                    finish();
                    dismissProgressbar();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                dismissProgressbar();
                Toast.makeText(getApplicationContext(),databaseError.getMessage()
                ,Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void verifyMail(final FirebaseUser user) {

        final AlertDialog alertDialog =
                new AlertDialog.Builder(MainActivity.this).setMessage(
                        getString(R.string.alertDialogverifyemail)+" " +user.getEmail())
                        .create();
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "OK"
                , new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //user.sendEmailVerification();
                        user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(getApplicationContext()
                                        ,getString(R.string.problem_while_loading_user_data_verifaction_email_resent)
                                                +" "+ user.getEmail(),Toast.LENGTH_LONG).show();
                            }
                        });

                       procide(user);

                        alertDialog.dismiss();
                        dismissProgressbar();

                    }

                });
        alertDialog.setCancelable(false);
        alertDialog.show();

    }
    private void saveprofilePicture(FirebaseUser user){
        String facebookUserId = "";
        // find the Facebook profile and get the user's id
        List<? extends UserInfo> list=user.getProviderData();
        String providerId=list.get(1).getProviderId();

            // check if the provider id matches "facebook.com"
            if(providerId.equals(getString(R.string.facebook_provider_id))) {
                facebookUserId = list.get(1).getUid();
            }

        // construct the URL to the profile picture, with a custom height
        // alternatively, use '?type=small|medium|large' instead of ?height=
        final String photoUrl = "https://graph.facebook.com/" + facebookUserId + "/picture?type=large";
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference();
        reference.child(ConfigApp.FIREBASE_APP_URL_USERS).child(user.getUid())
                .child("userPublic").child("profilePhotoUri").setValue(photoUrl);

    }


    void prepareFirebaseUI(){
        startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder()
                .setProviders(AuthUI.EMAIL_PROVIDER,
                        AuthUI.FACEBOOK_PROVIDER)
                .setIsSmartLockEnabled(!BuildConfig.DEBUG)
                .setTheme(R.style.AppTheme)
                .build(),FB_SIGN_IN);

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==FB_SIGN_IN){
            if(resultCode==RESULT_OK){

                boolean isfacebook = false;
                boolean ispassword =false;
                final FirebaseUser user=auth.getCurrentUser();

                if(user!=null){


                    List<? extends UserInfo> list=user.getProviderData();
                    String providerId=list.get(1).getProviderId();
                    if(providerId.equals(getString(R.string.facebook_provider_id))) {
                        isfacebook=true;

                    }else if (providerId.equals(getString(R.string.password_provider_id))){
                        ispassword=true;
                    }

                    final String email=user.getEmail();
                    final String uid=user.getUid();
                    final String name = user.getDisplayName();


                    final DatabaseReference ref =root.child(ConfigApp.FIREBASE_APP_URL_USERS)
                            .child(uid);
                    final UserPublic userfb=new UserPublic();

                    final boolean emailVerified=user.isEmailVerified();
                    userSharedPreference.setEmailVerified(emailVerified);
                    userfb.setEmail(email);

                    userfb.setName(name);

                    userfb.setUniquefirebasebId(uid);

                    Map<String,Object> children=new HashMap<>();

                    DatabaseReference refAds=root.child(ConfigApp.FIREBASE_APP_URL_USERS_POSTS_USER).child(user.getUid());
                    final boolean finalIsfacebook = isfacebook;
                    final boolean finalIspassword = ispassword;
                    refAds.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            //user already register
                            if(dataSnapshot.hasChildren()){

                                userfb.setNumberOfAds(dataSnapshot.getChildrenCount());
                                userSharedPreference.setUserNumberofAds(dataSnapshot.getChildrenCount());

                                final DatabaseReference refUSerPublice=ref.child("userPublic");

                                Map<String,Object> children=new HashMap<>();
                                children.put("/email",userfb.getEmail());
                                children.put("/name",userfb.getName());
                                children.put("/numberOfAds",userfb.getNumberOfAds());
                                children.put("/uniquefirebasebId",userfb.getUniquefirebasebId());
                                children.put("/chatId",FirebaseInstanceId.getInstance().getToken());

                                refUSerPublice.updateChildren(children).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isComplete() && task.isSuccessful()){
                                            if(finalIsfacebook){
                                                procide(user);
                                                saveprofilePicture(user);


                                            }else if ( !emailVerified && finalIspassword){
                                                user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        verifyMail(user);

                                                    }
                                                });
                                            }else if (emailVerified && finalIspassword ){
                                                procide(user);
                                            }
                                        }else {
                                            showErrorSignInAndRelaunch(getString(R.string.Error_while_updating_user_profile_information));
                                            dismissProgressbar();

                                        }
                                    }
                                });

                            }else {
                                userfb.setNumberOfAds(0);
                                userSharedPreference.setUserNumberofAds(0);
                                final DatabaseReference refUSerPublice=ref.child("userPublic");
                                Map<String,Object> children=new HashMap<>();
                                children.put("/email",userfb.getEmail());
                                children.put("/name",userfb.getName());
                                children.put("/numberOfAds",userfb.getNumberOfAds());
                                children.put("/uniquefirebasebId",userfb.getUniquefirebasebId());
                                children.put("/chatId",FirebaseInstanceId.getInstance().getToken());

                                refUSerPublice.updateChildren(children).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isComplete() && task.isSuccessful()){
                                            if(finalIsfacebook){
                                                procide(user);
                                                saveprofilePicture(user);


                                            }else if ( !emailVerified && finalIspassword){
                                                verifyMail(user);

                                            }else if (emailVerified && finalIspassword ){
                                                procide(user);
                                            }
                                        }else {
                                            showErrorSignInAndRelaunch(getString(R.string.Error_while_updating_user_profile_information));
                                            dismissProgressbar();
                                        }
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            showErrorSignInAndRelaunch(databaseError.getMessage()+" 1");
                            dismissProgressbar();
                        }
                    });




                }else {
                    dismissProgressbar();
                    Toast.makeText(this,getString(R.string.problem_while_loading_user_data),Toast.LENGTH_LONG).show();
                    prepareFirebaseUI();
                }



            }
            // Sign in canceled
            if (resultCode == RESULT_CANCELED) {

                Toast.makeText(getApplicationContext()
                        ,getString(R.string.connection_to_server_cancelled)
                        ,Toast.LENGTH_SHORT).show();

                if(!haveNetworkConnection()){
                    noConnectionContinue();
                }else {
                    prepareFirebaseUI();
                    dismissProgressbar();
                }
                return;
            }

            // No network
            if (resultCode == ResultCodes.RESULT_NO_NETWORK) {
                if(!haveNetworkConnection()){
                    noConnectionContinue();
                }else {
                    dismissProgressbar();
                }
                showErrorSignInAndRelaunch("result no network");
                return;
            }

        }
    }


    private void showErrorSignInAndRelaunch(String message){
       // getString(R.string.mainpage_alertdialog_signin_request_false)
        Toast.makeText(getApplicationContext()
                ,message
                ,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        this.finishAffinity();
        System.exit(0);

    }
/**
    @Override
    public void setUsername(final String username) {
        final FirebaseUser user = auth.getCurrentUser();
        if(user!=null){
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(username).build();
            user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        user.reload();
                        user.sendEmailVerification()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(getApplicationContext()
                                                    ,getString(R.string.string_toast_text_success) + " email",Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                        Toast.makeText(getApplicationContext()
                                ,getString(R.string.string_toast_text_success) + " displayname",Toast.LENGTH_SHORT).show();

                        final String email=user.getEmail();
                        final String uid=user.getUid();
                        final String name = username;


                        final DatabaseReference ref =root.child(ConfigApp.FIREBASE_APP_URL_USERS)
                                .child(uid);
                        final UserPublic userfb=new UserPublic();

                        userfb.setEmail(email);
                        userfb.setName(name);

                        userfb.setUniquefirebasebId(uid);

                        DatabaseReference refAds=root.child(ConfigApp.FIREBASE_APP_URL_USERS_POSTS_USER).child(user.getUid());
                        refAds.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.hasChildren()){
                                    userfb.setNumberOfAds(dataSnapshot.getChildrenCount());
                                    userSharedPreference.setUserNumberofAds(dataSnapshot.getChildrenCount());
                                    ref.child("userPublic").setValue(userfb).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Toast.makeText(getApplicationContext(),
                                                        getString(R.string.mainpage_toast_loggingAs)+" "+ auth.getCurrentUser().getEmail()
                                                        ,Toast.LENGTH_SHORT).show();

                                                startActivity(new Intent(MainActivity.this,LocationsActivity.class)
                                                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                                finish();
                                                if (progressBar!=null){
                                                    progressBar.dismiss();
                                                }
                                            }else {
                                                if (progressBar!=null){
                                                    progressBar.dismiss();
                                                }
                                            }
                                        }
                                    });
                                }else {
                                    userfb.setNumberOfAds(0);
                                    userSharedPreference.setUserNumberofAds(0);
                                    ref.child("userPublic").setValue(userfb).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Toast.makeText(getApplicationContext(),
                                                        getString(R.string.mainpage_toast_loggingAs)+" "+ auth.getCurrentUser().getEmail()
                                                        ,Toast.LENGTH_SHORT).show();

                                                startActivity(new Intent(MainActivity.this,LocationsActivity.class)
                                                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                                finish();
                                                if (progressBar!=null){
                                                    progressBar.dismiss();
                                                }
                                            }else {
                                                if (progressBar!=null){
                                                    progressBar.dismiss();
                                                }
                                            }
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                if (progressBar!=null){
                                    progressBar.dismiss();
                                }
                            }
                        });
                    }else {
                        if (progressBar!=null){
                            progressBar.dismiss();
                        }
                        Toast.makeText(getApplicationContext()
                                ,getString(R.string.string_toast_text_error) + " displayname",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

**/
    @Override
    protected void onStop() {
        super.onStop();
       dismissProgressbar();
    }

    private void showProgressbar(){
        progressBar.setVisibility(View.VISIBLE);
    }

    private void dismissProgressbar(){
        if (progressBar!=null){
            progressBar.setVisibility(View.GONE);
        }
    }
    @Override
    protected void onStart() {
        super.onStart();

    }


}

