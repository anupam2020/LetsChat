package com.sbdev.letschat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.pranavpandey.android.dynamic.toasts.DynamicToast;

import java.util.Arrays;
import java.util.HashMap;

public class FacebookActivity extends AppCompatActivity {

    private CallbackManager callbackManager;

    private FirebaseAuth mAuth;

    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facebook);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().setStatusBarColor(Color.WHITE);

        mAuth=FirebaseAuth.getInstance();

        reference= FirebaseDatabase.getInstance().getReference("Users");

        callbackManager = CallbackManager.Factory.create();
        //loginButton.setReadPermissions("email", "public_profile");

        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email","public_profile"));

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // App code
                        handleFacebookAccessToken(loginResult.getAccessToken());

                    }

                    @Override
                    public void onCancel() {
                        // App code
                        finish();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                        DynamicToast.make(FacebookActivity.this,exception.getMessage(),3000).show();
                        finish();
                    }
                });


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }


    private void handleFacebookAccessToken(AccessToken token) {

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();


                            HashMap map=new HashMap();
                            map.put("Name",mAuth.getCurrentUser().getDisplayName());
                            map.put("Email",mAuth.getCurrentUser().getEmail());
                            map.put("UID",mAuth.getCurrentUser().getUid());
                            map.put("status","Online");
                            map.put("last_text_time","0");
                            map.put("isLoggedIn","true");


                            reference.child(mAuth.getCurrentUser().getUid())
                                    .setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if(task.isSuccessful())
                                            {

                                                FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {
                                                    @Override
                                                    public void onSuccess(String s) {

                                                        reference.child(mAuth.getCurrentUser().getUid()).child("token").setValue(s);

                                                        DynamicToast.make(FacebookActivity.this,"Registration Successful!",3000).show();
                                                        startActivity(new Intent(FacebookActivity.this,ProfilePicActivity.class));
                                                        finish();

                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        DynamicToast.make(FacebookActivity.this, e.getMessage(), 3000).show();
                                                    }
                                                });

                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                            DynamicToast.make(FacebookActivity.this,e.getMessage(),3000).show();
                                            finish();
                                        }
                                    });


                        } else {
                            // If sign in fails, display a message to the user.
                            DynamicToast.make(FacebookActivity.this,task.getException().toString(),3000).show();
                            finish();
                        }
                    }
                });
    }


}