package com.sbdev.letschat;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.viewpager2.widget.ViewPager2;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
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
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.pranavpandey.android.dynamic.toasts.DynamicToast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    ImageView more,weather;

    FirebaseAuth firebaseAuth;

    DatabaseReference reference;

    StorageReference storageReference;

    Handler handler;

    TabLayout tabLayout;

    ViewPager2 viewPager2;

    ChatStateAdapter adapter;

    String url="https://api.weatherapi.com/v1/current.json?key=ceea495be7374dc6a39174422222906%20&q=India&aqi=no";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        more=findViewById(R.id.chatMore);
        weather=findViewById(R.id.chatWeather);
        tabLayout=findViewById(R.id.chatTabLayout);
        viewPager2=findViewById(R.id.viewPager2);

        firebaseAuth=FirebaseAuth.getInstance();

        adapter=new ChatStateAdapter(getSupportFragmentManager(),getLifecycle());
        viewPager2.setAdapter(adapter);

        tabLayout.addTab(tabLayout.newTab().setText("Chats"));
        tabLayout.addTab(tabLayout.newTab().setText("Users"));

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
                                Toast.makeText(ChatActivity.this, "Profile", Toast.LENGTH_SHORT).show();
                                break;
                            case R.id.favMsg:
                                Toast.makeText(ChatActivity.this, "Fav Msg", Toast.LENGTH_SHORT).show();
                                break;
                            case R.id.settings:
                                Toast.makeText(ChatActivity.this, "Settings", Toast.LENGTH_SHORT).show();
                                break;
                            case R.id.logout:
                                firebaseAuth.signOut();
                                startActivity(new Intent(ChatActivity.this,MainActivity.class));
                                finishAffinity();
                                break;
                        }

                        return false;

                    }
                });

                popupMenu.setForceShowIcon(true);
                popupMenu.show();

            }
        });

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

                        changeWeather(text,isDay,temp);


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


    public void changeWeather(String textCondition, int isDay, int temp)
    {

        if(isDay==0)
        {

            textCondition=textCondition.toLowerCase();
            if(textCondition.contains("fog") || textCondition.contains("mist"))
            {
                weather.setImageResource(R.drawable.mist);
            }
            else if(textCondition.contains("clear") || textCondition.contains("cloudy"))
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
                    weather.setImageResource(R.drawable.heavy_rain_night);
                }
            }
            else if(textCondition.contains("drizzle"))
            {
                weather.setImageResource(R.drawable.drizzle_night);
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

                firebaseAuth.signOut();
                startActivity(new Intent(ChatActivity.this,MainActivity.class));
                finishAffinity();

            }
        });

        builder.show();

    }
}