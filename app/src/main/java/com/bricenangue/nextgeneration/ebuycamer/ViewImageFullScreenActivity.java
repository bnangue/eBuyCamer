package com.bricenangue.nextgeneration.ebuycamer;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class ViewImageFullScreenActivity extends AppCompatActivity {

    private Uri uri;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image_full_screen);
        Bundle extras = getIntent().getExtras();
        if(extras!=null){
            uri=Uri.parse(extras.getString("imageUri"));
        }
        final ImageView imageView =(ImageView) findViewById(R.id.imageView_Fullscreen);
        progressBar=(ProgressBar)findViewById(R.id.progressbar_viewimageFullsize);

        if(uri!=null){
            if(uri.toString().contains("firebasestorage")){
                Picasso.with(this).load(uri).networkPolicy(NetworkPolicy.OFFLINE)
                        .fit().centerInside()
                        .into(imageView, new Callback() {
                            @Override
                            public void onSuccess() {
                                progressBar.setVisibility(View.GONE);
                            }

                            @Override
                            public void onError() {
                                Picasso.with(getApplicationContext()).load(uri)
                                        .fit().centerInside().into(imageView);
                                progressBar.setVisibility(View.GONE);

                            }
                        });

            }else {
                imageView.setImageURI(uri);
                progressBar.setVisibility(View.GONE);

            }
        }
    }
}
