package com.sbdev.letschat;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pranavpandey.android.dynamic.toasts.DynamicToast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageActivity extends AppCompatActivity {

    ImageView back,more,gallery,send,searchImg;

    TextInputLayout textInputLayout;

    TextInputEditText textInputEditText;

    EditText msgText;

    CircleImageView profilePic;

    TextView userName,userStatus;

    FirebaseAuth firebaseAuth;

    DatabaseReference reference,chatsRef,dRef,usersRef,statusRef,typingRef,seenRef,friendDBRef;

    StorageReference storageReference;

    String myUID,receiverStr,senderStr,receiverName,senderName,friendToken,friendUID,myToken;

    private final int reqCodeWall=1,reqCodeMsg=2;

    Uri imgURI,imageURI;

    RecyclerView recyclerView;

    ArrayList<MessageModel> arrayList, filterList;

    MessageAdapter adapter;

    ProgressDialog progressDialog;

    RelativeLayout layout,topLayout, lottieRelative;

    Animation animation,animationClick;

    String downloadURL="";

    LottieAnimationView lottie;

    int flag=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        getWindow().setBackgroundDrawableResource(R.color.white);

        back=findViewById(R.id.msgBack);
        more=findViewById(R.id.msgMore);
        gallery=findViewById(R.id.msgGallery);
        //background=findViewById(R.id.msgBackgroundImg);
        msgText=findViewById(R.id.msgEditText);
        send=findViewById(R.id.msgSend);
        profilePic=findViewById(R.id.msgProfilePic);
        userName=findViewById(R.id.msgName);
        userStatus=findViewById(R.id.msgStatus);
        recyclerView=findViewById(R.id.msgRecycler);
        layout=findViewById(R.id.relativeMsg);
        topLayout=findViewById(R.id.msgRelativeTop);
        lottieRelative=findViewById(R.id.messageRelativeVIS);
        lottie=findViewById(R.id.msgLottie);
        searchImg=findViewById(R.id.messageSearchImg);
        textInputLayout=findViewById(R.id.messageTIL);
        textInputEditText=findViewById(R.id.messageET);

        filterList=new ArrayList<>();
        arrayList=new ArrayList<>();
        adapter=new MessageAdapter(arrayList,MessageActivity.this);
        recyclerView.setAdapter(adapter);

        firebaseAuth=FirebaseAuth.getInstance();

        reference=FirebaseDatabase.getInstance().getReference("Users");
        reference.keepSynced(true);
        chatsRef=FirebaseDatabase.getInstance().getReference("Chats");
        chatsRef.keepSynced(true);
        dRef=FirebaseDatabase.getInstance().getReference("Chats");
        dRef.keepSynced(true);
        usersRef= FirebaseDatabase.getInstance().getReference("Users").child(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid());
        usersRef.keepSynced(true);
        typingRef=FirebaseDatabase.getInstance().getReference("Typing");
        typingRef.keepSynced(true);
        seenRef=FirebaseDatabase.getInstance().getReference("Seen");
        seenRef.keepSynced(true);
        friendDBRef=FirebaseDatabase.getInstance().getReference("Friend");
        friendDBRef.keepSynced(true);

        //connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");

        storageReference= FirebaseStorage.getInstance().getReference("Pictures");

        if(getIntent().getStringExtra("friendUID")!=null) {
            friendUID=getIntent().getStringExtra("friendUID");
        }
        else {
            friendUID=getIntent().getStringExtra("myUID");
        }

        if(getIntent().getStringExtra("friendToken")!=null) {
            friendToken=getIntent().getStringExtra("friendToken");
        }
        else {
            friendToken=getIntent().getStringExtra("myToken");
        }
        Log.d("friendUID",friendUID+"");
        Log.d("friendToken",friendToken+"");
        friendDBRef.child(firebaseAuth.getCurrentUser().getUid()).child("friendUID").setValue(friendUID);
        myUID=firebaseAuth.getCurrentUser().getUid();

        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String s) {
                myToken=s;
            }
        });

        statusRef= FirebaseDatabase.getInstance().getReference("Users");
        statusRef.keepSynced(true);

        progressDialog=new ProgressDialog(this);

        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog_dots);
        progressDialog.setCancelable(true);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        if(!isNetworkConnected())
        {
            progressDialog.dismiss();
            Snackbar.make(layout,"Your device is offline!",Snackbar.LENGTH_SHORT).show();
        }

        loadWallpaper();

        loadProfile(friendUID);

        //recyclerView.scrollToPosition(arrayList.size() - 1);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        searchImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(flag==0){
                    textInputLayout.setVisibility(View.VISIBLE);
                    flag=1;

                    textInputEditText.requestFocus();
                    InputMethodManager imm = (InputMethodManager) MessageActivity.this.getSystemService(Activity.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(textInputEditText, 0);
                    textInputEditText.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {

                            filterList.clear();

                            if(s.toString().trim().isEmpty())
                            {
                                adapter=new MessageAdapter(arrayList,MessageActivity.this);
                            }
                            else
                            {

                                for(MessageModel model : arrayList)
                                {
                                    if(model.getText().toLowerCase().contains(s.toString().toLowerCase()) && model.getImgURI()==null)
                                    {
                                        filterList.add(model);
                                    }
                                }

                                adapter=new MessageAdapter(filterList,MessageActivity.this);

                            }
                            recyclerView.setAdapter(adapter);
                            adapter.notifyDataSetChanged();

                        }
                    });

                }
                else{
                    textInputLayout.setVisibility(View.GONE);
                    flag=0;
                    textInputEditText.setText("");

                    InputMethodManager imm = (InputMethodManager) MessageActivity.this.getSystemService(Activity.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }

            }
        });

        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isNetworkConnected())
                {
                    Snackbar.make(layout,"Your device is offline!",Snackbar.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
                else
                {
                    openGallery();
                }
            }
        });

        topLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent=new Intent(MessageActivity.this,FriendProfile.class);
                intent.putExtra("friendUID",friendUID);
                startActivity(intent);

            }
        });

        typingRef.child(friendUID)
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if(snapshot.exists())
                    {
                        TypingModel typingModel=snapshot.getValue(TypingModel.class);
                        assert typingModel != null;
                        if(typingModel.isTyping())
                        {
                            userStatus.setText("Typing...");
                        }
                        else
                        {

                            statusRef.child(friendUID)
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                                        UserModel userModel=snapshot.getValue(UserModel.class);
                                        assert userModel != null;
                                        String status=userModel.getStatus();
                                        if(status.equals("Online") || status.equals("Offline"))
                                        {
                                            userStatus.setText(status);
                                        }
                                        else
                                        {
                                            userStatus.setText("Last seen "+status);
                                        }

                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        DynamicToast.make(MessageActivity.this, error.getMessage(), getResources().getDrawable(R.drawable.warning),
                                                getResources().getColor(R.color.white), getResources().getColor(R.color.black), 3000).show();
                                    }
                                });

                        }
                    }
                    else
                    {

                        statusRef.child(friendUID)
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {

                                    UserModel userModel=snapshot.getValue(UserModel.class);
                                    assert userModel != null;
                                    String status=userModel.getStatus();
                                    if(status.equals("Online") || status.equals("Offline"))
                                    {
                                        userStatus.setText(status);
                                    }
                                    else
                                    {
                                        userStatus.setText("Last seen "+status);
                                    }

                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    DynamicToast.make(MessageActivity.this, error.getMessage(), getResources().getDrawable(R.drawable.warning),
                                            getResources().getColor(R.color.white), getResources().getColor(R.color.black), 3000).show();
                                }
                            });

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

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
                else
                {
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                DynamicToast.make(MessageActivity.this, error.getMessage(), getResources().getDrawable(R.drawable.warning),
                        getResources().getColor(R.color.white), getResources().getColor(R.color.black), 3000).show();
            }
        });

        more.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.Q)
            @Override
            public void onClick(View v) {

                PopupMenu popupMenu=new PopupMenu(MessageActivity.this,v);
                popupMenu.getMenuInflater().inflate(R.menu.msg_more,popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        if (item.getItemId() == R.id.change_wallpaper) {
                            selectImgAndSetBg();
                        }
                        else if(item.getItemId() == R.id.reset_wallpaper)
                        {
                            progressDialog.show();
                            progressDialog.setContentView(R.layout.progress_dialog_dots);
                            progressDialog.setCancelable(true);
                            progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

                            storageReference.child(myUID)
                                .child("Wallpaper").delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        getWindow().setBackgroundDrawableResource(R.color.white);
                                        progressDialog.dismiss();
                                        reference.child(myUID).child("Wallpaper").removeValue();
                                        DynamicToast.make(MessageActivity.this, "Wallpaper reset successful!", getResources().getDrawable(R.drawable.checked),
                                                getResources().getColor(R.color.white), getResources().getColor(R.color.black), 3000).show();

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        DynamicToast.make(MessageActivity.this, e.getMessage(), getResources().getDrawable(R.drawable.warning),
                                                getResources().getColor(R.color.white), getResources().getColor(R.color.black), 3000).show();
                                    }
                                });
                        }

                        return false;

                    }
                });

                popupMenu.show();

            }
        });



        send.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {

                if(!isNetworkConnected())
                {
                    Snackbar.make(layout,"Your device is offline!",Snackbar.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
                else
                {
                    String text=msgText.getText().toString().trim();

                    if(text.isEmpty())
                    {
                        DynamicToast.make(MessageActivity.this, "Please write a message to send!", getResources().getDrawable(R.drawable.warning),
                                getResources().getColor(R.color.white), getResources().getColor(R.color.black), 3000).show();
                    }
                    else
                    {
                        msgText.setText("");
                        downloadURL="";
                        addTextToFirebase(myUID,friendUID,text);
                    }
                }

            }
        });

