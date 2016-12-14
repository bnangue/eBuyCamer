package com.bricenangue.nextgeneration.ebuycamer;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class MainPageActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private FirebaseAuth auth;
    private Toolbar toolbar, toolbar_bottom;
    private RecyclerView recyclerView;
    private DatabaseReference root;
    private LinearLayoutManager layoutManager;
    private ProgressDialog progressBar;
    private String locationName,category;
    private UserSharedPreference userSharedPreference;
    private FirebaseUser user;
    private ArrayList<Publication> data=new ArrayList<>();
    private RecyclerViewAdapterPosts adapterPosts;
    private RecyclerViewAdapterPosts.MyRecyclerAdaptaterPostClickListener adaptaterPostClickListener;
    private Spinner spinnerToolbar;
    private int positionSpinner=0;

    private String [] categoriesArray;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        userSharedPreference=new UserSharedPreference(this);
        Bundle extras=getIntent().getExtras();

        if(extras!=null && extras.containsKey("category")){

            category=extras.getString("category");
            if(extras.containsKey("position")){
                positionSpinner=extras.getInt("position");
            }
            if ((userSharedPreference.getUserLocation()==null || userSharedPreference.getUserLocation().isEmpty())
                    && extras.containsKey("locationName")){
                locationName=extras.getString("locationName");

                userSharedPreference.storeUserLocation(locationName);
            }else {
                locationName=userSharedPreference.getUserLocation();
            }

        }

        categoriesArray=getResources().getStringArray(R.array.categories_arrays_create);
        auth=FirebaseAuth.getInstance();
        toolbar=(Toolbar)findViewById(R.id.toolbar);



        if(auth!=null){

            user=auth.getCurrentUser();
            Toast.makeText(getApplicationContext(), getString(R.string.welcome_back)
                    ,Toast.LENGTH_SHORT).show();

        }else {
            startActivity(new Intent(MainPageActivity.this,MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));

        }
        root= FirebaseDatabase.getInstance().getReference()
                .child(ConfigApp.FIREBASE_APP_URL_REGIONS).child(locationName).child(categoriesArray[positionSpinner]);

        setSupportActionBar(toolbar);
       getSupportActionBar().setTitle(null);
       // getSupportActionBar().setSubtitle(category);

        toolbar_bottom=(Toolbar)findViewById(R.id.toolbar_bottom);


        recyclerView=(RecyclerView)findViewById(R.id.recyclerview_mainpage);

        layoutManager=new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        Button img1=(Button)findViewById(R.id.button_create_new_post);
        Button img2=(Button)findViewById(R.id.button_view_favorite);
        Button img3=(Button)findViewById(R.id.button_view_my_post);
        Button img4=(Button)findViewById(R.id.button_go_to_setting);

        img1.setOnClickListener(this);
        img2.setOnClickListener(this);
        img3.setOnClickListener(this);
        img4.setOnClickListener(this);




        final ArrayAdapter spinnerAdapter = ArrayAdapter.createFromResource(getSupportActionBar().getThemedContext(),
                R.array.categories_array_activity, R.layout.spinner_toolbar_layout);

        spinnerToolbar = new Spinner(getSupportActionBar().getThemedContext());

        spinnerToolbar.setAdapter(spinnerAdapter);
        spinnerToolbar.setSelection(positionSpinner);

        spinnerToolbar.setOnItemSelectedListener(this);

        toolbar.addView(spinnerToolbar);

        adaptaterPostClickListener =new RecyclerViewAdapterPosts.MyRecyclerAdaptaterPostClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                Publication model=data.get(position);
                startActivity(new Intent(MainPageActivity.this,ViewContentActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .putExtra("post",model.getPrivateContent().getUniquefirebaseId())
                        .putExtra("location",model.getPrivateContent().getLocation().getName())
                        .putExtra("categorie",model.getPrivateContent().getCategorie().getName()));

                DatabaseReference root= FirebaseDatabase.getInstance().getReference()
                        .child(ConfigApp.FIREBASE_APP_URL_REGIONS).child(locationName)
                        .child(categoriesArray[spinnerToolbar.getSelectedItemPosition()]);

                updateViewer(root.child(model.getPrivateContent().getUniquefirebaseId())
                                .child("publicContent"),
                        model.getPrivateContent().getUniquefirebaseId(),model.getPrivateContent().getCreatorid());
            }

            @Override
            public void onLongClick(int position, View v) {

            }
        };

    }


    private int getPosition(String categorie){
        if(categorie.equals(getString(R.string.categories_post_electronic))){
            return 0;
        }else if (categorie.equals(getString(R.string.categories_post_cars_and_motors))){
            return 1;
        }else if (categorie.equals(getString(R.string.categories_post_fashion_accessoire))){
            return 2;
        }else if (categorie.equals(getString(R.string.categories_post_baby_child))){
            return 3;
        }else if (categorie.equals(getString(R.string.categories_post_sport_leisure))){
            return 4;
        }else if (categorie.equals(getString(R.string.categories_post_services))){
            return 5;
        }else if (categorie.equals(getString(R.string.categories_post_home_garden))){
            return 6;
        }else if (categorie.equals(getString(R.string.categories_post_movies_books))){
            return 7;
        }else if (categorie.equals(getString(R.string.categories_post_pets_accessoire))){
            return 8;
        }else {
            return 9;
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_page,menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_refresh_mainpage :
                fetchPost(categoriesArray[spinnerToolbar.getSelectedItemPosition()]);
                return true;
            case R.id.action_logout:
              loggout();
                return true;
            case R.id.action_settings:

                startActivity(new Intent(MainPageActivity.this,SettingsActivity.class));
                return true;

            case R.id.action_user_profile:

                startActivity(new Intent(MainPageActivity.this,UserProfileActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                return true;

            case R.id.action_check_location:
                userSharedPreference.storeUserLocation(null);
                startActivity(new Intent(MainPageActivity.this,LocationsActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                finish();
                return true;


        }
        return super.onOptionsItemSelected(item);

    }

    private void loggout() {

        final AlertDialog alertDialog =
                new AlertDialog.Builder(MainPageActivity.this).setMessage(
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
                        startActivity(new Intent(MainPageActivity.this,MainActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                        finish();
                    }
                });
        alertDialog.setCancelable(false);
        alertDialog.show();

    }
    private void loadData(ArrayList<Publication> loadedData){
        adapterPosts=new RecyclerViewAdapterPosts(this,loadedData,adaptaterPostClickListener);
        recyclerView.setAdapter(adapterPosts);
        if (progressBar!=null){
            progressBar.dismiss();
        }
        adapterPosts.notifyDataSetChanged();

    }


    private void fetchPost(String postcategory){
        data.clear();
        progressBar = new ProgressDialog(this);
        progressBar.setCancelable(false);
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.show();
        final Query reference= FirebaseDatabase.getInstance().getReference()
                .child(ConfigApp.FIREBASE_APP_URL_REGIONS)
                .child(locationName)
                .child(postcategory).limitToLast(1000);


        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()){


                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){

                        data.add(snapshot.getValue(Publication.class));

                        if(data.size() ==dataSnapshot.getChildrenCount()){
                            loadData(data);

                            if (progressBar!=null){
                                progressBar.dismiss();
                            }
                        }
                    }
                }else {
                    loadData(data);
                    Toast.makeText(getApplicationContext(),getString(R.string.mainpage_no_item_at_location),Toast.LENGTH_SHORT).show();
                    if (progressBar!=null){
                        progressBar.dismiss();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Toast.makeText(getApplicationContext(),getString(R.string.mainpage_error_while_loading_data),Toast.LENGTH_SHORT).show();
                if (progressBar!=null){
                    progressBar.dismiss();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
      // fetchPost(category);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

        category=spinnerToolbar.getSelectedItem().toString();
        fetchPost(categoriesArray[i]);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }


    public static class PublicationViewHolder extends RecyclerView.ViewHolder{
        ImageView postPicture,imageViewLocation;
        TextView titel, time, price, mylocation;
        private View view;


        public PublicationViewHolder(View itemView) {
            super(itemView);
            view=itemView;

            postPicture=(ImageView) itemView.findViewById(R.id.imageView_publicationFirstphoto);
            imageViewLocation=(ImageView) itemView.findViewById(R.id.imageView_publicationLocation);

            titel=(TextView) itemView.findViewById(R.id.textView_publication_title);
            time=(TextView) itemView.findViewById(R.id.textView_publication_time);
            price=(TextView) itemView.findViewById(R.id.textView_publication_price);
            mylocation=(TextView) itemView.findViewById(R.id.textView_publication_locatiomn);


        }
    }


    private void updateViewer(DatabaseReference postRef, final String postid,final String userid) {
        postRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                final PublicContent p = mutableData.getValue(PublicContent.class);
                if (p == null) {
                    return Transaction.success(mutableData);
                }

                if (p.getViewers()!=null && !p.getViewers().containsKey(auth.getCurrentUser().getUid()) ) {

                    long rating = p.getNumberofView();
                    rating = rating + 1;
                    p.setNumberofView(rating);

                    HashMap<String,String> map=p.getViewers();

                    map.put(auth.getCurrentUser().getUid(),auth.getCurrentUser().getUid());
                    p.setViewers(map);
                }else if(p.getViewers()==null){
                    HashMap<String,String> map=new HashMap<String, String>();

                    map.put(auth.getCurrentUser().getUid(),auth.getCurrentUser().getUid());
                    p.setViewers(map);
                    p.setNumberofView(1);
                }

                // Set value and report transaction success
                mutableData.setValue(p);

                final DatabaseReference refRoot=FirebaseDatabase.getInstance().getReference();
                refRoot.child(ConfigApp.FIREBASE_APP_URL_USERS_POSTS_USER).child(userid).child(postid)
                        .child("publicContent").setValue(p).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        refRoot.child(ConfigApp.FIREBASE_APP_URL_USERS_POSTS).child(postid).child("publicContent").setValue(p);

                    }
                });

                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                // Transaction completed
                // Log.d(TAG, "postTransaction:onComplete:" + databaseError);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(MainPageActivity.this,CategoryActivity.class)
        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        finish();
        //auth.signOut();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.button_create_new_post:
                startActivity(new Intent(MainPageActivity.this,CreateAndModifyPublicationActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                break;
            case R.id.button_view_favorite:
                startActivity(new Intent(MainPageActivity.this,MyFavoritesActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                break;
            case R.id.button_view_my_post:
                startActivity(new Intent(MainPageActivity.this,MyPostActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                break;
            case R.id.button_go_to_setting:
                startActivity(new Intent(MainPageActivity.this,SettingsActivity.class));
                break;
        }
    }
}
