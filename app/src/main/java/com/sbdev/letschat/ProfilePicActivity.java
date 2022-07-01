package com.sbdev.letschat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pranavpandey.android.dynamic.toasts.DynamicToast;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfilePicActivity extends AppCompatActivity {

    CircleImageView img;

    AppCompatButton upload;

    final int reqCode=121;

    Uri imageURI;

    FirebaseAuth firebaseAuth;

    DatabaseReference reference;

    StorageReference storageReference;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_pic);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().setStatusBarColor(Color.WHITE);

        img=findViewById(R.id.circularProfile);
        upload=findViewById(R.id.btnUpload);

        progressDialog=new ProgressDialog(this);

        firebaseAuth=FirebaseAuth.getInstance();

        reference= FirebaseDatabase.getInstance().getReference("Users");

        storageReference= FirebaseStorage.getInstance().getReference("Pictures");

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

                uploadImgToFirebase();

            }
        });

    }

    private void uploadImgToFirebase()
    {

        if(imageURI==null)
        {
            progressDialog.dismiss();
            DynamicToast.make(ProfilePicActivity.this,"Please select an image!",3000).show();
        }
        else
        {

            storageReference.child(firebaseAuth.getCurrentUser().getUid())
                .child("Profile_Pic")
                .putFile(imageURI)
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
                                                    DynamicToast.make(ProfilePicActivity.this,"Profile picture was successfully uploaded!",3000).show();
                                                    startActivity(new Intent(ProfilePicActivity.this,ChatActivity.class));
                                                    finishAffinity();
                                                }

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                DynamicToast.make(ProfilePicActivity.this,e.getMessage(),3000).show();
                                            }
                                        });

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    DynamicToast.make(ProfilePicActivity.this,e.getMessage(),3000).show();

                                }
                            });

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        progressDialog.dismiss();
                        DynamicToast.make(ProfilePicActivity.this,e.getMessage(),3000).show();

                    }
                });

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