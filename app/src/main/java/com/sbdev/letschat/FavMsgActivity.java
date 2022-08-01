package com.sbdev.letschat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

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
import com.google.firebase.storage.StorageReference;
import com.pranavpandey.android.dynamic.toasts.DynamicToast;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class FavMsgActivity extends AppCompatActivity {

    RecyclerView recyclerView;

    ImageView back,bg;

    FavMsgAdapter adapter;

    ArrayList<FavMsgModel> arrayList;

    FirebaseAuth firebaseAuth;

    DatabaseReference favRef,usersRef;

    StorageReference storageReference;

    ProgressDialog progressDialog;

    RelativeLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fav_msg);

        recyclerView=findViewById(R.id.favRecycler);
        back=findViewById(R.id.favBack);
        bg=findViewById(R.id.favBackground);
        layout=findViewById(R.id.favLayout);

        arrayList=new ArrayList<>();
        adapter=new FavMsgAdapter(FavMsgActivity.this,arrayList);
        recyclerView.setAdapter(adapter);

        progressDialog=new ProgressDialog(this);

        firebaseAuth=FirebaseAuth.getInstance();

        usersRef= FirebaseDatabase.getInstance().getReference("Users").child(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid());
        usersRef.keepSynced(true);
        favRef= FirebaseDatabase.getInstance().getReference("Favorites");
        favRef.keepSynced(true);

        storageReference= FirebaseStorage.getInstance().getReference("Pictures");

        bg.setBackgroundResource(R.color.white);

        checkRealTimeNetwork();

        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog_dots);
        progressDialog.setCancelable(true);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        favRef.child(firebaseAuth.getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot dataSnapshot : snapshot.getChildren())
                {
                    FavMsgModel favMsgModel=dataSnapshot.getValue(FavMsgModel.class);
                    arrayList.add(favMsgModel);
                }

                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
                DynamicToast.make(FavMsgActivity.this,error.getMessage(),3000).show();
            }
        });


        storageReference.child(firebaseAuth.getCurrentUser().getUid())
                .child("Wallpaper")
                .getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        Glide.with(FavMsgActivity.this)
                                .load(uri)
                                .into(bg);

                        progressDialog.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        bg.setBackgroundResource(R.color.white);
                        progressDialog.dismiss();

                    }
                });


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
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

                                    }
                                });
                            }

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                DynamicToast.make(FavMsgActivity.this,error.getMessage(),3000).show();
            }
        });

    }

    private void checkRealTimeNetwork()
    {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");

                connectedRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean connected = snapshot.getValue(Boolean.class);
                        if (!connected) {
                            Snackbar.make(layout,"Your device is offline!",Snackbar.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        DynamicToast.make(FavMsgActivity.this,error.getMessage(),3000).show();
                    }
                });

            }
        },2000);

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
        //dRef.removeEventListener(seenListener);
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