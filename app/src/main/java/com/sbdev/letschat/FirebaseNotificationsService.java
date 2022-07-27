package com.sbdev.letschat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class FirebaseNotificationsService extends FirebaseMessagingService {

    private UserModel userModel=new UserModel();

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {

        if(message.getData().size()>0)
        {
            Map<String,String> map=new HashMap<>();
            String title=map.get("title");
            String text=map.get("text");
            String receiver=map.get("receiver");
            String receiverPic=map.get("receiverPic");
            String key=map.get("key");

            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
            {
                createOreoNotification(title,text,receiver,receiverPic,key);
            }
            else
            {
                createNormalNotification(title,text,receiver,receiverPic,key);
            }

        }

        super.onMessageReceived(message);
    }

    @Override
    public void onNewToken(@NonNull String token) {
        updateToken(token);
        super.onNewToken(token);
    }

    private void updateToken(String token){

        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users").child(userModel.getUID());

        HashMap map=new HashMap();
        map.put("token",token);

        reference.updateChildren(map);

    }

    public void createNormalNotification(String title, String text, String receiver, String receiverPic, String key)
    {

        Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder=new NotificationCompat.Builder(this,"1000");
        builder.setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.chat_box1)
                .setSound(uri);

        Intent intent=new Intent();
        intent.putExtra("key",key);
        intent.putExtra("receiver",receiver);
        intent.putExtra("receiverPic",receiverPic);

        PendingIntent pendingIntent=PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT);

        builder.setContentIntent(pendingIntent);

        NotificationManager notificationManager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(new Random().nextInt(85-65),builder.build());

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void createOreoNotification(String title, String text, String receiver, String receiverPic, String key)
    {

        NotificationChannel notificationChannel=new NotificationChannel("1000","Message", NotificationManager.IMPORTANCE_HIGH);

        notificationChannel.setShowBadge(true);
        notificationChannel.enableLights(true);
        notificationChannel.enableVibration(true);
        notificationChannel.setDescription("Message Description");
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        NotificationManager notificationManager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.createNotificationChannel(notificationChannel);

        Intent intent=new Intent();
        intent.putExtra("receiver",receiver);
        intent.putExtra("receiverPic",receiverPic);
        intent.putExtra("key",key);
        PendingIntent pendingIntent=PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT);

        Notification notification=new Notification.Builder(this,"1000")
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.chat_box1)
                .build();

        notificationManager.notify(new Random().nextInt(85-65),notification);

    }

}
