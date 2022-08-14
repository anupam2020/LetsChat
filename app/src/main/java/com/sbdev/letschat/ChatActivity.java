package com.sbdev.letschat;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleObserver;
import androidx.viewpager2.widget.ViewPager2;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.pranavpandey.android.dynamic.toasts.DynamicToast;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class ChatActivity extends AppCompatActivity implements LifecycleObserver {

    ImageView more,weather;

    FirebaseAuth firebaseAuth;

    DatabaseReference reference,usersRef,locationRef,chatsRef;

    ChatStateAdapter adapter;

    String city="";

    RelativeLayout layout;

    public static NetworkClass.FirebaseListener fragmentListener = null ;

    int flag=0;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        if(getIntent().getExtras() !=null){
            if(getIntent().getStringExtra("myUID")!=null || getIntent().getStringExtra("myToken")!=null){
                Intent intent=new Intent(ChatActivity.this,MessageActivity.class);
                intent.putExtra("myUID",getIntent().getStringExtra("myUID"));
                intent.putExtra("myToken",getIntent().getStringExtra("myToken"));
                intent.putExtra("myName",getIntent().getStringExtra("myName"));
                intent.putExtra("myPic",getIntent().getStringExtra("myPic"));
                startActivity(intent);
            }
        }

        more=findViewById(R.id.chatMore);
        weather=findViewById(R.id.chatWeather);
        final TabLayout tabLayout=findViewById(R.id.chatTabLayout);
        final ViewPager2 viewPager2=findViewById(R.id.viewPager2);
        layout=findViewById(R.id.relativeChats);

        firebaseAuth=FirebaseAuth.getInstance();

        progressDialog=new ProgressDialog(this);

        reference=FirebaseDatabase.getInstance().getReference("Users");
        reference.keepSynced(true);
        usersRef= FirebaseDatabase.getInstance().getReference("Users").child(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid());
        usersRef.keepSynced(true);
        locationRef=FirebaseDatabase.getInstance().getReference("Location");
        locationRef.keepSynced(true);
        chatsRef=FirebaseDatabase.getInstance().getReference("Chats");
        chatsRef.keepSynced(true);

        //connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");

        if(firebaseAuth.getCurrentUser()!=null)
        {
            reference.child(firebaseAuth.getCurrentUser().getUid()).child("isLoggedIn").setValue("true");
        }

        if(!isNetworkConnected())
        {
            Snackbar.make(layout,"Your device is offline!",Snackbar.LENGTH_SHORT).show();
        }

        weather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ChatActivity.this,WeatherActivity.class));
            }
        });

        getCity();

        adapter=new ChatStateAdapter(getSupportFragmentManager(),getLifecycle());
        viewPager2.setAdapter(adapter);

        tabLayout.addTab(tabLayout.newTab().setText("Chats"));
        tabLayout.addTab(tabLayout.newTab().setText("Status"));
        tabLayout.addTab(tabLayout.newTab().setText("Users"));

        NetworkClass.myChats=new NetworkClass.FirebaseListener() {
            @Override
            public void onChatDataChange(DataSnapshot snapshot) {
                if (fragmentListener != null) {
                    fragmentListener.onChatDataChange(snapshot);
                }

            }
            @Override
            public void onChatListDataChange(DataSnapshot snapshot) {
                if(fragmentListener != null)
                {
                    fragmentListener.onChatListDataChange(snapshot);
                }
            }

            @Override
            public void onStatusDataChange(DataSnapshot snapshot) {
                if(fragmentListener != null)
                {
                    fragmentListener.onStatusDataChange(snapshot);
                }
            }

            @Override
            public void onUserDataChange(DataSnapshot snapshot) {

                if(fragmentListener != null)
                {
                    fragmentListener.onUserDataChange(snapshot);
                }

            }

            @Override
            public void onFavChatsChange(DataSnapshot snapshot) {

                if(fragmentListener != null)
                {
                    fragmentListener.onFavChatsChange(snapshot);
                }
            }

            @Override
            public void onWallpaperChange(DataSnapshot snapshot) {

            }
        };

