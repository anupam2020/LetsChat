package com.sbdev.letschat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
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
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.pranavpandey.android.dynamic.toasts.DynamicToast;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageActivity extends AppCompatActivity {

    ImageView back,more;

    //ImageView background;

    EditText msgText;

    ImageView send;

    CircleImageView profilePic;

    TextView userName,userStatus;

    FirebaseAuth firebaseAuth;

    DatabaseReference reference,chatsRef,dRef,usersRef,statusRef,typingRef;

    StorageReference storageReference;

    String friendUID,myUID,receiverStr,senderStr,receiverName,senderName;

    private final int reqCode=1;

    Uri imgURI;

    RecyclerView recyclerView;

    ArrayList<MessageModel> arrayList;

    MessageAdapter adapter;

    ProgressDialog progressDialog;

    RelativeLayout layout;

    ValueEventListener seenListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        getWindow().setBackgroundDrawableResource(R.drawable.msg_bg);

        back=findViewById(R.id.msgBack);
        more=findViewById(R.id.msgMore);
        //background=findViewById(R.id.msgBackgroundImg);
        msgText=findViewById(R.id.msgEditText);
        send=findViewById(R.id.msgSend);
        profilePic=findViewById(R.id.msgProfilePic);
        userName=findViewById(R.id.msgName);
        userStatus=findViewById(R.id.msgStatus);
        recyclerView=findViewById(R.id.msgRecycler);
        layout=findViewById(R.id.relativeMsg);

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
        usersRef= FirebaseDatabase.getInstance().getReference("Users").child(firebaseAuth.getCurrentUser().getUid());
        usersRef.keepSynced(true);
        typingRef=FirebaseDatabase.getInstance().getReference("Typing");
        typingRef.keepSynced(true);

        //connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");

        storageReference= FirebaseStorage.getInstance().getReference("Pictures");

        friendUID=getIntent().getStringExtra("friendUID");
        myUID=firebaseAuth.getCurrentUser().getUid();

        statusRef= FirebaseDatabase.getInstance().getReference("Users").child(friendUID);

        progressDialog=new ProgressDialog(this);

        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog_dots);
        progressDialog.setCancelable(true);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        checkRealTimeNetwork();

        loadWallpaper();

        loadProfile();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        typingRef.child(friendUID)
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if(snapshot.exists())
                    {
                        TypingModel typingModel=snapshot.getValue(TypingModel.class);
                        if(typingModel.isTyping())
                        {
                            userStatus.setText("Typing...");
                        }
                        else
                        {

                            statusRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {

                                    UserModel userModel=snapshot.getValue(UserModel.class);
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
                                    DynamicToast.make(MessageActivity.this,error.getMessage(),3000).show();
                                }
                            });

                        }
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

                    loadProfile();
                    loadWallpaper();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                DynamicToast.make(MessageActivity.this,error.getMessage(),3000).show();
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
                                        progressDialog.dismiss();
                                        DynamicToast.make(MessageActivity.this,"Wallpaper reset successful!",3000).show();
                                        getWindow().setBackgroundDrawableResource(R.drawable.msg_bg);

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        DynamicToast.make(MessageActivity.this,e.getMessage(),3000).show();
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
                        DynamicToast.make(MessageActivity.this,"Please write a message to send!",3000).show();
                    }
                    else
                    {
                        msgText.setText("");
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
                }
                else
                {
                    typingRef.child(firebaseAuth.getCurrentUser().getUid())
                            .child("typing")
                            .setValue(false);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        //seenMsg(friendUID);

        DatabaseReference senderRef=FirebaseDatabase.getInstance().getReference("Users").child(myUID);
        DatabaseReference receiverRef=FirebaseDatabase.getInstance().getReference("Users").child(friendUID);

        senderRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                UserModel userModel=snapshot.getValue(UserModel.class);
                if(userModel.getProfilePic()!=null)
                {
                    senderStr=userModel.getProfilePic();
                    senderName=userModel.getName();
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
                if(userModel.getProfilePic()!=null)
                {
                    receiverStr=userModel.getProfilePic();
                    receiverName=userModel.getName();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public void seenMsg(String friendUID)
    {

        dRef=FirebaseDatabase.getInstance().getReference("Chats");
        seenListener=dRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot dataSnapshot : snapshot.getChildren())
                {

                    MessageModel messageModel=dataSnapshot.getValue(MessageModel.class);
                    if(messageModel.getReceiver().equals(firebaseAuth.getCurrentUser().getUid())
                            && messageModel.getSender().equals(friendUID))
                    {
                        HashMap map=new HashMap();
                        map.put("isSeen",true);
                        dataSnapshot.getRef().updateChildren(map);
                    }

                }

                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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
                        DynamicToast.make(MessageActivity.this,error.getMessage(),3000).show();
                    }
                });

            }
        },2000);

    }

    public void loadWallpaper()
    {

        storageReference.child(myUID)
                .child("Wallpaper")
                .getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        Glide.with(getApplicationContext())
                                .load(uri)
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
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        progressDialog.dismiss();
                        getWindow().setBackgroundDrawableResource(R.drawable.msg_bg);
                        //background.setImageResource(R.drawable.msg_bg);

                    }
                });

    }

    public void loadProfile()
    {

        reference.child(friendUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                UserModel userModel=snapshot.getValue(UserModel.class);
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
                DynamicToast.make(MessageActivity.this,error.getMessage(),3000).show();
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

                    if(messageModel.getSender().equals(uid) && messageModel.getReceiver().equals(friendUID) ||
                        messageModel.getReceiver().equals(uid) && messageModel.getSender().equals(friendUID))
                    {
                        arrayList.add(messageModel);
                    }

                    adapter.notifyDataSetChanged();
                    recyclerView.scrollToPosition(arrayList.size() - 1);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                DynamicToast.make(MessageActivity.this,error.getMessage(),3000).show();
            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
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

                String key=chatsRef.push().getKey();
                map.put("key",key);

                DatabaseReference friendRef=FirebaseDatabase.getInstance().getReference("Users").child(receiver);

                chatsRef.child(key).setValue(map)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if(task.isSuccessful())
                                {
                                    //recyclerView.scrollToPosition(arrayList.size() - 1);
                                    usersRef.child("last_text_time").setValue(Long.toString(estimatedServerTimeMs));
                                    friendRef.child("last_text_time").setValue(Long.toString(estimatedServerTimeMs));
                                }

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                DynamicToast.make(MessageActivity.this,e.getMessage(),3000).show();
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

    private void selectImgAndSetBg()
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

            imgURI=data.getData();

            //background.setImageURI(imgURI);

            Drawable drawable;
            try {
                InputStream inputStream = getContentResolver().openInputStream(imgURI);
                drawable = Drawable.createFromStream(inputStream, imgURI.toString());
            } catch (FileNotFoundException e) {
                drawable = getResources().getDrawable(R.drawable.msg_bg);
            }

            getWindow().setBackgroundDrawable(drawable);

            storageReference.child(myUID)
                    .child("Wallpaper")
                    .putFile(imgURI);

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

        typingRef.child(firebaseAuth.getCurrentUser().getUid())
                .child("typing")
                .setValue(false);

        finish();

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

}