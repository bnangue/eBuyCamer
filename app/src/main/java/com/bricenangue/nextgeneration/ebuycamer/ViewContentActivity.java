package com.bricenangue.nextgeneration.ebuycamer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.common.api.BooleanResult;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
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
import java.util.Objects;

public class ViewContentActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener, View.OnClickListener {

    private Publication publication;
    private PublicationPhotos publicationPhotos;
    private FirebaseAuth auth;
    private DatabaseReference root;
    private String postUniqueFbId;
    private TextView textView_title, textView_price, textView_category, textView_description, textView_username, textView_user_type, textView_user_numberofAds;


    private ProgressDialog progressBar;

    private Button button_location, button_time, button_viewer, buttonCall, buttonSendMail;
    private FirebaseUser user;
    private ViewPager intro_images;
    private LinearLayout pager_indicator;
    private int dotsCount;
    private ImageView[] dots;
    private ViewPagerAdapter mAdapter;
    private Toolbar toolbar;
    private UserSharedPreference userSharedPreference;
    private String postCategorie;
    private ArrayList<PublicationPhotos> arrayList = new ArrayList<>();
    private ImageView imageView_userPhoto;
    private DatabaseReference referenceFavorites;
    private boolean isInFavorite;

    private MenuItem menuItem;
    private boolean fromFav = false;
    private String location;
    private int position;

    private String[] categoriesArray;
    private String[] currencyArray;
    private ShareActionProvider mShareActionProvider;
    private boolean loaded = false;
    private UserPublic userPublic;
    private static final int MY_PERMISSIONS_REQUEST_PHONE_CALL=237;
    private static final int REQUEST_ID_MULTIPLE_PERMISSIONS=1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);

        userSharedPreference = new UserSharedPreference(this);
        setContentView(R.layout.activity_view_content);
        intro_images = (ViewPager) findViewById(R.id.pager_introduction);
        pager_indicator = (LinearLayout) findViewById(R.id.viewPagerCountDots);
        toolbar = (Toolbar) findViewById(R.id.toolbar_view_content);

        categoriesArray = getResources().getStringArray(R.array.categories_array_activity);
        currencyArray = getResources().getStringArray(R.array.currency);

        textView_title = (TextView) findViewById(R.id.textView_viewcontent_title);
        textView_price = (TextView) findViewById(R.id.textView_viewcontent_price);
        textView_description = (TextView) findViewById(R.id.textView_viewcontent_description);
        textView_category = (TextView) findViewById(R.id.textView_viewcontent_category);
        textView_username = (TextView) findViewById(R.id.textView_viewcontent_user_name);
        textView_user_numberofAds = (TextView) findViewById(R.id.textView_viewcontent_numberofAds);

        button_location = (Button) findViewById(R.id.button_viewcontent_location);
        button_viewer = (Button) findViewById(R.id.button_viewcontent_viewer);
        button_time = (Button) findViewById(R.id.button_viewcontent_time);
        imageView_userPhoto = (ImageView) findViewById(R.id.imageView_viewcontent_userpic);


        buttonCall = (Button) findViewById(R.id.button_viewcontent_call);
        buttonSendMail = (Button) findViewById(R.id.button_viewcontent_sendmail);

        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(ViewContentActivity.this, MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            finish();

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
            postCategorie = extras.getString("categorie");
            location = extras.getString("location");
            if (extras.containsKey("FromFav")) {
                fromFav = extras.getBoolean("FromFav");
            }
            if (extras.containsKey("position")) {
                position = extras.getInt("position");
            }
        }
        if (savedInstanceState == null) {
            checkifexist();

        }
        buttonSendMail.setOnClickListener(this);
        buttonCall.setOnClickListener(this);

    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void checkifexist() {
        progressBar = new ProgressDialog(this);
        progressBar.setCancelable(false);
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.show();

        DatabaseReference reffav = root.child(ConfigApp.FIREBASE_APP_URL_POSTS_EXIST);
        reffav.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(postUniqueFbId)) {
                    getPost();
                } else {
                    if (fromFav) {
                        Map<String, Object> childreen = new HashMap<>();
                        childreen.put("/" + ConfigApp.FIREBASE_APP_URL_USERS_FAVORITES + "/" + user.getUid()
                                + "/" + postUniqueFbId, null);
                        childreen.put("/" + ConfigApp.FIREBASE_APP_URL_USERS_FAVORITES_USER + "/" + user.getUid()
                                + "/" + postUniqueFbId, null);

                        root.updateChildren(childreen);
                        Toast.makeText(getApplicationContext(), getString(R.string.string_toast_viewcontent_Post_deleted)
                                , Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(ViewContentActivity.this, MainPageActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                .putExtra("locationName", location).putExtra("category", postCategorie)
                                .putExtra("position", publication.getPrivateContent().getCategorie().getCatNumber()));
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.string_toast_viewcontent_Post_deleted)
                                , Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(ViewContentActivity.this, MainPageActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                .putExtra("locationName", location).putExtra("category", postCategorie)
                                .putExtra("position", publication.getPrivateContent().getCategorie().getCatNumber()));
                        finish();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void getPost() {

        if (postUniqueFbId != null) {
            DatabaseReference reference = root.child(ConfigApp.FIREBASE_APP_URL_REGIONS).child(location).child(postCategorie)
                    .child(postUniqueFbId);
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    publication = dataSnapshot.getValue(Publication.class);
                    populate(publication);
                    if (publication.getPrivateContent().getCreatorid().equals(user.getUid())) {
                        if (menuItem != null) {
                            menuItem.setEnabled(false);
                            menuItem.setVisible(false);
                            buttonSendMail.setVisibility(View.GONE);
                            buttonCall.setVisibility(View.GONE);

                        }

                    } else {


                        buttonSendMail.setVisibility(View.VISIBLE);
                        buttonCall.setVisibility(View.VISIBLE);
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

                            }
                        });
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
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

                if (isInFavorite) {
                    if (publication != null) {
                        Map<String, Object> childreen = new HashMap<>();
                        childreen.put("/" + ConfigApp.FIREBASE_APP_URL_USERS_FAVORITES + "/" + user.getUid()
                                + "/" + publication.getPrivateContent().getUniquefirebaseId(), null);
                        childreen.put("/" + ConfigApp.FIREBASE_APP_URL_USERS_FAVORITES_USER + "/" + user.getUid()
                                + "/" + publication.getPrivateContent().getUniquefirebaseId(), null);

                        root.updateChildren(childreen).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                item.setIcon(getResources().getDrawable(R.drawable.ic_star_border_white_36dp));
                                isInFavorite = false;
                            }
                        });
                    } else {
                        finish();
                        Toast.makeText(getApplicationContext(), getString(R.string.string_viewcontent_error_post_null), Toast.LENGTH_SHORT).show();

                    }
                } else {
                    if (publication != null) {
                        Map<String, Object> childreen = new HashMap<>();
                        childreen.put("/" + ConfigApp.FIREBASE_APP_URL_USERS_FAVORITES + "/" + user.getUid()
                                + "/" + publication.getPrivateContent().getUniquefirebaseId(), publication);
                        childreen.put("/" + ConfigApp.FIREBASE_APP_URL_USERS_FAVORITES_USER + "/" + user.getUid()
                                + "/" + publication.getPrivateContent().getUniquefirebaseId(), true);

                        root.updateChildren(childreen).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                item.setIcon(getResources().getDrawable(R.drawable.ic_star_white_36dp));
                                isInFavorite = true;
                            }
                        });
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.string_viewcontent_error_post_null), Toast.LENGTH_SHORT).show();
                    }
                }
                return true;

            case R.id.action_share_viewcontent:
                Toast.makeText(getApplicationContext(), getString(R.string.string_toast_text_sharing_unavialable), Toast.LENGTH_SHORT).show();
