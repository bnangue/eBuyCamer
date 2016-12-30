package com.bricenangue.nextgeneration.ebuycamer;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Map;

public class UserProfileActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText editTextEmail, editTextNumbOfAds,editTextLocation,editTextPhoneNumber, editTextCode;
    private ImageButton imageButtoneditMail, imageButtonChangeLocation,imageButtonEditPhone;
    private TextView textViewUsername;
    private CircularImageView imageViewUserPicture;
    private Button buttonSavePicture;
    private ArrayList<String> keys=new ArrayList<>();
    private ArrayList<Publication> mypublications=new ArrayList<>();


    private boolean emailEnable=false;
    private FirebaseAuth auth;
    private FirebaseUser  user;
    private UserPublic userPublic;

    private static final int GALLERY_INTENT_PROFILE_PICTURE=2;
    private static final int LOCATION_INTENT=3;
    private ProgressDialog progressBar;
    private Uri pictureUri;
    private DatabaseReference root;
    private StorageReference rootStorage;
    private static final int ENTERPASSWORD=4;
    private static final int UPDATEEMAIL=5;
    private ProgressBar progressBare;
    private UserSharedPreference userSharedPreference;

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
        setContentView(R.layout.activity_user_profile);

        userSharedPreference=new UserSharedPreference(this);
        auth=FirebaseAuth.getInstance();

        if(auth!=null){
            user=auth.getCurrentUser();
        }else {
            startActivity(new Intent(this,MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));

        }
        root=FirebaseDatabase.getInstance().getReference();
        rootStorage=FirebaseStorage.getInstance().getReference();

        progressBare=(ProgressBar)findViewById(R.id.progress_bar_user_profile);
        editTextEmail=(EditText)findViewById(R.id.editText_userprofile_email);
        editTextNumbOfAds=(EditText)findViewById(R.id.editText_userprofile_numberofAds);

        editTextLocation=(EditText)findViewById(R.id.editText_userprofile_location);

        imageButtonChangeLocation=(ImageButton)findViewById(R.id.imageButton_userprofile_edit_location);
        imageButtoneditMail=(ImageButton)findViewById(R.id.imageButton_userprofile_edit_email);

        imageViewUserPicture=(CircularImageView)findViewById(R.id.imageView_user_profile);

        imageButtonEditPhone=(ImageButton)findViewById(R.id.imageButton_userprofile_edit_phone_number);

        textViewUsername=(TextView)findViewById(R.id.textView_userprofile_user_name);

        buttonSavePicture=(Button)findViewById(R.id.button_userprofile_save_picture);
        editTextPhoneNumber=(EditText)findViewById(R.id.editText_userprofile_phone_number);
        editTextCode=(EditText)findViewById(R.id.editText_userprofile_country_code);


        imageButtoneditMail.setOnClickListener(this);
        imageButtonChangeLocation.setOnClickListener(this);
        imageViewUserPicture.setOnClickListener(this);
        buttonSavePicture.setOnClickListener(this);
        imageButtonEditPhone.setOnClickListener(this);

        if (!haveNetworkConnection()){
            Toast.makeText(getApplicationContext(),getString(R.string.connection_to_server_not_aviable)
                    ,Toast.LENGTH_SHORT).show();
        }


    }



    private void dismissProgressbar(){
        if (progressBar!=null){
            progressBar.dismiss();
        }
    }

    private void fetchUser(){
        showProgressBar();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child(ConfigApp.FIREBASE_APP_URL_USERS)
                .child(user.getUid())
                .child("userPublic");

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()){
                    userPublic=dataSnapshot.getValue(UserPublic.class);
                    if(userPublic!=null){
                        showUser(userPublic);

                    }
                }else {
                    dismissProgressbar();
                    cancelProgressbar();
                    //NO user Profile
                    finish();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                cancelProgressbar();
            }
        });

        if (!haveNetworkConnection()){
            dismissProgressbar();
        }
    }

    private void showUser(final UserPublic userPublic){
        dismissProgressbar();
        editTextNumbOfAds.setText(String.valueOf(userPublic.getNumberOfAds()));
        editTextLocation.setText(userPublic.getLocation().getName());
        editTextEmail.setText(userPublic.getEmail());
        textViewUsername.setText(userPublic.getName());
        if(userPublic.getPhoneNumber()!=null){
            editTextCode.setText(userPublic.getPhoneNumber().getCode());
            editTextPhoneNumber.setText(userPublic.getPhoneNumber().getPhoneNumber());
        }


        if(userPublic.getProfilePhotoUri()!=null){
            Picasso.with(getApplicationContext()).load(userPublic.getProfilePhotoUri()).networkPolicy(NetworkPolicy.OFFLINE)
                    .fit().centerInside()
                    .into(imageViewUserPicture, new Callback() {
                        @Override
                        public void onSuccess() {
                            progressBare.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {
                            Picasso.with(getApplicationContext()).load(userPublic.getProfilePhotoUri())
                                    .fit().centerInside().into(imageViewUserPicture);
                            progressBare.setVisibility(View.GONE);


                        }
                    });
        }else {
            progressBare.setVisibility(View.GONE);

        }

        cancelProgressbar();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.imageButton_userprofile_edit_email:

                if (!haveNetworkConnection()){
                    Toast.makeText(getApplicationContext(),getString(R.string.connection_to_server_not_aviable)
                            ,Toast.LENGTH_SHORT).show();
                }else {
                    startActivityForResult(new Intent(UserProfileActivity.this,
                            DeleteAccountEnterPasswordActivity.class).putExtra("isEmail",true),UPDATEEMAIL);

                }

                break;
            case R.id.imageButton_userprofile_edit_location:
                //startactivity for result and update location in preference
                if (!haveNetworkConnection()){
                    Toast.makeText(getApplicationContext(),getString(R.string.connection_to_server_not_aviable)
                            ,Toast.LENGTH_SHORT).show();
                }else {
                    startActivityForResult(new Intent(UserProfileActivity.this,LocationsActivity.class)
                            .putExtra("user_location",true),LOCATION_INTENT);
                }

                break;

            case R.id.imageView_user_profile:

                getPicture();
                break;

            case R.id.button_userprofile_save_picture:
                if (!haveNetworkConnection()){
                    Toast.makeText(getApplicationContext(),getString(R.string.connection_to_server_not_aviable)
                            ,Toast.LENGTH_SHORT).show();
                }else {
                    if(pictureUri!=null){
                        saveProfilePic(pictureUri);
                    }
                }

                break;
            case R.id.imageButton_userprofile_edit_phone_number:
                if (!haveNetworkConnection()){
                    Toast.makeText(getApplicationContext(),getString(R.string.connection_to_server_not_aviable)
                            ,Toast.LENGTH_SHORT).show();
                }else {
                    startActivity(new Intent(UserProfileActivity.this,ChangePhoneNumberActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                }

                break;

        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_view_profile,menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_delete_account :
                if (!haveNetworkConnection()){
                    Toast.makeText(getApplicationContext(),getString(R.string.connection_to_server_not_aviable)
                            ,Toast.LENGTH_SHORT).show();
                }else {
                    deleteAccount();
                }

                return true;
            case R.id.action_logout:
                if (!haveNetworkConnection()){
                    Toast.makeText(getApplicationContext(),getString(R.string.connection_to_server_not_aviable)
                            ,Toast.LENGTH_SHORT).show();
                }else {
                    loggout();
                }

                return true;
            case R.id.action_settings:
                startActivity(new Intent(UserProfileActivity.this,SettingsActivity.class));
                return true;


        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchUser();
    }

    private void continueDeleteAccount(){


        showProgressBar();
        // delete all publications and user Account
        final DatabaseReference referenceuser=root.child(ConfigApp.FIREBASE_APP_URL_USERS).child(user.getUid());

        //delete users' publications
        final DatabaseReference refuserPost1=root.child(ConfigApp.FIREBASE_APP_URL_USERS_POSTS_USER).child(user.getUid());
        refuserPost1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()){
                    for(DataSnapshot publication : dataSnapshot.getChildren()){
                        keys.add(publication.getKey());
                        mypublications.add(publication.getValue(Publication.class));
                        DatabaseReference ref =refuserPost1.child(publication.getKey());
                        ref.child("publicContent").removeValue();
                        ref.child("privateContent").removeValue();
                        if(keys.size()==dataSnapshot.getChildrenCount()){
                            //delete publication marked as favorite

                            DatabaseReference refPostfav=root.child(ConfigApp.FIREBASE_APP_URL_USERS_FAVORITES_USER).child(user.getUid());
                            refPostfav.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isComplete()&& task.isSuccessful()){
                                        DatabaseReference refuserFav=root.child(ConfigApp.FIREBASE_APP_URL_USERS_FAVORITES)
                                                .child(user.getUid());
                                        refuserFav.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isComplete()&& task.isSuccessful()){

                                                    DatabaseReference refPostexist=root.child(ConfigApp.FIREBASE_APP_URL_POSTS_EXIST);
                                                    for (String key : keys){

                                                        refPostexist.child(key).removeValue();

                                                    }

                                                    final DatabaseReference refPost=root.child(ConfigApp.FIREBASE_APP_URL_USERS_POSTS);
                                                    refPost.addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            for (String key: keys){
                                                                for (DataSnapshot pub : dataSnapshot.getChildren()){


                                                                    if(!pub.getKey().equals(key)){
                                                                        continue;
                                                                    }else {
                                                                        refPost.child(key).child("publicContent").removeValue();
                                                                        refPost.child(key).child("privateContent").removeValue();
                                                                    }


                                                                }
                                                            }

                                                            for(Publication pub : mypublications){
                                                                DatabaseReference refCity=root.child(ConfigApp.FIREBASE_APP_URL_REGIONS)
                                                                        .child(pub.getPrivateContent().getLocation().getName())
                                                                        .child(pub.getPrivateContent().getCategorie().getName())
                                                                        .child(pub.getPrivateContent().getUniquefirebaseId());

                                                                refCity.child("publicContent").removeValue();
                                                                refCity.child("privateContent").removeValue();
                                                            }

                                                            referenceuser.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()){
                                                                        // stop
                                                                        user.delete()
                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        if (task.isSuccessful()) {
                                                                                            accountdeleted();
                                                                                        }
                                                                                    }
                                                                                });

                                                                    }
                                                                }
                                                            });
                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {
                                                            cancelProgressbar();
                                                        }
                                                    });


                                                }else{

                                                    cancelProgressbar();
                                                    //error
                                                }
                                            }
                                        });

                                    }else {
                                        cancelProgressbar();
                                        // error
                                    }
                                }
                            });

                        }
                    }
                }else {
                    //user has no post continue and check if has favorite
                    final DatabaseReference refPostfav=root.child(ConfigApp.FIREBASE_APP_URL_USERS_FAVORITES_USER).child(user.getUid());
                    refPostfav.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChildren()){
                                for(DataSnapshot pubKey : dataSnapshot.getChildren()){
                                    DatabaseReference refuserFav=root.child(ConfigApp.FIREBASE_APP_URL_USERS_FAVORITES)
                                            .child(user.getUid());
                                    refuserFav.child(pubKey.getKey()).removeValue();

                                    refuserFav.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (!dataSnapshot.hasChildren()){
                                                refPostfav.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                     @Override
                                                                                                       public void onComplete(@NonNull Task<Void> task) {
                                                                                                           if (task.isComplete()&& task.isSuccessful()){
                                                                                                               referenceuser.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                   @Override
                                                                                                                   public void onComplete(@NonNull Task<Void> task) {
                                                                                                                       if (task.isSuccessful()){
                                                                                                                           // stop
                                                                                                                           user.delete()
                                                                                                                                   .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                                       @Override
                                                                                                                                       public void onComplete(@NonNull Task<Void> task) {
                                                                                                                                           if (task.isSuccessful()) {
                                                                                                                                               accountdeleted();
                                                                                                                                           }
                                                                                                                                       }
                                                                                                                                   });

                                                                                                                       }
                                                                                                                   }
                                                                                                               });
                                                                                                           }else {
                                                                                                               cancelProgressbar();
                                                                                                           }
                                                                                                       }
                                                                                               }
                                                );

                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            cancelProgressbar();
                                        }
                                    });
                                }
                            }else {
                                referenceuser.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            // stop
                                            user.delete()
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                accountdeleted();
                                                            }
                                                        }
                                                    });

                                        }else {
                                            cancelProgressbar();
                                        }
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            cancelProgressbar();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                cancelProgressbar();
            }
        });

    }


    private void deleteAccount() {
        final AlertDialog alertDialog =
                new AlertDialog.Builder(UserProfileActivity.this).setTitle(
                        getString(R.string.alertDialogdeleteAccount)+" " +user.getEmail())
                        .setMessage(  getString(R.string.alertDialogdeleteAccountMessage))
                        .create();
        alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.button_cancel)
                , new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        alertDialog.dismiss();

                    }

                });
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.button_delete_Account)
                , new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                       // auth.signOut();

                        startActivityForResult(new Intent(UserProfileActivity.this,
                                DeleteAccountEnterPasswordActivity.class),ENTERPASSWORD);

                    }
                });
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    private void accountdeleted(){
        startActivity(new Intent(UserProfileActivity.this,MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        cancelProgressbar();
        finish();
    }


    private void loggout() {

        final AlertDialog alertDialog =
                new AlertDialog.Builder(UserProfileActivity.this).setMessage(
                        getString(R.string.alertDialoglogout)+" " +user.getEmail())
                        .create();
        alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.button_cancel)
                , new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        alertDialog.dismiss();


                    }

                });
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.button_logout)
                , new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        FirebaseAuth.getInstance().signOut();
                        userSharedPreference.clearUserData();
                        startActivity(new Intent(UserProfileActivity.this,MainActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                        finish();
                    }
                });
        alertDialog.setCancelable(false);
        alertDialog.show();

    }
    private void saveProfilePic(Uri uri){
        showProgressBar();
        StorageReference referencePost = FirebaseStorage.getInstance().getReference()
                .child(ConfigApp.FIREBASE_APP_URL_STORAGE_USER_PROFILES)
                .child(user.getUid()).
                        child(ConfigApp.FIREBASE_APP_URL_STORAGE_USER_PROFILE_PICTURE);

        referencePost.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                final Uri downloadUri=taskSnapshot.getDownloadUrl();
                //load Image into ImageView
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setPhotoUri(downloadUri)
                        .build();

                user.updateProfile(profileUpdates)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    DatabaseReference reference=FirebaseDatabase.getInstance().getReference();
                                    reference.child(ConfigApp.FIREBASE_APP_URL_USERS).child(auth.getCurrentUser().getUid())
                                            .child("userPublic").child("profilePhotoUri").setValue(downloadUri.toString())
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    cancelProgressbar();
                                                    Picasso.with(getApplicationContext()).load(downloadUri).networkPolicy(NetworkPolicy.OFFLINE)
                                                            .fit().centerInside()
                                                            .into(imageViewUserPicture, new Callback() {
                                                                @Override
                                                                public void onSuccess() {
                                                                }

                                                                @Override
                                                                public void onError() {
                                                                    Picasso.with(getApplicationContext()).load(downloadUri)
                                                                            .fit().centerInside().into(imageViewUserPicture);

                                                                }
                                                            });
                                                    buttonSavePicture.setVisibility(View.GONE);
                                                }
                                            });

                                }
                            }
                        });


            }
        });

    }

    private void showProgressBar(){
        progressBar = new ProgressDialog(this);
        progressBar.setCancelable(false);
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.show();
    }

    private void cancelProgressbar(){
        if (progressBar!=null){
            progressBar.dismiss();
        }
    }
    private void getPicture(){
        Intent intent =new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, GALLERY_INTENT_PROFILE_PICTURE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_INTENT_PROFILE_PICTURE && resultCode==RESULT_OK){
            Uri uri= data.getData();
            imageViewUserPicture.setImageURI(uri);

            pictureUri=uri;
            buttonSavePicture.setVisibility(View.VISIBLE);

        }else if (requestCode == LOCATION_INTENT && resultCode==RESULT_OK){
            String location=data.getExtras().getString("user_location");
            int  position=data.getExtras().getInt("position_location");
            editTextLocation.setText(location);
            UserSharedPreference preference =new UserSharedPreference(getApplicationContext());
            preference.storeUserLocation(location,position);

        }else if (requestCode == ENTERPASSWORD && resultCode==RESULT_OK){
            continueDeleteAccount();
        }else if(requestCode == UPDATEEMAIL && resultCode==RESULT_OK){
            changeEmail(data.getExtras().getString("email"));
            showProgressBar();
        }
    }

    private void changeEmail(final String email) {

        user.updateEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                                    .child(ConfigApp.FIREBASE_APP_URL_USERS)
                                    .child(user.getUid())
                                    .child("userPublic").child("email");
                            reference.setValue(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    editTextEmail.setText(email);
                                    user.sendEmailVerification();
                                    cancelProgressbar();
                                }
                            });

                        }
                    }
                });
    }
}
