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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pranavpandey.android.dynamic.toasts.DynamicToast;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfilePicActivity extends AppCompatActivity {

    CircleImageView img;

    AppCompatButton upload;

    final int reqCode=121;

    Uri imageURI;

    FirebaseAuth firebaseAuth;

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
                progressDialog.setContentView(R.layout.progress_dialog);
                progressDialog.setCancelable(true);
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
            DynamicToast.make(ProfilePicActivity.this,"Please select an image!",2500).show();
        }
        else
        {

            storageReference.child(firebaseAuth.getCurrentUser().getUid())
                    .child("Profile_Pic")
                    .putFile(imageURI)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            progressDialog.dismiss();
                            DynamicToast.make(ProfilePicActivity.this,"Profile picture was successfully uploaded!",2500).show();
                            startActivity(new Intent(ProfilePicActivity.this,ChatActivity.class));
                            finishAffinity();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            progressDialog.dismiss();
                            DynamicToast.make(ProfilePicActivity.this,e.getMessage(),2500).show();

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