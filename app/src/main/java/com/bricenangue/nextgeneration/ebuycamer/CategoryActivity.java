package com.bricenangue.nextgeneration.ebuycamer;

import android.content.DialogInterface;
import android.content.Intent;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        userSharedPreference=new UserSharedPreference(this);
        Bundle extras=getIntent().getExtras();
        if(extras!=null){
            location=extras.getString("locationName");
        }
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
            getSupportActionBar().setTitle(userSharedPreference.getUserLocation());
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

        Button img1=(Button)findViewById(R.id.button_create_new_post_category_activity);
        Button img2=(Button)findViewById(R.id.button_view_favorite_category_activity);
        Button img3=(Button)findViewById(R.id.button_view_my_post_category_activity);
        Button img4=(Button)findViewById(R.id.button_go_to_setting_category_activity);

        img1.setOnClickListener(this);
        img2.setOnClickListener(this);
        img3.setOnClickListener(this);
        img4.setOnClickListener(this);


        toolbar_bottom.setTitle(null);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.button_create_new_post_category_activity:
                if((userSharedPreference.getUserLocation()==null || userSharedPreference.getUserLocation().isEmpty())
                        && location!=null){
                    userSharedPreference.storeUserLocation(location);
                }
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
            case R.id.button_go_to_setting_category_activity:
                startActivity(new Intent(CategoryActivity.this,SettingsActivity.class));

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
                loggout();
                return true;
            case R.id.action_settings_category:
                startActivity(new Intent(CategoryActivity.this,SettingsActivity.class));
                return true;

            case R.id.action_user_profile_category:

                startActivity(new Intent(CategoryActivity.this,UserProfileActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                return true;

            case R.id.action_check_location_category:
                userSharedPreference.storeUserLocation(null);
                startActivity(new Intent(CategoryActivity.this,LocationsActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                finish();
                return true;


        }

        return super.onOptionsItemSelected(item);

    }



    private void loggout() {

        final AlertDialog alertDialog =
                new AlertDialog.Builder(CategoryActivity.this).setMessage(
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

}
