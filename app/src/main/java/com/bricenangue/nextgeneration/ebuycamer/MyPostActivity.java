package com.bricenangue.nextgeneration.ebuycamer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
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



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_post);


        userSharedPreference=new UserSharedPreference(this);
        auth=FirebaseAuth.getInstance();
        if(auth!=null){
            user=auth.getCurrentUser();
        }else {
            startActivity(new Intent(MyPostActivity.this,MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        }
        currencyArray=getResources().getStringArray(R.array.currency);

        root= FirebaseDatabase.getInstance().getReference().child(ConfigApp.FIREBASE_APP_URL_USERS_POSTS_USER);
        recyclerView=(RecyclerView)findViewById(R.id.recyclerview_mypost);
        layoutManager=new LinearLayoutManager(this);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
    }


    @Override
    protected void onStart() {
        super.onStart();
        progressBar = new ProgressDialog(this);
        progressBar.setCancelable(false);
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.show();
        final Query reference= root.child(user.getUid());
        FirebaseRecyclerAdapter<Publication,MyPublicationViewHolder> adapter=
                new FirebaseRecyclerAdapter<Publication, MyPublicationViewHolder>(
                        Publication.class,
                        R.layout.mypost_cardview,
                        MyPublicationViewHolder.class,
                        reference
                ) {
                    @Override
                    protected void populateViewHolder(final MyPublicationViewHolder viewHolder, final Publication model, int position) {

                        //   viewHolder.mylocation.setText(model.getLocation().getName());
                        viewHolder.titel.setText(model.getPrivateContent().getTitle());
                        viewHolder.mylocation.setText(model.getPrivateContent().getLocation().getName());
                        if(model.getPublicContent().getNumberofView()>0){
                            viewHolder.button_viewer.setText(String.valueOf(model.getPublicContent().getNumberofView()));
                        }else {
                            viewHolder.button_viewer.setText(String.valueOf(0));
                        }



                        if(model.getPrivateContent().getPublictionPhotos()!=null){
                            Picasso.with(getApplicationContext()).load(model.getPrivateContent().getPublictionPhotos().get(0).getUri()).networkPolicy(NetworkPolicy.OFFLINE)
                                    .fit().centerInside()
                                    .into(viewHolder.postPicture, new Callback() {
                                        @Override
                                        public void onSuccess() {

                                            if (progressBar!=null){
                                                progressBar.dismiss();
                                            }
                                        }

                                        @Override
                                        public void onError() {
                                            Picasso.with(getApplicationContext()).load(model.getPrivateContent().getPublictionPhotos().get(0).getUri())
                                                    .fit().centerInside().into(viewHolder.postPicture);
                                            if (progressBar!=null){
                                                progressBar.dismiss();
                                            }
                                        }
                                    });

                        }else {
                            viewHolder.postPicture.setImageDrawable(getResources().getDrawable(R.mipmap.ic_launcher));
                        }

                        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                startActivity(new Intent(MyPostActivity.this,ViewContentActivity.class)
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        .putExtra("post",model.getPrivateContent().getUniquefirebaseId())
                                        .putExtra("location",model.getPrivateContent().getLocation().getName())
                                        .putExtra("categorie",model.getPrivateContent().getCategorie().getName()));

                                //updateViewer(root.child(model.getUniquefirebaseId()));

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
                        viewHolder.time.setText(dateFormatted);

                        viewHolder.button_delete.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                deletePost(model);
                            }
                        });

                        viewHolder.button_share.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

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

                            }
                        });
                    }
                };

        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        if(adapter.getItemCount()==0){
            if (progressBar!=null){
                progressBar.dismiss();
            }
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
        TextView titel, time, price, mylocation;
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

            titel=(TextView) itemView.findViewById(R.id.textView_publication_title_mypost_cardview);
            time=(TextView) itemView.findViewById(R.id.textView_publication_time_mypost_cardview);
            price=(TextView) itemView.findViewById(R.id.textView_publication_price_mypost_cardview);
            mylocation=(TextView) itemView.findViewById(R.id.textView_publication_location_mypost_cardview);


        }
    }

    //save photo differently

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
                .child(post.getPrivateContent().getUniquefirebaseId()).child("publicContent").setValue(null)
        .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                reference.child(ConfigApp.FIREBASE_APP_URL_USERS).child(user.getUid()).child("/userPublic/numberOfAds").setValue(userSharedPreference.getUserNumberofAds());
                reference.child(ConfigApp.FIREBASE_APP_URL_POSTS_EXIST).child(post.getPrivateContent().getUniquefirebaseId()).setValue(null);
                reference.child(ConfigApp.FIREBASE_APP_URL_REGIONS).child(post.getPrivateContent().getLocation().getName()).child(post.getPrivateContent().getCategorie().getName())
                        .child(post.getPrivateContent().getUniquefirebaseId()).child("privateContent").setValue(null)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        reference.child(ConfigApp.FIREBASE_APP_URL_USERS_POSTS_USER).child(user.getUid()).child(post.getPrivateContent().getUniquefirebaseId()).child("publicContent").setValue(null);

                        reference.child(ConfigApp.FIREBASE_APP_URL_USERS_POSTS_USER).child(user.getUid()).child(post.getPrivateContent().getUniquefirebaseId()).child("privateContent").setValue(null)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                reference.child(ConfigApp.FIREBASE_APP_URL_USERS_POSTS).child(post.getPrivateContent().getUniquefirebaseId()).child("publicContent").setValue(null)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        reference.child(ConfigApp.FIREBASE_APP_URL_USERS_POSTS).child(post.getPrivateContent().getUniquefirebaseId()).child("privateContent").setValue(null)
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
                                                            if (progressBar!=null){
                                                                progressBar.dismiss();
                                                            }
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


    private int getCurrencyPosition(String currency){
        if(currency.equals(getString(R.string.currency_xaf))
                || currency.equals("FCFA") || currency.equals("XAF")){
            return 0;
        }else if (currency.equals(getString(R.string.currency_euro))
                || currency.equals("EURO") || currency.equals("EUR")){
            return 1;
        }else if (currency.equals(getString(R.string.currency_usd))
                || currency.equals("DOLLAR") || currency.equals("USD")){
            return 2;
        }else{
            return 3;
        }

    }
}
