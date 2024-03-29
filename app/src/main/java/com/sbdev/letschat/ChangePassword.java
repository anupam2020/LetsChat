package com.sbdev.letschat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
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
import java.util.Objects;

public class ChangePassword extends AppCompatActivity {

    ImageView back;

    TextInputLayout layout1,layout2,layout3;

    TextInputEditText oldPass,newPass,confirmPass;

    AppCompatButton update,passReset;

    FirebaseAuth firebaseAuth;

    RelativeLayout layout;

    ProgressDialog progressDialog;

    DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        back=findViewById(R.id.changePassBack);
        layout1=findViewById(R.id.changePassLayout1);
        layout2=findViewById(R.id.changePassLayout2);
        layout3=findViewById(R.id.changePassLayout3);
        oldPass=findViewById(R.id.changePassET1);
        newPass=findViewById(R.id.changePassET2);
        confirmPass=findViewById(R.id.changePassET3);
        update=findViewById(R.id.changePassUpdate);
        passReset=findViewById(R.id.changePassSendPassLink);
        layout=findViewById(R.id.changePassRelative);

        progressDialog=new ProgressDialog(this);

        firebaseAuth=FirebaseAuth.getInstance();

        usersRef= FirebaseDatabase.getInstance().getReference("Users").child(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid());
        usersRef.keepSynced(true);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        if(!isNetworkConnected())
        {
            Snackbar.make(layout,"Your device is offline!",Snackbar.LENGTH_SHORT).show();
        }

        NetworkClass.connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected){

                    usersRef.child("status").setValue("Online");
                    //usersRef.child("status").onDisconnect().setValue("offline");

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

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                DynamicToast.make(ChangePassword.this, error.getMessage(), getResources().getDrawable(R.drawable.warning),
                        getResources().getColor(R.color.white), getResources().getColor(R.color.black), 3000).show();
            }
        });

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                InputMethodManager imm = (InputMethodManager) ChangePassword.this.getSystemService(Activity.INPUT_METHOD_SERVICE);
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

                    String opass=oldPass.getText().toString().trim();
                    String npass=newPass.getText().toString().trim();
                    String cpass=confirmPass.getText().toString().trim();

                    if(opass.isEmpty())
                    {
                        progressDialog.dismiss();
                        layout1.setError("This cannot be empty!");
                    }
                    else if(npass.isEmpty())
                    {
                        progressDialog.dismiss();
                        layout2.setError("This cannot be empty!");
                    }
                    else if(cpass.isEmpty())
                    {
                        progressDialog.dismiss();
                        layout3.setError("This cannot be empty!");
                    }
                    else
                    {
                        if(!npass.equals(cpass))
                        {
                            progressDialog.dismiss();
                            layout2.setError("Passwords should match!");
                            layout3.setError("Passwords should match!");
                        }
                        else
                        {
                            layout2.setErrorEnabled(false);
                            layout3.setErrorEnabled(false);
                            updatePassword(npass,opass);
                        }
                    }

                }

            }
        });

        oldPass.addTextChangedListener(new TextWatcher() {
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

        newPass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                layout2.setErrorEnabled(false);
                if(newPass.getText().toString().trim().equals(confirmPass.getText().toString().trim()))
                {
                    layout3.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        confirmPass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                layout3.setErrorEnabled(false);
                if(newPass.getText().toString().trim().equals(confirmPass.getText().toString().trim()))
                {
                    layout2.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        passReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
                    if(firebaseAuth.getCurrentUser().getEmail()!=null)
                    {

                        firebaseAuth.sendPasswordResetEmail(firebaseAuth.getCurrentUser().getEmail()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if(task.isSuccessful())
                                {
                                    progressDialog.dismiss();
                                    DynamicToast.make(ChangePassword.this, "Password reset link was successfully sent to your mail!", getResources().getDrawable(R.drawable.mail),
                                            getResources().getColor(R.color.white), getResources().getColor(R.color.black), 3000).show();
                                }

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                DynamicToast.make(ChangePassword.this, e.getMessage(), getResources().getDrawable(R.drawable.warning),
                                        getResources().getColor(R.color.white), getResources().getColor(R.color.black), 3000).show();
                            }
                        });

                    }
                    else
                    {
                        progressDialog.dismiss();
                        DynamicToast.make(ChangePassword.this, "Email not found!", getResources().getDrawable(R.drawable.warning),
                                getResources().getColor(R.color.white), getResources().getColor(R.color.black), 3000).show();
                    }
                }

            }
        });

    }

    private void updatePassword(String npass,String opass)
    {

        AuthCredential credential = EmailAuthProvider.getCredential(Objects.requireNonNull(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getEmail()),opass);

        firebaseAuth.getCurrentUser().reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful())
                {

                    firebaseAuth.getCurrentUser().updatePassword(npass).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful())
                            {
                                progressDialog.dismiss();
                                DynamicToast.make(ChangePassword.this, "Password was successfully changed!", getResources().getDrawable(R.drawable.checked),
                                        getResources().getColor(R.color.white), getResources().getColor(R.color.black), 3000).show();
                            }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            DynamicToast.make(ChangePassword.this, e.getMessage(), getResources().getDrawable(R.drawable.warning),
                                    getResources().getColor(R.color.white), getResources().getColor(R.color.black), 3000).show();
                        }
                    });

                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                DynamicToast.make(ChangePassword.this, e.getMessage(), getResources().getDrawable(R.drawable.warning),
                        getResources().getColor(R.color.white), getResources().getColor(R.color.black), 3000).show();
            }
        });



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
    public void onBackPressed() {
        super.onBackPressed();

        finish();
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