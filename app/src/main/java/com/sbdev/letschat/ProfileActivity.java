package com.sbdev.letschat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pranavpandey.android.dynamic.toasts.DynamicToast;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    ImageView back,editImg;

    CircleImageView profilePic;

    TextInputLayout layoutName,layoutEmail;

    TextInputEditText name,email;

    AppCompatButton save;

    RelativeLayout layout;

    FirebaseAuth firebaseAuth;

    DatabaseReference reference;

    StorageReference storageReference;

    ProgressDialog progressDialog;

    Uri imgURI;

    public static final int reqCode=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        back=findViewById(R.id.profileBack);
        profilePic=findViewById(R.id.profileCircleImg);
        layoutName=findViewById(R.id.profileLayout1);
        layoutEmail=findViewById(R.id.profileLayout2);
        name=findViewById(R.id.profileETName);
        email=findViewById(R.id.profileETEmail);
        save=findViewById(R.id.profileSave);
        layout=findViewById(R.id.profileRelativeNew);
        editImg=findViewById(R.id.profileEditImg);

        progressDialog=new ProgressDialog(this);

        firebaseAuth=FirebaseAuth.getInstance();

        reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.keepSynced(true);

        storageReference= FirebaseStorage.getInstance().getReference("Pictures");

        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog_dots);
        progressDialog.setCancelable(true);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        if(!isNetworkConnected())
        {
            progressDialog.dismiss();
            Snackbar.make(layout,"Your device is offline!",Snackbar.LENGTH_SHORT).show();
        }

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        /*profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                View view=getLayoutInflater().inflate(R.layout.profile_pic_zoom,null);
                layout.addView(view);

            }
        });*/

        reference.child(firebaseAuth.getCurrentUser().getUid())
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    UserModel userModel=snapshot.getValue(UserModel.class);

                    name.setText(userModel.getName());
                    email.setText(userModel.getEmail());

                    ProfileActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Glide.with(ProfileActivity.this)
                                    .load(userModel.getProfilePic())
                                    .placeholder(R.drawable.item_user)
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

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                InputMethodManager imm = (InputMethodManager) ProfileActivity.this.getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                progressDialog.show();
                progressDialog.setContentView(R.layout.progress_dialog_dots);
                progressDialog.setCancelable(true);
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

                                        if(!userModel.getName().equals(n))
                                        {
                                            HashMap map=new HashMap();
                                            map.put("Name",n);

                                            reference.child(firebaseAuth.getCurrentUser().getUid()).updateChildren(map);
                                        }
                                        if(!userModel.getEmail().equals(e))
                                        {
                                            firebaseAuth.getCurrentUser().updateEmail(e);

                                            HashMap map=new HashMap();
                                            map.put("Email",e);

                                            reference.child(firebaseAuth.getCurrentUser().getUid()).updateChildren(map);
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

                                                                    progressDialog.dismiss();

                                                                    Snackbar.make(layout,"Profile updated!",Snackbar.LENGTH_SHORT).show();

                                                                }
                                                            });

                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        DynamicToast.make(ProfileActivity.this,e.getMessage(),3000).show();
                                                    }
                                                });
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

}