package com.sbdev.letschat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.pranavpandey.android.dynamic.toasts.DynamicToast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    TextInputLayout layout1,layout2,layout3;

    TextInputEditText name,email,password;

    AppCompatButton signUp;

    RelativeLayout google,facebook;

    TextView login;

    FirebaseAuth firebaseAuth;

    DatabaseReference reference;

    ProgressDialog progressDialog;

    static final int RC_SIGN_IN = 1;

    GoogleSignInClient mGoogleSignInClient;

    RelativeLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().setStatusBarColor(Color.WHITE);

        layout=findViewById(R.id.relativeRegister);
        layout1=findViewById(R.id.layout1);
        layout2=findViewById(R.id.layout2);
        layout3=findViewById(R.id.layout3);

        name=findViewById(R.id.etName);
        email=findViewById(R.id.etEmail);
        password=findViewById(R.id.etPassword);

        signUp=findViewById(R.id.btnSignUp);

        google=findViewById(R.id.relativeGoogle);
        facebook=findViewById(R.id.relativeFacebook);

        login=findViewById(R.id.registerLogin);

        progressDialog=new ProgressDialog(this);

        firebaseAuth=FirebaseAuth.getInstance();

        reference= FirebaseDatabase.getInstance().getReference("Users");

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("1029270978164-m80ep216u57m97jrp72qffsi4p27ppu3.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(RegisterActivity.this, gso);

        if(!isNetworkConnected())
        {
            Snackbar.make(layout,"Your device is offline!",Snackbar.LENGTH_SHORT).show();
        }

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
                finish();
            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                InputMethodManager imm = (InputMethodManager) RegisterActivity.this.getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                progressDialog.show();
                progressDialog.setContentView(R.layout.progress_dialog_dots);
                progressDialog.setCancelable(false);
                progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

                String n=name.getText().toString();
                String e=email.getText().toString();
                String p=password.getText().toString();

                if(isNetworkConnected())
                {
                    if(n.trim().isEmpty())
                    {
                        progressDialog.dismiss();
                        layout1.setError("Name cannot be empty!");
                    }
                    else if(e.trim().isEmpty())
                    {
                        progressDialog.dismiss();
                        layout2.setError("Email cannot be empty!");
                    }
                    else if(p.trim().isEmpty())
                    {
                        progressDialog.dismiss();
                        layout3.setError("Password cannot be empty!");
                    }
                    else
                    {
                        uploadToFirebase(n,e,p);
                    }
                }
                else
                {
                    progressDialog.dismiss();
                    Snackbar.make(layout,"Your device is offline!",Snackbar.LENGTH_SHORT).show();
                }

            }
        });

        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                layout1.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                layout2.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                layout3.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isNetworkConnected())
                {
                    signIn();
                }
                else
                {
                    progressDialog.dismiss();
                    Snackbar.make(layout,"Your device is offline!",Snackbar.LENGTH_SHORT).show();
                }

            }
        });

        facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isNetworkConnected())
                {
                    Intent intent=new Intent(RegisterActivity.this,FacebookActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                }
                else
                {
                    progressDialog.dismiss();
                    Snackbar.make(layout,"Your device is offline!",Snackbar.LENGTH_SHORT).show();
                }

            }
        });

    }

    private void signIn() {

        //For fresh registers
        mGoogleSignInClient.signOut();

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {

            progressDialog.show();
            progressDialog.setContentView(R.layout.progress_dialog_dots);
            progressDialog.setCancelable(false);
            progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately

                progressDialog.dismiss();
                DynamicToast.make(RegisterActivity.this, e.getMessage(), getResources().getDrawable(R.drawable.warning),
                        getResources().getColor(R.color.white), getResources().getColor(R.color.black), 3000).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        FirebaseUser user = firebaseAuth.getCurrentUser();

                        HashMap map=new HashMap();
                        map.put("Name",firebaseAuth.getCurrentUser().getDisplayName());
                        map.put("Email",firebaseAuth.getCurrentUser().getEmail());
                        map.put("UID",firebaseAuth.getCurrentUser().getUid());
                        map.put("status","Online");
                        map.put("last_text_time","0");
                        map.put("isLoggedIn","true");


                        reference.child(firebaseAuth.getCurrentUser().getUid())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {

                                    if (snapshot.exists()) {
                                        progressDialog.dismiss();
                                        DynamicToast.make(RegisterActivity.this, "Login Successful!", getResources().getDrawable(R.drawable.checked),
                                                getResources().getColor(R.color.white), getResources().getColor(R.color.black), 3000).show();
                                        startActivity(new Intent(RegisterActivity.this, ChatActivity.class));
                                        finish();
                                    }
                                    else {
                                        reference.child(firebaseAuth.getCurrentUser().getUid())
                                            .setValue(map)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    if (task.isSuccessful()) {

                                                        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {
                                                            @Override
                                                            public void onSuccess(String s) {

                                                                reference.child(firebaseAuth.getCurrentUser().getUid()).child("token").setValue(s);

                                                                progressDialog.dismiss();
                                                                DynamicToast.make(RegisterActivity.this, "Registration Successful!", getResources().getDrawable(R.drawable.checked),
                                                                        getResources().getColor(R.color.white), getResources().getColor(R.color.black), 3000).show();
                                                                startActivity(new Intent(RegisterActivity.this, ProfilePicActivity.class));
                                                                finish();

                                                            }
                                                        }).addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                progressDialog.dismiss();
                                                                DynamicToast.make(RegisterActivity.this, e.getMessage(), getResources().getDrawable(R.drawable.warning),
                                                                        getResources().getColor(R.color.white), getResources().getColor(R.color.black), 3000).show();
                                                            }
                                                        });
                                                    }

                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    progressDialog.dismiss();
                                                    DynamicToast.make(RegisterActivity.this, e.getMessage(), getResources().getDrawable(R.drawable.warning),
                                                            getResources().getColor(R.color.white), getResources().getColor(R.color.black), 3000).show();
                                                }
                                            });

                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    progressDialog.dismiss();
                                    DynamicToast.make(RegisterActivity.this, error.getMessage(), getResources().getDrawable(R.drawable.warning),
                                            getResources().getColor(R.color.white), getResources().getColor(R.color.black), 3000).show();
                                }
                            });


                    } else {
                        // If sign in fails, display a message to the user.

                        progressDialog.dismiss();
                        DynamicToast.make(RegisterActivity.this, task.getException().toString(), getResources().getDrawable(R.drawable.warning),
                                getResources().getColor(R.color.white), getResources().getColor(R.color.black), 3000).show();
                    }
                }
            });
    }


    public void uploadToFirebase(String name,String email,String password)
    {

        firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful())
                {

                    HashMap map=new HashMap();
                    map.put("Name",name);
                    map.put("Email",email);
                    map.put("UID",firebaseAuth.getCurrentUser().getUid());
                    map.put("status","Online");
                    map.put("last_text_time","0");
                    map.put("isLoggedIn","true");

                    reference.child(firebaseAuth.getCurrentUser().getUid())
                            .setValue(map)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful())
                                    {
                                        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {
                                            @Override
                                            public void onSuccess(String s) {

                                                reference.child(firebaseAuth.getCurrentUser().getUid()).child("token").setValue(s);

                                                progressDialog.dismiss();
                                                DynamicToast.make(RegisterActivity.this, "Registration Successful!", getResources().getDrawable(R.drawable.checked),
                                                        getResources().getColor(R.color.white), getResources().getColor(R.color.black), 3000).show();
                                                startActivity(new Intent(RegisterActivity.this,ProfilePicActivity.class));
                                                finish();

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                progressDialog.dismiss();
                                                DynamicToast.make(RegisterActivity.this, e.getMessage(), getResources().getDrawable(R.drawable.warning),
                                                        getResources().getColor(R.color.white), getResources().getColor(R.color.black), 3000).show();
                                            }
                                        });
                                    }

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    progressDialog.dismiss();
                                    DynamicToast.make(RegisterActivity.this, e.getMessage(), getResources().getDrawable(R.drawable.warning),
                                            getResources().getColor(R.color.white), getResources().getColor(R.color.black), 3000).show();

                                }
                            });

                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                progressDialog.dismiss();
                DynamicToast.make(RegisterActivity.this, e.getMessage(), getResources().getDrawable(R.drawable.warning),
                        getResources().getColor(R.color.white), getResources().getColor(R.color.black), 3000).show();

            }
        });

    }

    private boolean isNetworkConnected()
    {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    @Override
    public void onBackPressed() {

        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Exit");
        builder.setMessage("Do you ready want to exit?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finishAffinity();
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();

    }

}