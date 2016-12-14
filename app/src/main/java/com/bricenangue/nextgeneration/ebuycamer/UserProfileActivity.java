package com.bricenangue.nextgeneration.ebuycamer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class UserProfileActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText editTextEmail, editTextNumbOfAds,editTextLocation;
    private ImageButton imageButtoneditMail, imageButtonChangeLocation;
    private TextView textViewUsername;
    private ImageView imageViewUserPicture;
    private Button buttonSavePicture;

    private boolean emailEnable=false;
    private FirebaseAuth auth;
    private FirebaseUser  user;
    private UserPublic userPublic;

    private static final int GALLERY_INTENT_PROFILE_PICTURE=2;
    private static final int LOCATION_INTENT=3;
    private ProgressDialog progressBar;
    private Uri pictureUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        auth=FirebaseAuth.getInstance();

        if(auth!=null){
            user=auth.getCurrentUser();
        }else {
            startActivity(new Intent(this,MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));

        }
        editTextEmail=(EditText)findViewById(R.id.editText_userprofile_email);
        editTextNumbOfAds=(EditText)findViewById(R.id.editText_userprofile_numberofAds);

        editTextLocation=(EditText)findViewById(R.id.editText_userprofile_location);

        imageButtonChangeLocation=(ImageButton)findViewById(R.id.imageButton_userprofile_edit_location);
        imageButtoneditMail=(ImageButton)findViewById(R.id.imageButton_userprofile_edit_email);

        imageViewUserPicture=(ImageView)findViewById(R.id.imageView_user_profile);

        textViewUsername=(TextView)findViewById(R.id.textView_userprofile_user_name);

        buttonSavePicture=(Button)findViewById(R.id.button_userprofile_save_picture);


        imageButtoneditMail.setOnClickListener(this);
        imageButtonChangeLocation.setOnClickListener(this);
        imageViewUserPicture.setOnClickListener(this);
        buttonSavePicture.setOnClickListener(this);

        fetchUser();

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
    }

    private void showUser(final UserPublic userPublic){
        editTextNumbOfAds.setText(String.valueOf(userPublic.getNumberOfAds()));
        editTextLocation.setText(userPublic.getLocation().getName());
        editTextEmail.setText(userPublic.getEmail());
        textViewUsername.setText(userPublic.getName());

        if(userPublic.getProfilePhotoUri()!=null){
            Picasso.with(getApplicationContext()).load(userPublic.getProfilePhotoUri()).networkPolicy(NetworkPolicy.OFFLINE)
                    .fit().centerInside()
                    .into(imageViewUserPicture, new Callback() {
                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onError() {
                            Picasso.with(getApplicationContext()).load(userPublic.getProfilePhotoUri())
                                    .fit().centerInside().into(imageViewUserPicture);

                        }
                    });
        }

        cancelProgressbar();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.imageButton_userprofile_edit_email:

                break;
            case R.id.imageButton_userprofile_edit_location:
                //startactivity for result and update location in preference
                startActivityForResult(new Intent(UserProfileActivity.this,LocationsActivity.class)
                .putExtra("user_location",true),LOCATION_INTENT);
                break;

            case R.id.imageView_user_profile:
                getPicture();
                break;

            case R.id.button_userprofile_save_picture:
                if(pictureUri!=null){
                    saveProfilePic(pictureUri);
                }
                break;

        }
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
            editTextLocation.setText(location);
            UserSharedPreference preference =new UserSharedPreference(getApplicationContext());
            preference.storeUserLocation(location);

        }
    }
}
