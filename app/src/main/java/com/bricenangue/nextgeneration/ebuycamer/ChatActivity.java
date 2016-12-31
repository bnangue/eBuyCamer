package com.bricenangue.nextgeneration.ebuycamer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.util.Base64;
import android.util.Pair;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.Firebase;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener {

    public static boolean messageshowed= true;
    public static String chatpartner_uid;
    private TextView mUserMessageChatText;
    private ListView listView_chat;

    /* Sender and Recipient status*/
    private static final int SENDER_STATUS=0;
    private static final int RECIPIENT_STATUS=1;
    private ImageView postPic;
    private TextView textView_post_title, textView_post_price;
    private ImageButton arrow_right;


    /* Listen to change in chat in firabase-remember to remove it */
    private ChildEventListener mMessageChatListener;
    private EditText editText;
    private Button buttonSend;
    private DatabaseReference root;
    private FirebaseAuth auth;
    private String temp_key,reciever_token, creator_token;
    private String foreign_uid;

    private ChildEventListener childEventListener;
    private List<MessageChatModel> newMessages=new ArrayList<>();
    private FirebaseUser user;
    private   String post_id;
    private   String creator_uid;
    public static String post_id_not;
    public static String creator_uid_not;

    private UserPublic creator_profile, receiver_profile;
    private String is_deal;
    private String chat_first_pic,chat_title;
    private  PrivateContent postcontent;
    private String[] currencyArray;
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
        setContentView(R.layout.activity_chat);

        userSharedPreference=new UserSharedPreference(this);

        messageshowed=false;

        Bundle extras = getIntent().getExtras();
        if(extras!=null){
            if(extras.containsKey("key")){
                foreign_uid=extras.getString("key");
            }
            if(extras.containsKey("post_id")){
                post_id=extras.getString("post_id");
            }
            if(extras.containsKey("creator_uid")){
                creator_uid=extras.getString("creator_uid");
            }
            if(extras.containsKey("is_deal")){
                is_deal=extras.getString("is_deal");
            }
        }

        chatpartner_uid=foreign_uid;
        creator_uid_not=creator_uid;
        post_id_not=post_id;

        currencyArray=getResources().getStringArray(R.array.currency);
        editText=(EditText)findViewById(R.id.chat_user_message);
        buttonSend=(Button)findViewById(R.id.sendUserMessage);
        listView_chat=(ListView)findViewById(R.id.chat_listview) ;

        textView_post_title=(TextView) findViewById(R.id.textView_viewcontent_title_activity);
        textView_post_price=(TextView)findViewById(R.id.textView_viewcontent_price_activity);
        postPic=(ImageView) findViewById(R.id.imageView_viewcontent_postpic_activity) ;

        arrow_right=(ImageButton) findViewById(R.id.arrow_view_post) ;
        arrow_right.setOnClickListener(this);

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
        auth=FirebaseAuth.getInstance();
        userSharedPreference.setUserDataRefreshed(haveNetworkConnection());
        if(auth!=null){
            user=auth.getCurrentUser();
        }else {

            if(userSharedPreference.getUserLoggedIn()){
                // user offline
                if(!userSharedPreference.getUserDataRefreshed()){
                    // user refreshed data on start
                }

            }else {
                // user online but auth problem
                Toast.makeText(this,getString(R.string.problem_while_loading_user_data_auth_null),Toast.LENGTH_LONG).show();

                startActivity(new Intent(ChatActivity.this,MainActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                Intent.FLAG_ACTIVITY_NEW_TASK));
                finish();
            }

        }

        if(post_id!=null && is_deal.equals("deal")){
            final ImageView img =(ImageView) findViewById(R.id.imageView_my_favorite_sale_logo_chat) ;

            DatabaseReference refPost= FirebaseDatabase.getInstance().getReference()
                    .child(ConfigApp.FIREBASE_APP_URL_USERS_DEAL).child(post_id)
                    .child("privateContent");
            refPost.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot!=null){
                        postcontent=dataSnapshot.getValue(PrivateContent.class);

                        if(postcontent!=null){
                            chat_first_pic=postcontent.getFirstPicture();
                            chat_title=postcontent.getTitle();
                            populatePost(postcontent);
                            img.setVisibility(View.VISIBLE);
                        }

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(getApplicationContext(),databaseError.getMessage(),Toast.LENGTH_SHORT).show();

                }
            });
        }else if(post_id!=null && !is_deal.equals("deal")){
            DatabaseReference refPost= FirebaseDatabase.getInstance().getReference()
                    .child(ConfigApp.FIREBASE_APP_URL_USERS_POSTS).child(post_id)
                    .child("privateContent");
            refPost.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot!=null){

                        postcontent=dataSnapshot.getValue(PrivateContent.class);

                        if (postcontent!=null){
                            chat_first_pic=postcontent.getFirstPicture();
                            chat_title=postcontent.getTitle();
                            populatePost(postcontent);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                    Toast.makeText(getApplicationContext(),databaseError.getMessage(),Toast.LENGTH_SHORT).show();
                }
            });
        }

        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        root= FirebaseDatabase.getInstance().getReference();

        if(creator_uid!=null && post_id!=null && foreign_uid!=null){

            DatabaseReference refprofile =root.child(ConfigApp.FIREBASE_APP_URL_USERS)
                    .child(creator_uid).child("userPublic");
            refprofile.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot!=null){
                        creator_profile= dataSnapshot.getValue(UserPublic.class);
                    }else {
                        //user do not exist
                        finish();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(getApplicationContext(),databaseError.getMessage(),Toast.LENGTH_SHORT).show();

                }
            });


            // keep token uptodate for notification
            DatabaseReference refreciever =root.child(ConfigApp.FIREBASE_APP_URL_USERS)
                    .child(foreign_uid).child("userPublic/chatId");
            refreciever.keepSynced(true);

            DatabaseReference refcreator =root.child(ConfigApp.FIREBASE_APP_URL_USERS)
                    .child(creator_uid).child("userPublic/chatId");
            refcreator.keepSynced(true);

            DatabaseReference refrecieverprofile =root.child(ConfigApp.FIREBASE_APP_URL_USERS)
                    .child(foreign_uid).child("userPublic");
            refrecieverprofile.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot!=null){
                        receiver_profile= dataSnapshot.getValue(UserPublic.class);
                        getSupportActionBar().setCustomView(R.layout.chat_action_bar_layout);
                        getSupportActionBar().setDisplayShowTitleEnabled(false);
                        getSupportActionBar().setDisplayShowCustomEnabled(true);
                        getSupportActionBar().setDisplayUseLogoEnabled(false);
                        getSupportActionBar().setDisplayShowHomeEnabled(false);
                        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                        getSupportActionBar().setHomeButtonEnabled(false);

                        CircularImageView imageView=(CircularImageView)getSupportActionBar().getCustomView().findViewById(R.id.imageView_viewcontent_userpic_actionBarLayout);
                        TextView name=(TextView) getSupportActionBar().getCustomView().findViewById(R.id.textView_viewcontent_user_name_actionBarLayout);


                        if(creator_uid.equals(user.getUid())){
                            if (receiver_profile!=null){
                                name.setText(receiver_profile.getName());
                                if (receiver_profile.getProfilePhotoUri()!=null){
                                    loadPicture(receiver_profile.getProfilePhotoUri(),imageView);

                                }
                            }else {
                                if (!haveNetworkConnection()){
                                    Toast.makeText(getApplicationContext(),getString(R.string.alertDialog_no_internet_connection),Toast.LENGTH_SHORT).show();
                                }
                            }

                        }else {
                            if (creator_profile!=null){
                                if (creator_profile.getProfilePhotoUri()!=null){
                                    loadPicture(creator_profile.getProfilePhotoUri(),imageView);
                                }
                                name.setText(creator_profile.getName());
                            }else {
                                if (!haveNetworkConnection()){
                                    Toast.makeText(getApplicationContext(),getString(R.string.alertDialog_no_internet_connection),Toast.LENGTH_SHORT).show();
                                }
                            }

                        }

                    }else {
                        //user do not exist
                        finish();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(getApplicationContext(),databaseError.getMessage(),Toast.LENGTH_SHORT).show();

                }
            });

            refcreator.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    creator_token=dataSnapshot.getValue(String.class);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(getApplicationContext(),databaseError.getMessage(),Toast.LENGTH_SHORT).show();

                }
            });
            refreciever.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    reciever_token=dataSnapshot.getValue(String.class);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(getApplicationContext(),databaseError.getMessage(),Toast.LENGTH_SHORT).show();

                }
            });
        }

    }

    private void sendMessage() {

        if(creator_uid!=null && post_id!=null && foreign_uid!=null && reciever_token!=null){

            DatabaseReference message_root=root.child(ConfigApp.FIREBASE_APP_URL_CHATS)
                    .child(creator_uid)
                    .child(post_id)
                    .child(foreign_uid);
            temp_key=message_root.push().getKey();

            final String message=editText.getText().toString();
            if(!editText.getText().toString().isEmpty()){

                Map<String,Object> map1=new HashMap<String, Object>();

                map1.put("sender", user.getUid());
                map1.put("recipient",foreign_uid);
                map1.put("message",editText.getText().toString());

                message_root.child(temp_key).updateChildren(map1)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                //send Notification
                                editText.setText("");
                                if(!task.isSuccessful()){
                                    Toast.makeText(getApplicationContext(),getString(R.string.toast_text_pending)
                                            ,Toast.LENGTH_SHORT).show();
                                    if (!haveNetworkConnection()){
                                        Toast.makeText(getApplicationContext(),getString(R.string.alertDialog_no_internet_connection),Toast.LENGTH_SHORT).show();
                                    }
                                }else {
                                    sendNotification(message);
                                    if(receiver_profile!=null && creator_profile!=null){
                                        ChatLoad chatLoad=new ChatLoad(
                                                receiver_profile.getName(),creator_uid,post_id,foreign_uid,message,System.currentTimeMillis()
                                                ,chat_first_pic,chat_title,is_deal,true,creator_profile.getName());
                                        createMychat(chatLoad);
                                    }else {
                                        if (!haveNetworkConnection()){
                                            Toast.makeText(getApplicationContext(),getString(R.string.alertDialog_no_internet_connection),Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                }
                            }


                        });

            }
        }
    }


    private void sendNotification(String message) {
       new SendNotification(message).execute();
    }

    @Override
    public void onClick(View view) {
        if (postcontent!= null){
            openViewContent(postcontent);
        }else {
            Toast.makeText(getApplicationContext(),getString(R.string.action_not_avialable_or_offline),Toast.LENGTH_SHORT).show();
        }

    }


    private void populatePost(PrivateContent content){

        textView_post_title.setText(content.getTitle());
        DecimalFormat decFmt = new DecimalFormat("#,###.##", DecimalFormatSymbols.getInstance(Locale.FRENCH));
        decFmt.setMaximumFractionDigits(2);
        decFmt.setMinimumFractionDigits(2);

        String p=content.getPrice();
        BigDecimal amt = new BigDecimal(p);
        String preValue = decFmt.format(amt)+ " " +currencyArray[0];
        if(content.getCategorie().getCatNumber()==20){
            if(content.getCreatorid().equals(user.getUid())){
                //show price for owner only
                textView_post_price.setText(preValue);

            }else {
                textView_post_price.setText(getString(R.string.string_price_of_deal_negotiable));

            }
        }else {
            textView_post_price.setText(preValue);
        }

        if (content.getFirstPicture()!=null){
            byte[] decodedString = Base64.decode(content.getFirstPicture(), Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

            postPic.setImageBitmap(decodedByte);
        }

    }


    private void openViewContent(PrivateContent postcontent) {
        if(postcontent.getCategorie().getCatNumber()==20){
            if(postcontent.getCreatorid().equals(user.getUid())){
                startActivity(new Intent(ChatActivity.this,SingleDealActivityActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .putExtra("user_uid",postcontent.getCreatorid())
                        .putExtra("dealid",postcontent.getUniquefirebaseId()));


            }else {
                startActivity(new Intent(ChatActivity.this,ViewContentDealActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .putExtra("user_uid",postcontent.getCreatorid())
                        .putExtra("post",postcontent.getUniquefirebaseId()));

            }
           // Deal
        }else {

            //Post
            startActivity(new Intent(ChatActivity.this,ViewContentActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .putExtra("post",postcontent.getUniquefirebaseId())
                    .putExtra("location",postcontent.getLocation().getName())
                    .putExtra("categorie",postcontent.getCategorie().getName()));
        }
    }


    public class SendNotification extends AsyncTask<Void,Void,Void>
    {

        String  message;

        SendNotification(String message){
            this.message=message;
        }
        @Override
        protected void onPostExecute(Void reponse) {
            super.onPostExecute(reponse);
        }

        @Override
        protected Void doInBackground(Void... params) {

            HttpURLConnection conn=null;
            try {
                ArrayList<Pair<String,String>> data=new ArrayList<>();


                String title=getString(R.string.fcm_Notification_message);

                data.add(new Pair<String, String>("message", message));
                if(creator_uid.equals(user.getUid())){
                    data.add(new Pair<String, String>("receivertoken",reciever_token));
                    data.add(new Pair<String, String>("receiver",receiver_profile.getName()));
                    data.add(new Pair<String, String>("sender", creator_profile.getName() ));
                    data.add(new Pair<String, String>("sender_uid", creator_profile.getUniquefirebasebId() ));

                }else {
                    data.add(new Pair<String, String>("receivertoken",creator_token));
                    data.add(new Pair<String, String>("receiver",creator_profile.getName()));
                    data.add(new Pair<String, String>("sender", receiver_profile.getName() ));
                    data.add(new Pair<String, String>("sender_uid", receiver_profile.getUniquefirebasebId() ));
                }

                data.add(new Pair<String, String>("title",title ));
                data.add(new Pair<String, String>("is_deal",is_deal ));
                //unique secure path to chat
                data.add(new Pair<String, String>("foreign_uid",foreign_uid));
                data.add(new Pair<String, String>("post_id",post_id ));
                data.add(new Pair<String, String>("creator_uid",creator_uid ));

                byte[] bytes = getData(data).getBytes("UTF-8");


                URL url=new URL(ConfigApp.OOOWEBHOST_SERVER_URL+ "FirebasePushNotification.php");
                conn=(HttpURLConnection)url.openConnection();
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setFixedLengthStreamingMode(bytes.length);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded;charset=UTF-8");
                // post the request
                OutputStream out = conn.getOutputStream();
                out.write(bytes);
                out.close();

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuffer reponse = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    reponse.append(inputLine);
                }
                final String response =reponse.toString();
                System.out.print(response);

            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                if(conn!=null){
                    conn.disconnect();
                }
            }
            return null;
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        messageshowed=false;
        chatpartner_uid=foreign_uid;
        creator_uid_not=creator_uid;
        post_id_not=post_id;

    }

    private void loadChat(){
        if(creator_uid!=null && post_id!=null && foreign_uid!=null){
            DatabaseReference ref=root.child(ConfigApp.FIREBASE_APP_URL_CHATS)
                    .child(creator_uid)
                    .child(post_id)
                    .child(foreign_uid);

            final FirebaseListAdapter<MessageChatModel> adapter=new FirebaseListAdapter<MessageChatModel>(
                    ChatActivity.this,
                    MessageChatModel.class,
                    R.layout.chat,
                    ref
            ) {
                @Override
                protected void populateView(View v, MessageChatModel model, int position) {
                    boolean left;
                    if(model.getSender().equals(user.getUid())){
                        model.setRecipientOrSenderStatus(SENDER_STATUS);
                    }else{
                        model.setRecipientOrSenderStatus(RECIPIENT_STATUS);
                    }

                    LinearLayout layout=(LinearLayout)v.findViewById(R.id.message1);

                    if(model.getRecipientOrSenderStatus()==RECIPIENT_STATUS){
                        left=true;
                    }else{
                        left=false;
                    }
                    TextView chattext=(TextView)v.findViewById(R.id.singlemessage);
                    TextView sendStatus=(TextView)v.findViewById(R.id.textView_chat_item_send_status);

                    chattext.setText(model.getMessage());
                    chattext.setBackgroundResource(left ? R.drawable.out_message_bg : R.drawable.in_message_bg);
                    layout.setGravity(left ? Gravity.LEFT : Gravity.RIGHT);
                    LinearLayout.LayoutParams params= (LinearLayout.LayoutParams) layout.getLayoutParams();
                    if(left){
                        params.setMargins(0,0,100,0);
                    }else {
                        params.setMargins(100,0,0,0);
                    }

                    layout.setLayoutParams(params);

                }
            };
            listView_chat.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
           // listView_chat.setStackFromBottom(true);
            listView_chat.setAdapter(adapter);
            listView_chat.setScrollY(adapter.getCount()-1);
            listView_chat.post(new Runnable(){
                public void run() {
                    listView_chat.setSelection(adapter.getCount() - 1);
                }});
           // listView_chat.smoothScrollToPosition(adapter.getCount() -1);

        }else {
            if (!haveNetworkConnection()){
                Toast.makeText(getApplicationContext(),getString(R.string.alertDialog_no_internet_connection),Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void loadPicture(final String photoUrl, final ImageView imageView) {
        if(photoUrl.contains("https")){
            Picasso.with(getApplicationContext()).load(photoUrl).networkPolicy(NetworkPolicy.OFFLINE)

                    .into(imageView, new Callback() {
                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onError() {
                            Picasso.with(getApplicationContext()).load(photoUrl)
                                    .into(imageView);

                        }
                    });

        }else {
            byte[] decodedString = Base64.decode(photoUrl, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

            imageView.setImageBitmap(decodedByte);
        }
    }

    private static String getData(ArrayList<Pair<String, String>> values) throws UnsupportedEncodingException {
        StringBuilder result=new StringBuilder();
        for(Pair<String,String> pair : values){

            if(result.length()!=0)

                result.append("&");
            result.append(URLEncoder.encode(pair.first, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.second, "UTF-8"));

        }
        return result.toString();
    }

    @Override
    protected void onStop() {
        super.onStop();
        messageshowed=true;
        chatpartner_uid=null;
        creator_uid_not=null;
        post_id_not=null;

    }

    @Override
    protected void onPause() {
        super.onPause();
        messageshowed=true;

        chatpartner_uid=null;
        creator_uid_not=null;
        post_id_not=null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        messageshowed=false;
        loadChat();
        MyFireBaseMessagingService.notificationId=0;
        chatpartner_uid=foreign_uid;
        creator_uid_not=creator_uid;
        post_id_not=post_id;


    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void createMychat(final ChatLoad chatLoad){
        //to save chat load keys
        final DatabaseReference reference=FirebaseDatabase.getInstance().getReference()
                .child(ConfigApp.FIREBASE_APP_URL_MY_CHAT)
                .child(user.getUid())
                .child(creator_uid)
                .child(post_id)
                .child(foreign_uid);

        final DatabaseReference ref=FirebaseDatabase.getInstance().getReference()
                .child(ConfigApp.FIREBASE_APP_URL_CHAT_USER)
                .child(user.getUid());

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()){
                    DataSnapshot snapshot =dataSnapshot.getChildren().iterator().next();
                    ref.child(snapshot.getKey()).setValue(chatLoad);
                }else {
                    String key_temp=ref.push().getKey();

                    ref.child(key_temp).setValue(chatLoad);
                    reference.child(key_temp).setValue(key_temp);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

}
