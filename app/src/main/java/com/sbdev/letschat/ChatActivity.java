package com.sbdev.letschat;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
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
import com.google.firebase.database.OnDisconnect;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.pranavpandey.android.dynamic.toasts.DynamicToast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "Connection";
    ImageView more,weather;

    FirebaseAuth firebaseAuth;

    DatabaseReference reference,usersRef;

    TabLayout tabLayout;

    ViewPager2 viewPager2;

    ChatStateAdapter adapter;

    String url="https://api.weatherapi.com/v1/current.json?key=ceea495be7374dc6a39174422222906%20&q=India&aqi=no";

    RelativeLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        more=findViewById(R.id.chatMore);
        weather=findViewById(R.id.chatWeather);
        tabLayout=findViewById(R.id.chatTabLayout);
        viewPager2=findViewById(R.id.viewPager2);
        layout=findViewById(R.id.relativeChats);

        firebaseAuth=FirebaseAuth.getInstance();

        reference=FirebaseDatabase.getInstance().getReference("Users");
        reference.keepSynced(true);
        usersRef= FirebaseDatabase.getInstance().getReference("Users").child(firebaseAuth.getCurrentUser().getUid());
        usersRef.keepSynced(true);

        //connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");

        updateWeather();

        adapter=new ChatStateAdapter(getSupportFragmentManager(),getLifecycle());
        viewPager2.setAdapter(adapter);

        tabLayout.addTab(tabLayout.newTab().setText("Chats"));
        tabLayout.addTab(tabLayout.newTab().setText("Users"));

        checkRealTimeNetwork();

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

        NetworkClass.connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected){
                    updateWeather();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                DynamicToast.make(ChatActivity.this,error.getMessage(),3000).show();
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
                            case R.id.favMsg:
                                Toast.makeText(ChatActivity.this, "Fav Msg", Toast.LENGTH_SHORT).show();
                                break;
                            case R.id.settings:
                                Toast.makeText(ChatActivity.this, "Settings", Toast.LENGTH_SHORT).show();
                                break;
                            case R.id.logout:
                                logoutUser();
                                break;
                        }

                        return false;

                    }
                });

                popupMenu.show();

            }
        });

    }

    private void checkRealTimeNetwork() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                NetworkClass.connectedRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean connected = snapshot.getValue(Boolean.class);
                        if (connected) {
                            updateWeather();
                        }
                        else {
                            Snackbar.make(layout,"Your device is offline!",Snackbar.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        DynamicToast.make(ChatActivity.this, error.getMessage(), 3000).show();
                    }
                });

            }
        }, 2000);

    }


    public void updateWeather()
    {

        StringRequest request=new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                if(response!=null)
                {
                    try {

                        JSONObject jsonObject=new JSONObject(response);

                        JSONObject current=jsonObject.getJSONObject("current");
                        int isDay=current.getInt("is_day");

                        changeWeather(isDay);


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


    public void changeWeather(int isDay)
    {

        if(isDay==0)
        {
            weather.setImageResource(R.drawable.moon_clear);
        }
        else
        {
            weather.setImageResource(R.drawable.cloudy);
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
                logoutUser();
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
                        DynamicToast.make(ChatActivity.this, e.getMessage(), 3000).show();
                    }
                });
        }

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
                    DynamicToast.make(ChatActivity.this, error.getMessage(), 3000).show();
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
    protected void onResume() {
        super.onResume();
        checkStatus("Online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        checkStatus("Offline");
    }

}