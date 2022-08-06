package com.sbdev.letschat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.res.ResourcesCompat;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pranavpandey.android.dynamic.toasts.DynamicToast;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class WeatherActivity extends AppCompatActivity {

    ImageView day_night_mode,weather;

    TextInputLayout textInputLayout;

    TextInputEditText city;

    TextView cityText,country,pressure,wind,humidity,temp,temp_type,presText,humText,windText;

    RelativeLayout layout;

    AppCompatButton done;

    FirebaseAuth firebaseAuth;

    DatabaseReference usersRef,locationRef;

    ProgressDialog progressDialog;

    String url="",dbCity="";

    RelativeLayout relativeLayout;

    private SharedPreferences sp;

    private String mode;

    private String SHARED_PREFS="SHARED_PREFS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        textInputLayout=findViewById(R.id.allWeatherLayout);
        city=findViewById(R.id.allWeatherEditText);
        layout=findViewById(R.id.weatherRelative);
        done=findViewById(R.id.allWeatherButton);
        day_night_mode=findViewById(R.id.allWeatherMode);
        cityText=findViewById(R.id.allWeatherCityText);
        country=findViewById(R.id.allWeatherCountryText);
        pressure=findViewById(R.id.allWeatherPressureValue);
        wind=findViewById(R.id.allWeatherWindValue);
        humidity=findViewById(R.id.allWeatherHumidityValue);
        temp=findViewById(R.id.allWeatherTempText);
        temp_type=findViewById(R.id.allWeatherSkyTypeText);
        presText=findViewById(R.id.allWeatherPressureText);
        humText=findViewById(R.id.allWeatherHumidityText);
        windText=findViewById(R.id.allWeatherWindText);
        weather=findViewById(R.id.allWeatherWeatherImg);
        relativeLayout=findViewById(R.id.allWeatherSwipeRelative);

        relativeLayout.setVisibility(View.GONE);

        textInputLayout.setBoxBackgroundColorResource(R.color.white);

        progressDialog=new ProgressDialog(this);

        firebaseAuth=FirebaseAuth.getInstance();

        usersRef= FirebaseDatabase.getInstance().getReference("Users").child(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid());
        usersRef.keepSynced(true);

        locationRef= FirebaseDatabase.getInstance().getReference("Location");
        locationRef.keepSynced(true);

        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog_dots);
        progressDialog.setCancelable(true);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        if(!isNetworkConnected())
        {
            progressDialog.dismiss();
            Snackbar.make(layout,"Your device is offline!",Snackbar.LENGTH_SHORT).show();
        }

        sp=getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);

        mode=sp.getString("bgMode","1");
        if(mode.equals("0"))
        {
            day_night_mode.setImageResource(R.drawable.night_mode);
            nightMode();
        }
        else
        {
            day_night_mode.setImageResource(R.drawable.day_mode);
            dayMode();
        }


        locationRef.child(firebaseAuth.getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists())
                {
                    LocationModel locationModel=snapshot.getValue(LocationModel.class);
                    assert locationModel != null;
                    city.setText(locationModel.getLocation());
                    updateLocation(city.getText().toString().trim());
                    dbCity=city.getText().toString().trim();
                }
                progressDialog.dismiss();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
            }
        });

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                InputMethodManager imm = (InputMethodManager) WeatherActivity.this.getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                progressDialog.show();
                progressDialog.setContentView(R.layout.progress_dialog_dots);
                progressDialog.setCancelable(false);
                progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

                if(!isNetworkConnected())
                {
                    progressDialog.dismiss();
                    Snackbar.make(layout,"Your device is offline!",Snackbar.LENGTH_SHORT).show();
                }
                else
                {

                    String c=city.getText().toString().trim();

                    if(c.isEmpty())
                    {
                        progressDialog.dismiss();
                        textInputLayout.setError("City cannot be empty!");
                    }
                    else
                    {
                        updateLocation(c);
                    }

                }

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

                    if(!dbCity.isEmpty()){
                        updateLocation(dbCity);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
                DynamicToast.make(WeatherActivity.this, error.getMessage(), getResources().getDrawable(R.drawable.warning),
                        getResources().getColor(R.color.white), getResources().getColor(R.color.black), 3000).show();
            }
        });

    }

    public void dayMode()
    {

        cityText.setTextColor(Color.BLACK);
        country.setTextColor(Color.DKGRAY);
        temp.setTextColor(Color.BLACK);
        temp_type.setTextColor(Color.BLACK);
        pressure.setTextColor(Color.BLACK);
        humidity.setTextColor(Color.BLACK);
        wind.setTextColor(Color.BLACK);

        presText.setTextColor(Color.DKGRAY);
        humText.setTextColor(Color.DKGRAY);
        windText.setTextColor(Color.DKGRAY);

    }

    public void nightMode()
    {

        cityText.setTextColor(Color.WHITE);
        country.setTextColor(Color.LTGRAY);
        temp.setTextColor(Color.WHITE);
        temp_type.setTextColor(Color.WHITE);
        pressure.setTextColor(Color.WHITE);
        humidity.setTextColor(Color.WHITE);
        wind.setTextColor(Color.WHITE);

        presText.setTextColor(Color.LTGRAY);
        humText.setTextColor(Color.LTGRAY);
        windText.setTextColor(Color.LTGRAY);

    }

    public void updateLocation(String city)
    {

        url="https://api.weatherapi.com/v1/current.json?key=ceea495be7374dc6a39174422222906%20&q="+city+"&aqi=no";

        StringRequest request=new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                if(response!=null) {

                    updateWeather(response);

                    locationRef.child(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid())
                        .child("location").setValue(city);

                }
                else{
                    DynamicToast.make(WeatherActivity.this, "City not found!", getResources().getDrawable(R.drawable.warning),
                            getResources().getColor(R.color.white), getResources().getColor(R.color.black), 3000).show();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                DynamicToast.make(WeatherActivity.this, "Something went wrong!", getResources().getDrawable(R.drawable.warning),
                        getResources().getColor(R.color.white), getResources().getColor(R.color.black), 3000).show();
            }
        });

        RequestQueue queue= Volley.newRequestQueue(this);
        queue.add(request);

    }

    private void updateWeather(String response)
    {

        try {

            JSONObject jsonObject=new JSONObject(response);
            JSONObject location=jsonObject.getJSONObject("location");

            String strName=location.getString("name");
            cityText.setText(strName);
            String strCountry=location.getString("country");
            String strContinent=location.getString("tz_id");
            strContinent=strContinent.substring(0,strContinent.indexOf('/'));
            country.setText(strCountry+", "+strContinent);

            JSONObject current=jsonObject.getJSONObject("current");
            int temp_c= (int) current.getDouble("temp_c");
            Typeface face = ResourcesCompat.getFont(WeatherActivity.this, R.font.aladin);
            temp.setTypeface(face);
            temp.setText(temp_c+"\u2103");

            int is_day=current.getInt("is_day");

            SharedPreferences.Editor editor=sp.edit();
            if(is_day==0)
            {
                day_night_mode.setImageResource(R.drawable.night_mode);
                nightMode();

                editor.putString("bgMode","0");
            }
            else
            {
                day_night_mode.setImageResource(R.drawable.day_mode);
                dayMode();

                editor.putString("bgMode","1");
            }
            editor.apply();

            JSONObject condition=current.getJSONObject("condition");
            String text=condition.getString("text");
            temp_type.setText(text);
            text=text.toLowerCase();

            if(is_day==0)
            {

                text=text.toLowerCase();
                if(text.contains("fog") || text.contains("mist"))
                {
                    weather.setImageResource(R.drawable.mist);
                }
                else if(text.contains("clear"))
                {
                    if(temp_c<=20)
                    {
                        weather.setImageResource(R.drawable.mist);
                    }
                    else
                    {
                        weather.setImageResource(R.drawable.moon_clear);
                    }
                }
                else if(text.contains("rain"))
                {
                    if(text.contains("light") || text.contains("patchy"))
                    {
                        weather.setImageResource(R.drawable.light_rain_night);
                    }
                    else if(text.contains("moderate"))
                    {
                        weather.setImageResource(R.drawable.moderate_rain_night);
                    }
                    else
                    {
                        if(text.contains("heavy"))
                        {
                            weather.setImageResource(R.drawable.heavy_rain_night);
                        }
                    }
                }
                else if(text.contains("drizzle"))
                {
                    weather.setImageResource(R.drawable.drizzle_night);
                }
                else if(text.contains("cloudy"))
                {
                    if(temp_c<=20)
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

                text=text.toLowerCase();
                if(text.contains("cloudy"))
                {
                    if(temp_c<=25)
                    {
                        weather.setImageResource(R.drawable.mist);
                    }
                    else
                    {
                        weather.setImageResource(R.drawable.cloudy);
                    }
                }
                if(text.contains("sunny"))
                {
                    if(temp_c<=20)
                    {
                        weather.setImageResource(R.drawable.mist);
                    }
                    else
                    {
                        weather.setImageResource(R.drawable.clear_sky);
                    }
                }
                else if(text.contains("mist") || text.contains("fog"))
                {
                    weather.setImageResource(R.drawable.mist);
                }
                else if(text.contains("rain"))
                {
                    if(text.contains("light") || text.contains("patchy"))
                    {
                        weather.setImageResource(R.drawable.light_rain);
                    }
                    else if(text.contains("moderate"))
                    {
                        weather.setImageResource(R.drawable.moderate_rain);
                    }
                    else
                    {
                        if(text.contains("heavy"))
                        {
                            weather.setImageResource(R.drawable.heavy_rain);
                        }
                    }
                }
                else if(text.contains("drizzle"))
                {
                    weather.setImageResource(R.drawable.morning_drizzle);
                }
                else
                {
                    weather.setImageResource(R.drawable.cloudy);
                }

            }

            String strWind=current.getString("wind_mph");
            wind.setText(strWind+" mph");

            int strHumidity=current.getInt("humidity");
            humidity.setText(strHumidity+"%");

            int strPressure=current.getInt("pressure_mb");
            String pressure_two_decimal=String.format("%.2f",strPressure*0.750062);
            pressure.setText(pressure_two_decimal+" mmhg");

            relativeLayout.setVisibility(View.VISIBLE);

            progressDialog.dismiss();

        } catch (JSONException e) {
            progressDialog.dismiss();
            DynamicToast.make(WeatherActivity.this, e.getMessage(), getResources().getDrawable(R.drawable.warning),
                    getResources().getColor(R.color.white), getResources().getColor(R.color.black), 3000).show();
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
                    progressDialog.dismiss();
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
        //dRef.removeEventListener(seenListener);
        checkStatus("Offline");
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