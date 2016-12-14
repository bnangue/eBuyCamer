package com.bricenangue.nextgeneration.ebuycamer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.lucasr.twowayview.TwoWayView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CreateAndModifyPublicationActivity extends AppCompatActivity implements AdapterView.OnItemLongClickListener {

    private static final int GALLERY_INTENT=01;
    private TwoWayView listview;
    private TowWaysViewAdapter towWaysViewAdapter;
    private ArrayList<Uri> uris =new ArrayList<>();
    private ArrayList<PublicationPhotos> downloadUris=new ArrayList<>();
    private static final int CAMERA_INTENT=02;
    private android.support.v7.app.AlertDialog alertDialog;
    private EditText editTextDescription;
    private EditText editTextTitle;
    private EditText editTextPrice;
    private String categorie;
    private FirebaseAuth auth;
    private DatabaseReference root;
    private StorageReference rootStorage;
    private Spinner spinnerCategory;
    private Map<String,Uri> images;
    private ProgressDialog progressBar;
    private UserSharedPreference userSharedPreference;
    private String postToeditId;
    private String[] photonames={"photo1","photo2","photo3","photo4","photo5"};
    private PrivateContent postToedit;
    private String location;



    private String [] categoriesArray;

    private Spinner spinnerCurrency;
    private ArrayList<Object> sortOptionslist;
    ImageView imageViewAddNewImage;
    private String categoryFromPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_and_modify_publication);

        Bundle extras =getIntent().getExtras();
        if(extras!=null && extras.containsKey("postToedit")){
            postToeditId=extras.getString("postToedit");
        }
        userSharedPreference=new UserSharedPreference(this);
        auth=FirebaseAuth.getInstance();
        if(auth.getCurrentUser()==null){
            startActivity(new Intent(CreateAndModifyPublicationActivity.this
                    ,MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        }

        //to save the path in one language (english)
        categoriesArray=getResources().getStringArray(R.array.categories_arrays_create);

        root= FirebaseDatabase.getInstance().getReference();
        rootStorage= FirebaseStorage.getInstance().getReference();
        spinnerCurrency=(Spinner) findViewById(R.id.spinner_currency);

        spinnerCategory=(Spinner)findViewById(R.id.spinner_choose_categorie);



        editTextDescription=(EditText) findViewById(R.id.editText_description_post);
        editTextPrice=(EditText) findViewById(R.id.editText_price_post);
        editTextTitle=(EditText) findViewById(R.id.editText_title_post);


        listview=(TwoWayView) findViewById(R.id.lvItems);

        towWaysViewAdapter=new TowWaysViewAdapter(this,uris);
        listview.setAdapter(towWaysViewAdapter);
        listview.setOnItemLongClickListener(this);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                startActivity(new Intent(CreateAndModifyPublicationActivity.this,
                        ViewImageFullScreenActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra("imageUri",towWaysViewAdapter.getItem(i).toString()));
            }
        });

         imageViewAddNewImage=(ImageView)findViewById(R.id.imageView_Choose_Image_post);
        imageViewAddNewImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getPicture();
            }
        });

        if(postToeditId!=null && !postToeditId.isEmpty()){
            populate();
        }
    }

    private void populate() {
        DatabaseReference reference=root.child(ConfigApp.FIREBASE_APP_URL_USERS_POSTS_USER)
                .child(auth.getCurrentUser().getUid()).child(postToeditId).child("privateContent");

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                PrivateContent post = dataSnapshot.getValue(PrivateContent.class);
                if(dataSnapshot.hasChild("description")){
                    editTextDescription.setText(post.getDescription());
                }

               editTextPrice.setText(post.getPrice());
                editTextTitle.setText(post.getTitle());

               if(dataSnapshot.hasChild("publictionPhotos")){
                   for(PublicationPhotos uri : post.getPublictionPhotos()){
                       towWaysViewAdapter.add(Uri.parse(uri.getUri()));
                       //uris.add(uri);
                       //towWaysViewAdapter.notifyDataSetChanged();
                       if (towWaysViewAdapter.getCount()>=5){
                           imageViewAddNewImage.setEnabled(false);
                       }else {
                           imageViewAddNewImage.setEnabled(true);
                       }
                   }
               }

                categoryFromPost=categoriesArray[post.getCategorie().getCatNumber()];
                int positionSpinner=post.getCategorie().getCatNumber();
                spinnerCategory.setSelection(positionSpinner);
                location=post.getLocation().getName();
                spinnerCurrency.setSelection(getCurrencyPosition(post.getCurrency()));
                spinnerCategory.setEnabled(false);
                spinnerCurrency.setEnabled(false);
                postToedit=post;

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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

    public void ButtonConfirmActionPostClicked(View view){

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

        // Check for a valid email address.
        if (TextUtils.isEmpty(price)) {
            editTextPrice.setError(getString(R.string.error_field_required_price));
            focusView = editTextPrice;
            cancel = true;
        }

        if(spinnerCategory.getSelectedItem().toString().equals(getString(R.string.string_create_new_publication_spinner_category_default))){
            focusView=spinnerCategory;
            cancel=true;
        }
        if (cancel) {
            // There was an error;
            focusView.requestFocus();
        } else {
            if(postToeditId!=null && !postToeditId.isEmpty()){

                updateItem(postToeditId, categoryFromPost, location);
            }else {
                createAndSaveItem();
            }

        }
    }

    private void updateItem(final String postid, final String categoryFpost,final String location) {
        progressBar = new ProgressDialog(this);
        progressBar.setCancelable(false);
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.show();
        final String title=editTextTitle.getText().toString();
        final String price=editTextPrice.getText().toString();
        final String description=editTextDescription.getText().toString();

        categorie = categoriesArray[spinnerCategory.getSelectedItemPosition()];
        final String currency=spinnerCurrency.getSelectedItem().toString();

        //updatechildren
        if(!categorie.equals(categoryFpost)){
            final DatabaseReference reftoedit=root.child(ConfigApp.FIREBASE_APP_URL_REGIONS).child(location)
                    .child(categoryFpost).child(postid);
            reftoedit.removeValue();
        }

        final ArrayList<Uri> arrayList =towWaysViewAdapter.getUris();
        final StorageReference referencePost = rootStorage.child(auth.getCurrentUser().getUid()).
                child(ConfigApp.FIREBASE_APP_URL_USERS_POSTS).child(postid);

        if(arrayList.size()>0){

            for(int i=0; i<arrayList.size(); i++){

                //check if picture is already save in firebasestorage if not save
                if (arrayList.get(i).toString().contains("firebasestorage")){
                    downloadUris.add(new PublicationPhotos(arrayList.get(i).toString()));
                    if(i==(arrayList.size() - 1)){

                        Map<String, Object> childUpdates=new HashMap<String, Object>();
                        childUpdates.put("/title",title);
                        childUpdates.put("/description",description);
                        childUpdates.put("/price",price);
                        childUpdates.put("/currency",currency);
                        childUpdates.put("/categorie",new Categories(categorie,null,spinnerCategory.getSelectedItemPosition()));
                        childUpdates.put("/publictionPhotos",downloadUris);


                        root.child("Cities").child(location).child(categorie)
                                .child(postid).child("privateContent").updateChildren(childUpdates).addOnCompleteListener(
                                new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isComplete() && task.isSuccessful()){
                                            Map<String, Object> childUpdates=new HashMap<String, Object>();

                                            childUpdates=new HashMap<String, Object>();

                                            childUpdates.put("/title",title);
                                            childUpdates.put("/description",description);
                                            childUpdates.put("/price",price);
                                            childUpdates.put("/currency",currency);
                                            childUpdates.put("/categorie",new Categories(categorie,null,spinnerCategory.getSelectedItemPosition()));
                                            childUpdates.put("/publictionPhotos",downloadUris);



                                            root.child(ConfigApp.FIREBASE_APP_URL_USERS_POSTS_USER).child(auth.getCurrentUser().getUid())
                                                    .child(postid).child("privateContent").updateChildren(childUpdates)
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
                                                        childUpdates.put("/categorie",new Categories(categorie,null,spinnerCategory.getSelectedItemPosition()));
                                                        childUpdates.put("/publictionPhotos",downloadUris);


                                                        root.child(ConfigApp.FIREBASE_APP_URL_USERS_POSTS).child(postid).child("privateContent")
                                                                .updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){
                                                                    Toast.makeText(getApplicationContext(),getString(R.string.string_toast_text_success),Toast.LENGTH_SHORT).show();
                                                                    finish();
                                                                    if (progressBar!=null){
                                                                        progressBar.dismiss();
                                                                    }
                                                                }else {
                                                                    Toast.makeText(getApplicationContext(),getString(R.string.string_toast_text_error),Toast.LENGTH_SHORT).show();
                                                                    if (progressBar!=null){
                                                                        progressBar.dismiss();
                                                                    }
                                                                }
                                                            }
                                                        });
                                                    }else {
                                                        Toast.makeText(getApplicationContext(),getString(R.string.string_toast_text_error_user),Toast.LENGTH_SHORT).show();
                                                        if (progressBar!=null){
                                                            progressBar.dismiss();
                                                        }
                                                        //error editing user posts
                                                    }
                                                }
                                            });

                                        }else {
                                            Toast.makeText(getApplicationContext(),getString(R.string.string_toast_text_error_city),Toast.LENGTH_SHORT).show();
                                            if (progressBar!=null){
                                                progressBar.dismiss();
                                            }
                                            //error editing category and post
                                        }
                                    }
                                }
                        );
                    }
                }else {
                    // pic not saved yet in firebase storage
                    StorageReference refPostPic = referencePost.child(photonames[i]);

                    final int finalI = i;
                    refPostPic.putFile(arrayList.get(i)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Uri downloadUri=taskSnapshot.getDownloadUrl();
                            assert downloadUri != null;
                            PublicationPhotos p=new PublicationPhotos();
                            p.setUri(downloadUri.toString());
                            downloadUris.add(p);

                            if(finalI ==(arrayList.size() - 1)){

                                Map<String, Object> childUpdates=new HashMap<String, Object>();

                                childUpdates.put("/title",title);
                                childUpdates.put("/description",description);
                                childUpdates.put("/price",price);
                                childUpdates.put("/currency",currency);
                                childUpdates.put("/categorie",new Categories(categorie,null,spinnerCategory.getSelectedItemPosition()));
                                childUpdates.put("/publictionPhotos",downloadUris);


                                root.child("Cities").child(location).child(categorie)
                                        .child(postid).child("privateContent").updateChildren(childUpdates)
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
                                            childUpdates.put("/categorie",new Categories(categorie,null,spinnerCategory.getSelectedItemPosition()));
                                            childUpdates.put("/publictionPhotos",downloadUris);



                                            root.child(ConfigApp.FIREBASE_APP_URL_USERS_POSTS_USER).child(auth.getCurrentUser().getUid())
                                                    .child(postid).child("privateContent").updateChildren(childUpdates)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isComplete() && task.isSuccessful()){
                                                        Map<String, Object> childUpdates=new HashMap<String, Object>();

                                                        childUpdates=new HashMap<String, Object>();

                                                        childUpdates.put("/title",title);
                                                        childUpdates.put("/description",description);
                                                        childUpdates.put("/price",price);
                                                        childUpdates.put("/currency",currency);
                                                        childUpdates.put("/categorie",new Categories(categorie,null,spinnerCategory.getSelectedItemPosition()));
                                                        childUpdates.put("/publictionPhotos",downloadUris);


                                                        root.child(ConfigApp.FIREBASE_APP_URL_USERS_POSTS).child(postid).child("privateContent")
                                                                .updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){
                                                                    Toast.makeText(getApplicationContext(),getString(R.string.string_toast_text_success),Toast.LENGTH_SHORT).show();
                                                                    finish();
                                                                    if (progressBar!=null){
                                                                        progressBar.dismiss();
                                                                    }
                                                                }else {
                                                                    Toast.makeText(getApplicationContext(),getString(R.string.string_toast_text_error),Toast.LENGTH_SHORT).show();
                                                                    if (progressBar!=null){
                                                                        progressBar.dismiss();
                                                                    }
                                                                }
                                                            }
                                                        });
                                                    }else {

                                                        Toast.makeText(getApplicationContext(),getString(R.string.string_toast_text_error_user),Toast.LENGTH_SHORT).show();
                                                        if (progressBar!=null){
                                                            progressBar.dismiss();
                                                        }
                                                        // error editing user post

                                                    }
                                                }
                                            });

                                        }else {

                                            Toast.makeText(getApplicationContext(),getString(R.string.string_toast_text_error_city),Toast.LENGTH_SHORT).show();
                                            if (progressBar!=null){
                                                progressBar.dismiss();
                                            }
                                            // error editing post in city with picture
                                        }
                                    }
                                });

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
            childUpdates.put("/categorie",new Categories(categorie,null,spinnerCategory.getSelectedItemPosition()));

            root.child("Cities").child(location).child(categorie)
                    .child(postid).child("privateContent").updateChildren(childUpdates)
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
                        childUpdates.put("/categorie",new Categories(categorie,null,spinnerCategory.getSelectedItemPosition()));


                        root.child(ConfigApp.FIREBASE_APP_URL_USERS_POSTS_USER).child(auth.getCurrentUser().getUid())
                                .child(postid).child("privateContent").updateChildren(childUpdates)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isComplete() && task.isSuccessful()){
                                    Map<String, Object> childUpdates=new HashMap<String, Object>();

                                    childUpdates=new HashMap<String, Object>();

                                    childUpdates.put("/title",title);
                                    childUpdates.put("/description",description);
                                    childUpdates.put("/price",price);
                                    childUpdates.put("/currency",currency);
                                    childUpdates.put("/categorie",new Categories(categorie,null,spinnerCategory.getSelectedItemPosition()));

                                    root.child(ConfigApp.FIREBASE_APP_URL_USERS_POSTS).child(postid).child("privateContent")
                                            .updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Toast.makeText(getApplicationContext(),getString(R.string.string_toast_text_success),Toast.LENGTH_SHORT).show();
                                                finish();
                                                if (progressBar!=null){
                                                    progressBar.dismiss();
                                                }
                                            }else {
                                                Toast.makeText(getApplicationContext(),getString(R.string.string_toast_text_error),Toast.LENGTH_SHORT).show();
                                                if (progressBar!=null){
                                                    progressBar.dismiss();
                                                }
                                            }
                                        }
                                    });
                                }else {
                                    Toast.makeText(getApplicationContext(),getString(R.string.string_toast_text_error_user),Toast.LENGTH_SHORT).show();
                                    if (progressBar!=null){
                                        progressBar.dismiss();
                                    }
                                    // errro edting task user post
                                }
                            }
                        });
                    }else {
                        Toast.makeText(getApplicationContext(),getString(R.string.string_toast_text_error_city),Toast.LENGTH_SHORT).show();
                        if (progressBar!=null){
                            progressBar.dismiss();
                        }
                        //error editing post in  city
                    }
                }
            });

        }

    }

    private void createAndSaveItem() {
        progressBar = new ProgressDialog(this);
        progressBar.setCancelable(false);
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.show();
        String title=editTextTitle.getText().toString();
        String price=editTextPrice.getText().toString();
        String description=editTextDescription.getText().toString();

        categorie=categoriesArray[spinnerCategory.getSelectedItemPosition()];
        final Publication post= new Publication();

        final PrivateContent privateContent= new PrivateContent();
        final PublicContent publicContent=new PublicContent();

        privateContent.setPrice(price);
        privateContent.setTitle(title);
        privateContent.setCurrency(spinnerCurrency.getSelectedItem().toString() );
        privateContent.setCategorie(new Categories(categorie,null,spinnerCategory.getSelectedItemPosition()));
        if(!description.isEmpty()){
            privateContent.setDescription(description);
        }
        privateContent.setLocation(new Locations(userSharedPreference.getUserLocation()));
        privateContent.setCreatorid(auth.getCurrentUser().getUid());
        //updatechildren
        final DatabaseReference ref = root;
        final String key= ref.push().getKey();
        privateContent.setUniquefirebaseId(key);
        privateContent.setTimeofCreation(System.currentTimeMillis());

        publicContent.setNumberoflikes(0);
        publicContent.setNumberofView(0);
        userSharedPreference.addNumberofAds();


        final ArrayList<Uri> arrayList =towWaysViewAdapter.getUris();
        StorageReference referencePost = rootStorage.child(auth.getCurrentUser().getUid()).
                child(ConfigApp.FIREBASE_APP_URL_USERS_POSTS).child(key);
        if(arrayList.size()>0){

            for(int j=0; j<arrayList.size();j++){
                StorageReference refPostPic = referencePost.child(photonames[j]);
                refPostPic.putFile(arrayList.get(j)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Uri downloadUri=taskSnapshot.getDownloadUrl();
                        assert downloadUri != null;
                        PublicationPhotos p=new PublicationPhotos();
                        p.setUri(downloadUri.toString());
                        downloadUris.add(p);
                        if(downloadUris.size()==arrayList.size()){
                            privateContent.setPublictionPhotos(downloadUris);

                            post.setPrivateContent(privateContent);
                            post.setPublicContent(publicContent);
                            //create in city
                           DatabaseReference r0= root.child(ConfigApp.FIREBASE_APP_URL_REGIONS)
                                   .child(post.getPrivateContent().getLocation().getName()).child(privateContent.getCategorie().getName())
                                    .child(key).child("privateContent");
                            r0.setValue(privateContent).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    DatabaseReference r1=root.child(ConfigApp.FIREBASE_APP_URL_REGIONS)
                                            .child(post.getPrivateContent().getLocation().getName()).child(privateContent.getCategorie().getName())
                                            .child(key).child("publicContent");
                                    r1.setValue(publicContent);


                                    //create in users' posts
                                    root.child(ConfigApp.FIREBASE_APP_URL_USERS_POSTS_USER)
                                            .child(auth.getCurrentUser().getUid()).child(key).child("publicContent").setValue(post.getPublicContent());
                                    root.child(ConfigApp.FIREBASE_APP_URL_USERS_POSTS_USER)
                                            .child(auth.getCurrentUser().getUid()).child(key).child("privateContent").setValue(post.getPrivateContent())
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    //create in exists post and all posts
                                                    root.child(ConfigApp.FIREBASE_APP_URL_USERS_POSTS).child(key).child("privateContent").setValue(privateContent);
                                                    root.child(ConfigApp.FIREBASE_APP_URL_USERS_POSTS).child(key).child("publicContent").setValue(publicContent);
                                                    root.child(ConfigApp.FIREBASE_APP_URL_POSTS_EXIST).child(key).setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            //update user number of ads
                                                            root.child(ConfigApp.FIREBASE_APP_URL_USERS).child(auth.getCurrentUser().getUid())
                                                                    .child("userPublic").child("numberOfAds").setValue(userSharedPreference.getUserNumberofAds())
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if(task.isSuccessful()){
                                                                                Toast.makeText(getApplicationContext(),getString(R.string.string_toast_text_success),Toast.LENGTH_SHORT).show();
                                                                                finish();
                                                                                if (progressBar!=null){
                                                                                    progressBar.dismiss();
                                                                                }
                                                                            }else {
                                                                                Toast.makeText(getApplicationContext(),getString(R.string.string_toast_text_error)
                                                                                        + " " +task.getException().getMessage(),Toast.LENGTH_SHORT).show();
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

                    }
                });
            }
        }else {

            post.setPrivateContent(privateContent);
            post.setPublicContent(publicContent);


            //create in users' posts
            root.child(ConfigApp.FIREBASE_APP_URL_USERS_POSTS_USER)
                    .child(auth.getCurrentUser().getUid()).child(key).child("publicContent").setValue(post.getPublicContent());
            root.child(ConfigApp.FIREBASE_APP_URL_USERS_POSTS_USER)
                    .child(auth.getCurrentUser().getUid()).child(key).child("privateContent").setValue(post.getPrivateContent())
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            //create in exists post and all posts
                            //create in city
                            DatabaseReference r0 = root.child(ConfigApp.FIREBASE_APP_URL_REGIONS).child(post.getPrivateContent().getLocation().getName()).child(privateContent.getCategorie().getName())
                                    .child(key).child("privateContent");
                            r0.setValue(privateContent);
                            DatabaseReference r1 = root.child(ConfigApp.FIREBASE_APP_URL_REGIONS).child(post.getPrivateContent().getLocation().getName()).child(privateContent.getCategorie().getName())
                                    .child(key).child("publicContent");
                            r1.setValue(publicContent).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    root.child(ConfigApp.FIREBASE_APP_URL_USERS_POSTS).child(key).child("privateContent").setValue(privateContent);
                                    root.child(ConfigApp.FIREBASE_APP_URL_USERS_POSTS).child(key).child("publicContent").setValue(publicContent);
                                    root.child(ConfigApp.FIREBASE_APP_URL_POSTS_EXIST).child(key).setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            //update user number of ads
                                            root.child(ConfigApp.FIREBASE_APP_URL_USERS).child(auth.getCurrentUser().getUid())
                                                    .child("userPublic").child("numberOfAds").setValue(userSharedPreference.getUserNumberofAds())
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if(task.isSuccessful()){
                                                                Toast.makeText(getApplicationContext(),getString(R.string.string_toast_text_success),Toast.LENGTH_SHORT).show();
                                                                finish();
                                                                if (progressBar!=null){
                                                                    progressBar.dismiss();
                                                                }
                                                            }else {
                                                                Toast.makeText(getApplicationContext(),getString(R.string.string_toast_text_error)
                                                                        + " " +task.getException().getMessage(),Toast.LENGTH_SHORT).show();
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

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_INTENT && resultCode==RESULT_OK){
            Uri uri= data.getData();
            towWaysViewAdapter.add(uri);
            //uris.add(uri);
            //towWaysViewAdapter.notifyDataSetChanged();
            if (towWaysViewAdapter.getCount()>=5){
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

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {

        progressBar = new ProgressDialog(this);
        progressBar.setCancelable(false);
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.show();
        StorageReference referencePost = rootStorage.child(auth.getCurrentUser().getUid()).
                child(ConfigApp.FIREBASE_APP_URL_USERS_POSTS).child(auth.getCurrentUser().getUid());


        if(uris.get(i).toString().contains("firebasestorage")){
            if(uris.get(i).toString().contains(photonames[0])){
                StorageReference reference=referencePost.child(photonames[0]);
                reference.delete();
            }else if (uris.get(i).toString().contains(photonames[1])){
                StorageReference reference=referencePost.child(photonames[1]);
                reference.delete();
            }else if (uris.get(i).toString().contains(photonames[2])){
                StorageReference reference=referencePost.child(photonames[2]);
                reference.delete();
            }else if (uris.get(i).toString().contains(photonames[3])){
                StorageReference reference=referencePost.child(photonames[3]);
                reference.delete();
            }else if (uris.get(i).toString().contains(photonames[4])){
                StorageReference reference=referencePost.child(photonames[4]);
                reference.delete();
            }

            ArrayList<PublicationPhotos> publicationPhotoses =postToedit.getPublictionPhotos();
            for (PublicationPhotos p: publicationPhotoses){
                if(p.getUri().equals(uris.get(i).toString())){
                    publicationPhotoses.remove(p);
                    Map<String, Object> childUpdates=new HashMap<String, Object>();

                    root.child("Cities").child(location).child(postToedit.getCategorie().getName())
                            .child(postToeditId).child("privateContent/publictionPhotos").setValue(publicationPhotoses);

                    root.child("Cities").child(location).child(postToedit.getCategorie().getName())
                            .child(postToeditId).child("privateContent/publictionPhotos").setValue(publicationPhotoses)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                root.child(ConfigApp.FIREBASE_APP_URL_USERS_POSTS)
                                        .child(postToeditId).child("privateContent/publictionPhotos").setValue(downloadUris)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        towWaysViewAdapter.remove(uris.get(i));
                                        if (progressBar!=null){
                                            progressBar.dismiss();
                                        }
                                    }
                                });

                            }else {
                                //error
                                Toast.makeText(getApplicationContext(),getString(R.string.string_toast_text_error)
                                        + " " +task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                if (progressBar!=null){
                                    progressBar.dismiss();
                                }
                            }
                        }
                    });

                    break;
                }
            }

        }else {
            towWaysViewAdapter.remove(uris.get(i));
            if (progressBar!=null){
                progressBar.dismiss();
            }
        }
        //uris.remove(uris);
        //towWaysViewAdapter.notifyDataSetChanged();
        if(towWaysViewAdapter.getCount()<5){
            imageViewAddNewImage.setEnabled(true);
        }
        return false;
    }
}
