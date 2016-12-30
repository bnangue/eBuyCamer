package com.bricenangue.nextgeneration.ebuycamer;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
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

public class MyDealsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private FirebaseAuth auth;
    private DatabaseReference root;
    private FirebaseUser user;
    private ProgressDialog progressBar;
    private UserSharedPreference userSharedPreference;
    private String[] currencyArray;
    private ShareActionProvider mShareActionProvider;
    private String[] categoriesArray;
    private static  final int REQUEST_INVITE=237;

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
        setContentView(R.layout.activity_my_deals);


        if (!haveNetworkConnection()){
            dismissProgressbar();
            Toast.makeText(getApplicationContext(),getString(R.string.connection_to_server_not_aviable)
                    ,Toast.LENGTH_SHORT).show();
            finish();
        }

        userSharedPreference=new UserSharedPreference(this);
        auth=FirebaseAuth.getInstance();
        if(auth!=null){
            user=auth.getCurrentUser();
        }else {
            startActivity(new Intent(MyDealsActivity.this,MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        }
        currencyArray=getResources().getStringArray(R.array.currency);
        categoriesArray = getResources().getStringArray(R.array.categories_array_activity);


        getSupportActionBar().setTitle(getString(R.string.action_my_deal));
            root= FirebaseDatabase.getInstance().getReference().child(ConfigApp.FIREBASE_APP_URL_USERS_DEAL_USER);

        recyclerView=(RecyclerView)findViewById(R.id.recyclerview_mydeal);
        layoutManager=new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);



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

        fetchMyDeals();


    }

    @Override
    protected void onResume() {
        super.onResume();


    }

    @Override
    protected void onPause() {
        super.onPause();
        dismissProgressbar();
    }

    private void fetchMyDeals() {
        showProgressbar();
        final Query reference= root.child(user.getUid());
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
        FirebaseRecyclerAdapter<Deals,MyDealViewHolder> adapter=
                new FirebaseRecyclerAdapter<Deals, MyDealViewHolder>(
                        Deals.class,
                        R.layout.mydeal_cardview,
                        MyDealViewHolder.class,
                        reference
                ) {
                    @Override
                    protected void populateViewHolder(final MyDealViewHolder viewHolder, final Deals model, int position) {

                        //   viewHolder.mylocation.setText(model.getLocation().getName());
                        if (model==null){
                            fetchMyDeals();
                        }

                        if(getItemCount()==0){
                            dismissProgressbar();
                        }else if (position==getItemCount()-1){
                            dismissProgressbar();
                        }

                        assert model != null;
                        if(model.getPublicContent()!=null && model.getPrivateContent()!=null
                                && model.getOffers()!=null && model.getCategoriesDeal()!=null){

                            viewHolder.titel.setText(model.getPrivateContent().getTitle());
                            viewHolder.mylocation.setText(model.getPrivateContent().getLocation().getName());
                            if(model.getPublicContent()!=null && model.getPublicContent().getNumberofView()>0){
                                viewHolder.button_viewer.setText(String.valueOf(model.getPublicContent().getNumberofView()));
                            }else {
                                viewHolder.button_viewer.setText(String.valueOf(0));
                            }


                            viewHolder.category.setText(categoriesArray[model.getCategoriesDeal().getCatNumber() + 1]);
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
                                        startActivity(new Intent(MyDealsActivity.this,SingleDealActivityActivity.class)
                                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                .putExtra("user_uid",model.getPrivateContent().getCreatorid())
                                                .putExtra("dealid",model.getPrivateContent().getUniquefirebaseId()));

                                        // open dealActivity
                                    }


                                }
                            });

                            DecimalFormat decFmt = new DecimalFormat("#,###.##", DecimalFormatSymbols.getInstance(Locale.GERMAN));
                            decFmt.setMaximumFractionDigits(2);
                            decFmt.setMinimumFractionDigits(2);

                            String p=model.getPrivateContent().getPrice();
                            BigDecimal amt = new BigDecimal(p);
                            String preValue = decFmt.format(amt);


                            if(currencyArray!=null){
                                viewHolder.price.setText(preValue + " " + currencyArray[getCurrencyPosition(model.getPrivateContent().getCurrency())]);

                            }else {
                                viewHolder.price.setText(preValue + " " + model.getPrivateContent().getCurrency());

                            }
                            Date date = new Date(model.getPrivateContent().getTimeofCreation());
                            DateFormat formatter = new SimpleDateFormat("HH:mm");
                            String dateFormatted = formatter.format(date);
                            CheckTimeStamp checkTimeStamp= new CheckTimeStamp(getApplicationContext(),model.getPrivateContent().getTimeofCreation());

                            viewHolder.time.setText(checkTimeStamp.checktime());

                            viewHolder.button_delete.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (!haveNetworkConnection()){
                                        Toast.makeText(getApplicationContext(),getString(R.string.connection_to_server_not_aviable)
                                                ,Toast.LENGTH_SHORT).show();
                                    }else {
                                        showProgressbar();
                                        deleteDeal(model);
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

                            viewHolder.button_edit.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    if (!haveNetworkConnection()){
                                        Toast.makeText(getApplicationContext(),getString(R.string.connection_to_server_not_aviable)
                                                ,Toast.LENGTH_SHORT).show();
                                    }else {
                                        startActivity(new Intent(MyDealsActivity.this,CreateAndModifyDealsActivity.class)
                                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                .putExtra("dealToedit",model.getPrivateContent().getUniquefirebaseId()));
                                    }

                                }
                            });

                            viewHolder.button_overview.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (!haveNetworkConnection()){
                                        Toast.makeText(getApplicationContext(),getString(R.string.connection_to_server_not_aviable)
                                                ,Toast.LENGTH_SHORT).show();
                                    }else {
                                        startActivity(new Intent(MyDealsActivity.this,ViewContentDealActivity.class)
                                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                .putExtra("user_uid",model.getPrivateContent().getCreatorid())
                                                .putExtra("post",model.getPrivateContent().getUniquefirebaseId())
                                                .putExtra("location",model.getPrivateContent().getLocation().getName())
                                                .putExtra("categorie",model.getPrivateContent().getCategorie().getName()));
                                    }


                                }
                            });

                            viewHolder.button_promote.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Toast.makeText(getApplicationContext(), getString(R.string.string_toast_text_sharing_unavialable), Toast.LENGTH_SHORT).show();

                                }
                            });
                        }
                    }
                };

        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();


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
    private void deleteDeal(final Deals post) {

        StorageReference storageReference= FirebaseStorage.getInstance().getReference();

        storageReference.child(user.getUid()).child(ConfigApp.FIREBASE_APP_URL_USERS_DEAL)
                .child(post.getPrivateContent().getUniquefirebaseId())
                .child(post.getPrivateContent().getTitle())
                .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }
        });


        userSharedPreference.reduceNumberofAds();

        final DatabaseReference reference=FirebaseDatabase.getInstance().getReference();

        reference.child(ConfigApp.FIREBASE_APP_URL_USERS_DEAL)
                .child(post.getPrivateContent().getUniquefirebaseId()).child("categoriesDeal").removeValue();
        reference.child(ConfigApp.FIREBASE_APP_URL_USERS_DEAL)
                .child(post.getPrivateContent().getUniquefirebaseId()).child("publicContent").removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        reference.child(ConfigApp.FIREBASE_APP_URL_USERS).child(user.getUid()).child("/userPublic/numberOfAds").setValue(userSharedPreference.getUserNumberofAds());
                        reference.child(ConfigApp.FIREBASE_APP_URL_DEAL_EXIST).child(post.getPrivateContent().getUniquefirebaseId()).removeValue()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        reference.child(ConfigApp.FIREBASE_APP_URL_USERS_DEAL)
                                                .child(post.getPrivateContent().getUniquefirebaseId()).child("privateContent").removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        reference.child(ConfigApp.FIREBASE_APP_URL_USERS_DEAL_USER).child(user.getUid()).child(post.getPrivateContent().getUniquefirebaseId()).child("publicContent").removeValue();
                                                        reference.child(ConfigApp.FIREBASE_APP_URL_USERS_DEAL_USER).child(user.getUid()).child(post.getPrivateContent().getUniquefirebaseId()).child("offers").removeValue();
                                                        reference.child(ConfigApp.FIREBASE_APP_URL_USERS_DEAL_USER).child(user.getUid()).child(post.getPrivateContent().getUniquefirebaseId()).child("categoriesDeal").removeValue();
                                                        reference.child(ConfigApp.FIREBASE_APP_URL_USERS_DEAL_USER).child(user.getUid()).child(post.getPrivateContent().getUniquefirebaseId()).child("privateContent").removeValue()
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if(task.isComplete() && task.isSuccessful()){
                                                                            Toast.makeText(getApplicationContext(),getString(R.string.string_toast_text_deleted),Toast.LENGTH_SHORT).show();
                                                                            if (progressBar!=null){
                                                                                progressBar.dismiss();

                                                                            }
                                                                        }else {
                                                                            //error
                                                                            Toast.makeText(getApplicationContext(),getString(R.string.string_toast_text_error),Toast.LENGTH_SHORT).show();
                                                                            dismissProgressbar();
                                                                        }
                                                                    }
                                                                });
                                                    }
                                                });
                                    }
                                });

                    }
                });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_myposts,menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_add_a_post:
                    startActivity(new Intent(MyDealsActivity.this,CreateAndModifyDealsActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));


                return true;

        }
        return super.onOptionsItemSelected(item);

    }

    public static class MyDealViewHolder extends RecyclerView.ViewHolder{
        ImageView postPicture;
        TextView titel, time, price, mylocation,category;
        private View view;
        private Button button_viewer,button_edit,button_delete,button_promote,button_share
                ,button_overview;


        public MyDealViewHolder(View itemView) {
            super(itemView);
            view=itemView;

            postPicture=(ImageView) itemView.findViewById(R.id.imageView_publicationFirstphoto_mydeal_cardview);

            button_delete=(Button) itemView.findViewById(R.id.button_delete_mydeal_cardview);
            button_edit=(Button) itemView.findViewById(R.id.button_edit_mydeal_cardview);
            button_promote=(Button) itemView.findViewById(R.id.button_promote_mydeal_cardview);
            button_share=(Button) itemView.findViewById(R.id.button_share_mydeal_cardview);
            button_viewer=(Button) itemView.findViewById(R.id.button_mydeal_cardview_viewer);
            button_overview=(Button)itemView.findViewById(R.id.button_overview_mydeal_cardview);

            category=(TextView)itemView.findViewById(R.id.textView_publication_category_deal);
            titel=(TextView) itemView.findViewById(R.id.textView_publication_title_mydeal_cardview);
            time=(TextView) itemView.findViewById(R.id.textView_publication_time_mydeal_cardview);
            price=(TextView) itemView.findViewById(R.id.textView_publication_price_mydeal_cardview);
            mylocation=(TextView) itemView.findViewById(R.id.textView_publication_location_mydeal_cardview);


        }
    }


    protected void onStop() {
        super.onStop();
        dismissProgressbar();
    }

    private int getCurrencyPosition(String currency){
        if(currency.equals(getString(R.string.currency_xaf))
                || currency.equals("F CFA") || currency.equals("XAF")){
            return 0;
        }
        return 0;

    }
}
