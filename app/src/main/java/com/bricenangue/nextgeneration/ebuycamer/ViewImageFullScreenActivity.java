package com.bricenangue.nextgeneration.ebuycamer;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class ViewImageFullScreenActivity extends AppCompatActivity {

    Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image_full_screen);
        Bundle extras = getIntent().getExtras();
        if(extras!=null){
            uri=Uri.parse(extras.getString("imageUri"));
        }
        final ImageView imageView =(ImageView) findViewById(R.id.imageView_Fullscreen);

        if(uri!=null){
            if(uri.toString().contains("firebasestorage")){
                Picasso.with(this).load(uri).networkPolicy(NetworkPolicy.OFFLINE)
                        .fit().centerInside()
                        .into(imageView, new Callback() {
                            @Override
                            public void onSuccess() {
                            }

                            @Override
                            public void onError() {
                                Picasso.with(getApplicationContext()).load(uri)
                                        .fit().centerInside().into(imageView);

                            }
                        });

            }else {
                imageView.setImageURI(uri);

            }
        }
    }
}
