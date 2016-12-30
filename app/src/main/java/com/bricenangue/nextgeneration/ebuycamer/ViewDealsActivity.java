package com.bricenangue.nextgeneration.ebuycamer;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ViewDealsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private FirebaseAuth auth;
    private DatabaseReference root;
    private FirebaseUser user;
    private ProgressDialog progressBar;
    private UserSharedPreference userSharedPreference;
    private String[] categoriesArray;

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
        setContentView(R.layout.activity_view_deal);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               startActivity(new Intent(ViewDealsActivity.this,CreateAndModifyDealsActivity.class)
               .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

            }
        });

        categoriesArray = getResources().getStringArray(R.array.categories_array_activity);


        getSupportActionBar().setTitle(getString(R.string.all_deals));
        userSharedPreference=new UserSharedPreference(this);
        auth=FirebaseAuth.getInstance();
        if(auth!=null){
            user=auth.getCurrentUser();
        }else {
            startActivity(new Intent(ViewDealsActivity.this,MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        }
        root= FirebaseDatabase.getInstance().getReference().child(ConfigApp.FIREBASE_APP_URL_USERS_DEAL);
        recyclerView=(RecyclerView)findViewById(R.id.horizontal_recycler_view_create_post_deal);
        layoutManager=new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        if (!haveNetworkConnection()){
            dismissProgressbar();
            Toast.makeText(getApplicationContext(),getString(R.string.connection_to_server_not_aviable)
                    ,Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_all_deals,menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_refresh_viewdeal :

                if (!haveNetworkConnection()){
                    Toast.makeText(getApplicationContext(),getString(R.string.connection_to_server_not_aviable)
                            ,Toast.LENGTH_SHORT).show();
                }else {
                    fetchDeals();
                }


                return true;

            case R.id.action_settings_viewdeal:

                startActivity(new Intent(ViewDealsActivity.this,SettingsActivity.class));
                return true;

            case R.id.action_user_profile_category_viewdeal:

                startActivity(new Intent(ViewDealsActivity.this,UserProfileActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                return true;
            case R.id.action_mydeal_viewdeal:

                startActivity(new Intent(ViewDealsActivity.this,MyDealsActivity.class)

                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                return true;


        }
        return super.onOptionsItemSelected(item);

    }

    private void fetchDeals() {

        showProgressbar();
        final Query reference= FirebaseDatabase.getInstance().getReference()
                .child(ConfigApp.FIREBASE_APP_URL_USERS_DEAL);
       // reference.keepSynced(true);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChildren()){
                    dismissProgressbar();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        FirebaseRecyclerAdapter<Deals,DealViewHolder> adapter=
                new FirebaseRecyclerAdapter<Deals, DealViewHolder>(
                        Deals.class,
                        R.layout.layout_deal_item,
                        DealViewHolder.class,
                        reference
                ) {
                    @Override
                    protected void populateViewHolder(final DealViewHolder viewHolder, final Deals model, int position) {

                        if(getItemCount()==0){
                            dismissProgressbar();
                        }else if (position==getItemCount()-1){
                            dismissProgressbar();
                        }

                        assert model!=null;
                        if(model.getPublicContent()!=null && model.getPrivateContent()!=null && model.getCategoriesDeal()!=null){
                            viewHolder.titel.setText(model.getPrivateContent().getTitle());
                            viewHolder.mylocation.setText(model.getPrivateContent().getLocation().getName());
                            if(model.getPrivateContent().getFirstPicture()!=null){
                                byte[] decodedString = Base64.decode(model.getPrivateContent().getFirstPicture(), Base64.DEFAULT);
                                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                                viewHolder.postPicture.setImageBitmap(decodedByte);

                                viewHolder.category.setText(categoriesArray[model.getCategoriesDeal().getCatNumber() + 1]);

                            }else {
                                if(model.getPrivateContent().getPublictionPhotos()!=null){
                                    Picasso.with(getApplicationContext()).load(model.getPrivateContent().getPublictionPhotos().get(0).getUri())
                                            .networkPolicy(NetworkPolicy.OFFLINE)
                                            .fit().centerInside()
                                            .into(viewHolder.postPicture, new Callback() {
                                                @Override
                                                public void onSuccess() {

                                                }

                                                @Override
                                                public void onError() {
                                                    Picasso.with(getApplicationContext()).load(model.getPrivateContent()
                                                            .getPublictionPhotos().get(0).getUri())
                                                            .fit().centerInside().into(viewHolder.postPicture);

                                                }
                                            });

                                }else {
                                    viewHolder.postPicture.setImageDrawable(getResources().getDrawable(R.mipmap.ic_launcher));
                                }
                            }



                            viewHolder.price.setText(getString(R.string.string_price_of_deal_negotiable));

                            Date date = new Date(model.getPrivateContent().getTimeofCreation());
                            DateFormat formatter = new SimpleDateFormat("HH:mm");
                            String dateFormatted = formatter.format(date);
                            CheckTimeStamp checkTimeStamp= new CheckTimeStamp(getApplicationContext(),model.getPrivateContent().getTimeofCreation());

                            viewHolder.time.setText(checkTimeStamp.checktime());

                            viewHolder.view.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (!haveNetworkConnection()){
                                        Toast.makeText(getApplicationContext(),getString(R.string.connection_to_server_not_aviable)
                                                ,Toast.LENGTH_SHORT).show();
                                    }else {

                                        if(model.getPrivateContent().getCreatorid().equals(user.getUid())){
                                            startActivity(new Intent(ViewDealsActivity.this,SingleDealActivityActivity.class)
                                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                    .putExtra("user_uid",model.getPrivateContent().getCreatorid())
                                                    .putExtra("dealid",model.getPrivateContent().getUniquefirebaseId()));
                                            DatabaseReference root= FirebaseDatabase.getInstance().getReference()
                                                    .child(ConfigApp.FIREBASE_APP_URL_USERS_DEAL);

                                            updateViewer(root.child(model.getPrivateContent().getUniquefirebaseId())
                                                            .child("publicContent"),
                                                    model.getPrivateContent().getUniquefirebaseId(),model.getPrivateContent().getCreatorid());


                                        }else {
                                            startActivity(new Intent(ViewDealsActivity.this,ViewContentDealActivity.class)
                                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                    .putExtra("user_uid",model.getPrivateContent().getCreatorid())
                                                    .putExtra("post",model.getPrivateContent().getUniquefirebaseId()));

                                            DatabaseReference root= FirebaseDatabase.getInstance().getReference()
                                                    .child(ConfigApp.FIREBASE_APP_URL_USERS_DEAL);

                                            updateViewer(root.child(model.getPrivateContent().getUniquefirebaseId())
                                                            .child("publicContent"),
                                                    model.getPrivateContent().getUniquefirebaseId(),model.getPrivateContent().getCreatorid());

                                        }

                                    }

                                }
                            });
                        }
                    }
                };

        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();



    }

    private void procideOffline() {
        //show snackbar

        Toast.makeText(getApplicationContext(),getString(R.string.connection_to_server_not_aviable)
                ,Toast.LENGTH_SHORT).show();
        dismissProgressbar();
    }
    private void showProgressbar(){
        progressBar = new ProgressDialog(this);
        progressBar.setCancelable(false);
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.show();
    }

    private void dismissProgressbar(){
        if (progressBar!=null){
            progressBar.dismiss();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        fetchDeals();

    }

    @Override
    protected void onPause() {
        super.onPause();
        dismissProgressbar();
    }

    protected void onStop() {
        super.onStop();
        dismissProgressbar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MyFireBaseMessagingService.notificationIdDeal=0;

    }

    private void updateViewer(DatabaseReference postRef, final String postid, final String userid) {
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
                refRoot.child(ConfigApp.FIREBASE_APP_URL_USERS_DEAL_USER).child(userid).child(postid)
                        .child("publicContent").setValue(p).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

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

    public static class DealViewHolder extends RecyclerView.ViewHolder{
        ImageView postPicture,imageViewLocation;
        TextView titel, time, price, mylocation,category;
        private View view;

        public DealViewHolder(View itemView) {
            super(itemView);
            view=itemView;

            postPicture=(ImageView) itemView.findViewById(R.id.imageView_publicationFirstphoto_deal);
            imageViewLocation=(ImageView) itemView.findViewById(R.id.imageView_publicationLocation_deal);

            category=(TextView) itemView.findViewById(R.id.textView_publication_category_deal);
            titel=(TextView) itemView.findViewById(R.id.textView_publication_title_deal);
            time=(TextView) itemView.findViewById(R.id.textView_publication_time_deal);
            price=(TextView) itemView.findViewById(R.id.textView_publication_price_deal);
            mylocation=(TextView) itemView.findViewById(R.id.textView_publication_locatiomn_deal);


        }
    }


    private void showSnackbar(View view, String message){
        Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    private int getCurrencyPosition(String currency){
        if(currency.equals(getString(R.string.currency_xaf))
                || currency.equals("F CFA") || currency.equals("XAF")){
            return 0;
        }
        return 0;

    }

}
