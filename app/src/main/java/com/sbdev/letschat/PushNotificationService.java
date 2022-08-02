package com.sbdev.letschat;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Objects;

public class PushNotificationService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title= Objects.requireNonNull(remoteMessage.getNotification()).getTitle();
        String body=remoteMessage.getNotification().getBody();
        String icon=remoteMessage.getNotification().getIcon();

        final String CHANNEL_ID="HEADS_UP_NOTIFICATION";

        NotificationChannel channel=new NotificationChannel(CHANNEL_ID,"Heads Up Notification", NotificationManager.IMPORTANCE_HIGH);

        getSystemService(NotificationManager.class).createNotificationChannel(channel);

        Notification.Builder notification;
        if(icon==null || icon.length()==0)
        {
            notification=new Notification.Builder(this,CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.drawable.chat_box1)
                .setAutoCancel(true);
        }
        else
        {
            notification=new Notification.Builder(this,CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                    .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                            R.drawable.image_sent))
                .setSmallIcon(R.drawable.chat_box1)
                .setAutoCancel(true);
        }

        NotificationManagerCompat.from(this).notify(1,notification.build());

    }

}