package com.bricenangue.nextgeneration.ebuycamer;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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
    private String locationName;
    private UserSharedPreference userSharedPreference;
    private FirebaseUser user;
    private ArrayList<Publication> data=new ArrayList<>();
    private RecyclerViewAdapterPosts adapterPosts;
    private RecyclerViewAdapterPosts.MyRecyclerAdaptaterPostClickListener adaptaterPostClickListener;
    private Spinner spinnerToolbar;
    private int positionSpinner=0;

    private String [] categoriesArray;
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
        setContentView(R.layout.activity_main_page);

        userSharedPreference=new UserSharedPreference(this);
        Bundle extras=getIntent().getExtras();

        if(extras!=null){
            if(extras.containsKey("position")){
                positionSpinner=extras.getInt("position");
            }

        }
        if (!userSharedPreference.getUserLocation().getName().isEmpty()){
            locationName=userSharedPreference.getUserLocation().getName();
        }else {
            startActivityForResult(new Intent(MainPageActivity.this,LocationsActivity.class)
                    .putExtra("user_location",true),LOCATION_INTENT);
        }


        categoriesArray=getResources().getStringArray(R.array.categories_arrays_create_mainpage);
        auth=FirebaseAuth.getInstance();
        toolbar=(Toolbar)findViewById(R.id.toolbar);

        userSharedPreference.setUserDataRefreshed(haveNetworkConnection());

        if(auth!=null){

            user=auth.getCurrentUser();

        }else {
            if(userSharedPreference.getUserLoggedIn()){
                // user offline
                if(!userSharedPreference.getUserDataRefreshed()){
                    // user refreshed data on start

                }

            }else {
                // user online but auth problem
                Toast.makeText(this,getString(R.string.problem_while_loading_user_data_auth_null),Toast.LENGTH_LONG).show();

                startActivity(new Intent(MainPageActivity.this,MainActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                Intent.FLAG_ACTIVITY_NEW_TASK));
                finish();
            }

        }
        if (categoriesArray[positionSpinner].equals("All Publications")) {
            root= FirebaseDatabase.getInstance().getReference()
                    .child(ConfigApp.FIREBASE_APP_URL_USERS_POSTS);

        }else{
            root= FirebaseDatabase.getInstance().getReference()
                    .child(ConfigApp.FIREBASE_APP_URL_REGIONS).child(locationName).child(categoriesArray[positionSpinner]);
        }



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

        ImageButton img1=(ImageButton)findViewById(R.id.button_create_new_post);
        ImageButton img2=(ImageButton)findViewById(R.id.button_view_favorite);
        ImageButton img3=(ImageButton)findViewById(R.id.button_view_my_post);
        ImageButton img4=(ImageButton)findViewById(R.id.button_go_to_chatting_activity);

        ImageButton img5=(ImageButton)findViewById(R.id.button_go_to_sales_activity);

        img1.setOnClickListener(this);
        img2.setOnClickListener(this);
        img3.setOnClickListener(this);
        img4.setOnClickListener(this);
        img5.setOnClickListener(this);


        CustomSpinnerAdapter customSpinnerAdapter=new CustomSpinnerAdapter(this
                ,getResources().getStringArray(R.array.categories_array_activity),locationName);

        spinnerToolbar = new Spinner(getSupportActionBar().getThemedContext());

        spinnerToolbar.setAdapter(customSpinnerAdapter);

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
                            .child(ConfigApp.FIREBASE_APP_URL_REGIONS).child(model.getPrivateContent().getLocation().getName())
                            .child(categoriesArray[model.getPrivateContent().getCategorie().getCatNumber()+1]);

                    updateViewer(root.child(model.getPrivateContent().getUniquefirebaseId())
                                    .child("publicContent"),
                            model.getPrivateContent().getUniquefirebaseId(),model.getPrivateContent().getCreatorid());
            }

            @Override
            public void onLongClick(int position, View v) {

            }
        };

    }

    private void procideOffline() {
        //show snackbar
        Toast.makeText(getApplicationContext(),getString(R.string.connection_to_server_not_aviable)
                ,Toast.LENGTH_SHORT).show();
        dismissProgressbar();
    }


    private int getPosition(String categorie){
        if(categorie.equals(getString(R.string.categories_post_electronic))){
            return 1;
        }else if (categorie.equals(getString(R.string.categories_post_cars_and_motors))){
            return 2;
        }else if (categorie.equals(getString(R.string.categories_post_fashion_accessoire))){
            return 3;
        }else if (categorie.equals(getString(R.string.categories_post_baby_child))){
            return 4;
        }else if (categorie.equals(getString(R.string.categories_post_sport_leisure))){
            return 5;
        }else if (categorie.equals(getString(R.string.categories_post_services))){
            return 6;
        }else if (categorie.equals(getString(R.string.categories_post_home_garden))){
            return 7;
        }else if (categorie.equals(getString(R.string.categories_post_movies_books))){
            return 8;
        }else if (categorie.equals(getString(R.string.categories_post_pets_accessoire))){
            return 9;
        }else {
            return 10;
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
                fetchPost(spinnerToolbar.getSelectedItemPosition());
                return true;
            case R.id.action_logout:
               if(haveNetworkConnection()){
                   loggout();
               }else {
                   Toast.makeText(getApplicationContext(),getString(R.string.connection_to_server_not_aviable)
                   ,Toast.LENGTH_SHORT).show();
               }

                return true;
            case R.id.action_settings:

                startActivity(new Intent(MainPageActivity.this,SettingsActivity.class));
                return true;

            case R.id.action_user_profile:

                startActivity(new Intent(MainPageActivity.this,UserProfileActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                return true;

            case R.id.action_check_location:
                startActivity(new Intent(MainPageActivity.this,LocationsActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                finish();
                return true;


        }
        return super.onOptionsItemSelected(item);

    }

    private void loggout() {

        final AlertDialog alertDialog =
                new AlertDialog.Builder(MainPageActivity.this)
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
                        startActivity(new Intent(MainPageActivity.this,MainActivity.class)
                                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                        Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                        Intent.FLAG_ACTIVITY_NEW_TASK));
                        finish();
                    }
                });
        alertDialog.setCancelable(false);
        alertDialog.show();

    }

    private void showProgressbar(){
        progressBar = new ProgressDialog(this);
        progressBar.setCancelable(false);
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.setMessage(getString(R.string.progress_dialog_loading));
        progressBar.show();
    }

    private void dismissProgressbar(){
        if (progressBar!=null){
            progressBar.dismiss();
        }
    }

    private void loadData(ArrayList<Publication> loadedData){
        adapterPosts=new RecyclerViewAdapterPosts(this,loadedData,adaptaterPostClickListener);
        recyclerView.setAdapter(adapterPosts);
        dismissProgressbar();
        adapterPosts.notifyDataSetChanged();

    }


    private void fetchPost(int position){

        data.clear();
        showProgressbar();
        final Query reference;

        if(position==0){

            reference= FirebaseDatabase.getInstance().getReference()
                    .child(ConfigApp.FIREBASE_APP_URL_USERS_POSTS_ALL_CITY)
                    .child(getString(R.string.fcm_notification_city)+String.valueOf(userSharedPreference.getUserLocation().getNumberLocation()));
            reference.keepSynced(true);

        }else {
            reference= FirebaseDatabase.getInstance().getReference()
                    .child(ConfigApp.FIREBASE_APP_URL_REGIONS)
                    .child(locationName)
                    .child(categoriesArray[position]);
            reference.keepSynced(true);

        }


        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()){


                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){

                        data.add(snapshot.getValue(Publication.class));

                        if(data.size() ==dataSnapshot.getChildrenCount()){
                            loadData(data);

                            dismissProgressbar();

                        }
                    }
                }else {
                    loadData(data);
                    Toast.makeText(getApplicationContext(),getString(R.string.mainpage_no_item_at_location),Toast.LENGTH_SHORT).show();
                    dismissProgressbar();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Toast.makeText(getApplicationContext(),getString(R.string.mainpage_error_while_loading_data),Toast.LENGTH_SHORT).show();
                dismissProgressbar();
            }
        });
        if (!haveNetworkConnection()){
            dismissProgressbar();
            Toast.makeText(getApplicationContext(),getString(R.string.alertDialog_no_internet_connection),Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onPause() {
        super.onPause();
        dismissProgressbar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MyFireBaseMessagingService.notificationIdPublication=0;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        fetchPost(i);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

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
            case R.id.button_go_to_chatting_activity:
                startActivity(new Intent(MainPageActivity.this,MyChatActivity.class));
                break;
            case R.id.button_go_to_sales_activity:
                startActivity(new Intent(MainPageActivity.this,ViewDealsActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

                break;
        }
    }


     class CustomSpinnerAdapter extends BaseAdapter {
        Context context;
        String[] categoriesNames;
        LayoutInflater inflter;
         private String cityname;

        public CustomSpinnerAdapter(Context applicationContext
                , String[] categoriesNames, String cityname) {
            this.context = applicationContext;
            this.categoriesNames = categoriesNames;
            inflter = (LayoutInflater.from(applicationContext));
            this.cityname=cityname;
        }

        @Override
        public int getCount() {
            return categoriesNames.length;
        }

        @Override
        public Object getItem(int i) {
            return categoriesNames[i];
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = inflter.inflate(R.layout.spinner_toolbar_layout, null);
            TextView categroy = (TextView) view.findViewById(R.id.textView_category_toolbar);
            TextView city = (TextView) view.findViewById(R.id.textView_category_toolbar_city);
            if(i!=0){
                city.setText(cityname);
                categroy.setText(categoriesNames[i]);
            }else {
                city.setText(cityname);
                categroy.setText(categoriesNames[i]);
            }

            return view;
        }
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
}
