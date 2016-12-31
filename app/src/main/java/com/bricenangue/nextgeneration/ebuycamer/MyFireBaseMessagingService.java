package com.bricenangue.nextgeneration.ebuycamer;


import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bricenangue on 12/08/16.
 */
public class MyFireBaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private final static String GROUP_KEY_MESSAGES = "group_key_messages";
    private final static String GROUP_KEY_PUBLICATION = "group_key_publication";
    private final static String GROUP_KEY_DEAL = "group_key_deal";
    public static final int NOTIFICATION_ID = 1;

    private NotificationManager mNotificationManager;

    private int incomingNotifiId;
    public static int notificationId=0;
    public static int notificationIdOffer=0;
    public static int notificationIdDeal=0;
    public static int notificationIdPublication=0;


    public static ArrayList<String> incomingmessages=new ArrayList<>();



    private FirebaseAuth auth;
    private FirebaseUser user;
    private int UNIQUE_REQUEST_CODE=0;
    public static int number=0;
    public static int numberDeal=0;
    private static final String GROUP_KEY_MESSAGES_OFFERS="group_key_offer";


    public MyFireBaseMessagingService() {

    }

    @TargetApi(Build.VERSION_CODES.N)
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        //Displaying data in log
        //It is optional
        remoteMessage.getData();

        Log.d(TAG, "From: " + remoteMessage.getFrom());
       // Log.d(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());


        Map<String,String > maps=remoteMessage.getData();
        if(maps.containsKey("title")){
            String title=maps.get("title");


            if(title.contains("New chat message")){
                if(ChatActivity.chatpartner_uid==null && ChatActivity.messageshowed ){
                    sendChatNotification(maps);
                }else if (ChatActivity.chatpartner_uid!=null && ChatActivity.creator_uid_not!=null && !ChatActivity.messageshowed && ChatActivity.post_id_not!=null){
                    if (user.getUid().equals(maps.get("creator_uid"))
                            && !ChatActivity.chatpartner_uid.equals(maps.get("foreign_uid"))){
                        sendChatNotification(maps);
                    }else if (user.getUid().equals(maps.get("foreign_uid")) && !ChatActivity.creator_uid_not.equals(maps.get("creator_uid"))){
                        sendChatNotification(maps);

                    } else if (!ChatActivity.post_id_not.equals(maps.get("post_id")) && !user.getUid().equals(maps.get("creator_uid"))
                            && ChatActivity.chatpartner_uid.equals(maps.get("foreign_uid"))){
                        sendChatNotification(maps);

                    }else if (!ChatActivity.post_id_not.equals(maps.get("post_id")) && !user.getUid().equals(maps.get("foreign_uid"))
                            && ChatActivity.creator_uid_not.equals(maps.get("creator_uid"))){
                        sendChatNotification(maps);
                    }
                }else {
                    sendChatNotification(maps);
                }


            }else if (title.contains("New Publication")){
                if(maps.get("sender_uid")!=null && !maps.get("sender_uid").equals(user.getUid())){
                    sendPostNotification(maps);
                }


            }else if (title.contains("New Deal")){
                if(maps.get("sender_uid")!=null && !maps.get("sender_uid").equals(user.getUid())){
                    sendDealNotification(maps);
                }

            }else if (title.contains("New Offer")){
                if (SingleDealActivityActivity.singledeal){
                    sendNewOfferNotification(maps);
                }

            }
        }else {
          sendNotification(maps);

        }

        /**
        else{
            if(!LiveChatActivity.messageshowed && maps.get("sender").equals(LiveChatActivity.receiverName)){
                sendOrderedBroadcast(i, null);
            }else if(LiveChatActivity.messageshowed){
                sendChatNotification(maps);
               // LiveChatBroadcastReceiver.completeWakefulIntent(intent);
            }else if(!LiveChatActivity.messageshowed && !maps.get("sender").equals(LiveChatActivity.receiverName)){
                sendChatNotification(maps);
               // LiveChatBroadcastReceiver.completeWakefulIntent(intent);
            }

        }
        **/
        //Calling method to generate notification

    }

    @Override
    public void onCreate() {
        super.onCreate();

        auth=FirebaseAuth.getInstance();
        if(auth!=null){
            user=auth.getCurrentUser();
        }

    }

    //This method is only generating push notification
    private void sendNotification(Map<String,String> stringMap) {
        Map<String,String> map=stringMap;

        Intent intent = new Intent(this, ChatActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_app_logo)
                .setContentTitle(map.get("title"))
                .setContentText(map.get("message"))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());
    }



    private void sendChatNotification(Map<String,String> stringMap) {

        Map<String,String> map =stringMap;
        notificationId++;
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_app_logo);

        //String chattingFrom = extras.getString("chattingFrom");
        String sender = map.get("sender");// will be user as receiver name in current Device getting the notifiction
        String msg = map.get("message");
        String title=map.get("title");
        String reciever=map.get("receiver");
        String senderuid=map.get("foreign_uid");
        String creator_uid=map.get("creator_uid");
        String post_id=map.get("post_id");
        String is_deal=map.get("is_deal");
        String sender_uid=map.get("sender_uid");

        String message=sender+": " +msg;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                this).setSmallIcon(R.mipmap.ic_app_logo)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(""))
                .setContentText( getString(R.string.Chat_Message_recieved_from)+" " +sender);
        builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        builder.setAutoCancel(true);
        mNotificationManager = (NotificationManager) this
                .getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(this,ChatActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        //intent.putExtra("chattingFrom", chattingFrom);
        intent.putExtra("key",senderuid);
        intent.putExtra("post_id", post_id);
        intent.putExtra("creator_uid",creator_uid);
        intent.putExtra("is_deal",is_deal);

        createMychat(creator_uid,post_id,senderuid,msg,System.currentTimeMillis(),sender,reciever,is_deal,sender_uid);

        Intent backIntent = new Intent(getApplicationContext(), MainPageActivity.class);
        backIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

        final PendingIntent pendingIntent = PendingIntent.getActivities(getApplicationContext(), UNIQUE_REQUEST_CODE++,
                new Intent[] {backIntent, intent}, PendingIntent.FLAG_CANCEL_CURRENT);

        incomingmessages.add(message);
        NotificationCompat.InboxStyle style=new NotificationCompat.InboxStyle();
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setContentText(message);
        for(int i=0;i<incomingmessages.size();i++){
            style.addLine(incomingmessages.get(i));
            style.setBigContentTitle(notificationId + " new messages");
            style.setSummaryText("messages");
        }
        if(notificationId>=2){
            builder.setContentIntent(pendingIntent)
                    .setStyle(style)
                    .setAutoCancel(true)
                    .setGroup(GROUP_KEY_MESSAGES)
                    .setGroupSummary(true);
            incomingmessages.clear();
            //notificationId=0;
        }
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        boolean bool = settings.getBoolean("notifications_new_message", true);

        if(bool){
            builder.setContentIntent(pendingIntent);
            mNotificationManager.cancelAll();
            mNotificationManager.notify(notificationId, builder.build());

        }


    }

    private void sendPostNotification(Map<String,String> stringMap) {

        number++;
        notificationIdPublication++;
        Map<String,String> map =stringMap;
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_app_logo);

        String title = getString(R.string.fcm_Notification_new_publication_translatable);
        String msg=getString(R.string.fcm_Notification_new_publication_message);



        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                this).setSmallIcon(R.mipmap.ic_app_logo)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setContentText(msg)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg));
        builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        builder.setAutoCancel(true);
        mNotificationManager = (NotificationManager) this
                .getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(this,MainPageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);



        Intent backIntent = new Intent(getApplicationContext(), CategoryActivity.class);
        backIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

        final PendingIntent pendingIntent = PendingIntent.getActivities(getApplicationContext(), UNIQUE_REQUEST_CODE++,
                new Intent[] {backIntent, intent}, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.InboxStyle style=new NotificationCompat.InboxStyle();
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                .setContentText(msg);

            style.setBigContentTitle(notificationIdPublication + " "+getString(R.string.fcm_Notification_new_publication_translatable));
            style.setSummaryText(title);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        boolean bool = settings.getBoolean("notifications_new_publication", true);

        if(number>=3 && bool){
            builder.setContentIntent(pendingIntent)
                    .setStyle(style)
                    .setAutoCancel(true)
                    .setGroup(GROUP_KEY_PUBLICATION)
                    .setGroupSummary(true);
            builder.setContentIntent(pendingIntent);
            mNotificationManager.cancelAll();
            mNotificationManager.notify(notificationIdPublication, builder.build());
            number=0;
        }


    }

    // deal, publication, topic ( categories, [city + arraynumber])


    private void sendDealNotification(Map<String,String> stringMap) {

        numberDeal++;
        notificationIdDeal++;
        Map<String,String> map =stringMap;
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_app_logo);

        //String chattingFrom = extras.getString("chattingFrom");
      // will be user as receiver name in current Device getting the notifiction
        String msg = getString(R.string.new_deal_in_your_area);
        String title=getString(R.string.notification_new_deal_title);


        String message=msg;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                this).setSmallIcon(R.mipmap.ic_app_logo)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                ;
        builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        builder.setAutoCancel(true);
        mNotificationManager = (NotificationManager) this
                .getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(this,ViewDealsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);


        Intent backIntent = new Intent(getApplicationContext(), MainPageActivity.class);
        backIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

        final PendingIntent pendingIntent = PendingIntent.getActivities(getApplicationContext(), UNIQUE_REQUEST_CODE++,
                new Intent[] {backIntent, intent}, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.InboxStyle style=new NotificationCompat.InboxStyle();
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                .setContentText(msg);

        style.setBigContentTitle(notificationIdDeal + " "+getString(R.string.notification_new_deal_title));
        style.setSummaryText(title);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        boolean bool = settings.getBoolean("notifications_new_publication", true);

        if(numberDeal>=1 && bool){
            builder.setContentIntent(pendingIntent)
                    .setStyle(style)
                    .setAutoCancel(true)
                    .setGroup(GROUP_KEY_DEAL)
                    .setGroupSummary(true);
            builder.setContentIntent(pendingIntent);
            mNotificationManager.cancelAll();
            mNotificationManager.notify(notificationIdDeal, builder.build());
            numberDeal=0;
        }

    }


    private void sendNewOfferNotification(Map<String,String> stringMap) {

        Map<String,String> map =stringMap;
        notificationIdOffer++;
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_app_logo);

        String title=getString(R.string.fcm_Notification_new_offers_translatable);
        String post_id=map.get("post_id");

        String message=getString(R.string.fcm_Notification_new_offers_message);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                this).setSmallIcon(R.mipmap.ic_app_logo)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(""))
                .setContentText(message);

        builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        builder.setAutoCancel(true);

        mNotificationManager = (NotificationManager) this
                .getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(this,SingleDealActivityActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        intent.putExtra("dealid", post_id);

        /**
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
// Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(intent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT

                );

**/
        Intent backIntent = new Intent(getApplicationContext(), MyDealsActivity.class);
        backIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

        final PendingIntent pendingIntent = PendingIntent.getActivities(getApplicationContext(), UNIQUE_REQUEST_CODE++,
                new Intent[] {backIntent, intent}, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.InboxStyle style=new NotificationCompat.InboxStyle();
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setContentTitle(notificationIdOffer + " "+ getString(R.string.fcm_Notification_new_offers_translatables))
                .setContentText(message);


        style.setSummaryText(message);

        if(notificationIdOffer>=2){
            builder.setContentIntent(pendingIntent)
                    .setStyle(style)
                    .setAutoCancel(true)
                    .setContentText(message)
                    .setGroup(GROUP_KEY_MESSAGES_OFFERS)
                    .setGroupSummary(true);

        }
        builder.setContentIntent(pendingIntent);
        mNotificationManager.cancelAll();
        mNotificationManager.notify(notificationIdOffer, builder.build());

    }

    private void createMychat(final String creator_uid, final String post_id, final String foreign_uid
            , final String last_message, final long time, final String namebyuer, final String namecreator
            , final String is_deal,final String sender_uid){
        //to save chat load keys
        final DatabaseReference reference= FirebaseDatabase.getInstance().getReference()
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
                    final DataSnapshot snapshot =dataSnapshot.getChildren().iterator().next();
                    Map<String,Object> map=new HashMap<String, Object>();
                    map.put("lastmessage",last_message);
                    map.put("lastmessage_timestamp",time);
                    map.put("buyer_name",namebyuer);

                    map.put("path_creator_uid",creator_uid);
                    map.put("path_post_id",post_id);
                    map.put("path_buyer",foreign_uid);

                    map.put("buyer_name",namebyuer);
                    map.put("creator_name",namecreator);
                    map.put("is_deal",is_deal);

                    ref.child(snapshot.getKey()).updateChildren(map);

                }else {
                    String key=reference.push().getKey();
                    Map<String,Object> map=new HashMap<String, Object>();
                    map.put("lastmessage",last_message);
                    map.put("lastmessage_timestamp",time);
                    map.put("buyer_name",namebyuer);

                    map.put("path_creator_uid",creator_uid);
                    map.put("path_post_id",post_id);
                    map.put("path_buyer",foreign_uid);

                    map.put("buyer_name",namebyuer);
                    map.put("creator_name",namecreator);
                    map.put("is_deal",is_deal);

                    ref.child(key).updateChildren(map);
                    reference.child(key).setValue(key);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public static void setBadge(Context context, int count) {
        String launcherClassName = getLauncherClassName(context);
        if (launcherClassName == null) {
            return;
        }
        Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
        intent.putExtra("badge_count", count);
        intent.putExtra("badge_count_package_name", context.getPackageName());
        intent.putExtra("badge_count_class_name", launcherClassName);
        context.sendBroadcast(intent);
    }

    public static String getLauncherClassName(Context context) {

        PackageManager pm = context.getPackageManager();

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resolveInfos) {
            String pkgName = resolveInfo.activityInfo.applicationInfo.packageName;
            if (pkgName.equalsIgnoreCase(context.getPackageName())) {
                String className = resolveInfo.activityInfo.name;
                return className;
            }
        }
        return null;
    }
}
