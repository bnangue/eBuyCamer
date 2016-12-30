package com.bricenangue.nextgeneration.ebuycamer;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;


public class ChangePhoneNumberActivity extends AppCompatActivity {

    private EditText editTextCode,editTextPhoneNumber,editTextVerification_code;
    private Button buttonConfirm;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS =0 ;
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
        setContentView(R.layout.activity_change_phone_number);

        userSharedPreference=new UserSharedPreference(this);
        auth=FirebaseAuth.getInstance();
        if(auth!=null){
            user=auth.getCurrentUser();
        }else {
            finish();
        }
        editTextCode=(EditText)findViewById(R.id.editText_userprofile_country_code_change_activity);
        editTextPhoneNumber=(EditText)findViewById(R.id.editText_userprofile_phone_number_change_activity);

        buttonConfirm=(Button) findViewById(R.id.button_change_phone);
        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              //  sendSMSMessage();
                if (haveNetworkConnection()){
                    String code=editTextCode.getText().toString();
                    String phonenumber=editTextPhoneNumber.getText().toString();

                    if (TextUtils.isEmpty(code)){
                        editTextCode.setError(getString(R.string.maimpage_alertdialog_edittext_error_empty));
                        editTextCode.requestFocus();
                        editTextCode.performClick();
                    }else if (TextUtils.isEmpty(phonenumber)){
                        editTextPhoneNumber.setError(getString(R.string.maimpage_alertdialog_edittext_error_empty));
                        editTextPhoneNumber.requestFocus();
                        editTextPhoneNumber.performClick();
                    }else {
                        DatabaseReference ref= FirebaseDatabase.getInstance().getReference()
                                .child(ConfigApp.FIREBASE_APP_URL_USERS)
                                .child(user.getUid())
                                .child("userPublic")
                                .child("phoneNumber");

                        ref.setValue(new PhoneNumber(code,phonenumber)).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                DatabaseReference ref= FirebaseDatabase.getInstance().getReference()
                                        .child(ConfigApp.FIREBASE_APP_URL_USERS)
                                        .child(user.getUid())
                                        .child("userPublic");
                                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        userSharedPreference.storeUserData(dataSnapshot.getValue(UserPublic.class));
                                        finish();
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                            }
                        });
                    }
                }else {
                    Toast.makeText(getApplicationContext(),getString(R.string.connection_to_server_not_aviable)
                            ,Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    protected void sendSMSMessage() {


        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.SEND_SMS)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.SEND_SMS},
                        MY_PERMISSIONS_REQUEST_SEND_SMS);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SEND_SMS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage("", null, "", null, null);
                    Toast.makeText(getApplicationContext(), "SMS sent.",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(),
                            "SMS faild, please try again.", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }

    }

}
