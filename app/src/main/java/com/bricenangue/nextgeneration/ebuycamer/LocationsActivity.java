package com.bricenangue.nextgeneration.ebuycamer;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LocationsActivity extends AppCompatActivity {



    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private Toolbar toolbar;
    private RecyclerAdapterSmallCards adapterSmallCards;
    ArrayList<String> locations_list =new ArrayList<>();
    private ProgressBar progressBar;

    private FirebaseAuth auth;
    private DatabaseReference root;

    private boolean getuserlocation=false;
    FirebaseUser user;
    private UserSharedPreference userSharedPreference;

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
        setContentView(R.layout.activity_locations);

        userSharedPreference=new UserSharedPreference(this);
        recyclerView=(RecyclerView) findViewById(R.id.recyclerview_locations);

        progressBar=(ProgressBar)findViewById(R.id.progress_bar_location_activity);
        userSharedPreference.setUserDataRefreshed(haveNetworkConnection());
        auth=FirebaseAuth.getInstance();
        if (auth.getCurrentUser()==null){

            if(userSharedPreference.getUserLoggedIn()){
                // user offline
                if(!userSharedPreference.getUserDataRefreshed()){
                    // user refreshed data on start

                }

            }else {
                // user online but auth problem
                Toast.makeText(this,getString(R.string.problem_while_loading_user_data_auth_null),Toast.LENGTH_LONG).show();

                startActivity(new Intent(LocationsActivity.this,MainActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                Intent.FLAG_ACTIVITY_NEW_TASK));
                finish();
            }

        }else {
            user=auth.getCurrentUser();

        }

        Bundle extras=getIntent().getExtras();

        if(extras!=null){
            if (extras.containsKey("user_location")){
                getuserlocation=extras.getBoolean("user_location");
            }
        }

        toolbar=(Toolbar)findViewById(R.id.toolbar_location_activity);
        setSupportActionBar(toolbar);


       int i= userSharedPreference.getUserLocation().getNumberLocation();
        unsubscribeCityTopic(i);
        userSharedPreference.storeUserLocation(null,-1);

        root= FirebaseDatabase.getInstance().getReference();

        recyclerView=(RecyclerView)findViewById(R.id.recyclerview_locations);
        layoutManager=new LinearLayoutManager(this);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        List<String> lines = Arrays.asList(getResources().getStringArray(R.array.locations_array_activity));
        locations_list=new ArrayList<>(lines);
        adapterSmallCards=new RecyclerAdapterSmallCards(this, locations_list,
                new RecyclerAdapterSmallCards.RecyclerAdaptaterCategoryClickListener() {
            @Override
            public void onItemClick(final int position, View v) {


               subscribeCityTopic(position);
                if (!haveNetworkConnection()){
                    // user is off store everything in sharepreference
                    Toast.makeText(getApplicationContext(),getString(R.string.connection_to_server_not_aviable)
                            ,Toast.LENGTH_SHORT).show();

                    if(getuserlocation){
                        // subscribeCityTopic(position);
                        userSharedPreference.storeUserLocation(locations_list.get(position),position);

                        root.child(ConfigApp.FIREBASE_APP_URL_USERS).child(auth.getCurrentUser().getUid())
                                .child("userPublic").child("Location").setValue(new Locations(locations_list.get(position),position));
                        setResult(RESULT_OK,new Intent()
                                .putExtra("position_location",position)
                                .putExtra("user_location",locations_list.get(position)));
                        Toast.makeText(getApplicationContext(),getString(R.string.no_internet_app_not_properly_work),Toast.LENGTH_LONG).show();
                        finish();


                    }else {
                        userSharedPreference.storeUserLocation(locations_list.get(position),position);
                        root.child(ConfigApp.FIREBASE_APP_URL_USERS).child(auth.getCurrentUser().getUid())
                                .child("userPublic").child("Location").setValue(new Locations(locations_list.get(position),position));
                        startActivity(new Intent(LocationsActivity.this,CategoryActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));

                        Toast.makeText(getApplicationContext(),getString(R.string.no_internet_app_not_properly_work),Toast.LENGTH_LONG).show();
                        finish();


                    }


                }else {
                    progressBar.setVisibility(View.VISIBLE);

                    if(getuserlocation){
                       // subscribeCityTopic(position);
                        userSharedPreference.storeUserLocation(locations_list.get(position),position);

                        root.child(ConfigApp.FIREBASE_APP_URL_USERS).child(auth.getCurrentUser().getUid())
                                .child("userPublic").child("Location").setValue(new Locations(locations_list.get(position),position))
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        setResult(RESULT_OK,new Intent()
                                                .putExtra("position_location",position)
                                                .putExtra("user_location",locations_list.get(position)));

                                        progressBar.setVisibility(View.GONE);
                                        finish();
                                    }
                                });

                    }else {
                        userSharedPreference.storeUserLocation(locations_list.get(position),position);
                        root.child(ConfigApp.FIREBASE_APP_URL_USERS).child(auth.getCurrentUser().getUid())
                                .child("userPublic").child("Location").setValue(new Locations(locations_list.get(position),position))
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        progressBar.setVisibility(View.GONE);
                                    }
                                });
                        startActivity(new Intent(LocationsActivity.this,CategoryActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));

                        finish();
                    }
                }
            }
        });

        recyclerView.setAdapter(adapterSmallCards);



    }

    private void subscribeCityTopic(int position){
         FirebaseMessaging
        .getInstance()
         .subscribeToTopic(getString(R.string.fcm_notification_city) + String.valueOf(position));

    }

    private void unsubscribeCityTopic(int position){
        FirebaseMessaging
                .getInstance()
                .unsubscribeFromTopic(getString(R.string.fcm_notification_city) + String.valueOf(position));

    }
    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_locations,menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){

            case R.id.action_logout_location:
                if (!haveNetworkConnection()){
                    Toast.makeText(getApplicationContext(),getString(R.string.connection_to_server_not_aviable)
                            ,Toast.LENGTH_SHORT).show();
                }else {
                    loggout();
                }

                return true;
            case R.id.action_settings_location:
                startActivity(new Intent(LocationsActivity.this,SettingsActivity.class));
                return true;

            case R.id.action_add_new_location:

                return true;


        }

        return super.onOptionsItemSelected(item);

    }


    @Override
    protected void onStop() {
        super.onStop();
        progressBar.setVisibility(View.GONE);
    }

    private void loggout() {

        final AlertDialog alertDialog =
                new AlertDialog.Builder(LocationsActivity.this)
                        .setIcon(getResources().getDrawable(R.drawable.ic_action_shutdown))
                        .setMessage(
                        getString(R.string.alertDialoglogout)+" " +user.getEmail())
                        .create();
        alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.button_cancel)
                , new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        alertDialog.dismiss();

                    }

                });
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.button_logout)
                , new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        auth.signOut();
                        startActivity(new Intent(LocationsActivity.this,MainActivity.class)
                                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        );
                        finish();
                    }
                });
        alertDialog.setCancelable(false);
        alertDialog.show();

    }


}




