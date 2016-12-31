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

import com.firebase.ui.auth.AuthUI;
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
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MyPostActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private FirebaseAuth auth;
    private DatabaseReference root;
    private FirebaseUser user;
    private ProgressDialog progressBar;
    private UserSharedPreference userSharedPreference;
    private String[] currencyArray;
    private ShareActionProvider mShareActionProvider;
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
        setContentView(R.layout.activity_my_post);

        userSharedPreference=new UserSharedPreference(this);
        auth=FirebaseAuth.getInstance();
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

                startActivity(new Intent(this,MainActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                Intent.FLAG_ACTIVITY_NEW_TASK));
                finish();
            }
        }
        currencyArray=getResources().getStringArray(R.array.currency);


        root= FirebaseDatabase.getInstance().getReference().child(ConfigApp.FIREBASE_APP_URL_USERS_POSTS_USER);

        recyclerView=(RecyclerView)findViewById(R.id.recyclerview_mypost);
        layoutManager=new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);


    }


    private void showProgressbar(){
        progressBar = new ProgressDialog(this);
        progressBar.setCancelable(false);
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.setMessage(getString(R.string.progress_dialog_loading));
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

        fetchMyPost();


    }

    @Override
    protected void onPause() {
        super.onPause();
        dismissProgressbar();
    }

    private void fetchMyPost() {

        showProgressbar();
        final Query reference= root.child(user.getUid());
        reference.keepSynced(true);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChildren()){
                    dismissProgressbar();
                    if (!haveNetworkConnection()){
                        Toast.makeText(getApplicationContext(),getString(R.string.alertDialog_no_internet_connection),Toast.LENGTH_SHORT).show();
                        reference.removeEventListener(this);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                reference.removeEventListener(this);
                Toast.makeText(getApplicationContext(),databaseError.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
        FirebaseRecyclerAdapter<Publication,MyPublicationViewHolder> adapter=
                new FirebaseRecyclerAdapter<Publication, MyPublicationViewHolder>(
                        Publication.class,
                        R.layout.mypost_cardview,
                        MyPublicationViewHolder.class,
                        reference
                ) {
                    @Override
                    protected void populateViewHolder(final MyPublicationViewHolder viewHolder, final Publication model, int position) {

                        assert model!=null;
                        if(getItemCount()==0){
                            dismissProgressbar();
                        }else if (position==getItemCount()-1){
                            dismissProgressbar();
                        }
                        if(model.getPrivateContent()!=null && model.getPublicContent()!=null){
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
                                    startActivity(new Intent(MyPostActivity.this,ViewContentActivity.class)
                                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            .putExtra("post",model.getPrivateContent().getUniquefirebaseId())
                                            .putExtra("location",model.getPrivateContent().getLocation().getName())
                                            .putExtra("categorie",model.getPrivateContent().getCategorie().getName()));

                                }
                            });

                            DecimalFormat decFmt = new DecimalFormat("#,###.##", DecimalFormatSymbols.getInstance(Locale.FRENCH));
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

                            viewHolder.button_delete.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (!haveNetworkConnection()){
                                        Toast.makeText(getApplicationContext(),getString(R.string.connection_to_server_not_aviable)
                                                ,Toast.LENGTH_SHORT).show();
                                        Toast.makeText(getApplicationContext(),getString(R.string.connection_to_server_not_aviable_cannot_delete_deal)
                                                ,Toast.LENGTH_SHORT).show();
                                    }else {
                                        showProgressbar();
                                        deletePost(model);
                                    }

                                }
                            });

                            viewHolder.button_share.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    onInviteClicked();
                                }
                            });

                            viewHolder.button_edit.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    startActivity(new Intent(MyPostActivity.this,CreateAndModifyPublicationActivity.class)
                                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            .putExtra("postToedit",model.getPrivateContent().getUniquefirebaseId()));

                                }
                            });


                            viewHolder.button_promote.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Toast.makeText(getApplicationContext(),getString(R.string.action_not_avialable_or_offline),Toast.LENGTH_SHORT).show();

                                }
                            });
                        }
                    }
                };

        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        if (!haveNetworkConnection()){
            dismissProgressbar();
            Toast.makeText(getApplicationContext(),getString(R.string.alertDialog_no_internet_connection),Toast.LENGTH_SHORT).show();
        }
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
                startActivity(new Intent(MyPostActivity.this,CreateAndModifyPublicationActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));


                return true;

        }
        return super.onOptionsItemSelected(item);

    }

    public static class MyPublicationViewHolder extends RecyclerView.ViewHolder{
        ImageView postPicture;
        TextView titel, time, price, mylocation,isnegotiable;
        private View view;
        private Button button_viewer,button_edit,button_delete,button_promote,button_share;


        public MyPublicationViewHolder(View itemView) {
            super(itemView);
            view=itemView;

            postPicture=(ImageView) itemView.findViewById(R.id.imageView_publicationFirstphoto_mypost_cardview);

            button_delete=(Button) itemView.findViewById(R.id.button_delete_mypost_cardview);
            button_edit=(Button) itemView.findViewById(R.id.button_edit_mypost_cardview);
            button_promote=(Button) itemView.findViewById(R.id.button_promote_mypost_cardview);
            button_share=(Button) itemView.findViewById(R.id.button_share_mypost_cardview);
            button_viewer=(Button) itemView.findViewById(R.id.button_mypost_cardview_viewer);

            isnegotiable=(TextView)itemView.findViewById(R.id.textView_publication_is_negotiable_mypost_cardview);
            titel=(TextView) itemView.findViewById(R.id.textView_publication_title_mypost_cardview);
            time=(TextView) itemView.findViewById(R.id.textView_publication_time_mypost_cardview);
            price=(TextView) itemView.findViewById(R.id.textView_publication_price_mypost_cardview);
            mylocation=(TextView) itemView.findViewById(R.id.textView_publication_location_mypost_cardview);


        }
    }

    //save photo differently


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

    private void deletePost(final Publication post) {

        StorageReference storageReference= FirebaseStorage.getInstance().getReference();

        storageReference.child(user.getUid()).child(ConfigApp.FIREBASE_APP_URL_USERS_POSTS)
                .child(post.getPrivateContent().getUniquefirebaseId())
                .child(post.getPrivateContent().getTitle())
                .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }
        });



        userSharedPreference.reduceNumberofAds();

        final DatabaseReference reference=FirebaseDatabase.getInstance().getReference();

        reference.child(ConfigApp.FIREBASE_APP_URL_REGIONS).child(post.getPrivateContent().getLocation().getName()).child(post.getPrivateContent().getCategorie().getName())
                .child(post.getPrivateContent().getUniquefirebaseId()).child("publicContent").removeValue()
        .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                reference.child(ConfigApp.FIREBASE_APP_URL_USERS).child(user.getUid()).child("/userPublic/numberOfAds").setValue(userSharedPreference.getUserNumberofAds());
                reference.child(ConfigApp.FIREBASE_APP_URL_POSTS_EXIST).child(post.getPrivateContent().getUniquefirebaseId()).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        reference.child(ConfigApp.FIREBASE_APP_URL_USERS_POSTS_ALL_CITY)
                                .child(getString(R.string.fcm_notification_city)+String.valueOf(post.getPrivateContent().getLocation().getNumberLocation()))
                                .child(post.getPrivateContent().getUniquefirebaseId()).removeValue();

                        reference.child(ConfigApp.FIREBASE_APP_URL_REGIONS).child(post.getPrivateContent().getLocation().getName()).child(post.getPrivateContent().getCategorie().getName())
                                .child(post.getPrivateContent().getUniquefirebaseId()).child("privateContent").removeValue()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        reference.child(ConfigApp.FIREBASE_APP_URL_USERS_POSTS_USER).child(user.getUid())
                                                .child(post.getPrivateContent().getUniquefirebaseId()).child("publicContent").removeValue();

                                        reference.child(ConfigApp.FIREBASE_APP_URL_USERS_POSTS_USER).child(user.getUid())
                                                .child(post.getPrivateContent().getUniquefirebaseId()).child("privateContent").removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        reference.child(ConfigApp.FIREBASE_APP_URL_USERS_POSTS)
                                                                .child(post.getPrivateContent().getUniquefirebaseId()).child("publicContent").removeValue()
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        reference.child(ConfigApp.FIREBASE_APP_URL_USERS_POSTS).child(post.getPrivateContent().getUniquefirebaseId())
                                                                                .child("privateContent").removeValue()
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
                });
            }
        });

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
