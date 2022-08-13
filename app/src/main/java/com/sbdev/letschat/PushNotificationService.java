package com.sbdev.letschat;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.text.Html;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class PushNotificationService extends FirebaseMessagingService {

    FirebaseAuth firebaseAuth;
    DatabaseReference reference;

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
        String uid=remoteMessage.getData().get("myUID");
        String token=remoteMessage.getData().get("myToken");
        String name=remoteMessage.getData().get("myName");
        String pic=remoteMessage.getData().get("myPic");
        String image=remoteMessage.getData().get("image");

        Bitmap bitmap=null;
        try {
            bitmap= Glide.with(getApplicationContext())
                    .asBitmap()
                    .load(image)
                    .submit()
                    .get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        Intent myIntent = new Intent(this, MessageActivity.class);
        myIntent.putExtra("myUID",uid);
        myIntent.putExtra("myToken",token);
        myIntent.putExtra("myName",name);
        myIntent.putExtra("myPic",pic);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                myIntent,0);

        final String CHANNEL_ID="HEADS_UP_NOTIFICATION";

        NotificationChannel channel=new NotificationChannel(CHANNEL_ID,"Heads Up Notification", NotificationManager.IMPORTANCE_HIGH);

        getSystemService(NotificationManager.class).createNotificationChannel(channel);

        Notification.Builder notification;
        if(image==null || image.length()==0)
        {
            notification=new Notification.Builder(this,CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.drawable.chat_box1)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        }
        else
        {
            notification=new Notification.Builder(this,CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setLargeIcon(bitmap)
                .setStyle(new Notification.BigPictureStyle().bigPicture(bitmap))
                .setSmallIcon(R.drawable.chat_box1)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        }

        NotificationManagerCompat.from(this).notify(1,notification.build());

    }

}