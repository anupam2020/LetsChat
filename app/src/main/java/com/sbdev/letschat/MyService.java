package com.sbdev.letschat;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pranavpandey.android.dynamic.toasts.DynamicToast;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class MyService extends Service {

    FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();

    public static String SET_OFFLINE="Offline";
    public static String SET_ONLINE="Online";



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {



        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(intent.getAction().equals(SET_OFFLINE)){

            Log.d("onStartCommand",intent.getStringExtra("sample_str"));

        }else if(intent.getAction().equals(SET_ONLINE)){



        }


        return super.onStartCommand(intent, flags, startId);
    }

    public MyService getServices() {
        return this;
    }
    public void checkStatus(String status , String friendUID)
    {

        DatabaseReference friendDBRef=FirebaseDatabase.getInstance().getReference("Friend");
        friendDBRef.keepSynced(true);

        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");

        HashMap map=new HashMap();
        if(status.equals("Offline"))
        {

            friendDBRef.child(firebaseAuth.getCurrentUser().getUid()).removeValue();

            DatabaseReference serverTimeRef = FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset");
            serverTimeRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    long offset = snapshot.getValue(Long.class);
                    long estimatedServerTimeMs = System.currentTimeMillis() + offset;

                    Timestamp timestamp=new Timestamp(estimatedServerTimeMs);
                    Date date=timestamp;
                    SimpleDateFormat simpleDateFormat=new SimpleDateFormat("MMM dd, hh:mm a");
                    String strDateTime=simpleDateFormat.format(date);

                    map.put("status",strDateTime);
                    if(firebaseAuth.getCurrentUser()!=null)
                    {
                        reference.child(firebaseAuth.getCurrentUser().getUid())
                                .updateChildren(map);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }
        else
        {
            friendDBRef.child(firebaseAuth.getCurrentUser().getUid()).child("friendUID").setValue(friendUID);

            map.put("status",status);
            if(firebaseAuth.getCurrentUser()!=null)
            {
                reference.child(firebaseAuth.getCurrentUser().getUid())
                        .updateChildren(map);
            }
        }


    }

//    private void setFirebaseOFFLINE()
//    {
//
//        NetworkClass.connectedRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                boolean connected = snapshot.getValue(Boolean.class);
//                if (connected){
//                    usersRef.child("status").setValue("Online");
//                    //usersRef.child("status").onDisconnect().setValue("Offline");
//
//                    usersRef.child("status").onDisconnect().setValue("Offline").addOnCompleteListener(new OnCompleteListener<Void>() {
//                        @Override
//                        public void onComplete(@NonNull Task<Void> task) {
//
//                            if(task.isSuccessful())
//                            {
//                                DatabaseReference serverTimeRef = FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset");
//                                serverTimeRef.addValueEventListener(new ValueEventListener() {
//                                    @Override
//                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//
//                                        long offset = snapshot.getValue(Long.class);
//                                        long estimatedServerTimeMs = System.currentTimeMillis() + offset;
//
//                                        Timestamp timestamp=new Timestamp(estimatedServerTimeMs);
//                                        Date date=timestamp;
//                                        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("MMM dd, hh:mm a");
//                                        String strDateTime=simpleDateFormat.format(date);
//
//                                        usersRef.child("status").onDisconnect().setValue(strDateTime);
//
//                                    }
//
//                                    @Override
//                                    public void onCancelled(@NonNull DatabaseError error) {
//
//                                    }
//                                });
//                            }
//
//                        }
//                    });
//
//                    chatsList();
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                DynamicToast.make(getActivity(), error.getMessage(), getResources().getDrawable(R.drawable.warning),
//                        getResources().getColor(R.color.white), getResources().getColor(R.color.black), 3000).show();
//            }
//        });
//
//    }

}
