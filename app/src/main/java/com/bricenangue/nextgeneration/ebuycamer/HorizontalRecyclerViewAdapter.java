package com.bricenangue.nextgeneration.ebuycamer;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bricenangue on 14/12/2016.
 */

public class HorizontalRecyclerViewAdapter  extends RecyclerView.Adapter<HorizontalRecyclerViewAdapter.ViewHolder>{


    private ArrayList<Uri> uris;
    private Context context;
    private HorizontalAdapterClickListener clickListener;

    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView profilaePicture;
        private Button delete;
        private ProgressBar progressBar;
        private HorizontalAdapterClickListener myclickListener;

        ViewHolder(View view, final HorizontalAdapterClickListener clickListener) {
            super(view);
            this.myclickListener=clickListener;
            profilaePicture = (ImageView) view.findViewById(R.id.imageView_horizotal_recyclerview_cardview);
            delete = (Button) view.findViewById(R.id.button_delete_horizotal_recyclerview_cardview);
            progressBar=(ProgressBar)view.findViewById(R.id.progressbar_horizontal_recyclerview);


            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    myclickListener.onItemClick(getAdapterPosition(),view);
                }
            });

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clickListener.ondelteCLick(getAdapterPosition());
                }
            });
        }
    }


    HorizontalRecyclerViewAdapter(Context context, ArrayList<Uri> uris, HorizontalAdapterClickListener clickListener) {
        this.clickListener=clickListener;
        this.context=context;
        this.uris = uris;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.horizontal_recyclerview_item, parent, false);

        return new ViewHolder(itemView,clickListener);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        if(uris.get(holder.getAdapterPosition()).toString().contains("firebasestorage")){
            Picasso.with(context).load(uris.get(holder.getAdapterPosition())).networkPolicy(NetworkPolicy.OFFLINE)
                    .fit()
                    .into(holder.profilaePicture, new Callback() {
                        @Override
                        public void onSuccess() {
                            holder.progressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {
                            Picasso.with(context).load(uris.get(holder.getAdapterPosition()))
                                    .fit().into(holder.profilaePicture);
                            holder.progressBar.setVisibility(View.GONE);

                        }
                    });

        }else {
            holder.progressBar.setVisibility(View.GONE);
            holder.profilaePicture.setImageURI(uris.get(holder.getAdapterPosition()));

        }

    }



    @Override
    public int getItemCount() {
        return uris.size();
    }

    void addUri(Uri uri){
        uris.add(uri);
        notifyItemInserted(uris.size() - 1);
    }

    void delete(Uri uri, int position){
        if(uris.contains(uri)){
            uris.remove(uri);
            notifyItemRemoved(position);
        }
    }


    interface HorizontalAdapterClickListener {
        public void onItemClick(int position, View v);
        public void ondelteCLick(int position);
    }
}
