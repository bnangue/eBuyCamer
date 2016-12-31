package com.bricenangue.nextgeneration.ebuycamer;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.bricenangue.nextgeneration.ebuycamer.ConfigApp.getData;

public class ViewContentDealActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener,  View.OnClickListener {

    private static final int REQUEST_INVITE = 237;
    private Deals publication;
    private PublicationPhotos publicationPhotos;
    private FirebaseAuth auth;
    private DatabaseReference root;
    private String postUniqueFbId;
    private TextView textView_title, textView_price, textView_category, textView_description, textView_username,
            textView_user_type, textView_user_numberofAds, textView_my_current_offer,textView_all_offers;


    private ProgressDialog progressBar;
    private String myOffer, user_uid ="",creator_token;

    private Button button_location, button_time, button_viewer, buttonMakeoffer;
    private FirebaseUser user;
    private ViewPager intro_images;
    private LinearLayout pager_indicator;
    private int dotsCount;
    private ImageView[] dots;
    private ViewPagerAdapter mAdapter;
    private Toolbar toolbar;
    private UserSharedPreference userSharedPreference;
    private ArrayList<PublicationPhotos> arrayList = new ArrayList<>();
    private CircularImageView imageView_userPhoto;
    private DatabaseReference referenceFavorites;
    private boolean isInFavorite;

    private MenuItem menuItem;
    private boolean fromFav = false;

    private String[] currencyArray;
    private boolean loaded = false;
    private UserPublic userPublic;
    private static final int MY_PERMISSIONS_REQUEST_PHONE_CALL=237;
    private static final int REQUEST_ID_MULTIPLE_PERMISSIONS=1;
    private String[] categoriesArray;