//        chatsRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//
//                if(snapshot.exists())
//                {
//
//                    int unreadMsg=0;
//
//                    for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
//
//                        MessageModel messageModel=dataSnapshot.getValue(MessageModel.class);
//                        assert messageModel != null;
//                        if(messageModel.getSender()!=null)
//                        {
//                            if(messageModel.getReceiver().equals(firebaseAuth.getCurrentUser().getUid()) && messageModel.getIsSeen()==0)
//                            {
//                                unreadMsg++;
//                            }
//                        }
////                        else
////                        {
////                            chatsRef.child(Objects.requireNonNull(dataSnapshot.getKey())).removeValue();
////                        }
//
//                    }
//
//                    DatabaseReference chatsListRef=FirebaseDatabase.getInstance().getReference("ChatsList");
//
//                    int finalUnreadMsg = unreadMsg;
//                    chatsListRef.child(firebaseAuth.getCurrentUser().getUid())
//                            .addValueEventListener(new ValueEventListener() {
//                                @Override
//                                public void onDataChange(@NonNull DataSnapshot snapshot) {
//
//                                    if(snapshot.exists()){
//
//                                        if(finalUnreadMsg ==0) {
//                                            tabLayout.getTabAt(0).setText("Chats");
//                                        }
//                                        else {
//                                            tabLayout.getTabAt(0).setText("Chats ("+ finalUnreadMsg +")");
//                                        }
//
//                                    }
//                                    else{
//
//                                        if(finalUnreadMsg ==0) {
//                                            tabLayout.getTabAt(2).setText("Users");
//                                        }
//                                        else {
//                                            tabLayout.getTabAt(2).setText("Users ("+ finalUnreadMsg +")");
//                                        }
//
//                                    }
//
//                                }
//
//                                @Override
//                                public void onCancelled(@NonNull DatabaseError error) {
//
//                                }
//                            });
//
//                }
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });

        tabLayout.setTabTextColors(ColorStateList.valueOf(Color.WHITE));

        tabLayout.setTabRippleColor(ColorStateList.valueOf(Color.WHITE));

        tabLayout.setSelectedTabIndicatorColor(Color.WHITE);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager2.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                tabLayout.selectTab(tabLayout.getTabAt(position));
            }
        });



        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String s) {

                Log.d("Chat Token",s);

                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        for(DataSnapshot dataSnapshot : snapshot.getChildren())
                        {

                            UserModel userModel=dataSnapshot.getValue(UserModel.class);
                            assert userModel != null;
                            if(firebaseAuth.getCurrentUser()!=null && userModel.getUID()!=null && userModel.getToken()!=null)
                            {
                                if(!userModel.getUID().equals(firebaseAuth.getCurrentUser().getUid()) && userModel.getToken().equals(s))
                                {
                                    flag=1;

                                    if(!ChatActivity.this.isFinishing())
                                    {
                                        //show dialog
                                        progressDialog.show();
                                        progressDialog.setContentView(R.layout.multiple_device_detect);
                                        progressDialog.setCancelable(false);
                                        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                                    }
                                    reference.child(firebaseAuth.getCurrentUser().getUid())
                                            .child("isLoggedIn")
                                            .setValue("false").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()) {

                                                        new Handler().postDelayed(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                logoutUserAndQuitApp();
                                                            }
                                                        },1500);
                                                    }
                                                }
                                            });
                                }
                            }

                        }

