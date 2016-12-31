package com.bricenangue.nextgeneration.ebuycamer;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CreateAndModifyDealsActivity extends AppCompatActivity {

    private static final int GALLERY_INTENT=1;
    private ArrayList<Uri> uris =new ArrayList<>();
    private ArrayList<PublicationPhotos> downloadUris=new ArrayList<>();
    private static final int CAMERA_INTENT=2;
    private android.support.v7.app.AlertDialog alertDialog;
    private EditText editTextDescription;
    private EditText editTextTitle;
    private EditText editTextPrice;
    private String categorie;
    private FirebaseAuth auth;
    private DatabaseReference root;
    private StorageReference rootStorage;

    private ProgressDialog progressBar;
    private UserSharedPreference userSharedPreference;
    private String dealToeditId;
    private String[] photonames={"photo1","photo2","photo3","photo4","photo5"};
    private PrivateContent postToedit;
    private RecyclerView recyclerViewHorizontal;
    private Spinner spinnerCategory;


    private Spinner spinnerCurrency;
    private ArrayList<Object> sortOptionslist;
    private ImageView imageViewAddNewImage;
    private LinearLayoutManager layoutManager;
    private HorizontalRecyclerViewAdapter recyclerViewAdapter;
    private String [] categoriesArray;
    private String categoryFromPost;




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

        setContentView(R.layout.activity_create_and_modify_deals);
        categorie=getString(R.string.textView_category_deal);
        Bundle extras =getIntent().getExtras();

        if(extras!=null && extras.containsKey("dealToedit")){
            dealToeditId=extras.getString("dealToedit");
        }

        categoriesArray=getResources().getStringArray(R.array.categories_arrays_create);

        userSharedPreference=new UserSharedPreference(this);
        userSharedPreference.setUserDataRefreshed(haveNetworkConnection());
        auth=FirebaseAuth.getInstance();
        if(auth==null){
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

        //to save the path in one language (english)

        root= FirebaseDatabase.getInstance().getReference();
        rootStorage= FirebaseStorage.getInstance().getReference();
        spinnerCurrency=(Spinner) findViewById(R.id.spinner_currency_deal);


        spinnerCategory=(Spinner)findViewById(R.id.spinner_choose_categorie_deal);


        editTextDescription=(EditText) findViewById(R.id.editText_description_Deal);
        editTextPrice=(EditText) findViewById(R.id.editText_price_Deal);
        editTextTitle=(EditText) findViewById(R.id.editText_title_Deal);

        recyclerViewHorizontal=(RecyclerView)findViewById(R.id.horizontal_recycler_view_create_Deal) ;
        layoutManager=new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false);

        recyclerViewHorizontal.setHasFixedSize(true);
        recyclerViewHorizontal.setLayoutManager(layoutManager);

        populateImages(uris);


        imageViewAddNewImage=(ImageView)findViewById(R.id.imageView_Choose_Image_Deal);
        imageViewAddNewImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getPicture();
            }
        });

        if(dealToeditId!=null && !dealToeditId.isEmpty()){
            populate();
        }

    }

    private void populate() {
        DatabaseReference reference=root.child(ConfigApp.FIREBASE_APP_URL_USERS_DEAL_USER)
                .child(auth.getCurrentUser().getUid()).child(dealToeditId);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Deals deal = dataSnapshot.getValue(Deals.class);
                if (deal!=null){
                    PrivateContent post=deal.getPrivateContent();
                    CategoriesDeal cat=deal.getCategoriesDeal();
                    if(dataSnapshot.hasChild("description")){
                        editTextDescription.setText(post.getDescription());
                    }

                    editTextPrice.setText(post.getPrice());
                    editTextTitle.setText(post.getTitle());

                    if(post.getPublictionPhotos()!=null){
                        for(PublicationPhotos uri : post.getPublictionPhotos()){
                            recyclerViewAdapter.addUri(Uri.parse(uri.getUri()));
                            //uris.add(Uri.parse(uri.getUri()));
                            //towWaysViewAdapter.notifyDataSetChanged();
                            if (recyclerViewAdapter!=null && recyclerViewAdapter.getItemCount()>=5){
                                imageViewAddNewImage.setEnabled(false);

                            }else {
                                imageViewAddNewImage.setEnabled(true);
                            }

                        }
                    }

                    int positionSpinner=cat.getCatNumber();

                    categoryFromPost=categoriesArray[cat.getCatNumber()];

                    spinnerCategory.setSelection(positionSpinner+1);

                    spinnerCategory.setEnabled(false);


                    spinnerCurrency.setSelection(getCurrencyPosition(post.getCurrency()));
                    spinnerCurrency.setEnabled(false);
                    postToedit=post;
                }else {
                    if (!haveNetworkConnection()){

                        Toast.makeText(getApplicationContext(),getString(R.string.alertDialog_no_internet_connection),Toast.LENGTH_SHORT).show();
                        finish();
                    }else {
                        Toast.makeText(getApplicationContext(), getString(R.string.string_toast_viewcontent_Post_deleted)
                                , Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), databaseError.getMessage()
                        , Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void populateImages(ArrayList<Uri> uris1){
        recyclerViewAdapter=new HorizontalRecyclerViewAdapter(this, uris1
                ,new HorizontalRecyclerViewAdapter.HorizontalAdapterClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                startActivity(new Intent(CreateAndModifyDealsActivity.this,
                        ViewImageFullScreenActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .putExtra("imageUri",uris.get(position).toString()));
            }

            @Override
            public void ondelteCLick(int position) {
                delete(position);

            }
        });

        recyclerViewHorizontal.setAdapter(recyclerViewAdapter);
    }

    private int getCurrencyPosition(String currency){
        if(currency.equals(getString(R.string.currency_xaf))
                || currency.equals("F CFA") || currency.equals("XAF")){
            return 0;
        }
        return 0;

    }

    public void ButtonConfirmActionPostDealClicked(View view){


        editTextDescription.setError(null);
        editTextTitle.setError(null);



        boolean cancel = false;
        View focusView = null;
        String title=editTextTitle.getText().toString();
        String price=editTextPrice.getText().toString();
        if (TextUtils.isEmpty(title)) {
            editTextTitle.setError(getString(R.string.error_field_required_title));
            focusView = editTextTitle;
            cancel = true;
        }

        if (!haveNetworkConnection()){
            Toast.makeText(getApplicationContext(),getString(R.string.connection_to_server_not_aviable)
                    ,Toast.LENGTH_SHORT).show();
        }else {
            // Check for a valid email address.
            if (TextUtils.isEmpty(price)) {
                editTextPrice.setError(getString(R.string.error_field_required_price));
                focusView = editTextPrice;
                cancel = true;
            }
            if(spinnerCategory.getSelectedItem().toString()
                    .equals(getString(R.string.string_create_new_publication_spinner_category_default))
                    ){
                focusView=spinnerCategory;
                cancel=true;
            }
            if (cancel) {
                // There was an error;
                focusView.requestFocus();
                focusView.performClick();
            } else {
                if(dealToeditId!=null && !dealToeditId.isEmpty()){


                    updateItem(dealToeditId);
                }else {
                    createAndSaveItem();
                }

            }
        }

    }
    private void  lockscreen(){
        ConfigApp.lockScreenOrientation(this);

    }
    private void unloockscreen(){
        ConfigApp.unlockScreenOrientation(this);
    }

    private void updateItem(final String postid) {
        lockscreen();
        progressBar = new ProgressDialog(this);
        progressBar.setCancelable(false);
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.show();
        final String title=editTextTitle.getText().toString();
        final String price=editTextPrice.getText().toString();
        final String description=editTextDescription.getText().toString();

        final String currency=spinnerCurrency.getSelectedItem().toString();
        categorie = categoriesArray[spinnerCategory.getSelectedItemPosition()-1];


        //updatechildren

        final ArrayList<Uri> arrayList =uris;

        if(arrayList.size()>0){
            final String firstimage=arrayList.get(0).toString();

            for(int i=0; i<arrayList.size(); i++){


                String downloadUri=arrayList.get(i).toString();
                PublicationPhotos p=new PublicationPhotos();
                p.setUri(downloadUri);
                downloadUris.add(p);

                if(downloadUris.size() == arrayList.size()){

                    Map<String, Object> childUpdates=new HashMap<String, Object>();

                    childUpdates.put("/title",title);
                    childUpdates.put("/description",description);
                    childUpdates.put("/price",price);
                    childUpdates.put("/currency",currency);
                    childUpdates.put("/publictionPhotos",downloadUris);
                    childUpdates.put("/firstPicture", firstimage);


                    root.child("Deals").child(postid).child("privateContent").updateChildren(childUpdates)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful() && task.isComplete()){
                                        Map<String, Object> childUpdates=new HashMap<String, Object>();

                                        childUpdates=new HashMap<String, Object>();

                                        childUpdates.put("/title",title);
                                        childUpdates.put("/description",description);
                                        childUpdates.put("/price",price);
                                        childUpdates.put("/currency",currency);
                                        childUpdates.put("/publictionPhotos",downloadUris);
                                        childUpdates.put("/firstPicture", firstimage);



                                        root.child(ConfigApp.FIREBASE_APP_URL_USERS_DEAL_USER).child(auth.getCurrentUser().getUid())
                                                .child(postid).child("privateContent").updateChildren(childUpdates)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        if(task.isSuccessful()){
                                                            Toast.makeText(getApplicationContext(),getString(R.string.string_toast_text_success),Toast.LENGTH_SHORT).show();
                                                            finish();
                                                            if (progressBar!=null){
                                                                progressBar.dismiss();
                                                                unloockscreen();
                                                            }
                                                        }else {
                                                            Toast.makeText(getApplicationContext(),getString(R.string.string_toast_text_error),Toast.LENGTH_SHORT).show();
                                                            if (progressBar!=null){
                                                                progressBar.dismiss();
                                                                unloockscreen();
                                                            }
                                                        }

                                                    }
                                                });

                                    }else {

                                        Toast.makeText(getApplicationContext(),getString(R.string.string_toast_text_error_city),Toast.LENGTH_SHORT).show();
                                        if (progressBar!=null){
                                            progressBar.dismiss();
                                            unloockscreen();
                                        }
                                        // error editing post in city with picture
                                    }
                                }
                            });

                }

            }
        }else {

            Map<String, Object> childUpdates=new HashMap<String, Object>();
            childUpdates.put("/title",title);
            childUpdates.put("/description",description);
            childUpdates.put("/price",price);
            childUpdates.put("/currency",currency);

            root.child("Deals").child(postid).child("privateContent").updateChildren(childUpdates)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isComplete() && task.isSuccessful()){
                                Map<String, Object> childUpdates=new HashMap<String, Object>();

                                childUpdates=new HashMap<String, Object>();

                                childUpdates.put("/title",title);
                                childUpdates.put("/description",description);
                                childUpdates.put("/price",price);
                                childUpdates.put("/currency",currency);


                                root.child(ConfigApp.FIREBASE_APP_URL_USERS_DEAL_USER).child(auth.getCurrentUser().getUid())
                                        .child(postid).child("privateContent").updateChildren(childUpdates)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    Toast.makeText(getApplicationContext(),getString(R.string.string_toast_text_success),Toast.LENGTH_SHORT).show();
                                                    finish();
                                                    if (progressBar!=null){
                                                        progressBar.dismiss();
                                                        unloockscreen();
                                                    }
                                                }else {
                                                    Toast.makeText(getApplicationContext(),getString(R.string.string_toast_text_error),Toast.LENGTH_SHORT).show();
                                                    if (progressBar!=null){
                                                        progressBar.dismiss();
                                                        unloockscreen();
                                                    }
                                                }
                                            }
                                        });
                            }else {
                                Toast.makeText(getApplicationContext(),getString(R.string.string_toast_text_error_city),Toast.LENGTH_SHORT).show();
                                if (progressBar!=null){
                                    progressBar.dismiss();
                                    unloockscreen();
                                }
                                //error editing post in  city
                            }
                        }
                    });

        }

    }

    private void createAndSaveItem() {
        lockscreen();
        progressBar = new ProgressDialog(this);
        progressBar.setCancelable(false);
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.show();
        String title=editTextTitle.getText().toString();
        final String price=editTextPrice.getText().toString();
        String description=editTextDescription.getText().toString();

        categorie=categoriesArray[spinnerCategory.getSelectedItemPosition()-1];

        final Deals post= new Deals();

        final PrivateContent privateContent= new PrivateContent();
        final PublicContent publicContent=new PublicContent();
        final DealsOffers dealsOffers=new DealsOffers();

        privateContent.setPrice(price);
        privateContent.setTitle(title);
        privateContent.setCurrency(spinnerCurrency.getSelectedItem().toString() );
        privateContent.setCategorie(new Categories(categorie,null,20));
        if(!description.isEmpty()){
            privateContent.setDescription(description);
        }
        privateContent.setLocation(userSharedPreference.getUserLocation());
        privateContent.setCreatorid(auth.getCurrentUser().getUid());
        //updatechildren
        final DatabaseReference ref = root;
        final String key= ref.push().getKey();
        privateContent.setUniquefirebaseId(key);
        privateContent.setTimeofCreation(System.currentTimeMillis());
        post.setCategoriesDeal(new CategoriesDeal(categorie,null,spinnerCategory.getSelectedItemPosition()-1));


        publicContent.setNumberoflikes(0);
        publicContent.setNumberofView(0);
        dealsOffers.setNumberOfoffers(0);
        userSharedPreference.addNumberofAds();



        final ArrayList<Uri> arrayList =uris;

        if(arrayList.size()>0){
            privateContent.setFirstPicture(arrayList.get(0).toString());

            for(int j=0; j<arrayList.size();j++){

                String downloadUri=arrayList.get(j).toString();
                PublicationPhotos p=new PublicationPhotos();
                p.setUri(downloadUri);
                downloadUris.add(p);
                if(downloadUris.size()==arrayList.size()){
                    privateContent.setPublictionPhotos(downloadUris);

                    post.setPrivateContent(privateContent);
                    post.setPublicContent(publicContent);
                    post.setOffers(dealsOffers);
                    //create in city
                    DatabaseReference r0= root.child(ConfigApp.FIREBASE_APP_URL_USERS_DEAL)
                            .child(key).child("privateContent");
                    r0.setValue(privateContent).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                DatabaseReference r1cat=root.child(ConfigApp.FIREBASE_APP_URL_USERS_DEAL)
                                        .child(key).child("categoriesDeal");
                                r1cat.setValue(post.getCategoriesDeal());

                                DatabaseReference r1=root.child(ConfigApp.FIREBASE_APP_URL_USERS_DEAL)
                                        .child(key).child("publicContent");
                                r1.setValue(publicContent).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            //create in users' posts
                                            root.child(ConfigApp.FIREBASE_APP_URL_USERS_DEAL_USER)
                                                    .child(auth.getCurrentUser().getUid()).child(key).child("publicContent").setValue(post.getPublicContent());
                                            root.child(ConfigApp.FIREBASE_APP_URL_USERS_DEAL_USER)
                                                    .child(auth.getCurrentUser().getUid()).child(key).child("categoriesDeal").setValue(post.getCategoriesDeal());
                                            root.child(ConfigApp.FIREBASE_APP_URL_USERS_DEAL_USER)
                                                    .child(auth.getCurrentUser().getUid()).child(key).child("offers").setValue(post.getOffers())
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            root.child(ConfigApp.FIREBASE_APP_URL_USERS_DEAL_USER)
                                                                    .child(auth.getCurrentUser().getUid()).child(key).child("privateContent").setValue(post.getPrivateContent())
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            //create in exists post and all posts
                                                                            if(task.isComplete() && task.isSuccessful()){
                                                                                root.child(ConfigApp.FIREBASE_APP_URL_DEAL_EXIST).child(key).setValue(true)
                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                //update user number of ads
                                                                                                root.child(ConfigApp.FIREBASE_APP_URL_USERS).child(auth.getCurrentUser().getUid())
                                                                                                        .child("userPublic").child("numberOfAds").setValue(userSharedPreference.getUserNumberofAds())
                                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                            @Override
                                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                                if(task.isSuccessful()){

                                                                                                                    sendNotification("New Deal in your area");
                                                                                                                    Toast.makeText(getApplicationContext(),getString(R.string.string_toast_text_success),Toast.LENGTH_SHORT).show();
                                                                                                                    finish();
                                                                                                                    if (progressBar!=null){
                                                                                                                        progressBar.dismiss();
                                                                                                                        unloockscreen();
                                                                                                                    }
                                                                                                                }else {
                                                                                                                    Toast.makeText(getApplicationContext(),getString(R.string.string_toast_text_error)
                                                                                                                            + " " +task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                                                                                                    if (progressBar!=null){
                                                                                                                        progressBar.dismiss();
                                                                                                                        unloockscreen();
                                                                                                                    }
                                                                                                                }
                                                                                                            }
                                                                                                        });
                                                                                            }
                                                                                        });
                                                                            }else {

                                                                            }

                                                                        }
                                                                    });
                                                        }
                                                    });

                                        }else {

                                            if(progressBar!=null){
                                                progressBar.dismiss();
                                                unloockscreen();
                                            }
                                        }
                                    }
                                });

                            }else {
                                Toast.makeText(getApplicationContext(),getString(R.string.string_toast_text_error)
                                        + " " +task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                if (progressBar!=null){
                                    progressBar.dismiss();
                                    unloockscreen();
                                }
                            }

                        }
                    });

                }
            }

        }else {

            post.setPrivateContent(privateContent);
            post.setPublicContent(publicContent);
            post.setOffers(dealsOffers);


            //create in users' posts
            root.child(ConfigApp.FIREBASE_APP_URL_USERS_DEAL_USER)
                    .child(auth.getCurrentUser().getUid()).child(key).child("publicContent").setValue(post.getPublicContent());
            root.child(ConfigApp.FIREBASE_APP_URL_USERS_DEAL_USER)
                    .child(auth.getCurrentUser().getUid()).child(key).child("categoriesDeal").setValue(post.getCategoriesDeal());

            root.child(ConfigApp.FIREBASE_APP_URL_USERS_DEAL_USER)
                    .child(auth.getCurrentUser().getUid()).child(key).child("offers").setValue(post.getOffers())
            .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    root.child(ConfigApp.FIREBASE_APP_URL_USERS_DEAL_USER)
                            .child(auth.getCurrentUser().getUid()).child(key).child("privateContent").setValue(post.getPrivateContent())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    //create in exists post and all posts
                                    //create in city
                                    DatabaseReference r0 = root.child(ConfigApp.FIREBASE_APP_URL_USERS_DEAL)
                                            .child(key).child("privateContent");
                                    r0.setValue(privateContent);

                                    DatabaseReference r1cat=root.child(ConfigApp.FIREBASE_APP_URL_USERS_DEAL)
                                            .child(key).child("categoriesDeal");
                                    r1cat.setValue(post.getCategoriesDeal());

                                    DatabaseReference r1 = root.child(ConfigApp.FIREBASE_APP_URL_USERS_DEAL)
                                            .child(key).child("publicContent");
                                    r1.setValue(publicContent).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            root.child(ConfigApp.FIREBASE_APP_URL_DEAL_EXIST).child(key).setValue(true)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            //update user number of ads
                                                            root.child(ConfigApp.FIREBASE_APP_URL_USERS).child(auth.getCurrentUser().getUid())
                                                                    .child("userPublic").child("numberOfAds").setValue(userSharedPreference.getUserNumberofAds())
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if(task.isSuccessful()){
                                                                                sendNotification("New Deal in your area");
                                                                                Toast.makeText(getApplicationContext(),getString(R.string.string_toast_text_success),Toast.LENGTH_SHORT).show();
                                                                                finish();
                                                                                if (progressBar!=null){
                                                                                    progressBar.dismiss();
                                                                                    unloockscreen();
                                                                                }
                                                                            }else {
                                                                                Toast.makeText(getApplicationContext(),getString(R.string.string_toast_text_error)
                                                                                        + " " +task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                                                                if (progressBar!=null){
                                                                                    progressBar.dismiss();
                                                                                    unloockscreen();
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

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_INTENT && resultCode==RESULT_OK){
            Uri uri= data.getData();
            recyclerViewAdapter.addUri(Uri.parse(compressImage(uri)));
            //uris.add(uri);
            //towWaysViewAdapter.notifyDataSetChanged();
            if (recyclerViewAdapter.getItemCount()>=5){
                imageViewAddNewImage.setEnabled(false);
            }else {
                imageViewAddNewImage.setEnabled(true);
            }
        }else if (requestCode == CAMERA_INTENT && resultCode==RESULT_OK){

            Bitmap bm=ImagePicker.getImageFromResult(this,resultCode,data);
            Uri uri= ImagePicker.getUriFromResult(getApplicationContext(),resultCode,data);
            //towWaysViewAdapter.add(uri);
        }
    }


    void showDialogSortingOptions() {


        alertDialog = new android.support.v7.app.AlertDialog.Builder(this).create();
        LayoutInflater inflater = getLayoutInflater();
        View convertView = (View) inflater.inflate(R.layout.custom_sort_options, null);
        alertDialog.setView(convertView);
        alertDialog.setTitle("Categories");
        ListView listView = (ListView) convertView.findViewById(R.id.listView_addItmeListActivity_sort_options);
        sortOptionslist = new ArrayList<>();
        sortOptionslist.add("Cars");
        sortOptionslist.add("Smartphone");
        sortOptionslist.add("Laptop");
        sortOptionslist.add("Shoes");
        sortOptionslist.add("Clothes");


        listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });

        alertDialog.setCancelable(true);
        alertDialog.show();
    }


    private void getPicture(){
        Intent intent =new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // Intent intent=ImagePicker.getPickImageIntent(this);
        startActivityForResult(intent, GALLERY_INTENT);
    }

    private void delete(final int i){

        recyclerViewAdapter.delete(uris.get(i),i);

        if(recyclerViewAdapter.getItemCount()<5){
            imageViewAddNewImage.setEnabled(true);
        }

    }


    public String compressImage(Uri imageUri) {

        String filePath = getRealPathFromURI(imageUri);
        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

//      max Height and width values of the compressed image is taken as 816x612

        float maxHeight = 816.0f;
        float maxWidth = 612.0f;
        float imgRatio = actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;

//      width and height values are set maintaining the aspect ratio of the image

        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {               imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;

            }
        }

