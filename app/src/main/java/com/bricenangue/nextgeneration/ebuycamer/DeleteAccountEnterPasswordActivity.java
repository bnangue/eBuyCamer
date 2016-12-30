package com.bricenangue.nextgeneration.ebuycamer;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FacebookAuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;

import java.util.List;

public class DeleteAccountEnterPasswordActivity extends AppCompatActivity {

    private EditText editTextPassword,editTextEmail;
    private FirebaseAuth auth;
    private Button buttondelete;
    FirebaseUser user;
    private ProgressDialog progressBar;
    private boolean isEmail=false;

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
        setContentView(R.layout.activity_delete_account_enter_password);

        auth=FirebaseAuth.getInstance();
        if(auth!=null){
            user=auth.getCurrentUser();
        }else {
            finish();
        }

        Bundle extras=getIntent().getExtras();

        if(extras!=null){
            if (extras.containsKey("isEmail")){
                isEmail=extras.getBoolean("isEmail");
            }
        }

        editTextPassword=(EditText)findViewById(R.id.editText_delete_account_password);
        editTextEmail=(EditText)findViewById(R.id.editText_delete_account_email);

        buttondelete=(Button)findViewById(R.id.button_delete_account);

        if(isEmail){
            editTextEmail.setVisibility(View.VISIBLE);
            buttondelete.setText(getString(R.string.change_my_email));
        }else {
            editTextEmail.setVisibility(View.GONE);
            buttondelete.setText(getString(R.string.action_delete_account));
        }

        if (!haveNetworkConnection()){
            Toast.makeText(getApplicationContext(),getString(R.string.connection_to_server_not_aviable)
                    ,Toast.LENGTH_SHORT).show();
        }

        buttondelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String password=editTextPassword.getText().toString();
                final String email=editTextEmail.getText().toString();


                if(TextUtils.isEmpty(password) ){
                    editTextPassword.setError(getString(R.string.maimpage_alertdialog_edittext_error_empty));
                    editTextPassword.requestFocus();

                }else if (TextUtils.isEmpty(email) && editTextEmail.getVisibility()==View.VISIBLE){
                    editTextEmail.setError(getString(R.string.maimpage_alertdialog_edittext_error_empty));
                    editTextEmail.requestFocus();
                }
                else {


                    if (!haveNetworkConnection()){
                        Toast.makeText(getApplicationContext(),getString(R.string.connection_to_server_not_aviable)
                                ,Toast.LENGTH_SHORT).show();
                    }else {
                        showProgressBar();
                        List<?extends UserInfo> list=user.getProviderData();
                        String providerId=list.get(1).getProviderId();

                        // check if the provider id matches "facebook.com"
                        if(providerId.equals(getString(R.string.facebook_provider_id))) {

                            AccessToken token= AccessToken.getCurrentAccessToken();
                            if(token!=null){
                                AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
                                // Prompt the user to re-provide their sign-in credentials
                                user.reauthenticate(credential)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                setResult(RESULT_OK,new Intent().putExtra("reAuth",true)
                                                );
                                                cancelProgressbar();
                                                finish();

                                            }
                                        });
                            }

                        }else if (providerId.equals(getString(R.string.password_provider_id))){

                            AuthCredential credential = EmailAuthProvider
                                    .getCredential(user.getEmail(), password);

                            // Prompt the user to re-provide their sign-in credentials
                            user.reauthenticate(credential)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            setResult(RESULT_OK,new Intent().putExtra("reAuth",true)
                                                    .putExtra("email", email));
                                            cancelProgressbar();
                                            finish();

                                        }
                                    });

                        }else {
                            cancelProgressbar();
                            finish();
                        }
                    }

                }
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
}
