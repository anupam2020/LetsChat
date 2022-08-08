package com.sbdev.letschat;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class SplashScreen extends AppCompatActivity {

    Handler handler;

    ImageView img;

    Animation animation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().setStatusBarColor(Color.WHITE);

        img=findViewById(R.id.splashImg);

        animation= AnimationUtils.loadAnimation(SplashScreen.this,R.anim.anim);

        img.setAnimation(animation);

        handler=new Handler(Looper.getMainLooper());

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashScreen.this,MainActivity.class);
                intent.putExtra("myUID",getIntent().getStringExtra("myUID"));
                intent.putExtra("myToken",getIntent().getStringExtra("myToken"));
                startActivity(intent);
                //finish();
            }
        },1500);

    }
}