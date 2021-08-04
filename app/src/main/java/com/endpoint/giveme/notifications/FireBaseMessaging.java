package com.endpoint.giveme.notifications;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;


import com.endpoint.giveme.R;
import com.endpoint.giveme.activities_fragments.activity_chat.ChatActivity;
import com.endpoint.giveme.activities_fragments.activity_splash_loading.SplashLoadingActivity;
import com.endpoint.giveme.models.FavoriteLocationModel;
import com.endpoint.giveme.models.MessageModel;
import com.endpoint.giveme.models.NotFireModel;
import com.endpoint.giveme.models.UserModel;
import com.endpoint.giveme.preferences.Preferences;
import com.endpoint.giveme.remote.Api;
import com.endpoint.giveme.tags.Tags;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.Map;

import io.paperdb.Paper;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FireBaseMessaging extends FirebaseMessagingService {

    private Preferences preferences = Preferences.getInstance();
    private Map<String,String> map;


    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        map = remoteMessage.getData();

        for (String key :map.keySet()){
            Log.e("Key=",key+"_value="+map.get(key));
        }

        if (getSession().equals(Tags.session_login))
        {
            String to_user_id = String.valueOf(Integer.parseInt(map.get("to_user_id")));
            String my_id = String.valueOf(getUserData().getUser().getId());
            String notification_type =map.get("noti_type");
            String from_user_id =map.get("from_user_id");
            Log.e("not",notification_type+"__");

            if (notification_type.equals("chat")){
                ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
                String current_class = activityManager.getRunningTasks(1).get(0).topActivity.getClassName();
                if (current_class.equals("com.endpoint.giveme.activities_fragments.activity_chat.ChatActivity")){
                    if (from_user_id.equals(getChatUserId())){

                        String id = String.valueOf(map.get("id"));
                        String room_id = map.get("room_id");
                        String type = String.valueOf(map.get("type"));
                        String message = String.valueOf(map.get("message"));
                        String date = String.valueOf(map.get("date"));
                        String image="";
                        String voice="";
                        if (map.get("image")!=null){
                            image = map.get("image");
                        }

                        if (map.get("voice")!=null){
                            voice = map.get("voice");
                        }
                        MessageModel messageModel = new MessageModel(Integer.parseInt(id),room_id,from_user_id,to_user_id,type,message,image,voice,date);
                        EventBus.getDefault().post(messageModel);

                    }else {
                        manageNotification(map);

                    }
                }else {

                    manageNotification(map);
                }


            }
            else if (notification_type.equals("location")){
                double lat = Double.parseDouble(map.get("latitude"));
                double lng = Double.parseDouble(map.get("longitude"));
                FavoriteLocationModel model = new FavoriteLocationModel("","","",lat,lng);
                EventBus.getDefault().post(model);

            }else {
                manageNotification(map);

            }
        }
    }

    private void manageNotification(Map<String, String> map) {

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
        {
            createNewNotificationVersion(map);
        }else
        {
            createOldNotificationVersion(map);

        }

    }







    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        if (getSession().equals(Tags.session_login))
        {
            updateTokenFireBase(s);

        }

    }

    @SuppressLint("NewApi")
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void createNewNotificationVersion(Map<String, String> map) {
        Paper.init(this);
        String lang = Paper.book().read("lang","ar");
        String sound_Path = getRingtonePath();
        if (sound_Path.isEmpty()){
            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            sound_Path = uri.toString();
        }

        String title ="";
        String body ="";
        if (lang.equals("ar")){
            title = map.get("title");
            body = map.get("message");
        }else {
            title = map.get("title_en");
            body = map.get("message_en");

        }



        String notification_type =map.get("noti_type");
        String order_id =map.get("order_id");

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        String CHANNEL_ID = "my_channel_02";
        CharSequence CHANNEL_NAME = "my_channel_name";
        int IMPORTANCE = NotificationManager.IMPORTANCE_HIGH;

        final NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, IMPORTANCE);

        channel.setShowBadge(true);
        channel.setSound(Uri.parse(sound_Path), new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                .setLegacyStreamType(AudioManager.STREAM_NOTIFICATION)
                .build()
        );
        builder.setChannelId(CHANNEL_ID);
        builder.setSound(Uri.parse(sound_Path), AudioManager.STREAM_NOTIFICATION);
        builder.setSmallIcon(R.mipmap.ic_launcher_round);

        builder.setContentTitle(title);
        builder.setContentText(body);
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(body));




        if (notification_type.equals("chat")){
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("order_id",Integer.parseInt(order_id));
            intent.putExtra("fromFirebase", true);

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this);
            taskStackBuilder.addNextIntent(intent);
            PendingIntent pendingIntent = taskStackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(pendingIntent);
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.image_avatar);
            builder.setLargeIcon(bitmap);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (manager != null) {

                manager.createNotificationChannel(channel);
                manager.notify(Tags.not_tag,Tags.not_id, builder.build());
                EventBus.getDefault().post(new NotFireModel(true,"chat"));
                Log.e("chat_not", "chat_not");

            }

        }else if (notification_type.equals("order")){
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("order_id",Integer.parseInt(order_id));
            intent.putExtra("fromFirebase", true);
            /*Intent intent = new Intent(this, SplashLoadingActivity.class);
            intent.putExtra("notification",true);*/
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this);
            taskStackBuilder.addNextIntent(intent);
            PendingIntent pendingIntent = taskStackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(pendingIntent);
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher_round);
            builder.setLargeIcon(bitmap);

            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (manager != null) {

                manager.createNotificationChannel(channel);
                manager.notify(Tags.not_tag,Tags.not_id, builder.build());
                EventBus.getDefault().post(new NotFireModel(true,"order"));


            }

        }else if (notification_type.equals("offer")){
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("order_id",Integer.parseInt(order_id));
            intent.putExtra("fromFirebase", true);

           /* Intent intent = new Intent(this, SplashLoadingActivity.class);
            intent.putExtra("notification",true);*/
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this);
            taskStackBuilder.addNextIntent(intent);
            PendingIntent pendingIntent = taskStackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(pendingIntent);

            Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher_round);
            builder.setLargeIcon(bitmap);

            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (manager != null) {

                manager.createNotificationChannel(channel);
                manager.notify(Tags.not_tag,Tags.not_id, builder.build());

                EventBus.getDefault().post(new NotFireModel(true,"offer"));

            }
        }else if (notification_type.equals("order_other")||notification_type.equals("admin")||notification_type.equals("cancel_order")){

            Intent intent = new Intent(this, SplashLoadingActivity.class);
            intent.putExtra("notification",true);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this);
            taskStackBuilder.addNextIntent(intent);
            PendingIntent pendingIntent = taskStackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(pendingIntent);
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher_round);
            builder.setLargeIcon(bitmap);

            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (manager != null) {

                manager.createNotificationChannel(channel);
                manager.notify(Tags.not_tag,Tags.not_id, builder.build());
                EventBus.getDefault().post(new NotFireModel(true,"order_other"));


            }

        }else if (notification_type.equals("rate_driver")){

            Intent intent = new Intent(this, SplashLoadingActivity.class);
            intent.putExtra("order",true);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this);
            taskStackBuilder.addNextIntent(intent);
            PendingIntent pendingIntent = taskStackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(pendingIntent);
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher_round);
            builder.setLargeIcon(bitmap);

            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (manager != null) {

                manager.createNotificationChannel(channel);
                manager.notify(Tags.not_tag,Tags.not_id, builder.build());
                EventBus.getDefault().post(new NotFireModel(true,"order_other"));


            }

        }






    }

    private void createOldNotificationVersion(Map<String, String> map) {
        Paper.init(this);
        String lang = Paper.book().read("lang","ar");

        String sound_Path = getRingtonePath();
        if (sound_Path.isEmpty()){
            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            sound_Path = uri.toString();
        }
        String title ="";
        String body ="";
        if (lang.equals("ar")){
            title = map.get("title");
            body = map.get("message");
        }else {
            title = map.get("title_en");
            body = map.get("message_en");

        }


        String notification_type =map.get("noti_type");
        String order_id =map.get("order_id");

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);


        builder.setSound(Uri.parse(sound_Path), AudioManager.STREAM_NOTIFICATION);
        builder.setSmallIcon(R.mipmap.ic_launcher_round);

        builder.setContentTitle(title);
        builder.setContentText(body);
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(body));


        if (notification_type.equals("chat")){
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("order_id",Integer.parseInt(order_id));
            intent.putExtra("fromFirebase", true);

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this);
            taskStackBuilder.addNextIntent(intent);
            PendingIntent pendingIntent = taskStackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(pendingIntent);
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.image_avatar);
            builder.setLargeIcon(bitmap);

            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (manager != null) {

                manager.notify(Tags.not_tag,Tags.not_id, builder.build());


            }


        }else if (notification_type.equals("order")){
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("order_id",Integer.parseInt(order_id));
            intent.putExtra("fromFirebase", true);

            /*Intent intent = new Intent(this, SplashLoadingActivity.class);
            intent.putExtra("notification",true);*/
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this);
            taskStackBuilder.addNextIntent(intent);
            PendingIntent pendingIntent = taskStackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(pendingIntent);

            Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher_round);
            builder.setLargeIcon(bitmap);

            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.notify(Tags.not_tag,Tags.not_id, builder.build());
                EventBus.getDefault().post(new NotFireModel(true,"order"));

            }
        }else if (notification_type.equals("offer")){
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("fromFirebase", true);

            intent.putExtra("order_id",Integer.parseInt(order_id));
           /* Intent intent = new Intent(this, SplashLoadingActivity.class);
            intent.putExtra("notification",true);*/
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this);
            taskStackBuilder.addNextIntent(intent);
            PendingIntent pendingIntent = taskStackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(pendingIntent);

            Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher_round);
            builder.setLargeIcon(bitmap);

            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.notify(Tags.not_tag,Tags.not_id, builder.build());
                EventBus.getDefault().post(new NotFireModel(true,"offer"));

            }
        }else if (notification_type.equals("order_other")||notification_type.equals("admin")||notification_type.equals("cancel_order")){

            Intent intent = new Intent(this, SplashLoadingActivity.class);
            intent.putExtra("notification",true);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this);
            taskStackBuilder.addNextIntent(intent);
            PendingIntent pendingIntent = taskStackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(pendingIntent);
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher_round);
            builder.setLargeIcon(bitmap);

            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (manager != null) {

                manager.notify(Tags.not_tag,Tags.not_id, builder.build());
                EventBus.getDefault().post(new NotFireModel(true,"order_other"));


            }

        }else if (notification_type.equals("rate_driver")){

            Intent intent = new Intent(this, SplashLoadingActivity.class);
            intent.putExtra("order",true);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this);
            taskStackBuilder.addNextIntent(intent);
            PendingIntent pendingIntent = taskStackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(pendingIntent);
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher_round);
            builder.setLargeIcon(bitmap);

            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (manager != null) {

                manager.notify(Tags.not_tag,Tags.not_id, builder.build());
                EventBus.getDefault().post(new NotFireModel(true,"order_other"));


            }

        }




    }

    private void updateTokenFireBase(String token) {

        UserModel userModel = getUserData();

        if (userModel!=null){
            try {
                Log.e("token",token);
                Api.getService(Tags.base_url)
                        .updatePhoneToken(userModel.getUser().getToken(),token,userModel.getUser().getId(),"android")
                        .enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    userModel.getUser().setFireBaseToken(token);
                                    preferences.create_update_userdata(FireBaseMessaging.this,userModel);

                                    Log.e("token", "updated successfully");
                                } else {
                                    try {

                                        Log.e("errorToken", response.code() + "_" + response.errorBody().string());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                try {

                                    if (t.getMessage() != null) {
                                        Log.e("errorToken2", t.getMessage());
                                        if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                            Toast.makeText(FireBaseMessaging.this, R.string.something, Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(FireBaseMessaging.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                } catch (Exception e) {
                                }
                            }
                        });
            } catch (Exception e) {


            }
        }
    }

    private UserModel getUserData(){
        return preferences.getUserData(this);
    }

    private String getSession()
    {
        return preferences.getSession(this);
    }

    private String getChatUserId()
    {
        return preferences.getChat_User_Id(this);
    }

    private String getRingtonePath()
    {
        return preferences.getAppSetting(this).getRingToneUri();
    }

}