/**
 Intent sharingIntent = new Intent(Intent.ACTION_SEND);
 sharingIntent.setType("text/plain");
 final String photoUrl = "https://graph.facebook.com/picture?type=large";

 sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(photoUrl) );
 startActivity(Intent.createChooser(sharingIntent, getResources().getText(R.string.send_to))
 .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

 if(mShareActionProvider!=null){
 mShareActionProvider.setShareIntent(sharingIntent);
 }
 **/
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

    private void populate(Publication publication) {

        textView_title.setText(publication.getPrivateContent().getTitle());
        textView_category.setText(categoriesArray[publication.getPrivateContent().getCategorie().getCatNumber() + 1]);
        if (publication.getPrivateContent().getDescription() != null) {
            textView_description.setText(publication.getPrivateContent().getDescription());
        } else {
            textView_description.setText("");
        }

        DecimalFormat decFmt = new DecimalFormat("#,###.##", DecimalFormatSymbols.getInstance(Locale.GERMAN));
        decFmt.setMaximumFractionDigits(2);
        decFmt.setMinimumFractionDigits(2);

        String p = publication.getPrivateContent().getPrice();
        BigDecimal amt = new BigDecimal(p);
        String preValue = decFmt.format(amt);


        if (currencyArray != null) {
            textView_price.setText(preValue + " " + currencyArray[getCurrencyPosition(publication.getPrivateContent().getCurrency())]);

        } else {
            textView_price.setText(preValue + " " + publication.getPrivateContent().getCurrency());

        }
        Date date = new Date(publication.getPrivateContent().getTimeofCreation());
        DateFormat formatter = new SimpleDateFormat("HH:mm");
        final String dateFormatted = formatter.format(date);
        button_time.setText(dateFormatted);
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
                if (progressBar != null) {
                    progressBar.dismiss();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if (progressBar != null) {
                    progressBar.dismiss();
                }
            }
        });


        if (publication.getPrivateContent().getPublictionPhotos() != null) {
            mAdapter = new ViewPagerAdapter(ViewContentActivity.this, publication.getPrivateContent().getPublictionPhotos());
            intro_images.setAdapter(mAdapter);
            intro_images.setCurrentItem(0);
            intro_images.setOnPageChangeListener(this);
            setUiPageViewController();
        } else {
            mAdapter = new ViewPagerAdapter(ViewContentActivity.this, arrayList);
            intro_images.setAdapter(mAdapter);
            intro_images.setCurrentItem(0);
            intro_images.setOnPageChangeListener(this);
            setUiPageViewController();
        }


        // if no picture check if facebook picture avialable
        if (!loaded && publication.getPrivateContent().getCreatorid().equals(user.getUid())) {
            String facebookUserId = "";
            // find the Facebook profile and get the user's id
            for (UserInfo profile : user.getProviderData()) {
                // check if the provider id matches "facebook.com"
                if (profile.getProviderId().equals(getString(R.string.facebook_provider_id))) {
                    facebookUserId = profile.getUid();
                }
            }
            // construct the URL to the profile picture, with a custom height
            // alternatively, use '?type=small|medium|large' instead of ?height=
            final String photoUrl = "https://graph.facebook.com/" + facebookUserId + "/picture?type=large";

            // (optional) use Picasso to download and show to image
            loadPicture(photoUrl);
        }

        if (userPublic != null) {
            if (userPublic.getEmail() != null && !userPublic.getEmail().isEmpty()) {
                buttonSendMail.setEnabled(true);
                buttonSendMail.setOnClickListener(this);

            } else {
                buttonSendMail.setEnabled(false);
            }

            if (userPublic.getPhoneNumber() != null && !userPublic.getPhoneNumber().isEmpty()) {
                buttonCall.setEnabled(true);
                buttonCall.setOnClickListener(this);
            } else {
                buttonCall.setEnabled(false);
            }
        }
    }

    private void loadPicture(final String photoUrl) {
        Picasso.with(getApplicationContext()).load(photoUrl).networkPolicy(NetworkPolicy.OFFLINE)

                .into(imageView_userPhoto, new Callback() {
                    @Override
                    public void onSuccess() {
                        if (progressBar != null) {
                            progressBar.dismiss();
                        }
                    }

                    @Override
                    public void onError() {
                        Picasso.with(getApplicationContext()).load(photoUrl)
                                .into(imageView_userPhoto);

                        if (progressBar != null) {
                            progressBar.dismiss();
                        }
                    }
                });

    }

    private int getCurrencyPosition(String currency) {
        if (currency.equals(getString(R.string.currency_xaf))
                || currency.equals("FCFA") || currency.equals("XAF")) {
            return 0;
        } else if (currency.equals(getString(R.string.currency_euro))
                || currency.equals("EURO") || currency.equals("EUR")) {
            return 1;
        } else if (currency.equals(getString(R.string.currency_usd))
                || currency.equals("DOLLAR") || currency.equals("USD")) {
            return 2;
        } else {
            return 3;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onStart() {
        super.onStart();

    }


    @Override
    protected void onStop() {
        super.onStop();
        if (progressBar != null) {
            progressBar.dismiss();
        }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.button_viewcontent_sendmail:
                break;
            case R.id.button_viewcontent_call:
                assert userPublic.getPhoneNumber()!=null;
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + userPublic.getPhoneNumber()));

                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                startActivity(intent);
                 break;

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_PHONE_CALL: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private  boolean checkAndRequestPermissions() {
        int camera = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA);
        int storage = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int loc = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION);
        int loc2 = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        List<String> listPermissionsNeeded = new ArrayList<>();

        if (camera != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.CAMERA);
        }
        if (storage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (loc2 != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.ACCESS_FINE_LOCATION);

        }
        if (loc != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (!listPermissionsNeeded.isEmpty())
        {
            ActivityCompat.requestPermissions(this,listPermissionsNeeded.toArray
                    (new String[listPermissionsNeeded.size()]),REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }
}

