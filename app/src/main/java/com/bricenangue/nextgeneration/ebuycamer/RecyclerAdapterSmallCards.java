package com.bricenangue.nextgeneration.ebuycamer;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by bricenangue on 27/07/16.
 */
public class RecyclerAdapterSmallCards extends RecyclerView
        .Adapter<RecyclerAdapterSmallCards
        .DataObjectHolder>  {
    private static String LOG_TAG = "RecyclerAdaptaterCreateShoppingList";
    private ArrayList<String> mDataset;
    private RecyclerAdaptaterCategoryClickListener myClickListener;



    public static class DataObjectHolder extends RecyclerView.ViewHolder
            implements View
            .OnClickListener{
        TextView listname;
        private RecyclerAdaptaterCategoryClickListener myClickListener;

        public DataObjectHolder(View itemView, RecyclerAdaptaterCategoryClickListener myClickListener1) {
            super(itemView);
            myClickListener=myClickListener1;
            listname = (TextView) itemView.findViewById(R.id.textView_category_small_card);

            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            myClickListener.onItemClick(getAdapterPosition(), v);
        }


    }

    public void setOnshoppinglistsmallClickListener(RecyclerAdaptaterCategoryClickListener myClickListener) {
        this.myClickListener = myClickListener;
    }

    public RecyclerAdapterSmallCards(AppCompatActivity context, ArrayList<String> myDataset,
                                     RecyclerAdaptaterCategoryClickListener myClickListener) {
        mDataset = myDataset;
        this.myClickListener=myClickListener;

    }

    public RecyclerAdapterSmallCards(ArrayList<String> myDataset){

        mDataset = myDataset;
    }
    @Override
    public DataObjectHolder onCreateViewHolder(ViewGroup parent,
                                               int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.category_item_small_card, parent, false);

        DataObjectHolder dataObjectHolder = new DataObjectHolder(view,myClickListener);
        return dataObjectHolder;
    }

    @Override
    public void onBindViewHolder(final DataObjectHolder holder, final int position) {
        String category=mDataset.get(position);
        holder.listname.setText(category);
    }








    public void addItem(String dataObj) {

        mDataset.add(dataObj);

        notifyItemInserted(mDataset.size()-1);
    }


    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public interface RecyclerAdaptaterCategoryClickListener {
        public void onItemClick(int position, View v);

    }

}
