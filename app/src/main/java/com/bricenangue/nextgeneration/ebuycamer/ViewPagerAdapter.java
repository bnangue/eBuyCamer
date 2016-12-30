package com.bricenangue.nextgeneration.ebuycamer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.view.PagerAdapter;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by bricenangue on 02/12/2016.
 */
public class ViewPagerAdapter extends PagerAdapter {

    private Context mContext;
    private ArrayList<PublicationPhotos> mResources;

    public ViewPagerAdapter(Context mContext, ArrayList<PublicationPhotos> mResources) {
        this.mContext = mContext;
        this.mResources = mResources;
    }

    @Override
    public int getCount() {
        return mResources.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((LinearLayout) object);
    }


    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.pager_item, container, false);

        final ImageView imageView = (ImageView) itemView.findViewById(R.id.img_pager_item);


        if (mResources.get(position).getUri().contains("firebase")){
            Picasso.with(mContext).load(mResources.get(position).getUri()).networkPolicy(NetworkPolicy.OFFLINE)
                    .fit().centerInside()
                    .into(imageView, new Callback() {
                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onError() {
                            Picasso.with(mContext).load(mResources.get(position).getUri())
                                    .fit().centerInside().into(imageView);

                        }
                    });
        }else {
            byte[] decodedString = Base64.decode(mResources.get(position).getUri(), Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

            imageView.setImageBitmap(decodedByte);
        }


        container.addView(itemView);

        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }
}
