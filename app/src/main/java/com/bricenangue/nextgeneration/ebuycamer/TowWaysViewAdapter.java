package com.bricenangue.nextgeneration.ebuycamer;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by bricenangue on 01/12/2016.
 */

public class TowWaysViewAdapter extends ArrayAdapter<Uri> {

    private Context context;
    private ArrayList<Uri> uris;
    public TowWaysViewAdapter(Context context, ArrayList<Uri> uris) {
        super(context,0,uris);
        this.context=context;
        this.uris=uris;
    }

    @Override
    public int getCount() {
        return uris.size();
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_horizontal_listview, null);
        }
        final ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView_two_ways_list_item);

        if(uris.get(position).toString().contains("firebasestorage")){
            Picasso.with(context).load(uris.get(position)).networkPolicy(NetworkPolicy.OFFLINE)
                    .fit().centerInside()
                    .into(imageView, new Callback() {
                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onError() {
                            Picasso.with(context).load(uris.get(position))
                                    .fit().centerInside().into(imageView);

                        }
                    });

        }else {
            imageView.setImageURI(uris.get(position));

        }
        return convertView;
    }

    public ArrayList<Uri> getUris(){
        return uris;
    }
}
