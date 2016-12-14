package com.bricenangue.nextgeneration.ebuycamer;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LocationsActivity extends AppCompatActivity implements View.OnClickListener {



    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private TextView instruction;
    private Toolbar toolbar;
    private RecyclerAdapterSmallCards adapterSmallCards;
    ArrayList<String> locations_list =new ArrayList<>();

    private FirebaseAuth auth;
    private DatabaseReference root;

    private TextView textViewWelcomeUser;
    private String userDisplayedname;
    private boolean getuserlocation=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locations);

        recyclerView=(RecyclerView) findViewById(R.id.recyclerview_locations);

        auth=FirebaseAuth.getInstance();
        if (auth.getCurrentUser()==null){

        }else {
            userDisplayedname=auth.getCurrentUser().getDisplayName();

        }

        Bundle extras=getIntent().getExtras();

        if(extras!=null){
            if (extras.containsKey("user_location")){
                getuserlocation=extras.getBoolean("user_location");
            }
        }

        toolbar=(Toolbar)findViewById(R.id.toolbar_location_activity);
        setSupportActionBar(toolbar);


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

                if(getuserlocation){
                    root.child(ConfigApp.FIREBASE_APP_URL_USERS).child(auth.getCurrentUser().getUid())
                            .child("userPublic").child("Location").setValue(new Locations(locations_list.get(position)))
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    setResult(RESULT_OK,new Intent()
                                            .putExtra("user_location",locations_list.get(position)));

                                    finish();
                                }
                            });

                }else {
                    root.child(ConfigApp.FIREBASE_APP_URL_USERS).child(auth.getCurrentUser().getUid())
                            .child("userPublic").child("Location").setValue(new Locations(locations_list.get(position)));
                    startActivity(new Intent(LocationsActivity.this,CategoryActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            .putExtra("locationName",locations_list.get(position)));

                    finish();
                }

            }
        });

        recyclerView.setAdapter(adapterSmallCards);



    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    public void onClick(View view) {

    }

}




