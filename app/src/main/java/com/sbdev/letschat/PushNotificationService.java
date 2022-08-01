package com.sbdev.letschat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationManagerCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.concurrent.ExecutionException;

public class PushNotificationService extends FirebaseMessagingService {
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title=remoteMessage.getNotification().getTitle();
        String body=remoteMessage.getNotification().getBody();
        String imageUrl=remoteMessage.getData().get("imageUrl");
        Bitmap bitmap = null;

        try {
            bitmap=Glide
                .with(getApplicationContext())
                .asBitmap()
                .load(imageUrl)
                .submit()
                .get();
        } catch (ExecutionException | InterruptedException e) {
            Log.e("Exception",e.getMessage());
        }

        final String CHANNEL_ID="HEADS_UP_NOTIFICATION";

        NotificationChannel channel=new NotificationChannel(CHANNEL_ID,"Heads Up Notification", NotificationManager.IMPORTANCE_HIGH);

        getSystemService(NotificationManager.class).createNotificationChannel(channel);

        Notification.Builder notification=new Notification.Builder(this,CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setLargeIcon(bitmap)
                .setStyle(new Notification.BigPictureStyle().bigPicture(bitmap))
                .setSmallIcon(R.drawable.chat_box1)
                .setAutoCancel(true);

        NotificationManagerCompat.from(this).notify(1,notification.build());

    }
}