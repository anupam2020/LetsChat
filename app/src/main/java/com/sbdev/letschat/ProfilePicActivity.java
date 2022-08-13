package com.sbdev.letschat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pranavpandey.android.dynamic.toasts.DynamicToast;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfilePicActivity extends AppCompatActivity {

    CircleImageView img;

    AppCompatButton upload;

    final int reqCode=121;

    Uri imageURI;

    FirebaseAuth firebaseAuth;

    DatabaseReference reference,usersRef;

    StorageReference storageReference;

    ProgressDialog progressDialog;

    RelativeLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_pic);

        img=findViewById(R.id.circularProfile);
        upload=findViewById(R.id.btnUpload);
        layout=findViewById(R.id.profileRelative);

        progressDialog=new ProgressDialog(this);

        firebaseAuth=FirebaseAuth.getInstance();

        reference= FirebaseDatabase.getInstance().getReference("Users");

        storageReference= FirebaseStorage.getInstance().getReference("Pictures");

        usersRef= FirebaseDatabase.getInstance().getReference("Users").child(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid());
        usersRef.keepSynced(true);

        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                openGallery();

            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressDialog.show();
                progressDialog.setContentView(R.layout.progress_dialog_dots);
                progressDialog.setCancelable(false);
                progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

                if(!isNetworkConnected())
                {
                    Snackbar.make(layout,"Your device is offline!",Snackbar.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
                else
                {
                    uploadImgToFirebase();
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
                                        DynamicToast.make(ProfilePicActivity.this, error.getMessage(), getResources().getDrawable(R.drawable.warning),
                                                getResources().getColor(R.color.white), getResources().getColor(R.color.black), 3000).show();
                                    }
                                });
                            }

                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                DynamicToast.make(ProfilePicActivity.this, error.getMessage(), getResources().getDrawable(R.drawable.warning),
                        getResources().getColor(R.color.white), getResources().getColor(R.color.black), 3000).show();
            }
        });

    }

    private void uploadImgToFirebase()
    {

        if(imageURI==null)
        {
            progressDialog.dismiss();
            DynamicToast.make(ProfilePicActivity.this, "Please select an image!", getResources().getDrawable(R.drawable.image_sent),
                    getResources().getColor(R.color.white), getResources().getColor(R.color.black), 3000).show();
        }
        else
        {

            Snackbar.make(layout, "Please wait! We are uploading your image...", Snackbar.LENGTH_LONG).show();

            if(firebaseAuth.getCurrentUser()!=null)
            {
                storageReference.child(firebaseAuth.getCurrentUser().getUid())
                        .child("Profile_Pic")
                        .putFile(imageURI)
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                                double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                                //progressDialog.setMessage((int) progress + "");
                                progressDialog.show();
                                progressDialog.setContentView(R.layout.progress_image_upload_status);
                                progressDialog.setCancelable(false);
                                progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

                                TextView tv = progressDialog.findViewById(R.id.progressText);
                                tv.setText(String.format("%.2f", progress) + "%");

                            }
                        })
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
                                                        .updateChildren(map).addOnCompleteListener(new OnCompleteListener() {
                                                            @Override
                                                            public void onComplete(@NonNull Task task) {

                                                                if(task.isSuccessful())
                                                                {

                                                                    progressDialog.dismiss();
                                                                    DynamicToast.make(ProfilePicActivity.this, "Profile picture was successfully uploaded!", getResources().getDrawable(R.drawable.checked),
                                                                            getResources().getColor(R.color.white), getResources().getColor(R.color.black), 3000).show();
                                                                    startActivity(new Intent(ProfilePicActivity.this,ChatActivity.class));
                                                                    finishAffinity();
                                                                }

                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                DynamicToast.make(ProfilePicActivity.this, e.getMessage(), getResources().getDrawable(R.drawable.warning),
                                                                        getResources().getColor(R.color.white), getResources().getColor(R.color.black), 3000).show();
                                                            }
                                                        });

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {

                                                DynamicToast.make(ProfilePicActivity.this, e.getMessage(), getResources().getDrawable(R.drawable.warning),
                                                        getResources().getColor(R.color.white), getResources().getColor(R.color.black), 3000).show();

                                            }
                                        });

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                progressDialog.dismiss();
                                DynamicToast.make(ProfilePicActivity.this, e.getMessage(), getResources().getDrawable(R.drawable.warning),
                                        getResources().getColor(R.color.white), getResources().getColor(R.color.black), 3000).show();

                            }
                        });
            }

        }

    }

    private void openGallery()
    {

        Intent intent=new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,reqCode);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==reqCode && resultCode==RESULT_OK && data.getData()!=null)
        {

            imageURI=data.getData();
            img.setImageURI(imageURI);

        }

    }

    private boolean isNetworkConnected()
    {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    public void checkStatus(@NonNull String status)
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