    private ValueEventListener valueEventListener;
    private  DatabaseReference refcreator;
    private Button button_remove_offer;
    private ShareActionProvider mShareActionProvider;

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
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);

        userSharedPreference = new UserSharedPreference(this);

        setContentView(R.layout.activity_view_content_deal);
        intro_images = (ViewPager) findViewById(R.id.pager_introduction_deal);
        pager_indicator = (LinearLayout) findViewById(R.id.viewPagerCountDots_deal);
        toolbar = (Toolbar) findViewById(R.id.toolbar_view_content_deal);

        currencyArray = getResources().getStringArray(R.array.currency);
        categoriesArray = getResources().getStringArray(R.array.categories_array_activity);


        textView_title = (TextView) findViewById(R.id.textView_viewcontent_title_deal);
        textView_price = (TextView) findViewById(R.id.textView_viewcontent_price_deal);
        textView_description = (TextView) findViewById(R.id.textView_viewcontent_description_deal);
        textView_category = (TextView) findViewById(R.id.textView_viewcontent_category_deal);
        textView_username = (TextView) findViewById(R.id.textView_viewcontent_user_name_deal);
        textView_user_numberofAds = (TextView) findViewById(R.id.textView_viewcontent_numberofAds_deal);
        textView_my_current_offer=(TextView)findViewById(R.id.textView_viewcontent_offer_deal);
        textView_all_offers=(TextView)findViewById(R.id.textView_viewcontent_offer_deal_for_creator);

        button_location = (Button) findViewById(R.id.button_viewcontent_location_deal);
        button_viewer = (Button) findViewById(R.id.button_viewcontent_viewer_deal);
        button_time = (Button) findViewById(R.id.button_viewcontent_time_deal);
        button_remove_offer = (Button) findViewById(R.id.button_delete_my_offer_offer_deal);

        imageView_userPhoto = (CircularImageView) findViewById(R.id.imageView_viewcontent_userpic_deal);


        buttonMakeoffer = (Button) findViewById(R.id.button_viewcontent_makeoffer_deal);

        auth = FirebaseAuth.getInstance();
        userSharedPreference.setUserDataRefreshed(haveNetworkConnection());
        if (auth== null) {
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

        } else {
            user = auth.getCurrentUser();
        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);



        root = FirebaseDatabase.getInstance().getReference();
        referenceFavorites = root.child(ConfigApp.FIREBASE_APP_URL_USERS_FAVORITES).child(user.getUid());
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            postUniqueFbId = extras.getString("post");

            if(extras.containsKey("user_uid")){
                user_uid=extras.getString("user_uid");
            }
            if (extras.containsKey("FromFav")) {
                fromFav = extras.getBoolean("FromFav");
            }
        }
        if (savedInstanceState == null) {


        }

        buttonMakeoffer.setOnClickListener(this);




        if(user_uid!=null){
            refcreator =root.child(ConfigApp.FIREBASE_APP_URL_USERS).child(user_uid).child("userPublic/chatId");
            refcreator.keepSynced(true);
        }

        valueEventListener= new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                creator_token=dataSnapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Toast.makeText(getApplicationContext(),databaseError.getMessage(),Toast.LENGTH_SHORT).show();
            }
        };

    }

    public void ButtonRemoveMyOfferClicked(View view){
        if (!haveNetworkConnection()){

            Toast.makeText(getApplicationContext(),getString(R.string.action_not_avialable_or_offline),Toast.LENGTH_SHORT).show();
            Toast.makeText(getApplicationContext(),getString(R.string.connection_to_server_not_aviable)
                    ,Toast.LENGTH_SHORT).show();
        }else {
            final DatabaseReference referenceOffers=FirebaseDatabase.getInstance().getReference()
                    .child(ConfigApp.FIREBASE_APP_URL_USERS_DEAL_USER)
                    .child(publication.getPrivateContent().getCreatorid())
                    .child(publication.getPrivateContent().getUniquefirebaseId())
                    .child("offers");

            updateOffersListMinus(referenceOffers,publication.getPrivateContent().getUniquefirebaseId());
        }


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


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void checkifexist() {
        showProgressbar();

        dotsCount=0;
        DatabaseReference reffav = root.child(ConfigApp.FIREBASE_APP_URL_DEAL_EXIST)
                .child(postUniqueFbId);
        reffav.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot!=null) {
                    getPost();
                } else {
                    if (fromFav) {
                        if (haveNetworkConnection()){
                            Map<String, Object> childreen = new HashMap<>();
                            childreen.put("/" + ConfigApp.FIREBASE_APP_URL_USERS_FAVORITES + "/" + user.getUid()
                                    + "/" + postUniqueFbId, null);
                            childreen.put("/" + ConfigApp.FIREBASE_APP_URL_USERS_FAVORITES_USER + "/" + user.getUid()
                                    + "/" + postUniqueFbId, null);

                            root.updateChildren(childreen);

                            Toast.makeText(getApplicationContext(), getString(R.string.string_toast_viewcontent_Post_deleted)
                                    , Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(ViewContentDealActivity.this, MyFavoritesActivity.class)
                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                            finish();
                        }else {
                            startActivity(new Intent(ViewContentDealActivity.this, MyFavoritesActivity.class)
                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                            finish();
                        }



                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.string_toast_viewcontent_Post_deleted)
                                , Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(ViewContentDealActivity.this, ViewDealsActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                        finish();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                dismissProgressbar();
                Toast.makeText(getApplicationContext(),databaseError.getMessage()
                        ,Toast.LENGTH_SHORT).show();
                dismissProgressbar();
                finish();
            }
        });

        if (!haveNetworkConnection()){
            dismissProgressbar();
            Toast.makeText(getApplicationContext(),getString(R.string.alertDialog_no_internet_connection),Toast.LENGTH_SHORT).show();
        }
    }

    private void getPost() {



        if (postUniqueFbId != null) {

            refcreator.addValueEventListener(valueEventListener);

            if(user_uid!=null && user_uid.equals(user.getUid())){
                DatabaseReference reference = root.child(ConfigApp.FIREBASE_APP_URL_USERS_DEAL_USER)
                        .child(user_uid)
                        .child(postUniqueFbId);
                reference.keepSynced(true);
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        publication = dataSnapshot.getValue(Deals.class);
                        if(publication!=null){
                            populate(publication);
                            if (publication.getPrivateContent().getCreatorid().equals(user.getUid())) {
                                if (menuItem != null) {
                                    menuItem.setEnabled(false);
                                    menuItem.setVisible(false);
                                    buttonMakeoffer.setVisibility(View.GONE);

                                }

                            } else {


                                buttonMakeoffer.setVisibility(View.VISIBLE);
                                DatabaseReference reference1 = root.child(ConfigApp.FIREBASE_APP_URL_USERS_FAVORITES_USER)
                                        .child(user.getUid()).child(publication.getPrivateContent().getUniquefirebaseId());
                                reference1.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.getValue() != null) {
                                            Boolean b = dataSnapshot.getValue(boolean.class);
                                            if (b) {
                                                isInFavorite = b;
                                                menuItem.setIcon(getResources().getDrawable(R.drawable.ic_star_white_36dp));
                                            }
                                        }

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                        dismissProgressbar();
                                    }
                                });
                            }
                        }else {
                            if (haveNetworkConnection()){
                                // the post doesn't exist or has been deleted
                                Toast.makeText(getApplicationContext(), getString(R.string.string_toast_viewcontent_Post_deleted)
                                        , Toast.LENGTH_SHORT).show();
                                finish();
                            }

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(getApplicationContext(),databaseError.getMessage()
                                ,Toast.LENGTH_SHORT).show();
                        dismissProgressbar();
                        finish();
                    }
                });
            }else {
                DatabaseReference reference = root.child(ConfigApp.FIREBASE_APP_URL_USERS_DEAL)
                        .child(postUniqueFbId);
               reference.keepSynced(true);

                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        publication = dataSnapshot.getValue(Deals.class);
                        if(publication!=null){
                            populate(publication);

                            if (publication.getPrivateContent().getCreatorid().equals(user.getUid())) {
                                if (menuItem != null) {
                                    menuItem.setEnabled(false);
                                    menuItem.setVisible(false);
                                    buttonMakeoffer.setVisibility(View.GONE);

                                }

                            } else {


                                buttonMakeoffer.setVisibility(View.VISIBLE);
                                DatabaseReference reference1 = root.child(ConfigApp.FIREBASE_APP_URL_USERS_FAVORITES_USER)
                                        .child(user.getUid()).child(publication.getPrivateContent().getUniquefirebaseId());
                                reference1.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.getValue() != null) {
                                            Boolean b = dataSnapshot.getValue(boolean.class);
                                            if (b) {
                                                isInFavorite = b;
                                                menuItem.setIcon(getResources().getDrawable(R.drawable.ic_star_white_36dp));
                                            }
                                        }

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                        dismissProgressbar();
                                    }
                                });
                            }
                        }else {
                            if (haveNetworkConnection()){
                                // the post doesn't exist or has been deleted
                                Toast.makeText(getApplicationContext(), getString(R.string.string_toast_viewcontent_Post_deleted)
                                        , Toast.LENGTH_SHORT).show();
                                finish();
                            }

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(getApplicationContext(),databaseError.getMessage()
                                ,Toast.LENGTH_SHORT).show();
                        dismissProgressbar();
                        finish();
                    }
                });
            }


        }else {
            finish();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_view_content, menu);
        this.menuItem = menu.findItem(R.id.action_mark_as_favorite_viewcontent);
        MenuItem item = menu.findItem(R.id.action_share_viewcontent);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_mark_as_favorite_viewcontent:

                if (!haveNetworkConnection()){
                    Toast.makeText(getApplicationContext(),getString(R.string.connection_to_server_not_aviable)
                            ,Toast.LENGTH_SHORT).show();
                }
                    if (isInFavorite) {
                        if (publication != null) {
                            item.setIcon(getResources().getDrawable(R.drawable.ic_star_border_white_36dp));
                            isInFavorite = false;

                            if (!haveNetworkConnection()){
                                Toast.makeText(getApplicationContext(),
                                        getString(R.string.string_viewcontent_unmarked_as_favorite), Toast.LENGTH_SHORT).show();
                            }
                            Map<String, Object> childreen = new HashMap<>();
                            childreen.put("/" + ConfigApp.FIREBASE_APP_URL_USERS_FAVORITES + "/" + user.getUid()
                                    + "/" + publication.getPrivateContent().getUniquefirebaseId(), null);
                            childreen.put("/" + ConfigApp.FIREBASE_APP_URL_USERS_FAVORITES_USER + "/" + user.getUid()
                                    + "/" + publication.getPrivateContent().getUniquefirebaseId(), null);

                            root.updateChildren(childreen).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(getApplicationContext(),
                                            getString(R.string.string_viewcontent_unmarked_as_favorite), Toast.LENGTH_SHORT).show();


                                }
                            });
                        } else {
                            finish();
                            Toast.makeText(getApplicationContext(), getString(R.string.string_viewcontent_error_post_null), Toast.LENGTH_SHORT).show();

                        }
                    } else {
                        if (publication != null) {
                            item.setIcon(getResources().getDrawable(R.drawable.ic_star_white_36dp));
                            isInFavorite = true;
                            if (!haveNetworkConnection()){
                                Toast.makeText(getApplicationContext(),
                                        getString(R.string.string_viewcontent_marked_as_favorite), Toast.LENGTH_SHORT).show();
                            }

                            Map<String, Object> childreen = new HashMap<>();
                            childreen.put("/" + ConfigApp.FIREBASE_APP_URL_USERS_FAVORITES + "/" + user.getUid()
                                    + "/" + publication.getPrivateContent().getUniquefirebaseId(), publication);
                            childreen.put("/" + ConfigApp.FIREBASE_APP_URL_USERS_FAVORITES_USER + "/" + user.getUid()
                                    + "/" + publication.getPrivateContent().getUniquefirebaseId(), true);

                            root.updateChildren(childreen).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(getApplicationContext(),
                                            getString(R.string.string_viewcontent_marked_as_favorite), Toast.LENGTH_SHORT).show();


                                }
                            });
                        } else {
                            Toast.makeText(getApplicationContext(), getString(R.string.string_viewcontent_error_post_null), Toast.LENGTH_SHORT).show();
                            dismissProgressbar();
                        }
                    }


                return true;

            case R.id.action_share_viewcontent:
              //  Toast.makeText(getApplicationContext(), getString(R.string.string_toast_text_sharing_unavialable), Toast.LENGTH_SHORT).show();

                onInviteClicked();
                return true;

            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUiPageViewController() {

        dotsCount = mAdapter.getCount();
        if (dotsCount > 0) {
            dots = new ImageView[dotsCount];

            for (int i = 0; i < dotsCount; i++) {
                dots[i] = new ImageView(this);
                dots[i].setImageDrawable(getResources().getDrawable(R.drawable.nonselecteditem_dot));

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

                params.setMargins(4, 0, 4, 0);

                pager_indicator.addView(dots[i], params);
            }

            dots[0].setImageDrawable(getResources().getDrawable(R.drawable.selecteditem_dot));
        }
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        if (mAdapter.getCount() > 0) {
            for (int i = 0; i < dotsCount; i++) {
                dots[i].setImageDrawable(getResources().getDrawable(R.drawable.nonselecteditem_dot));
            }

            dots[position].setImageDrawable(getResources().getDrawable(R.drawable.selecteditem_dot));
        }

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private void populate(final Deals publication) {

        if(publication.getPrivateContent().getCreatorid().equals(user.getUid())){
            textView_my_current_offer.setVisibility(View.GONE);
            TextView textView=(TextView)findViewById(R.id.textView_deal_my_offer);
            textView.setVisibility(View.GONE);
            button_remove_offer.setVisibility(View.GONE);
            textView_all_offers.setVisibility(View.VISIBLE);
            textView_all_offers.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // open offer
                    startActivity(new Intent(ViewContentDealActivity.this,SingleDealActivityActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            .putExtra("user_uid",publication.getPrivateContent().getCreatorid())
                            .putExtra("dealid",publication.getPrivateContent().getUniquefirebaseId()));
                }
            });
            if(publication.getOffers().getOffers()!=null){
                if(publication.getOffers().getOffers().size()==1){
                    String pub= getString(R.string.viewdeal_user_opening) +" "+ publication.getOffers().getOffers().size()
                            +" "+ getString(R.string.viewdeal_user_opening_offer);
                    textView_all_offers.setText(pub);
                }else {
                    String pub= getString(R.string.viewdeal_user_opening) +" "+ publication.getOffers().getOffers().size()
                            +" "+ getString(R.string.viewdeal_user_opening_offers);
                    textView_all_offers.setText(pub);
                }

            }else {
                String pub= getString(R.string.viewdeal_user_opening) +" 0 "+ getString(R.string.viewdeal_user_opening_offer);
                textView_all_offers.setText(pub);
            }


        }else {
            textView_my_current_offer.setVisibility(View.VISIBLE);
            TextView textView=(TextView)findViewById(R.id.textView_deal_my_offer);
            textView.setVisibility(View.VISIBLE);
            textView_all_offers.setVisibility(View.GONE);
            DatabaseReference refOffer=root.child(ConfigApp.FIREBASE_APP_URL_USERS_OFFERS_USER)
                    .child(user.getUid()).child(postUniqueFbId);
            refOffer.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot!=null){
                        myOffer=dataSnapshot.getValue(String.class);
                        if(myOffer!=null){
                            String offerprice=myOffer+ " " +currencyArray[getCurrencyPosition(publication.getPrivateContent().getCurrency())];
                            textView_my_current_offer.setText(offerprice);
                            button_remove_offer.setVisibility(View.VISIBLE);
                        }else {
                            textView_my_current_offer.setText(getString(R.string.view_deal_content_no_offers_yet));
                            button_remove_offer.setVisibility(View.GONE);

                        }

                    }else {
                        textView_my_current_offer.setText(getString(R.string.view_deal_content_no_offers_yet));
                        button_remove_offer.setVisibility(View.GONE);

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                    Toast.makeText(getApplicationContext(),databaseError.getMessage(),Toast.LENGTH_SHORT).show();
                }
            });
        }

        textView_title.setText(publication.getPrivateContent().getTitle());
        String cat= categoriesArray[publication.getCategoriesDeal().getCatNumber() + 1] +" ("+ getString(R.string.textView_category_deal)+ ")";
        textView_category.setText(cat);
        if (publication.getPrivateContent().getDescription() != null) {
            textView_description.setText(publication.getPrivateContent().getDescription());
        } else {
            textView_description.setText("");
        }


        DecimalFormat decFmt = new DecimalFormat("#,###.##", DecimalFormatSymbols.getInstance(Locale.FRENCH));
        decFmt.setMaximumFractionDigits(2);
        decFmt.setMinimumFractionDigits(2);

        String p = publication.getPrivateContent().getPrice();
        BigDecimal amt = new BigDecimal(p);
        String preValue = decFmt.format(amt);


        textView_price.setText(getString(R.string.string_price_of_deal_negotiable));

        Date date = new Date(publication.getPrivateContent().getTimeofCreation());
        DateFormat formatter = new SimpleDateFormat("HH:mm");
        final String dateFormatted = formatter.format(date);
        CheckTimeStamp checkTimeStamp= new CheckTimeStamp(getApplicationContext(),publication.getPrivateContent().getTimeofCreation());

        button_time.setText(checkTimeStamp.checktime());
        button_location.setText(publication.getPrivateContent().getLocation().getName());
        if (publication.getPublicContent().getNumberofView() > 0) {
            button_viewer.setText(String.valueOf(publication.getPublicContent().getNumberofView()));
        } else {
            button_viewer.setText(String.valueOf(0));
        }

        DatabaseReference refuser = FirebaseDatabase.getInstance().getReference()
                .child(ConfigApp.FIREBASE_APP_URL_USERS).child(publication.getPrivateContent().getCreatorid())
                .child("userPublic");
        refuser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userPublic = dataSnapshot.getValue(UserPublic.class);
                if (dataSnapshot.hasChild("name") && !dataSnapshot.child("name").getValue(String.class).isEmpty()) {

                    textView_username.setText(dataSnapshot.child("name").getValue(String.class));

                } else if ((dataSnapshot.hasChild("name") && (dataSnapshot.child("name").getValue(String.class).isEmpty()
                        && dataSnapshot.hasChild("email")))
                        || (!dataSnapshot.hasChild("name") && dataSnapshot.hasChild("email"))) {

                    textView_username.setText(dataSnapshot.child("email").getValue(String.class));
                }

                if (dataSnapshot.hasChild("numberOfAds")) {
                    if (dataSnapshot.child("numberOfAds").getValue() != null) {
                        String ads = String.valueOf(dataSnapshot.child("numberOfAds").getValue(long.class))
                                + " " + getString(R.string.viewcontent_user_numberof_Ads);
                        textView_user_numberofAds.setText(ads);
                    } else {
                        String ads = "0 " + getString(R.string.viewcontent_user_numberof_Ads);
                        textView_user_numberofAds.setText(ads);
                    }
                }
                if (dataSnapshot.hasChild("profilePhotoUri")) {
                    loadPicture(dataSnapshot.child("profilePhotoUri").getValue(String.class));
                    loaded = true;
                }
                dismissProgressbar();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                dismissProgressbar();
            }
        });


        if (publication.getPrivateContent().getPublictionPhotos() != null) {
            mAdapter = new ViewPagerAdapter(ViewContentDealActivity.this, publication.getPrivateContent().getPublictionPhotos());
            intro_images.setAdapter(mAdapter);
            intro_images.setCurrentItem(0);
            intro_images.setOnPageChangeListener(this);
            setUiPageViewController();
        } else {
            mAdapter = new ViewPagerAdapter(ViewContentDealActivity.this, arrayList);
            intro_images.setAdapter(mAdapter);
            intro_images.setCurrentItem(0);
            intro_images.setOnPageChangeListener(this);
            setUiPageViewController();
        }


        // if no picture check if facebook picture avialable
        if (!loaded && publication.getPrivateContent().getCreatorid().equals(user.getUid())) {
            String facebookUserId = "";
            List<? extends UserInfo> list=user.getProviderData();
            String providerId=list.get(1).getProviderId();

            if (providerId.equals(getString(R.string.facebook_provider_id))) {
                facebookUserId = list.get(1).getUid();
            }

            // construct the URL to the profile picture, with a custom height
            // alternatively, use '?type=small|medium|large' instead of ?height=
            final String photoUrl = "https://graph.facebook.com/" + facebookUserId + "/picture?type=large";

            // (optional) use Picasso to download and show to image
            loadPicture(photoUrl);
        }


    }

    private void loadPicture(final String photoUrl) {
        Picasso.with(getApplicationContext()).load(photoUrl).networkPolicy(NetworkPolicy.OFFLINE)

                .into(imageView_userPhoto, new Callback() {
                    @Override
                    public void onSuccess() {
                        dismissProgressbar();
                    }

                    @Override
                    public void onError() {
                        Picasso.with(getApplicationContext()).load(photoUrl)
                                .into(imageView_userPhoto);

                        dismissProgressbar();
                    }
                });

    }

    private int getCurrencyPosition(String currency) {
        if (currency.equals(getString(R.string.currency_xaf))
                || currency.equals("F CFA") || currency.equals("XAF")) {
            return 0;
        }
        return 0;

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onStart() {
        super.onStart();
        checkifexist();
    }


    @Override
    protected void onPause() {
        super.onPause();
        pager_indicator.removeAllViews();
        if(refcreator!=null){
            refcreator.removeEventListener(valueEventListener);
        }
        dismissProgressbar();
    }

    @Override
    protected void onStop() {
        super.onStop();
        pager_indicator.removeAllViews();
        dismissProgressbar();
        if(refcreator!=null){
            refcreator.removeEventListener(valueEventListener);
        }


    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.button_viewcontent_makeoffer_deal:
                startOffersMaking();
                break;

        }
    }

    private void startOffersMaking() {
        if(publication!=null){
            final DatabaseReference referenceOffers=FirebaseDatabase.getInstance().getReference()
                    .child(ConfigApp.FIREBASE_APP_URL_USERS_DEAL_USER)
                    .child(publication.getPrivateContent().getCreatorid())
                    .child(publication.getPrivateContent().getUniquefirebaseId())
                    .child("offers");


            final AlertDialog alert = new AlertDialog.Builder(this).create();

            final EditText edittext = new EditText(this);
            edittext.setInputType(InputType.TYPE_CLASS_NUMBER);
            alert.setMessage(getString(R.string.alertDialog_viewDeal_make_offer_message));
            alert.setTitle(getString(R.string.alertDialog_viewDeal_make_offer_title));

            alert.setView(edittext);

            alert.setButton(DialogInterface.BUTTON_POSITIVE,getString(R.string.alertDialog_viewDeal_make_offer_button_ok)
                    , new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    //What ever you want to do with the value
                    //OR
                    String price = edittext.getText().toString();
                    if(TextUtils.isEmpty(price)){
                        edittext.setError(getString(R.string.maimpage_alertdialog_edittext_error_empty));
                        edittext.requestFocus();
                        edittext.performClick();

                    }else {
                        if (!haveNetworkConnection()){
                            Toast.makeText(getApplicationContext(),getString(R.string.action_not_avialable_or_offline),Toast.LENGTH_SHORT).show();

                            Toast.makeText(getApplicationContext(),getString(R.string.connection_to_server_not_aviable)
                                    ,Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }else {
                            updateOffersList(referenceOffers,price,publication.getPrivateContent().getUniquefirebaseId());
                            dialog.dismiss();
                        }

                    }
                }
            });

            alert.setButton(DialogInterface.BUTTON_NEUTRAL,getString(R.string.alertDialog_viewDeal_make_offer_button_cancel)
                    , new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // what ever you want to do with No option.
                    dialog.dismiss();

                }
            });

            alert.show();

        }

    }

    private void updateOffersList(DatabaseReference DealRef, final String price, final String dealId) {
        DealRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                final DealsOffers p = mutableData.getValue(DealsOffers.class);
                if (p == null) {

                    return Transaction.success(mutableData);
                }

                if(!p.getOffers().containsKey(auth.getCurrentUser().getUid())){
                    long offer = p.getNumberOfoffers();
                    offer = offer + 1;
                    p.setNumberOfoffers(offer);
                }


                HashMap<String,Offer> map=p.getOffers();
                if(map!=null){
                    map.put(auth.getCurrentUser().getUid(),new Offer(price,System.currentTimeMillis()));
                    p.setOffers(map);
                }else {
                    map=new HashMap<String, Offer>();
                    map.put(auth.getCurrentUser().getUid(),new Offer(price,System.currentTimeMillis()));
                    p.setOffers(map);
                }




                // Set value and report transaction success
                mutableData.setValue(p);
                DatabaseReference refMYoffers=FirebaseDatabase.getInstance().getReference()
                        .child(ConfigApp.FIREBASE_APP_URL_USERS_OFFERS_USER)
                        .child(user.getUid())
                        .child(dealId);
                refMYoffers.setValue(price);



                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                if(b){
                    sendNotification(getString(R.string.fcm_Notification_new_offers_message),dealId);
                    String offerprice=price+ " " +currencyArray[getCurrencyPosition(publication.getPrivateContent().getCurrency())];
                    textView_my_current_offer.setText(offerprice);
                    button_remove_offer.setVisibility(View.VISIBLE);
                }
                // Transaction completed
                // Log.d(TAG, "postTransaction:onComplete:" + databaseError);
            }
        });
    }

    private void updateOffersListMinus(DatabaseReference DealRef, final String dealId) {
        DealRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                final DealsOffers p = mutableData.getValue(DealsOffers.class);
                if (p == null) {

                    return Transaction.success(mutableData);
                }

                if(p.getOffers().containsKey(user.getUid())){
                    long offer = p.getNumberOfoffers();
                    offer = offer - 1;
                    p.setNumberOfoffers(offer);

                    HashMap<String,Offer> map=p.getOffers();
                    if(map!=null){
                        map.remove(user.getUid());
                        p.setOffers(map);
                    }
                }

                // Set value and report transaction success
                mutableData.setValue(p);
                DatabaseReference refOffer=root.child(ConfigApp.FIREBASE_APP_URL_USERS_OFFERS_USER)
                        .child(user.getUid()).child(dealId);
                refOffer.removeValue();

                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                if(b){
                    button_remove_offer.setVisibility(View.GONE);
                    textView_my_current_offer.setVisibility(View.VISIBLE);
                    TextView textView=(TextView)findViewById(R.id.textView_deal_my_offer);
                    textView.setVisibility(View.VISIBLE);
                    textView_all_offers.setVisibility(View.GONE);
                    textView_my_current_offer.setText(getString(R.string.view_deal_content_no_offers_yet));


                }
                // Transaction completed
                // Log.d(TAG, "postTransaction:onComplete:" + databaseError);
            }
        });
    }

    private void sendNotification(String message,String post_id) {
        new SendNotification(message,post_id).execute();
    }


    public class SendNotification extends AsyncTask<Void,Void,Void>
    {

        String  message;
        String post_id;

        SendNotification(String message, String post_id){
            this.post_id=post_id;
            this.message=message;
        }
        @Override
        protected void onPostExecute(Void reponse) {
            super.onPostExecute(reponse);
        }

        @Override
        protected Void doInBackground(Void... params) {

            HttpURLConnection conn=null;
            try {
                ArrayList<Pair<String,String>> data=new ArrayList<>();


                String title=getString(R.string.fcm_Notification_new_offers);

                data.add(new Pair<String, String>("message", message));
                data.add(new Pair<String, String>("receivertoken",creator_token));
                data.add(new Pair<String, String>("post_id",post_id ));
                data.add(new Pair<String, String>("title",title ));


                byte[] bytes = getData(data).getBytes("UTF-8");


                URL url=new URL(ConfigApp.OOOWEBHOST_SERVER_URL+ "FirebasePushNewOffer.php");
                conn=(HttpURLConnection)url.openConnection();
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setFixedLengthStreamingMode(bytes.length);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded;charset=UTF-8");
                // post the request
                OutputStream out = conn.getOutputStream();
                out.write(bytes);
                out.close();

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuffer reponse = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    reponse.append(inputLine);
                }
                final String response =reponse.toString();
                System.out.print(response);

            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                if(conn!=null){
                    conn.disconnect();
                }
            }
            return null;
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
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        dismissProgressbar();
        finish();
    }
}
