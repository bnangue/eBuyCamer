package com.bricenangue.nextgeneration.ebuycamer;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
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
import java.util.Locale;

public class SingleDealActivityActivity extends AppCompatActivity {

    public static boolean singledeal= true;

    private ImageView postPicture,imageViewLocation;
    private TextView titel, time, price, mylocation,category;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private FirebaseAuth auth;
    private DatabaseReference root;
    private FirebaseUser user;
    private ProgressDialog progressBar;
    private UserSharedPreference userSharedPreference;
    private RelativeLayout overviewLayout;
    private Deals deal;
    private String[] categoriesArray;


    private String dealid;
    private String[] currencyArray;

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

        singledeal=false;
        if (!haveNetworkConnection()){
            Toast.makeText(getApplicationContext(),getString(R.string.connection_to_server_not_aviable)
                    ,Toast.LENGTH_SHORT).show();
        }


        setContentView(R.layout.activity_single_deal_activity);
        userSharedPreference=new UserSharedPreference(this);
        auth=FirebaseAuth.getInstance();
        if(auth!=null){
            user=auth.getCurrentUser();
        }else {
            startActivity(new Intent(SingleDealActivityActivity.this,MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        }

        currencyArray=getResources().getStringArray(R.array.currency);
        categoriesArray = getResources().getStringArray(R.array.categories_array_activity);

        Bundle extras=getIntent().getExtras();
        if(extras!=null){
            dealid=extras.getString("dealid");
        }
        overviewLayout=(RelativeLayout)findViewById(R.id.relativLayout_mypost_cardview_deal_single);
        overviewLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!haveNetworkConnection() || deal==null){
                    Toast.makeText(getApplicationContext(),getString(R.string.connection_to_server_not_aviable)
                            ,Toast.LENGTH_SHORT).show();
                }else {
                    if(deal!=null){
                        startActivity(new Intent(SingleDealActivityActivity.this,ViewContentDealActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                .putExtra("user_uid",deal.getPrivateContent().getCreatorid())
                                .putExtra("post",deal.getPrivateContent().getUniquefirebaseId())
                                .putExtra("location",deal.getPrivateContent().getLocation().getName())
                                .putExtra("categorie",deal.getPrivateContent().getCategorie().getName()));
                    }
                }
            }
        });
        root=FirebaseDatabase.getInstance().getReference();
        postPicture=(ImageView) findViewById(R.id.imageView_publicationFirstphoto_deal_single);
        imageViewLocation=(ImageView) findViewById(R.id.imageView_publicationLocation_deal_single);

        titel=(TextView) findViewById(R.id.textView_publication_title_deal_single);
        time=(TextView) findViewById(R.id.textView_publication_time_deal_single);
        price=(TextView) findViewById(R.id.textView_publication_price_deal_single);
        category=(TextView)findViewById(R.id.textView_publication_category_deal_single);
        mylocation=(TextView) findViewById(R.id.textView_publication_locatiomn_deal_single);

        recyclerView=(RecyclerView)findViewById(R.id.recyclerview_activity_single_deal);
        layoutManager=new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);



    }

    private void fetchDeal(){
        if(dealid!=null && !dealid.isEmpty()){
            showProgressbar();

            DatabaseReference referenceDeal=root.child(ConfigApp.FIREBASE_APP_URL_USERS_DEAL_USER)
                    .child(user.getUid()).child(dealid);
            referenceDeal.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot!=null){
                        Deals d=dataSnapshot.getValue(Deals.class);
                        deal=d;
                        populate(d);
                        fetchOffers(d);
                    }else {
                        dismissProgressbar();
                    }


                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                    dismissProgressbar();
                }
            });



        }
    }

    private void fetchOffers(final Deals deal){

        final Query reference= root.child(ConfigApp.FIREBASE_APP_URL_USERS_DEAL_USER)
                .child(user.getUid()).child(dealid)
                .child("offers/offers").orderByChild("time");
      //  reference.keepSynced(true);
        final FirebaseRecyclerAdapter<Offer,DealSingleViewHolder> adapter=
                new FirebaseRecyclerAdapter<Offer, DealSingleViewHolder>(
                        Offer.class,
                        R.layout.single_deal_offer_item,
                        DealSingleViewHolder.class,
                        reference
                ) {
                    @Override
                    protected void populateViewHolder(final DealSingleViewHolder viewHolder, final Offer model, final int position) {

                        assert model!=null;

                        if(currencyArray!=null){
                            viewHolder.offer.setText(model.getOffermade() + " " + currencyArray[0]);

                        }else {
                            viewHolder.offer.setText(model.getOffermade() + " " + currencyArray[0]);

                        }
                            Date date = new Date(model.getTime());
                            DateFormat formatter = new SimpleDateFormat("HH:mm");
                            String dateFormatted = formatter.format(date);
                        CheckTimeStamp checkTimeStamp= new CheckTimeStamp(getApplicationContext(),model.getTime());

                        viewHolder.time.setText(checkTimeStamp.checktime());

                        viewHolder.contact.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // start chat

                                String key = getRef(position).getKey();

                                String chat_id=FirebaseDatabase.getInstance().getReference()
                                        .child(deal.getPrivateContent().getCreatorid())
                                        .child(dealid)
                                        .child(key).push().getKey();
                                startActivity(new Intent(SingleDealActivityActivity.this,ChatActivity.class)
                                        .putExtra("key",key)
                                        .putExtra("creator_uid",deal.getPrivateContent().getCreatorid())
                                        .putExtra("post_id",dealid)
                                        .putExtra("is_deal","deal")
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));


                            }
                        });
                    }
                };

        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        if(adapter.getItemCount()==0){
            dismissProgressbar();
        }
    }
    private void populate(final Deals model) {

        titel.setText(model.getPrivateContent().getTitle());
        category.setText(categoriesArray[model.getCategoriesDeal().getCatNumber() + 1]);
        mylocation.setText(model.getPrivateContent().getLocation().getName());
        if(model.getPrivateContent().getFirstPicture()!=null){
            byte[] decodedString = Base64.decode(model.getPrivateContent().getFirstPicture(), Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

            postPicture.setImageBitmap(decodedByte);
            dismissProgressbar();

        }else {
            if(model.getPrivateContent().getPublictionPhotos()!=null){
                Picasso.with(getApplicationContext()).load(model.getPrivateContent().getPublictionPhotos().get(0).getUri())
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .fit().centerInside()
                        .into(postPicture, new Callback() {
                            @Override
                            public void onSuccess() {

                                dismissProgressbar();
                            }

                            @Override
                            public void onError() {
                                Picasso.with(getApplicationContext()).load(model.getPrivateContent()
                                        .getPublictionPhotos().get(0).getUri())
                                        .fit().centerInside().into(postPicture);
                                dismissProgressbar();

                            }
                        });

            }else {
                postPicture.setImageDrawable(getResources().getDrawable(R.mipmap.ic_launcher));
            }
        }



        DecimalFormat decFmt = new DecimalFormat("#,###.##", DecimalFormatSymbols.getInstance(Locale.GERMAN));
        decFmt.setMaximumFractionDigits(2);
        decFmt.setMinimumFractionDigits(2);

        String p=model.getPrivateContent().getPrice();
        BigDecimal amt = new BigDecimal(p);
        String preValue = decFmt.format(amt);


        if(currencyArray!=null){
            price.setText(preValue + " " + currencyArray[getCurrencyPosition(model.getPrivateContent().getCurrency())]);

        }else {
            price.setText(preValue + " " + model.getPrivateContent().getCurrency());

        }

        Date date = new Date(model.getPrivateContent().getTimeofCreation());
        DateFormat formatter = new SimpleDateFormat("HH:mm");
        String dateFormatted = formatter.format(date);
        CheckTimeStamp checkTimeStamp= new CheckTimeStamp(getApplicationContext(),model.getPrivateContent().getTimeofCreation());

        time.setText(checkTimeStamp.checktime());

    }

    private int getCurrencyPosition(String currency){
        if(currency.equals(getString(R.string.currency_xaf))
                || currency.equals("F CFA") || currency.equals("XAF")){
            return 0;
        }
        return 0;

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
        fetchDeal();
        singledeal=false;
    }


    @Override
    protected void onResume() {
        super.onResume();
        singledeal=false;

    }

    @Override
    protected void onPause() {
        super.onPause();
        singledeal=true;
        dismissProgressbar();

    }

    protected void onStop() {
        super.onStop();
        dismissProgressbar();
        singledeal=true;
    }


    public static class DealSingleViewHolder extends RecyclerView.ViewHolder{
        Button contact;
        TextView offer,time;

        public DealSingleViewHolder(View itemView) {
            super(itemView);

            contact=(Button) itemView.findViewById(R.id.button_single_deal_item_contact);
            offer=(TextView) itemView.findViewById(R.id.textView_single_deal_item_user_offer);
            time=(TextView) itemView.findViewById(R.id.textView_single_deal_item_time);
        }
    }

}
