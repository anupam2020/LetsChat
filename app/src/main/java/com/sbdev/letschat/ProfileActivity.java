package com.sbdev.letschat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.facebook.login.Login;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jsibbold.zoomage.ZoomageView;
import com.pranavpandey.android.dynamic.toasts.DynamicToast;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    ImageView back,editImg,more,verificationImg;

    CircleImageView profilePic;

    TextInputLayout layoutName,layoutEmail;

    TextInputEditText name,email;

    AppCompatButton save;

    RelativeLayout layout;

    FirebaseAuth firebaseAuth;

    DatabaseReference reference,usersRef;

    StorageReference storageReference;

    ProgressDialog progressDialog;

    Uri imgURI;

    public static final int reqCode=1;

    String profilePicLink="";

    private boolean isUpdated=false,isEmailChanged=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        back=findViewById(R.id.profileBack);
        profilePic=findViewById(R.id.profileCircleImg);
        layoutName=findViewById(R.id.profileLayout1);
        layoutEmail=findViewById(R.id.profileLayout2);
        name=findViewById(R.id.profileETName);
        email=findViewById(R.id.profileETEmail);
        save=findViewById(R.id.profileSave);
        layout=findViewById(R.id.profileRelativeNew);
        editImg=findViewById(R.id.profileEditImg);
        more=findViewById(R.id.profileMore);
        verificationImg=findViewById(R.id.profileVerificationImg);

        progressDialog=new ProgressDialog(this);

        firebaseAuth=FirebaseAuth.getInstance();

        reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.keepSynced(true);
        usersRef= FirebaseDatabase.getInstance().getReference("Users").child(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid());
        usersRef.keepSynced(true);

        storageReference= FirebaseStorage.getInstance().getReference("Pictures");

        //connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");

        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog_dots);
        progressDialog.setCancelable(true);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        if(!isNetworkConnected()) {
            progressDialog.dismiss();
            Snackbar.make(layout,"Your device is offline!",Snackbar.LENGTH_SHORT).show();
        }

        reloadProfile();

        checkRealTimeNetwork();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        if(firebaseAuth.getCurrentUser()!=null) {
            if(firebaseAuth.getCurrentUser().isEmailVerified()) {
                verificationImg.setImageResource(R.drawable.verified);
            }
            else {
                verificationImg.setImageResource(R.drawable.info_not_verified);
            }
        }

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


                    reloadProfile();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                DynamicToast.make(ProfileActivity.this,error.getMessage(),3000).show();
            }
        });

        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ZoomageView profile=new ZoomageView(ProfileActivity.this);
                profile.setScaleType(ImageView.ScaleType.CENTER_CROP);
                profile.setAdjustViewBounds(true);

                Glide.with(getApplicationContext())
                        .load(profilePicLink)
                        .placeholder(R.drawable.item_user)
                        .error(R.drawable.item_user)
                        .into(profile);

                AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this).setView(profile);
                builder.show();

            }
        });



        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                isUpdated=false;
                isEmailChanged=false;

                InputMethodManager imm = (InputMethodManager) ProfileActivity.this.getSystemService(Activity.INPUT_METHOD_SERVICE);
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

                    String n=name.getText().toString().trim();
                    String e=email.getText().toString().trim();

                    if(isNetworkConnected())
                    {
                        if(n.trim().isEmpty())
                        {
                            progressDialog.dismiss();
                            layoutName.setError("Name cannot be empty!");
                        }
                        else if(e.trim().isEmpty())
                        {
                            progressDialog.dismiss();
                            layoutEmail.setError("Email cannot be empty!");
                        }
                        else
                        {

                            reference.child(firebaseAuth.getCurrentUser().getUid())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                                        UserModel userModel=snapshot.getValue(UserModel.class);

                                        assert userModel != null;
                                        if(!userModel.getName().equals(n))
                                        {
                                            HashMap map=new HashMap();
                                            map.put("Name",n);

                                            reference.child(firebaseAuth.getCurrentUser().getUid()).updateChildren(map);

                                            isUpdated=true;
                                        }
                                        if(!userModel.getEmail().equals(e))
                                        {

                                            AlertDialog.Builder builder=new AlertDialog.Builder(ProfileActivity.this);
                                            builder.setTitle("Update Email");
                                            builder.setMessage("Please enter your password to continue!");
                                            View view=getLayoutInflater().inflate(R.layout.password_layout,null);
                                            builder.setView(view);

                                            AlertDialog dialog=builder.create();

                                            dialog.show();

                                            TextInputLayout textInputLayout=view.findViewById(R.id.passwordLayout1);
                                            TextInputEditText editText=view.findViewById(R.id.passwordET1);
                                            AppCompatButton submit=view.findViewById(R.id.passwordSubmit);

                                            submit.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {

                                                    progressDialog.show();
                                                    progressDialog.setContentView(R.layout.progress_dialog_dots);
                                                    progressDialog.setCancelable(false);
                                                    progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

                                                    if(!isNetworkConnected())
                                                    {
                                                        dialog.dismiss();
                                                        progressDialog.dismiss();
                                                        Snackbar.make(layout,"Your device is offline!",Snackbar.LENGTH_SHORT).show();
                                                    }
                                                    else
                                                    {

                                                        String pass=editText.getText().toString().trim();
                                                        if(pass.isEmpty())
                                                        {
                                                            progressDialog.dismiss();
                                                            textInputLayout.setError("Password cannot be empty!");
                                                        }
                                                        else
                                                        {
                                                            if(firebaseAuth.getCurrentUser().getEmail()!=null)
                                                            {

                                                                AuthCredential credential = EmailAuthProvider
                                                                    .getCredential(firebaseAuth.getCurrentUser()
                                                                            .getEmail(),pass);

                                                                firebaseAuth.getCurrentUser().reauthenticate(credential)
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {

                                                                            if(task.isSuccessful())
                                                                            {
                                                                                firebaseAuth.getCurrentUser().updateEmail(e);

                                                                                HashMap map=new HashMap();
                                                                                map.put("Email",e);

                                                                                reference.child(firebaseAuth.getCurrentUser().getUid()).updateChildren(map);

                                                                                dialog.dismiss();
                                                                                progressDialog.dismiss();

                                                                                Snackbar.make(layout,"Profile successfully updated!",Snackbar.LENGTH_SHORT).show();
                                                                            }

                                                                        }
                                                                    }).addOnFailureListener(new OnFailureListener() {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception e) {
                                                                            progressDialog.dismiss();
                                                                            DynamicToast.make(ProfileActivity.this,e.getMessage(),3000).show();
                                                                        }
                                                                    });

                                                            }
                                                        }

                                                    }

                                                }
                                            });

                                            editText.addTextChangedListener(new TextWatcher() {
                                                @Override
                                                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                                                }

                                                @Override
                                                public void onTextChanged(CharSequence s, int start, int before, int count) {
                                                    textInputLayout.setErrorEnabled(false);
                                                }

                                                @Override
                                                public void afterTextChanged(Editable s) {

                                                }
                                            });
                                            isEmailChanged=true;
                                            isUpdated=true;
                                        }

                                        if(imgURI!=null)
                                        {
                                            storageReference.child(firebaseAuth.getCurrentUser().getUid())
                                                .child("Profile_Pic")
                                                .putFile(imgURI)
                                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                    @Override
                                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                                        storageReference.child(firebaseAuth.getCurrentUser().getUid())
                                                            .child("Profile_Pic")
                                                            .getDownloadUrl()
                                                            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                                @Override
                                                                public void onSuccess(Uri uri) {

                                                                    HashMap map=new HashMap();
                                                                    map.put("ProfilePic",uri.toString());

                                                                    reference.child(firebaseAuth.getCurrentUser().getUid())
                                                                            .updateChildren(map);

                                                                    imgURI=null;
                                                                    progressDialog.dismiss();

                                                                }
                                                            });

                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        DynamicToast.make(ProfileActivity.this,e.getMessage(),3000).show();
                                                    }
                                                });
                                            isUpdated=true;
                                        }

                                        if(isUpdated)
                                        {
                                            if(!isEmailChanged)
                                            {
                                                progressDialog.dismiss();
                                                Snackbar.make(layout,"Profile successfully updated!",Snackbar.LENGTH_SHORT).show();
                                            }
                                        }
                                        else
                                        {
                                            Snackbar.make(layout,"No changes found!",Snackbar.LENGTH_SHORT).show();
                                            progressDialog.dismiss();
                                        }

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        progressDialog.dismiss();
                                        DynamicToast.make(ProfileActivity.this,error.getMessage(),3000).show();
                                    }
                                });

                        }

                    }
                    else
                    {
                        progressDialog.dismiss();
                        Snackbar.make(layout,"Your device is offline!",Snackbar.LENGTH_SHORT).show();
                    }

                }

            }
        });


        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                layoutName.setErrorEnabled(false);
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
                layoutEmail.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PopupMenu popupMenu=new PopupMenu(ProfileActivity.this,v);
                popupMenu.getMenuInflater().inflate(R.menu.change_pass, popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        switch (item.getItemId())
                        {

                            case R.id.changePass:
                                startActivity(new Intent(ProfileActivity.this, ChangePassword.class));
                                break;

                            case R.id.deleteAccount:

                                AlertDialog.Builder builder=new AlertDialog.Builder(ProfileActivity.this);
                                builder.setTitle("Delete Account");
                                builder.setMessage("Please enter your password to continue!");
                                View view=getLayoutInflater().inflate(R.layout.password_layout,null);
                                builder.setView(view);

                                AlertDialog dialog=builder.create();

                                dialog.show();

                                TextInputLayout textInputLayout=view.findViewById(R.id.passwordLayout1);
                                TextInputEditText editText=view.findViewById(R.id.passwordET1);
                                AppCompatButton submit=view.findViewById(R.id.passwordSubmit);

                                submit.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        progressDialog.show();
                                        progressDialog.setContentView(R.layout.progress_dialog_dots);
                                        progressDialog.setCancelable(false);
                                        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

                                        if(!isNetworkConnected())
                                        {
                                            dialog.dismiss();
                                            progressDialog.dismiss();
                                            Snackbar.make(layout,"Your device is offline!",Snackbar.LENGTH_SHORT).show();
                                        }
                                        else
                                        {

                                            String pass=editText.getText().toString().trim();
                                            if(pass.isEmpty())
                                            {
                                                progressDialog.dismiss();
                                                textInputLayout.setError("Password cannot be empty!");
                                            }
                                            else
                                            {
                                                if(firebaseAuth.getCurrentUser().getEmail()!=null)
                                                {

                                                    AuthCredential credential = EmailAuthProvider
                                                            .getCredential(firebaseAuth.getCurrentUser()
                                                            .getEmail(),pass);

                                                    firebaseAuth.getCurrentUser().reauthenticate(credential)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                if(task.isSuccessful())
                                                                {
                                                                    deleteAccount();
                                                                }

                                                            }
                                                        }).addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                progressDialog.dismiss();
                                                                DynamicToast.make(ProfileActivity.this,e.getMessage(),3000).show();
                                                            }
                                                        });

                                                }
                                            }

                                        }

                                    }
                                });

                                editText.addTextChangedListener(new TextWatcher() {
                                    @Override
                                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                                    }

                                    @Override
                                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                                        textInputLayout.setErrorEnabled(false);
                                    }

                                    @Override
                                    public void afterTextChanged(Editable s) {

                                    }
                                });
                                break;
                            case R.id.verifyEmail:
                                progressDialog.show();
                                progressDialog.setContentView(R.layout.progress_dialog_dots);
                                progressDialog.setCancelable(true);
                                progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                                if(firebaseAuth.getCurrentUser()!=null)
                                {

                                    if(!firebaseAuth.getCurrentUser().isEmailVerified())
                                    {

                                        firebaseAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if(task.isSuccessful())
                                                {
                                                    DynamicToast.make(ProfileActivity.this,"Email verification link is sent to your mail!",3000).show();
                                                    progressDialog.dismiss();
                                                }

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                DynamicToast.make(ProfileActivity.this,e.getMessage(),3000).show();
                                                progressDialog.dismiss();
                                            }
                                        });

                                    }
                                    else
                                    {
                                        progressDialog.dismiss();
                                        DynamicToast.make(ProfileActivity.this,"Email is already verified!",3000).show();
                                    }

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

    private void deleteAccount()
    {

        String currentUID= Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
        firebaseAuth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful())
                {

                    //FirebaseDatabase.getInstance().getReference("Users").child(currentUID).removeValue();
                    FirebaseDatabase.getInstance().getReference("Location").child(currentUID).removeValue();
                    FirebaseDatabase.getInstance().getReference("Typing").child(currentUID).removeValue();
                    FirebaseDatabase.getInstance().getReference("Favorites").child(currentUID).removeValue();
                    FirebaseDatabase.getInstance().getReference("ChatsList").child(currentUID).removeValue();

                    FirebaseStorage.getInstance().getReference("Pictures")
                            .child(currentUID)
                            .child("Wallpaper")
                            .delete();

                    FirebaseStorage.getInstance().getReference("Pictures")
                        .child(currentUID)
                        .child("Profile_Pic")
                        .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {

                                progressDialog.dismiss();
                                DynamicToast.make(ProfileActivity.this,"Account successfully deleted!",3000).show();
                                startActivity(new Intent(ProfileActivity.this,MainActivity.class));

                            }
                        });


                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                DynamicToast.make(ProfileActivity.this,e.getMessage(),3000).show();
            }
        });

    }

    public void reloadProfile()
    {

        reference.child(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        UserModel userModel=snapshot.getValue(UserModel.class);

                        assert userModel != null;
                        name.setText(userModel.getName());
                        email.setText(userModel.getEmail());

                        profilePicLink=userModel.getProfilePic();

                        ProfileActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Glide.with(getApplicationContext())
                                        .load(userModel.getProfilePic())
                                        .placeholder(R.drawable.bw_loading1)
                                        .error(R.drawable.item_user)
                                        .into(profilePic);
                            }
                        });

                        progressDialog.dismiss();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressDialog.dismiss();
                        DynamicToast.make(ProfileActivity.this,error.getMessage(),3000).show();
                    }
                });

        editImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
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
                        if (!connected) {
                            Snackbar.make(layout,"Your device is offline!",Snackbar.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        DynamicToast.make(ProfileActivity.this, error.getMessage(), 3000).show();
                    }
                });

            }
        }, 2000);

    }

    public void openGallery()
    {

        Intent intent=new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent,reqCode);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==reqCode && resultCode==RESULT_OK && data.getData()!=null)
        {
            imgURI=data.getData();
            profilePic.setImageURI(imgURI);
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