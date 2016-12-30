package com.bricenangue.nextgeneration.ebuycamer;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.tasks.OnSuccessListener;
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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MyFavoritesActivity extends AppCompatActivity {


    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private FirebaseAuth auth;
    private DatabaseReference root;
    private FirebaseUser user;
    private ProgressDialog progressBar;
    private UserSharedPreference userSharedPreference;
    private String[] currencyArray;
    private String[] categoriesArray;
    private static final int REQUEST_INVITE=237;


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
        setContentView(R.layout.activity_my_favorites);

        userSharedPreference=new UserSharedPreference(this);
        auth=FirebaseAuth.getInstance();
        if(auth!=null){
            user=auth.getCurrentUser();
        }else {
            startActivity(new Intent(MyFavoritesActivity.this,MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        }
        currencyArray=getResources().getStringArray(R.array.currency);
        root= FirebaseDatabase.getInstance().getReference().child(ConfigApp.FIREBASE_APP_URL_USERS_FAVORITES);
        recyclerView=(RecyclerView)findViewById(R.id.recyclerview_myfavorite);
        layoutManager=new LinearLayoutManager(this);

        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        categoriesArray = getResources().getStringArray(R.array.categories_array_activity);

        if (!haveNetworkConnection()){
            Toast.makeText(getApplicationContext(),getString(R.string.connection_to_server_not_aviable)
                    ,Toast.LENGTH_SHORT).show();
        }
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
        lockscreen();
    }

    private void dismissProgressbar(){
        if (progressBar!=null){
            progressBar.dismiss();
            unloockscreen();
        }
    }

    private void  lockscreen(){
        ConfigApp.lockScreenOrientation(this);

    }
    private void unloockscreen(){
        ConfigApp.unlockScreenOrientation(this);
    }
    @Override
    protected void onStart() {
        super.onStart();

        showProgressbar();
        final Query reference= root.child(user.getUid());
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
       // reference.keepSynced(true);
        FirebaseRecyclerAdapter<Publication,MyPublicationFavViewHolder> adapter=
                new FirebaseRecyclerAdapter<Publication, MyPublicationFavViewHolder>(
                        Publication.class,
                        R.layout.myfavorite_cardview,
                        MyPublicationFavViewHolder.class,
                        reference
                ) {
                    @Override
                    protected void populateViewHolder(final MyPublicationFavViewHolder viewHolder, final Publication model, int position) {

                        //assert model!=null;
                        if(getItemCount()==0){
                            dismissProgressbar();
                        }else if (position==getItemCount()-1){
                            dismissProgressbar();
                        }

                        if (model.getPrivateContent().getCategorie().getCatNumber()==20){
                            viewHolder.saleLogo.setVisibility(View.VISIBLE);

                        }else {
                            viewHolder.saleLogo.setVisibility(View.GONE);
                        }
                        if(model.getPrivateContent()!=null && model.getPublicContent()!=null ){
                            //   viewHolder.mylocation.setText(model.getLocation().getName());
                            if(model.getPrivateContent().isNegotiable()){
                                viewHolder.isnegotiable.setText(getString(R.string.text_is_not_negotiable));
                            }else {
                                viewHolder.isnegotiable.setText("");
                            }
                            viewHolder.titel.setText(model.getPrivateContent().getTitle());
                            viewHolder.mylocation.setText(model.getPrivateContent().getLocation().getName());
                            if(model.getPublicContent().getNumberofView()>0){
                                viewHolder.button_viewer.setText(String.valueOf(model.getPublicContent().getNumberofView()));
                            }else {
                                viewHolder.button_viewer.setText(String.valueOf(0));
                            }


                            if(model.getCategoriesDeal()!=null ){
                                if( model.getPrivateContent().getCategorie().getCatNumber()==20){
                                    viewHolder.category.setVisibility(View.VISIBLE);
                                    viewHolder.category.setText(categoriesArray[model.getCategoriesDeal().getCatNumber() + 1]);

                                }
                            }

                            if(model.getPrivateContent().getFirstPicture()!=null){
                                byte[] decodedString = Base64.decode(model.getPrivateContent().getFirstPicture(), Base64.DEFAULT);
                                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                                viewHolder.postPicture.setImageBitmap(decodedByte);

                            }else {
                                if(model.getPrivateContent().getPublictionPhotos()!=null){
                                    Picasso.with(getApplicationContext()).load(model.getPrivateContent().getPublictionPhotos().get(0).getUri()).networkPolicy(NetworkPolicy.OFFLINE)
                                            .fit().centerInside()
                                            .into(viewHolder.postPicture, new Callback() {
                                                @Override
                                                public void onSuccess() {

                                                    dismissProgressbar();
                                                }

                                                @Override
                                                public void onError() {
                                                    Picasso.with(getApplicationContext()).load(model.getPrivateContent().getPublictionPhotos().get(0).getUri())
                                                            .fit().centerInside().into(viewHolder.postPicture);
                                                    dismissProgressbar();
                                                }
                                            });

                                }else {
                                    viewHolder.postPicture.setImageDrawable(getResources().getDrawable(R.mipmap.ic_launcher));
                                }
                            }




                            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {


                                    if (!haveNetworkConnection()){
                                        Toast.makeText(getApplicationContext(),getString(R.string.connection_to_server_not_aviable)
                                                ,Toast.LENGTH_SHORT).show();
                                    }else {
                                        if(model.getPrivateContent().getCategorie().getCatNumber()==20){
                                            startActivity(new Intent(MyFavoritesActivity.this,ViewContentDealActivity.class)
                                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                    .putExtra("user_uid",model.getPrivateContent().getCreatorid())
                                                    .putExtra("FromFav",true)
                                                    .putExtra("post",model.getPrivateContent().getUniquefirebaseId())
                                                    .putExtra("location",model.getPrivateContent().getLocation().getName())
                                                    .putExtra("categorie",model.getPrivateContent().getCategorie().getName()));


                                        }else {
                                            startActivity(new Intent(MyFavoritesActivity.this,ViewContentActivity.class)
                                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                    .putExtra("post",model.getPrivateContent().getUniquefirebaseId())
                                                    .putExtra("location",model.getPrivateContent().getLocation().getName())
                                                    .putExtra("FromFav",true)
                                                    .putExtra("categorie",model.getPrivateContent().getCategorie().getName()));


                                        }
                                    }

                                }
                            });

                            DecimalFormat decFmt = new DecimalFormat("#,###.##", DecimalFormatSymbols.getInstance(Locale.GERMAN));
                            decFmt.setMaximumFractionDigits(2);
                            decFmt.setMinimumFractionDigits(2);

                            String p=model.getPrivateContent().getPrice();
                            BigDecimal amt = new BigDecimal(p);
                            String preValue = decFmt.format(amt);


                            if(p.equals("0")){
                                viewHolder.price.setText(getString(R.string.check_box_create_post_hint_is_for_free));
                            }else {
                                if(currencyArray!=null){
                                    viewHolder.price.setText(preValue + " " + currencyArray[getCurrencyPosition(model.getPrivateContent().getCurrency())]);

                                }else {
                                    viewHolder.price.setText(preValue + " " + model.getPrivateContent().getCurrency());

                                }
                            }

                            Date date = new Date(model.getPrivateContent().getTimeofCreation());
                            DateFormat formatter = new SimpleDateFormat("HH:mm");
                            String dateFormatted = formatter.format(date);

                            CheckTimeStamp checkTimeStamp= new CheckTimeStamp(getApplicationContext(),model.getPrivateContent().getTimeofCreation());

                            viewHolder.time.setText(checkTimeStamp.checktime());

                            viewHolder.button_remove.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (!haveNetworkConnection()){
                                        Toast.makeText(getApplicationContext(),getString(R.string.connection_to_server_not_aviable)
                                                ,Toast.LENGTH_SHORT).show();
                                    }else {
                                        deletePostFromFavorite(model);
                                    }

                                }
                            });

                            viewHolder.button_share.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (!haveNetworkConnection()){
                                        Toast.makeText(getApplicationContext(),getString(R.string.connection_to_server_not_aviable)
                                                ,Toast.LENGTH_SHORT).show();
                                    }else {

                                        onInviteClicked();
                                    }
                                }
                            });
                        }

                    }
                };

        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();



    }

    @Override
    protected void onPause() {
        super.onPause();
        dismissProgressbar();
    }

    private void deletePostFromFavorite(Publication model) {
        Map<String,Object> childreen=new HashMap<>();
        childreen.put("/"+ConfigApp.FIREBASE_APP_URL_USERS_FAVORITES+"/"+user.getUid()
                +"/"+model.getPrivateContent().getUniquefirebaseId(),null );
        childreen.put("/"+ConfigApp.FIREBASE_APP_URL_USERS_FAVORITES_USER+"/"+user.getUid()
                +"/"+model.getPrivateContent().getUniquefirebaseId(),null );


        DatabaseReference reference =FirebaseDatabase.getInstance().getReference();
        reference.updateChildren(childreen);
        //reference.keepSynced(true);
    }


    public static class MyPublicationFavViewHolder extends RecyclerView.ViewHolder{
        ImageView postPicture,saleLogo;
        TextView titel, time, price, mylocation,isnegotiable,category;
        private View view;
        private Button button_viewer,button_remove,button_share;


        public MyPublicationFavViewHolder(View itemView) {
            super(itemView);
            view=itemView;

            postPicture=(ImageView) itemView.findViewById(R.id.imageView_publicationFirstphoto_myfavorite_cardview);
            saleLogo=(ImageView)itemView.findViewById(R.id.imageView_my_favorite_sale_logo);

            button_share=(Button) itemView.findViewById(R.id.button_share_myfavorite_cardview);
            button_viewer=(Button) itemView.findViewById(R.id.button_myfavorite_cardview_viewer);
            button_remove=(Button) itemView.findViewById(R.id.button_promote_myfavorite_cardview);

            category=(TextView) itemView.findViewById(R.id.textView_publication_is_category_myfavorite_cardview);
            titel=(TextView) itemView.findViewById(R.id.textView_publication_title_myfavorite_cardview);
            isnegotiable=(TextView) itemView.findViewById(R.id.textView_publication_is_negotiable_myfavorite_cardview);
            time=(TextView) itemView.findViewById(R.id.textView_publication_time_myfavorite_cardview);
            price=(TextView) itemView.findViewById(R.id.textView_publication_price_myfavorite_cardview);
            mylocation=(TextView) itemView.findViewById(R.id.textView_publication_location_myfavorite_cardview);


        }
    }



    private void onInviteClicked() {
        Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
                .setMessage(getString(R.string.invitation_message))
                .setDeepLink(Uri.parse(getString(R.string.invitation_deep_link)))

                .setCallToActionText(getString(R.string.invitation_cta))
                .build();
        startActivityForResult(intent, REQUEST_INVITE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
      //  Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == REQUEST_INVITE) {
            if (resultCode == RESULT_OK) {
                // Get the invitation IDs of all sent messages
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                for (String id : ids) {
                //    Log.d(TAG, "onActivityResult: sent invitation " + id);
                }
            } else {
                // Sending failed or it was canceled, show failure message to the user
                // ...
            }
        }
    }


    private int getCurrencyPosition(String currency){
        if(currency.equals(getString(R.string.currency_xaf))
                || currency.equals("F CFA") || currency.equals("XAF")){
            return 0;
        }
        return 0;
    }
}
