package com.bricenangue.nextgeneration.ebuycamer;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MyChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private FirebaseAuth auth;
    private DatabaseReference root;
    private FirebaseUser user;
    private ProgressDialog progressBar;
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
        setContentView(R.layout.activity_my_chat);

        if (!haveNetworkConnection()){
            dismissProgressbar();
            Toast.makeText(getApplicationContext(),getString(R.string.connection_to_server_not_aviable)
                    ,Toast.LENGTH_SHORT).show();
        }

        userSharedPreference=new UserSharedPreference(this);
        auth=FirebaseAuth.getInstance();
        if(auth!=null){
            user=auth.getCurrentUser();
        }else {
            startActivity(new Intent(MyChatActivity.this,MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        }


        root= FirebaseDatabase.getInstance().getReference().child(ConfigApp.FIREBASE_APP_URL_CHAT_USER).child(user.getUid());

        recyclerView=(RecyclerView)findViewById(R.id.recyclerview_my_chat);
        layoutManager=new LinearLayoutManager(this);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

    }

    @Override
    protected void onResume() {
        super.onResume();

        fetchMyPost();
    }
    private void showProgressbar(){
        progressBar = new ProgressDialog(this);
        progressBar.setCancelable(false);
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.show();
    }

    private void dismissProgressbar(){
        if (progressBar!=null){
            progressBar.dismiss();
        }
    }


    private void fetchMyPost() {
        showProgressbar();
        final Query reference= root.limitToLast(100);
        //reference.keepSynced(true);
        final FirebaseRecyclerAdapter<ChatLoad,MyChatViewHolder> adapter=
                new FirebaseRecyclerAdapter<ChatLoad,MyChatViewHolder>(
                        ChatLoad.class,
                        R.layout.mychat_activtiy_item,
                        MyChatViewHolder.class,
                        reference
                ) {
                    @Override
                    protected void populateViewHolder(final MyChatViewHolder viewHolder, final ChatLoad model, int position) {

                        assert model!=null;

                        if(getItemCount()==0){
                            dismissProgressbar();
                        }else if (position==getItemCount()-1){
                            dismissProgressbar();
                        }

                        if(model.getPath_creator_uid()!= null){
                            viewHolder.lastmassage.setText(model.getLastmessage());
                            /**hi
                             if(!model.isNotseen()){

                             viewHolder.lastmassage.setTextColor(getResources().getColor(R.color.colorAccent));
                             }else {
                             viewHolder.lastmassage.setText(model.getLastmessage());
                             viewHolder.lastmassage.setTextColor(getResources().getColor(R.color.divider));
                             }

                             **/
                            if (model.getPath_creator_uid().equals(user.getUid())){
                                viewHolder.name.setText(model.getBuyer_name());
                            }else {
                                viewHolder.name.setText(model.getCreator_name());
                            }


                            viewHolder.titel.setText(model.getPost_title());



                            if(model.getPost_first_image()!=null){
                                byte[] decodedString = Base64.decode(model.getPost_first_image(), Base64.DEFAULT);
                                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                                viewHolder.postPicture.setImageBitmap(decodedByte);

                            }




                            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (!haveNetworkConnection()){
                                        Toast.makeText(getApplicationContext(),getString(R.string.connection_to_server_not_aviable)
                                                ,Toast.LENGTH_SHORT).show();
                                    }else {
                                        startActivity(new Intent(MyChatActivity.this,ChatActivity.class)
                                                .putExtra("post_id",model.getPath_post_id())
                                                .putExtra("creator_uid",model.getPath_creator_uid())
                                                .putExtra("key",model.getPath_buyer())
                                                .putExtra("is_deal",model.getIs_deal())
                                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                                    }

                                }
                            });

                            viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                                @Override
                                public boolean onLongClick(View view) {

                                    showFilterPopup(view,model.getPath_creator_uid(),model.getPath_post_id(),model.getPath_buyer());
                                    return false;

                                }
                            });
                            Date date = new Date(model.getLastmessage_timestamp());
                            DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy - HH:mm");
                            String dateFormatted = formatter.format(date);

                            // CheckTimeStamp checkTimeStamp= new CheckTimeStamp(getApplicationContext(),model.getLastmessage_timestamp());

                            viewHolder.time.setText(dateFormatted);

                            viewHolder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                                    // open menu
                                }
                            });
                        }else {
                            viewHolder.lastmassage.setText(getString(R.string.new_conversation));
                        }

                    }
                };

        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        if(adapter.getItemCount()==0){
            dismissProgressbar();
        }

    }




    public static class MyChatViewHolder extends RecyclerView.ViewHolder{
        ImageView postPicture;
        TextView titel, time, name,lastmassage;
        private View view;
        CheckBox checkBox;


        public MyChatViewHolder(View itemView) {
            super(itemView);
            view=itemView;

            postPicture=(ImageView) itemView.findViewById(R.id.imageView_publicationFirstphoto_mychat_cardview);

            checkBox=(CheckBox) itemView.findViewById(R.id.checkbox_my_chat);

            name=(TextView)itemView.findViewById(R.id.textView_publication_title_mychat_name);
            titel=(TextView) itemView.findViewById(R.id.textView_publication_price_mychat_title);
            time=(TextView) itemView.findViewById(R.id.textView_publication_price_mychat_time);
            lastmassage=(TextView) itemView.findViewById(R.id.textView_publication_price_mychat_lastmessage);
        }
    }

    private void deleteChat(final String creator_uid, final String post_id, final String foreign_uid) {

        final AlertDialog alertDialog =
                new AlertDialog.Builder(MyChatActivity.this).setTitle(
                        getString(R.string.alertDialogdeleteCHatt)).setMessage(getString(R.string.alertDialogdeleteCHatt_message))
                        .create();
        alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.button_cancel)
                , new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        alertDialog.dismiss();

                    }

                });
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.button_delete)
                , new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        if(!haveNetworkConnection()){
                            Toast.makeText(getApplicationContext(),getString(R.string.connection_to_server_not_aviable)
                                    ,Toast.LENGTH_SHORT).show();
                        }else {
                            showProgressbar();
                            final DatabaseReference referencemychat=FirebaseDatabase.getInstance().getReference().child(ConfigApp.FIREBASE_APP_URL_MY_CHAT)
                                    .child(user.getUid())
                                    .child(creator_uid)
                                    .child(post_id)
                                    .child(foreign_uid);
                            referencemychat.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChildren()){
                                        DataSnapshot snapshot=dataSnapshot.getChildren().iterator().next();
                                        final String key=snapshot.getKey();
                                        DatabaseReference ref =  FirebaseDatabase.getInstance().getReference().child(ConfigApp.FIREBASE_APP_URL_CHAT_USER)
                                                .child(user.getUid())
                                                .child(key);
                                        ref.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                DatabaseReference referencechat=FirebaseDatabase.getInstance().getReference().child(ConfigApp.FIREBASE_APP_URL_CHATS)
                                                        .child(creator_uid)
                                                        .child(post_id)
                                                        .child(foreign_uid);
                                                referencechat.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        referencemychat.child(key).removeValue()
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if(task.isSuccessful()){
                                                                            dismissProgressbar();
                                                                            Toast.makeText(getApplicationContext(),
                                                                                    getString(R.string.toast_chat_deleted),Toast.LENGTH_SHORT).show();
                                                                            alertDialog.dismiss();
                                                                        }
                                                                    }
                                                                });
                                                    }
                                                });
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                    dismissProgressbar();
                                    alertDialog.dismiss();
                                }
                            });
                        }
                    }
                });
        alertDialog.setCancelable(false);
        alertDialog.show();

    }
    private void showFilterPopup(View v,final String creator_uid, final String post_id, final String foreign_uid) {
        PopupMenu popup = new PopupMenu(this, v);
        // Inflate the menu from xml
        popup.getMenuInflater().inflate(R.menu.popupmenu, popup.getMenu());
        // Setup menu item selection
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_delete_chat:


                        deleteChat(creator_uid, post_id,foreign_uid);
                        return true;
                    default:
                        return false;
                }
            }
        });
        // Handle dismissal with: popup.setOnDismissListener(...);
        // Show the menu
        popup.show();
    }
}
