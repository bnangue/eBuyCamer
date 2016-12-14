package com.bricenangue.nextgeneration.ebuycamer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
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
        FirebaseRecyclerAdapter<Publication,MyPublicationFavViewHolder> adapter=
                new FirebaseRecyclerAdapter<Publication, MyPublicationFavViewHolder>(
                        Publication.class,
                        R.layout.myfavorite_cardview,
                        MyPublicationFavViewHolder.class,
                        reference
                ) {
                    @Override
                    protected void populateViewHolder(final MyPublicationFavViewHolder viewHolder, final Publication model, int position) {

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

                                startActivity(new Intent(MyFavoritesActivity.this,ViewContentActivity.class)
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        .putExtra("post",model.getPrivateContent().getUniquefirebaseId())
                                        .putExtra("location",model.getPrivateContent().getLocation().getName())
                                        .putExtra("FromFav",true)
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

                        viewHolder.button_remove.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                deletePostFromFavorite(model);
                            }
                        });

                        viewHolder.button_share.setOnClickListener(new View.OnClickListener() {
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

    private void deletePostFromFavorite(Publication model) {
        Map<String,Object> childreen=new HashMap<>();
        childreen.put("/"+ConfigApp.FIREBASE_APP_URL_USERS_FAVORITES+"/"+user.getUid()
                +"/"+model.getPrivateContent().getUniquefirebaseId(),null );
        childreen.put("/"+ConfigApp.FIREBASE_APP_URL_USERS_FAVORITES_USER+"/"+user.getUid()
                +"/"+model.getPrivateContent().getUniquefirebaseId(),null );


        DatabaseReference reference =FirebaseDatabase.getInstance().getReference();
        reference.updateChildren(childreen);
    }


    public static class MyPublicationFavViewHolder extends RecyclerView.ViewHolder{
        ImageView postPicture;
        TextView titel, time, price, mylocation;
        private View view;
        private Button button_viewer,button_remove,button_share;


        public MyPublicationFavViewHolder(View itemView) {
            super(itemView);
            view=itemView;

            postPicture=(ImageView) itemView.findViewById(R.id.imageView_publicationFirstphoto_myfavorite_cardview);

            button_share=(Button) itemView.findViewById(R.id.button_share_myfavorite_cardview);
            button_viewer=(Button) itemView.findViewById(R.id.button_myfavorite_cardview_viewer);
            button_remove=(Button) itemView.findViewById(R.id.button_promote_myfavorite_cardview);

            titel=(TextView) itemView.findViewById(R.id.textView_publication_title_myfavorite_cardview);
            time=(TextView) itemView.findViewById(R.id.textView_publication_time_myfavorite_cardview);
            price=(TextView) itemView.findViewById(R.id.textView_publication_price_myfavorite_cardview);
            mylocation=(TextView) itemView.findViewById(R.id.textView_publication_location_myfavorite_cardview);


        }
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
