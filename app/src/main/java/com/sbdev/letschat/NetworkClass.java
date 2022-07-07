package com.sbdev.letschat;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class NetworkClass extends Application {

    public static DatabaseReference connectedRef;

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");

    }

}