//                        if(flag==1)
//                        {
//                            DynamicToast.make(ChatActivity.this, "One device multiple users detected", getResources().getDrawable(R.drawable.warning),
//                                    getResources().getColor(R.color.white), getResources().getColor(R.color.black), 3000).show();
//                        }
                        if(flag==0)
                        {
                            if(firebaseAuth.getCurrentUser()!=null)
                            {
                                reference.child(firebaseAuth.getCurrentUser().getUid()).child("token").setValue(s);
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });

        NetworkClass.connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected){
                    usersRef.child("status").setValue("Online");
                    //usersRef.child("status").onDisconnect().setValue("Offline");

                    usersRef.child("status").onDisconnect().setValue("Offline").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful())
                            {
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

                                        usersRef.child("status").onDisconnect().setValue(strDateTime);

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }

                        }
                    });
                    getCity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                DynamicToast.make(ChatActivity.this, error.getMessage(), getResources().getDrawable(R.drawable.warning),
                        getResources().getColor(R.color.white), getResources().getColor(R.color.black), 3000).show();
            }
        });


        more.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.Q)
            @Override
            public void onClick(View v) {

                PopupMenu popupMenu=new PopupMenu(ChatActivity.this,v);
                popupMenu.getMenuInflater().inflate(R.menu.menu,popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        switch (item.getItemId())
                        {

                            case R.id.profile:
                                startActivity(new Intent(ChatActivity.this,ProfileActivity.class));
                                break;
                            case R.id.favMsgMenu:
                                startActivity(new Intent(ChatActivity.this,FavMsgActivity.class));
                                break;
                            case R.id.activities:
                                startActivity(new Intent(ChatActivity.this,Activities.class));
                                break;
                            case R.id.more:
                                startActivity(new Intent(ChatActivity.this,MoreActivity.class));
                                break;
                            case R.id.logout:

                                if(!isNetworkConnected()) {
                                    Snackbar.make(layout,"Your device is offline!",Snackbar.LENGTH_SHORT).show();
                                }
                                else {

                                    reference.child(firebaseAuth.getCurrentUser().getUid())
                                        .child("isLoggedIn")
                                        .setValue("false").addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()) {
                                                    logoutUser();
                                                }
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                DynamicToast.make(ChatActivity.this, e.getMessage(), getResources().getDrawable(R.drawable.warning),
                                                        getResources().getColor(R.color.white), getResources().getColor(R.color.black), 3000).show();
                                            }
                                        });

                                }
                                break;
                        }

                        return false;

                    }
                });

                popupMenu.show();

            }
        });

    }

    public void getCity()
    {

        if(firebaseAuth.getCurrentUser()!=null)
        {
            locationRef.child(firebaseAuth.getCurrentUser().getUid())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if(snapshot.exists())
                            {
                                LocationModel locationModel=snapshot.getValue(LocationModel.class);
                                assert locationModel != null;
                                city=locationModel.getLocation();
                                updateWeather(city);
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            DynamicToast.make(ChatActivity.this, error.getMessage(), getResources().getDrawable(R.drawable.warning),
                                    getResources().getColor(R.color.white), getResources().getColor(R.color.black), 3000).show();
                        }
                    });
        }


    }


    public void updateWeather(String city)
    {

        String url="https://api.weatherapi.com/v1/current.json?key=ceea495be7374dc6a39174422222906%20&q="+city+"&aqi=no";

        StringRequest request=new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                if(response!=null)
                {
                    try {

                        JSONObject jsonObject=new JSONObject(response);

                        JSONObject current=jsonObject.getJSONObject("current");
                        int isDay=current.getInt("is_day");
                        int temp= (int) current.getDouble("temp_c");

                        JSONObject condition=current.getJSONObject("condition");
                        String text=condition.getString("text");

                        changeWeather(isDay,text,temp);

                    } catch (JSONException e) {
                        //Toast.makeText(ChatActivity.this, "Catch: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(ChatActivity.this,"Error: "+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        RequestQueue queue= Volley.newRequestQueue(this);
        queue.add(request);

    }


    public void changeWeather(int isDay,String textCondition,int temp)
    {

        if(isDay==0)
        {

            textCondition=textCondition.toLowerCase();
            if(textCondition.contains("fog") || textCondition.contains("mist"))
            {
                weather.setImageResource(R.drawable.mist);
            }
            else if(textCondition.contains("clear"))
            {
                if(temp<=20)
                {
                    weather.setImageResource(R.drawable.mist);
                }
                else
                {
                    weather.setImageResource(R.drawable.moon_clear);
                }
            }
            else if(textCondition.contains("rain"))
            {
                if(textCondition.contains("light") || textCondition.contains("patchy"))
                {
                    weather.setImageResource(R.drawable.light_rain_night);
                }
                else if(textCondition.contains("moderate"))
                {
                    weather.setImageResource(R.drawable.moderate_rain_night);
                }
                else
                {
                    if(textCondition.contains("heavy"))
                    {
                        weather.setImageResource(R.drawable.heavy_rain_night);
                    }
                }
            }
            else if(textCondition.contains("drizzle"))
            {
                weather.setImageResource(R.drawable.drizzle_night);
            }
            else if(textCondition.contains("cloudy"))
            {
                if(temp<=20)
                {
                    weather.setImageResource(R.drawable.mist);
                }
                else
                {
                    weather.setImageResource(R.drawable.moon_clear);
                }
            }
            else
            {
                weather.setImageResource(R.drawable.moon_clear);
            }

        }
        else
        {

            textCondition=textCondition.toLowerCase();
            if(textCondition.contains("cloudy"))
            {
                if(temp<=25)
                {
                    weather.setImageResource(R.drawable.mist);
                }
                else
                {
                    weather.setImageResource(R.drawable.cloudy);
                }
            }
            if(textCondition.contains("sunny"))
            {
                if(temp<=20)
                {
                    weather.setImageResource(R.drawable.mist);
                }
                else
                {
                    weather.setImageResource(R.drawable.clear_sky);
                }
            }
            else if(textCondition.contains("mist") || textCondition.contains("fog"))
            {
                weather.setImageResource(R.drawable.mist);
            }
            else if(textCondition.contains("rain"))
            {
                if(textCondition.contains("light") || textCondition.contains("patchy"))
                {
                    weather.setImageResource(R.drawable.light_rain);
                }
                else if(textCondition.contains("moderate"))
                {
                    weather.setImageResource(R.drawable.moderate_rain);
                }
                else
                {
                    if(textCondition.contains("heavy"))
                    {
                        weather.setImageResource(R.drawable.heavy_rain);
                    }
                }
            }
            else if(textCondition.contains("drizzle"))
            {
                weather.setImageResource(R.drawable.morning_drizzle);
            }
            else
            {
                weather.setImageResource(R.drawable.cloudy);
            }

        }

    }

    @Override
    public void onBackPressed() {

        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Warning");
        builder.setMessage("Please select an option!");

        builder.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finishAffinity();
            }
        }).setNegativeButton("Logout", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(!isNetworkConnected()) {
                    Snackbar.make(layout,"Your device is offline!",Snackbar.LENGTH_SHORT).show();
                }
                else{
                    if(firebaseAuth.getCurrentUser()!=null)
                    {
                        reference.child(firebaseAuth.getCurrentUser().getUid())
                            .child("isLoggedIn")
                            .setValue("false").addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()) {
                                        logoutUser();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    DynamicToast.make(ChatActivity.this, e.getMessage(), getResources().getDrawable(R.drawable.warning),
                                            getResources().getColor(R.color.white), getResources().getColor(R.color.black), 3000).show();
                                }
                            });
                    }
                }
            }
        });

        builder.show();

    }

    public void logoutUser()
    {

        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");

        HashMap map=new HashMap();
        map.put("status","Offline");

        if(firebaseAuth.getCurrentUser()!=null)
        {
            reference.child(firebaseAuth.getCurrentUser().getUid())
                .updateChildren(map).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {

                        if(task.isSuccessful())
                        {
                            firebaseAuth.signOut();
                            startActivity(new Intent(ChatActivity.this,MainActivity.class));
                            finishAffinity();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        DynamicToast.make(ChatActivity.this, e.getMessage(), getResources().getDrawable(R.drawable.warning),
                                getResources().getColor(R.color.white), getResources().getColor(R.color.black), 3000).show();
                    }
                });
        }

    }

    public void logoutUserAndQuitApp()
    {

        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");

        HashMap map=new HashMap();
        map.put("status","Offline");

        if(firebaseAuth.getCurrentUser()!=null)
        {
            reference.child(firebaseAuth.getCurrentUser().getUid())
                    .updateChildren(map).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {

                            if(task.isSuccessful())
                            {
                                progressDialog.dismiss();
                                firebaseAuth.signOut();
                                finishAffinity();
                            }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            DynamicToast.make(ChatActivity.this, e.getMessage(), getResources().getDrawable(R.drawable.warning),
                                    getResources().getColor(R.color.white), getResources().getColor(R.color.black), 3000).show();
                        }
                    });
        }

    }

    private boolean isNetworkConnected()
    {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    public void checkStatus(String status)
    {

        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");

        HashMap map=new HashMap();
        if(status.equals("Offline"))
        {

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
                    DynamicToast.make(ChatActivity.this, error.getMessage(), getResources().getDrawable(R.drawable.warning),
                            getResources().getColor(R.color.white), getResources().getColor(R.color.black), 3000).show();
                }
            });

        }
        else
        {
            map.put("status",status);
            if(firebaseAuth.getCurrentUser()!=null)
            {
                reference.child(firebaseAuth.getCurrentUser().getUid())
                        .updateChildren(map);
            }
        }


    }

    @Override
    protected void onPause() {
        super.onPause();
        checkStatus("Offline");
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkStatus("Online");
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkStatus("Online");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        checkStatus("Online");
    }
}