//      setting inSampleSize value allows to load a scaled down version of the original image

        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

//      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;

//      this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
//          load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();

        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight,Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

//      check the rotation of the image and display it properly
        ExifInterface exif;
        try {
            exif = new ExifInterface(filePath);

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 3) {
                matrix.postRotate(180);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 8) {
                matrix.postRotate(270);
                Log.d("EXIF", "Exif: " + orientation);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                    true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        String encodedImage=null;
        try {

//          write the compressed bitmap at the destination specified by filename.
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
            byte[] byteFormat = stream.toByteArray();
            encodedImage = Base64.encodeToString(byteFormat, Base64.NO_WRAP);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return encodedImage;

    }

    private String getRealPathFromURI(Uri contentURI) {

        //close cursor
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            return contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(index);
        }
    }


    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height/ (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;

        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }


    private void sendNotification(String message) {
        new SendNotification(message).execute();
    }


    private class SendNotification extends AsyncTask<Void,Void,Void>
    {

        String  message;

        SendNotification(String message)
        {
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

                data.add(new Pair<String, String>("message", message));
                data.add(new Pair<String, String>("topic","com.bricenangue.nextgeneration.ebuycamer"));
                data.add(new Pair<String, String>("title","New Deal" ));
                data.add(new Pair<String, String>("sender_uid",auth.getCurrentUser().getUid()));


                byte[] bytes = ConfigApp.getData(data).getBytes("UTF-8");


                URL url=new URL(ConfigApp.OOOWEBHOST_SERVER_URL+ "FirebasePushToTopicNotification.php");
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

}
