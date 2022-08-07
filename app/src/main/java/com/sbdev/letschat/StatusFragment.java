package com.sbdev.letschat;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
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
import com.jsibbold.zoomage.ZoomageView;
import com.pranavpandey.android.dynamic.toasts.DynamicToast;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class StatusFragment extends Fragment {

    CircleImageView img;

    TextView topText, date;

    ImageView more;

    RecyclerView recyclerView;

    ArrayList<StatusModel> arrayList;

    StatusAdapter adapter;

    FirebaseAuth firebaseAuth;

    DatabaseReference reference,statusDBRef,statusRef;

    StorageReference storageReference;

    ProgressDialog progressDialog;

    RelativeLayout layout;

    private final int reqCodeMsg=1;

    static Uri imgURI;

    String name="",profilePicURL="",statusPicURL="";

    LinearLayout linearLayout;

    int flag=0;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        img=view.findViewById(R.id.statusProfilePic);
        topText=view.findViewById(R.id.statusText1);
        date=view.findViewById(R.id.statusDate);
        more=view.findViewById(R.id.statusMore);
        recyclerView=view.findViewById(R.id.statusRecycler);
        layout=view.findViewById(R.id.statusRelative);
        linearLayout=view.findViewById(R.id.statusLinear);

        arrayList=new ArrayList<>();
        adapter=new StatusAdapter(getActivity(),arrayList);
        recyclerView.setAdapter(adapter);

        progressDialog=new ProgressDialog(getActivity());

        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog_dots);
        progressDialog.setCancelable(true);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        firebaseAuth=FirebaseAuth.getInstance();

        reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.keepSynced(true);
        statusDBRef=FirebaseDatabase.getInstance().getReference("StatusDB");
        statusDBRef.keepSynced(true);
        statusRef=FirebaseDatabase.getInstance().getReference("Status");
        statusRef.keepSynced(true);

        storageReference= FirebaseStorage.getInstance().getReference("Pictures");

        if(!isNetworkConnected())
        {
            progressDialog.dismiss();
            Snackbar.make(layout,"Your device is offline!",Snackbar.LENGTH_SHORT).show();
        }

        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!isNetworkConnected())
                {
                    Snackbar.make(layout,"Your device is offline!",Snackbar.LENGTH_SHORT).show();
                }
                else
                {
                    openGallery();
                }

            }
        });

        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d("On Click Flag", String.valueOf(flag));

                if(flag==1)
                {

                    ZoomageView profile=new ZoomageView(getActivity());
                    profile.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    profile.setAdjustViewBounds(true);

                    Glide.with(getActivity())
                            .load(statusPicURL)
                            .placeholder(R.drawable.bw_loading1)
                            .error(R.drawable.bw_loading1)
                            .into(profile);

                    AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
                    builder.setView(profile);

                    builder.show();

                }
                else
                {

                    ZoomageView profile=new ZoomageView(getActivity());
                    profile.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    profile.setAdjustViewBounds(true);

                    Glide.with(getActivity())
                            .load(profilePicURL)
                            .placeholder(R.drawable.item_user)
                            .error(R.drawable.item_user)
                            .into(profile);

                    AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
                    builder.setView(profile);

                    builder.show();

                }



            }
        });

        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PopupMenu popupMenu=new PopupMenu(getActivity(),v);
                popupMenu.getMenuInflater().inflate(R.menu.delete,popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        if(item.getItemId()==R.id.deleteItem)
                        {

                            if(!isNetworkConnected())
                            {
                                Snackbar.make(layout,"Your device is offline!",Snackbar.LENGTH_SHORT).show();
                            }
                            else
                            {

                                progressDialog.show();
                                progressDialog.setContentView(R.layout.progress_image_delete_status);
                                progressDialog.setCancelable(true);
                                progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

                                storageReference.child(firebaseAuth.getCurrentUser().getUid())
                                        .child("Status")
                                        .delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {

                                                statusDBRef.child(firebaseAuth.getCurrentUser().getUid()).removeValue();

                                                Glide.with(getActivity())
                                                        .load(profilePicURL)
                                                        .placeholder(R.drawable.bw_loading1)
                                                        .error(R.drawable.bw_loading1)
                                                        .into(img);

                                                date.setText("Tap to upload status");
                                                more.setVisibility(View.GONE);

                                                progressDialog.dismiss();

                                                DynamicToast.make(getActivity(), "Status successfully deleted!", getResources().getDrawable(R.drawable.checked),
                                                        getResources().getColor(R.color.white), getResources().getColor(R.color.black), 3000).show();

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                progressDialog.dismiss();
                                                DynamicToast.make(getActivity(), e.getMessage(), getResources().getDrawable(R.drawable.warning),
                                                        getResources().getColor(R.color.white), getResources().getColor(R.color.black), 3000).show();
                                            }
                                        });

                            }

                        }

                        return false;
                    }
                });

                popupMenu.show();

            }
        });

        statusDBRef.child(firebaseAuth.getCurrentUser().getUid())
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if(snapshot.exists())
                    {

                        flag=1;
                        StatusModel statusModel=snapshot.getValue(StatusModel.class);

                        assert statusModel != null;
                        statusPicURL=statusModel.getImageURL();
                        Glide.with(getActivity())
                                .load(statusModel.getImageURL())
                                .placeholder(R.drawable.bw_loading1)
                                .error(R.drawable.bw_loading1)
                                .into(img);

                        date.setText(statusModel.getDate());
                        more.setVisibility(View.VISIBLE);

                        progressDialog.dismiss();

                        Log.d("Exists Flag", String.valueOf(flag));

                    }
                    else
                    {

                        flag=0;
                        reference.child(firebaseAuth.getCurrentUser().getUid())
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {

                                    UserModel userModel=snapshot.getValue(UserModel.class);
                                    assert userModel != null;
                                    if(userModel.getProfilePic()!=null)
                                    {
                                        profilePicURL=userModel.getProfilePic();
                                        Glide.with(getActivity())
                                            .load(userModel.getProfilePic())
                                            .placeholder(R.drawable.item_user)
                                            .error(R.drawable.item_user)
                                            .into(img);
                                    }
                                    else
                                    {
                                        img.setImageResource(R.drawable.item_user);
                                    }
                                    name=userModel.getName();
                                    progressDialog.dismiss();

                                    Log.d("Not Exists Flag", String.valueOf(flag));

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                        date.setText("Tap to upload status");
                        more.setVisibility(View.GONE);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


        statusDBRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                arrayList.clear();

                for(DataSnapshot dataSnapshot : snapshot.getChildren())
                {

                    StatusModel statusModel=dataSnapshot.getValue(StatusModel.class);
                    if(!statusModel.getUid().equals(firebaseAuth.getCurrentUser().getUid()))
                    {
                        arrayList.add(statusModel);
                    }

                }

                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void openGallery()
    {

        Intent intent=new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,reqCodeMsg);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==reqCodeMsg && resultCode == getActivity().RESULT_OK && data.getData()!=null)
        {

            imgURI=data.getData();
            img.setImageURI(imgURI);

            uploadToFirebase();

        }

    }

    private void uploadToFirebase()
    {

        Snackbar.make(layout, "Please wait! We are uploading your image...", Snackbar.LENGTH_LONG).show();

        DatabaseReference serverTimeRef = FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset");
        serverTimeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                long offset = snapshot.getValue(Long.class);
                long estimatedServerTimeMs = System.currentTimeMillis() + offset;

                Log.d("Server Time", String.valueOf(estimatedServerTimeMs));

                Timestamp timestamp=new Timestamp(estimatedServerTimeMs);
                Date date=timestamp;
                SimpleDateFormat simpleDateFormat=new SimpleDateFormat("MMMM dd, yyyy - hh:mm a");
                String strDateTime=simpleDateFormat.format(date);

                storageReference.child(firebaseAuth.getCurrentUser().getUid())
                    .child("Status")
                        .putFile(imgURI)
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
                                        .child("Status")
                                        .getDownloadUrl()
                                        .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {

                                                HashMap map=new HashMap();
                                                map.put("uid",firebaseAuth.getCurrentUser().getUid());
                                                map.put("name",name);
                                                map.put("date",strDateTime);
                                                map.put("imageURL",uri.toString());

                                                Log.d("Uri",uri.toString());

                                                statusDBRef.child(firebaseAuth.getCurrentUser().getUid())
                                                    .setValue(map)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {

                                                            if(task.isSuccessful()){
                                                                imgURI=null;
                                                                DynamicToast.make(getActivity(),"Status successfully posted!", getResources().getDrawable(R.drawable.checked),
                                                                        getResources().getColor(R.color.white), getResources().getColor(R.color.black), 3000).show();
                                                            }

                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            DynamicToast.make(getActivity(), e.getMessage(), getResources().getDrawable(R.drawable.warning),
                                                                    getResources().getColor(R.color.white), getResources().getColor(R.color.black), 3000).show();
                                                        }
                                                    });

                                            }
                                        });

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                DynamicToast.make(getActivity(), e.getMessage(), getResources().getDrawable(R.drawable.warning),
                                        getResources().getColor(R.color.white), getResources().getColor(R.color.black), 3000).show();
                            }
                        });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private boolean isNetworkConnected()
    {
        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_status, container, false);
    }
}