package com.bricenangue.nextgeneration.ebuycamer;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CategoryActivity extends AppCompatActivity implements View.OnClickListener {

    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private TextView instruction;
    private Toolbar toolbar,toolbar_bottom;
    private RecyclerAdapterSmallCards adapterSmallCards;
    private FirebaseAuth auth;
    private String location;
    private ArrayList<String> categories =new ArrayList<>();
    private UserSharedPreference userSharedPreference;
    private FirebaseUser user;
    private static final int LOCATION_INTENT=3;

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
        setContentView(R.layout.activity_category);

        userSharedPreference=new UserSharedPreference(this);
        Bundle extras=getIntent().getExtras();
        if(extras!=null){
            location=extras.getString("locationName");

        }
        if(location==null || location.isEmpty()){
            location=userSharedPreference.getUserLocation().getName();
        }
        if((location==null || location.isEmpty())){

            //startactivity for result and update location in preference
            startActivityForResult(new Intent(CategoryActivity.this,LocationsActivity.class)
                    .putExtra("user_location",true),LOCATION_INTENT);

        }
        if((userSharedPreference.getLoggedInUser().getUserPublic().getPhoneNumber().getPhoneNumber()==null
                || userSharedPreference.getLoggedInUser().getUserPublic().getPhoneNumber().getPhoneNumber().isEmpty())){

            //ask user to add a phoneNumber
            changePhoneNumber();

        }

        FirebaseMessaging.getInstance().subscribeToTopic(getString(R.string.fcm_notification_city)
        +String.valueOf(userSharedPreference.getUserLocation().getNumberLocation()));
        auth=FirebaseAuth.getInstance();
        if(auth==null){
            startActivity(new Intent(CategoryActivity.this,MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            finish();
        }else {
            user=auth.getCurrentUser();
        }

        toolbar=(Toolbar)findViewById(R.id.toolbar_category_activity);
        setSupportActionBar(toolbar);

        if(location!=null && !location.isEmpty()){
            getSupportActionBar().setTitle(location);
        }else {
            getSupportActionBar().setTitle(userSharedPreference.getUserLocation().getName());
        }


        toolbar_bottom=(Toolbar)findViewById(R.id.toolbar_bottom_actegory_activity);

        recyclerView=(RecyclerView)findViewById(R.id.recyclerview_category);
        layoutManager=new LinearLayoutManager(this);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        List<String> lines = Arrays.asList(getResources().getStringArray(R.array.categories_array_activity));
        categories=new ArrayList<>(lines);
        adapterSmallCards=new RecyclerAdapterSmallCards(this, categories, new RecyclerAdapterSmallCards.RecyclerAdaptaterCategoryClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                startActivity(new Intent(CategoryActivity.this,MainPageActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        .putExtra("locationName",location).putExtra("category",categories.get(position))
                .putExtra("position",position));
                finish();
            }
        });

        recyclerView.setAdapter(adapterSmallCards);

        ImageButton img1=(ImageButton)findViewById(R.id.button_create_new_post_category_activity);
        ImageButton img2=(ImageButton)findViewById(R.id.button_view_favorite_category_activity);
        ImageButton img3=(ImageButton)findViewById(R.id.button_view_my_post_category_activity);
        ImageButton img4=(ImageButton)findViewById(R.id.button_go_to_chatting_activity);
        ImageButton img5=(ImageButton)findViewById(R.id.button_go_to_sales_activity);

        img1.setOnClickListener(this);
        img2.setOnClickListener(this);
        img3.setOnClickListener(this);
        img4.setOnClickListener(this);
        img5.setOnClickListener(this);

        toolbar_bottom.setTitle(null);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.button_create_new_post_category_activity:

                startActivity(new Intent(CategoryActivity.this,CreateAndModifyPublicationActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                break;
            case R.id.button_view_favorite_category_activity:
                startActivity(new Intent(CategoryActivity.this,MyFavoritesActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                break;
            case R.id.button_view_my_post_category_activity:
                startActivity(new Intent(CategoryActivity.this,MyPostActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                break;
            case R.id.button_go_to_chatting_activity:
                startActivity(new Intent(CategoryActivity.this,MyChatActivity.class));

                break;
            case R.id.button_go_to_sales_activity:
                startActivity(new Intent(CategoryActivity.this,ViewDealsActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_category_activity,menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){

            case R.id.action_logout_category:
                if(haveNetworkConnection()){
                    loggout();
                }else {
                    Toast.makeText(getApplicationContext()
                            ,getString(R.string.connection_to_server_not_aviable)
                            ,Toast.LENGTH_SHORT).show();
                }

                return true;
            case R.id.action_settings_category:
               startActivity(new Intent(CategoryActivity.this,SettingsActivity.class));
                return true;

            case R.id.action_user_profile_category:

                startActivity(new Intent(CategoryActivity.this,UserProfileActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                return true;

            case R.id.action_check_location_category:

                startActivity(new Intent(CategoryActivity.this,LocationsActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                finish();
                return true;


        }

        return super.onOptionsItemSelected(item);

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

     if (requestCode == LOCATION_INTENT && resultCode==RESULT_OK){
            String location=data.getExtras().getString("user_location");
         int  position=data.getExtras().getInt("position_location");
            getSupportActionBar().setTitle(location);
            UserSharedPreference preference =new UserSharedPreference(getApplicationContext());
            preference.storeUserLocation(location,position);

        }
    }
    private void loggout() {

        final AlertDialog alertDialog =
                new AlertDialog.Builder(CategoryActivity.this)
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
                        userSharedPreference.clearUserData();
                        startActivity(new Intent(CategoryActivity.this,MainActivity.class)
                                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                        Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                        Intent.FLAG_ACTIVITY_NEW_TASK)
                               );
                        finish();
                    }
                });
        alertDialog.setCancelable(false);
        alertDialog.show();

    }

    private void changePhoneNumber() {

        final AlertDialog alertDialog =
                new AlertDialog.Builder(CategoryActivity.this).setTitle(
                        getString(R.string.alertDialog_change_phone_number))
                        .setMessage(getString(R.string.alertDialog_change_phone_number_message))
                        .setIcon(getResources().getDrawable(R.drawable.ic_phone_black_24dp))
                        .create();
        alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.button_later)
                , new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        alertDialog.dismiss();

                    }

                });
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.button_add)
                , new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        alertDialog.dismiss();
                        startActivity(new Intent(CategoryActivity.this,ChangePhoneNumberActivity.class)
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

                    }
                });
        alertDialog.setCancelable(false);
        alertDialog.show();

    }

}