//        msgText.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                recyclerView.scrollToPosition(arrayList.size() - 1);
//            }
//        });


        msgText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(s.length()>0)
                {
                    typingRef.child(firebaseAuth.getCurrentUser().getUid())
                            .child("typing")
                            .setValue(true);

                    gallery.setVisibility(View.GONE);
                }
                else
                {
                    typingRef.child(firebaseAuth.getCurrentUser().getUid())
                            .child("typing")
                            .setValue(false);

                    animation= AnimationUtils.loadAnimation(MessageActivity.this,R.anim.anim_gallery_icon);
                    gallery.setAnimation(animation);
                    gallery.setVisibility(View.VISIBLE);;
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        DatabaseReference senderRef=FirebaseDatabase.getInstance().getReference("Users").child(myUID);
        DatabaseReference receiverRef=FirebaseDatabase.getInstance().getReference("Users").child(friendUID);

        senderRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                UserModel userModel=snapshot.getValue(UserModel.class);
                assert userModel != null;
                senderName=userModel.getName();
                if(userModel.getProfilePic()!=null)
                {
                    senderStr=userModel.getProfilePic();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        receiverRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                UserModel userModel=snapshot.getValue(UserModel.class);
                assert userModel != null;
                receiverName=userModel.getName();
                if(userModel.getProfilePic()!=null)
                {
                    receiverStr=userModel.getProfilePic();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                seenMsg();

            }
        },1000);

    }

    private void openGallery()
    {

        Intent intent=new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,reqCodeMsg);

    }

    public void seenMsg()
    {

        DatabaseReference FriendDB=FirebaseDatabase.getInstance().getReference("Friend");

        dRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot dataSnapshot : snapshot.getChildren())
                {

                    if(dataSnapshot.exists())
                    {

                        MessageModel messageModel=dataSnapshot.getValue(MessageModel.class);

                        FriendDB.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot1) {

                                for(DataSnapshot dataSnapshot1 : snapshot1.getChildren())
                                {

                                    String value=dataSnapshot1.getValue().toString();
                                    value=value.substring(value.indexOf('=')+1,value.length()-1);

                                    assert messageModel != null;
                                    if(dataSnapshot1.getKey().equals(messageModel.getReceiver()))
                                    {

                                        if(messageModel.getSender().equals(value))
                                        {
                                            dataSnapshot.getRef().child("isSeen").setValue(1);
                                        }

                                    }

                                }


                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public void loadWallpaper()
    {

        reference.child(myUID).child("Wallpaper").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Log.d("Snapshot", String.valueOf(snapshot));

                if(snapshot.exists())
                {

                    Glide.with(getApplicationContext())
                        .load(Objects.requireNonNull(snapshot.getValue()).toString())
                        .into(new CustomTarget<Drawable>() {
                            @Override
                            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {

                                getWindow().setBackgroundDrawable(resource);
                                progressDialog.dismiss();

                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {

                            }
                        });

                }
                else {
                    progressDialog.dismiss();
                    getWindow().setBackgroundDrawableResource(R.color.white);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public void loadProfile(String friendUID)
    {

        reference.child(friendUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                UserModel userModel=snapshot.getValue(UserModel.class);
                assert userModel != null;
                userName.setText(userModel.getName());

                Glide.with(getApplicationContext())
                        .load(userModel.getProfilePic())
                        .placeholder(R.drawable.item_user)
                        .error(R.drawable.item_user)
                        .into(profilePic);

                readMsg(myUID,friendUID);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                DynamicToast.make(MessageActivity.this, error.getMessage(), getResources().getDrawable(R.drawable.warning),
                        getResources().getColor(R.color.white), getResources().getColor(R.color.black), 3000).show();
            }
        });

    }

    private void readMsg(String uid, String friendUID)
    {

        chatsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                arrayList.clear();

                for(DataSnapshot dataSnapshot : snapshot.getChildren())
                {

                    MessageModel messageModel=dataSnapshot.getValue(MessageModel.class);

                    assert messageModel != null;
                    if(messageModel.getSender()!=null)
                    {
                        if(messageModel.getSender().equals(uid) && messageModel.getReceiver().equals(friendUID) ||
                                messageModel.getReceiver().equals(uid) && messageModel.getSender().equals(friendUID))
                        {
                            arrayList.add(messageModel);
                        }
                    }
//                    else
//                    {
//                        chatsRef.child(Objects.requireNonNull(dataSnapshot.getKey())).removeValue();
//                    }

                }

                adapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(arrayList.size() - 1);

                if(arrayList.isEmpty()){
                    lottieRelative.setVisibility(View.VISIBLE);
                    lottieRelative.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if(!isNetworkConnected())
                            {
                                Snackbar.make(layout,"Your device is offline!",Snackbar.LENGTH_SHORT).show();
                            }
                            else
                            {
                                addTextToFirebase(myUID,friendUID,"Hello!");
                            }

                        }
                    });
                }
                else{
                    lottieRelative.setVisibility(View.GONE);
                }
                progressDialog.dismiss();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
                DynamicToast.make(MessageActivity.this, error.getMessage(), getResources().getDrawable(R.drawable.warning),
                        getResources().getColor(R.color.white), getResources().getColor(R.color.black), 3000).show();
            }
        });

    }

    private void addTextToFirebase(String sender, String receiver, String text)
    {

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

                Log.d("Date", strDateTime);

                HashMap map=new HashMap();

                map.put("sender",sender);
                map.put("senderName",senderName);
                map.put("receiver",receiver);
                map.put("receiverName",receiverName);
                map.put("text",text);
                map.put("time",strDateTime);
                map.put("senderPic",senderStr);
                map.put("receiverPic",receiverStr);
                map.put("isSeen",0);

                String key=chatsRef.push().getKey();
                map.put("key",key);

                DatabaseReference friendRef=FirebaseDatabase.getInstance().getReference("Users").child(receiver);

                assert key != null;
                chatsRef.child(key).setValue(map)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful())
                            {
                                //recyclerView.scrollToPosition(arrayList.size() - 1);
                                usersRef.child("last_text_time").setValue(Long.toString(estimatedServerTimeMs));
                                friendRef.child("last_text_time").setValue(Long.toString(estimatedServerTimeMs));

                                reference.child(friendUID)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                                            UserModel userModel=snapshot.getValue(UserModel.class);
                                            assert userModel != null;
                                            if(userModel.getIsLoggedIn().equals("true"))
                                            {
                                                friendDBRef.child(friendUID).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                                                        if(snapshot.exists())
                                                        {
                                                            FriendClass friendClass=snapshot.getValue(FriendClass.class);
                                                            assert friendClass != null;
                                                            if(!friendClass.getFriendUID().equals(firebaseAuth.getCurrentUser().getUid()))
                                                            {
                                                                sendFCMPush(friendToken,text,senderName,"");
                                                            }
                                                        }
                                                        else
                                                        {
                                                            sendFCMPush(friendToken,text,senderName,"");
                                                        }

                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });
                                            }

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                });

                            }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            DynamicToast.make(MessageActivity.this, e.getMessage(), getResources().getDrawable(R.drawable.warning),
                                    getResources().getColor(R.color.white), getResources().getColor(R.color.black), 3000).show();
                        }
                    });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference chatsListRef=FirebaseDatabase.getInstance().getReference("ChatsList").child(sender).child(receiver);

        chatsListRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(!snapshot.exists())
                {
                    chatsListRef.child("UID").setValue(receiver);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }

    private void sendFCMPush(String fcm_token, String text, String receiverName, String url) {

        String SERVER_KEY = "AAAA76VUvnQ:APA91bGH0SR1DIt7IpkrFUoIg8RfAHaTXlVDzmA9Tk7XxVrOQy1vKfPW0NUP0-34dXR4ESeOmvtn9RTdJ_F5Ew3nQYlKUUB-hQIFv07hkZZXOpjSP60bOiA8YoHwqjQnzonUVXCtyWdL";
        String msg = receiverName+" : "+text;
        String title = "You've a new message!";
        String FCM_MESSAGE_URL = "https://fcm.googleapis.com/fcm/send";

        JSONObject obj = null;
        JSONObject objData = null;
        JSONObject dataobjData = null;

        try {
            obj = new JSONObject();
            objData = new JSONObject();

            objData.put("body", msg);
            objData.put("title", title);

            objData.put("priority", "high");

            dataobjData = new JSONObject();
            dataobjData.put("myUID", myUID);
            dataobjData.put("myToken", myToken);
            dataobjData.put("image", url);

            obj.put("to", fcm_token);
            //obj.put("priority", "high");

            obj.put("notification", objData);
            obj.put("data", dataobjData);
            Log.e("return here>>", obj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, FCM_MESSAGE_URL, obj,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e("True", response + "");
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("False", error + "");
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "key=" + SERVER_KEY);
                params.put("Content-Type", "application/json");
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        int socketTimeout = 1000 * 60;// 60 seconds
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsObjRequest.setRetryPolicy(policy);
        requestQueue.add(jsObjRequest);
    }

    private void selectImgAndSetBg()
    {

        Intent intent=new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,reqCodeWall);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == reqCodeWall && resultCode == RESULT_OK) {
            assert data != null;
            if (data.getData()!=null) {

                imgURI = data.getData();

                //background.setImageURI(imgURI);

                Drawable drawable;
                try {
                    InputStream inputStream = getContentResolver().openInputStream(imgURI);
                    drawable = Drawable.createFromStream(inputStream, imgURI.toString());
                } catch (FileNotFoundException e) {
                    drawable = getResources().getDrawable(R.color.white);
                }

                getWindow().setBackgroundDrawable(drawable);

                Snackbar.make(layout, "Please wait! We are uploading your image...", Snackbar.LENGTH_LONG).show();

                storageReference.child(myUID)
                    .child("Wallpaper")
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

                            storageReference.child(myUID)
                            .child("Wallpaper")
                            .getDownloadUrl()
                            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    reference.child(myUID)
                                             .child("Wallpaper")
                                             .setValue(uri.toString());

                                    DynamicToast.make(MessageActivity.this, "Wallpaper successfully changed!", getResources().getDrawable(R.drawable.checked),
                                            getResources().getColor(R.color.white), getResources().getColor(R.color.black), 3000).show();
                                    progressDialog.dismiss();

                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            DynamicToast.make(MessageActivity.this, e.getMessage(), getResources().getDrawable(R.drawable.warning),
                                    getResources().getColor(R.color.white), getResources().getColor(R.color.black), 3000).show();
                            progressDialog.dismiss();
                        }
                    });

            }
        }
        if(requestCode == reqCodeMsg && resultCode == RESULT_OK) {
            assert data != null;
            if (data.getData()!=null) {

                imageURI = data.getData();

                String pic_key = chatsRef.push().getKey();
                assert pic_key != null;
                storageReference
                        .child("Chat_Pics")
                        .child(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid())
                        .child(pic_key)
                        .putFile(imageURI).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                                Snackbar.make(layout, "Please wait! We are uploading your image...", Snackbar.LENGTH_LONG).show();

                                double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                                //progressDialog.setMessage((int) progress + "");
                                progressDialog.show();
                                progressDialog.setContentView(R.layout.progress_image_upload_status);
                                progressDialog.setCancelable(false);
                                progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

                                TextView tv = progressDialog.findViewById(R.id.progressText);
                                tv.setText(String.format("%.2f", progress) + "%");

                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                storageReference
                                        .child("Chat_Pics")
                                        .child(firebaseAuth.getCurrentUser().getUid())
                                        .child(pic_key)
                                        .getDownloadUrl()
                                        .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {

                                                progressDialog.dismiss();
                                                downloadURL = uri.toString();
                                                addImageTextToFirebase(myUID, friendUID, pic_key);
                                                adapter = new MessageAdapter(arrayList, MessageActivity.this, imageURI);
                                                recyclerView.setAdapter(adapter);
                                                adapter.notifyDataSetChanged();

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                DynamicToast.make(MessageActivity.this, e.getMessage(), getResources().getDrawable(R.drawable.warning),
                                                        getResources().getColor(R.color.white), getResources().getColor(R.color.black), 3000).show();
                                                progressDialog.dismiss();
                                            }
                                        });

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                DynamicToast.make(MessageActivity.this, e.getMessage(), getResources().getDrawable(R.drawable.warning),
                                        getResources().getColor(R.color.white), getResources().getColor(R.color.black), 3000).show();
                                progressDialog.dismiss();
                            }
                        });

            }
        }

    }

    public void addImageTextToFirebase(String sender, String receiver, String pic_key)
    {

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

                Log.d("Date", strDateTime);

                HashMap map=new HashMap();

                map.put("sender",sender);
                map.put("senderName",senderName);
                map.put("receiver",receiver);
                map.put("receiverName",receiverName);
                map.put("text","--Image--");
                map.put("imgURI",downloadURL);
                map.put("time",strDateTime);
                map.put("senderPic",senderStr);
                map.put("receiverPic",receiverStr);
                map.put("isSeen",0);

                //String key=chatsRef.push().getKey();
                map.put("key",pic_key);

                DatabaseReference friendRef=FirebaseDatabase.getInstance().getReference("Users").child(receiver);

                chatsRef.child(pic_key).setValue(map)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if(task.isSuccessful())
                                {
                                    //recyclerView.scrollToPosition(arrayList.size() - 1);
                                    usersRef.child("last_text_time").setValue(Long.toString(estimatedServerTimeMs));
                                    friendRef.child("last_text_time").setValue(Long.toString(estimatedServerTimeMs));

                                    reference.child(friendUID)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                                UserModel userModel=snapshot.getValue(UserModel.class);
                                                assert userModel != null;
                                                if(userModel.getIsLoggedIn().equals("true"))
                                                {

                                                    friendDBRef.child(friendUID).addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                                                            if(snapshot.exists())
                                                            {
                                                                FriendClass friendClass=snapshot.getValue(FriendClass.class);
                                                                assert friendClass != null;
                                                                if(!friendClass.getFriendUID().equals(firebaseAuth.getCurrentUser().getUid()))
                                                                {
                                                                    sendFCMPush(friendToken,"Image received...",senderName,downloadURL);
                                                                }
                                                            }
                                                            else
                                                            {
                                                                sendFCMPush(friendToken,"Image received...",senderName,downloadURL);
                                                            }

                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                        }
                                                    });

                                                }

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });

                                }

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                DynamicToast.make(MessageActivity.this, e.getMessage(), getResources().getDrawable(R.drawable.warning),
                                        getResources().getColor(R.color.white), getResources().getColor(R.color.black), 3000).show();
                            }
                        });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference chatsListRef=FirebaseDatabase.getInstance().getReference("ChatsList").child(sender).child(receiver);

        chatsListRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(!snapshot.exists())
                {
                    chatsListRef.child("UID").setValue(receiver);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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

        //friendUID="";

        friendDBRef.child(firebaseAuth.getCurrentUser().getUid()).removeValue();

        typingRef.child(firebaseAuth.getCurrentUser().getUid())
                .child("typing")
                .setValue(false);

        startActivity(new Intent(MessageActivity.this,ChatActivity.class));
        finish();

    }

    @Override
    protected void onPause() {
        super.onPause();
        //friendUID="";

        friendDBRef.child(firebaseAuth.getCurrentUser().getUid()).removeValue();
        checkStatus("Offline");
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkStatus("Online");
        friendDBRef.child(firebaseAuth.getCurrentUser().getUid()).child("friendUID").setValue(friendUID);
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkStatus("Online");
        friendDBRef.child(firebaseAuth.getCurrentUser().getUid()).child("friendUID").setValue(friendUID);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        checkStatus("Online");
        friendDBRef.child(firebaseAuth.getCurrentUser().getUid()).child("friendUID").setValue(friendUID);
    }

}