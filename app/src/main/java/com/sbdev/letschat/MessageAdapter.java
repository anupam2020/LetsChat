package com.sbdev.letschat;

import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jsibbold.zoomage.ZoomageView;
import com.pranavpandey.android.dynamic.toasts.DynamicToast;

import java.net.URI;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();

    DatabaseReference favorites= FirebaseDatabase.getInstance().getReference("Favorites");
    DatabaseReference chatsRef= FirebaseDatabase.getInstance().getReference("Chats");
    StorageReference storageReference= FirebaseStorage.getInstance().getReference("Pictures");

    public static final int MSG_LEFT=0;
    public static final int MSG_RIGHT=1;

    boolean isFav=false;

    Animation animation;

    ArrayList<MessageModel> arrayList;
    Context context;
    Uri imgUri;

    public MessageAdapter(ArrayList<MessageModel> arrayList, Context context) {
        this.arrayList = arrayList;
        this.context = context;
    }

    public MessageAdapter(ArrayList<MessageModel> arrayList, Context context, Uri imgUri) {
        this.arrayList = arrayList;
        this.context = context;
        this.imgUri=imgUri;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if(viewType==MSG_RIGHT)
        {
            return new MessageViewHolder(LayoutInflater.from(context).inflate(R.layout.chat_layout_right,parent,false));
        }
        else
        {
            return new MessageViewHolder(LayoutInflater.from(context).inflate(R.layout.chat_layout_left,parent,false));
        }

    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {

        favorites.keepSynced(true);
        chatsRef.keepSynced(true);

        MessageModel messageModel=arrayList.get(holder.getAdapterPosition());

        String key=messageModel.getKey();

        holder.msg.setText(messageModel.getText());

        holder.time.setText(messageModel.getTime());

        holder.msgImg.setVisibility(View.GONE);
        holder.msg.setVisibility(View.VISIBLE);
        if(messageModel.getSender().equals(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid())) {
            holder.layout.setBackground(context.getDrawable(R.drawable.chat_bg_right));
            //holder.time.setTextColor(context.getResources().getColor(R.color.light_white_right));
        }
        else {
            holder.layout.setBackground(context.getDrawable(R.drawable.chat_bg_left));
            //holder.time.setTextColor(context.getResources().getColor(R.color.light_grey_left));
        }

        if(messageModel.getText().equals("--Image--"))
        {
            if(messageModel.getImgURI()!=null)
            {
                holder.msg.setVisibility(View.GONE);
                Glide.with(context)
                        .load(messageModel.getImgURI())
                        .placeholder(R.drawable.bw_loading1)
                        .error(R.drawable.bw_loading1)
                        .into(holder.msgImg);
                holder.msgImg.setVisibility(View.VISIBLE);

                if(messageModel.getSender().equals(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid())) {
                    holder.layout.setBackground(context.getDrawable(R.drawable.chat_bg_right_img_border));
                }
                else {
                    holder.layout.setBackground(context.getDrawable(R.drawable.chat_bg_left_img_border));
                }
               //holder.time.setTextColor(context.getResources().getColor(R.color.light_grey_left));
            }
        }

        holder.favImg.setVisibility(View.GONE);
        favorites.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.child(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid()).hasChild(key))
                {
                    holder.favImg.setImageResource(R.drawable.heart_1);
                    holder.favImg.setVisibility(View.VISIBLE);
                }
                else
                {
                    holder.favImg.setVisibility(View.GONE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        for(int i=0;i<arrayList.size();i++)
        {

            if(holder.getAdapterPosition()==arrayList.size()-1){

                if(messageModel.getIsSeen()==1) {
                    if(messageModel.getSender().equals(firebaseAuth.getCurrentUser().getUid())) {
                        holder.tick.setImageResource(R.drawable.me);

                        Glide.with(context)
                             .load(messageModel.getReceiverPic())
                             .into(holder.tick);

                        holder.tick.setVisibility(View.VISIBLE);
                    }
                }
                else {
                    if(messageModel.getSender().equals(firebaseAuth.getCurrentUser().getUid())) {
                        holder.tick.setImageResource(R.drawable.single_tick);
                        holder.tick.setVisibility(View.VISIBLE);
                    }
                }

            }
            else {
                if(messageModel.getSender().equals(firebaseAuth.getCurrentUser().getUid())) {
                    holder.tick.setImageResource(R.drawable.double_tick);
                    holder.tick.setVisibility(View.VISIBLE);
                }
            }

        }

        holder.itemView.setOnTouchListener(new View.OnTouchListener() {

            GestureDetector gestureDetector=new GestureDetector(context,new GestureDetector.SimpleOnGestureListener(){

                @Override
                public void onLongPress(MotionEvent e) {
                    super.onLongPress(e);

                    if(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid().equals(messageModel.getSender()))
                    {

                        PopupMenu popupMenu=new PopupMenu(context,holder.itemView);
                        popupMenu.getMenuInflater().inflate(R.menu.text_options,popupMenu.getMenu());
                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {

                                switch (item.getItemId())
                                {
                                    case R.id.deleteMsg:
                                        arrayList.remove(holder.getAdapterPosition());
                                        notifyItemRemoved(holder.getAdapterPosition());

                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {

                                                chatsRef.child(key).removeValue();
                                                storageReference.child("Chat_Pics").child(firebaseAuth.getCurrentUser().getUid()).child(key).delete();

                                                isFav=true;
                                                favorites.addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                                                        if(isFav)
                                                        {
                                                            if(snapshot.child(firebaseAuth.getCurrentUser().getUid()).hasChild(key))
                                                            {
                                                                holder.favImg.setVisibility(View.GONE);
                                                                favorites.child(firebaseAuth.getCurrentUser().getUid()).child(key).removeValue();
                                                                isFav=false;
                                                            }
                                                        }

                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });

                                            }
                                        },500);


                                        //notifyItemRemoved(holder.getAdapterPosition());
                                        break;

                                    case R.id.copyMsg:

                                        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                                        ClipData clip = ClipData.newPlainText("Copied Text", messageModel.getText());
                                        clipboard.setPrimaryClip(clip);

                                        DynamicToast.make(context,"Text copied!",3000).show();
                                        break;
                                }

                                return false;
                            }
                        });

                        popupMenu.show();

                    }

                }

                @Override
                public boolean onDoubleTap(MotionEvent e) {

                    isFav=true;

                    favorites.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if(isFav)
                            {

                                if(!snapshot.child(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid()).hasChild(key)) {
                                    HashMap map = new HashMap();
                                    map.put("sender", messageModel.getSender());
                                    map.put("receiver", messageModel.getReceiver());
                                    map.put("text", messageModel.getText());
                                    map.put("time", messageModel.getTime());
                                    map.put("key", messageModel.getKey());
                                    map.put("senderName", messageModel.getSenderName());
                                    map.put("senderPic", messageModel.getSenderPic());
                                    map.put("receiverName", messageModel.getReceiverName());
                                    map.put("receiverPic", messageModel.getReceiverPic());

                                    if(messageModel.getText().equals("--Image--"))
                                    {
                                        map.put("imgURI",messageModel.getImgURI());
                                    }

                                    holder.favImg.setImageResource(R.drawable.heart_1);

//                                    animation= AnimationUtils.loadAnimation(context,R.anim.anim_fav_icon);
//                                    holder.favImg.setAnimation(animation);
                                    holder.favImg.setVisibility(View.VISIBLE);
                                    favorites.child(firebaseAuth.getCurrentUser().getUid()).child(key).setValue(map);
                                    isFav=false;
                                }

                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    return super.onDoubleTap(e);
                }

                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {

                    if(messageModel.getImgURI()!=null)
                    {

                        ZoomageView profile=new ZoomageView(context);
                        profile.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        profile.setAdjustViewBounds(true);

                        Glide.with(context)
                                .load(messageModel.getImgURI())
                                .placeholder(R.drawable.blue_img_gallery)
                                .error(R.drawable.blue_img_gallery)
                                .into(profile);

                        AlertDialog.Builder builder = new AlertDialog.Builder(context).setView(profile)
                                .setPositiveButton("", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        downloadURL(messageModel.getImgURI());
                                    }
                                })
                                .setPositiveButtonIcon(context.getDrawable(R.drawable.download_black_new))
                                .setNeutralButton("",new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .setNeutralButtonIcon(context.getDrawable(R.drawable.return_black_new));
                        builder.show();

                    }

                    return super.onSingleTapConfirmed(e);
                }
            });
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if(!isNetworkConnected())
                {
                    Snackbar.make(v,"Your device is offline!",Snackbar.LENGTH_SHORT).show();
                }
                else
                {
                    gestureDetector.onTouchEvent(event);
                }
                return true;
            }
        });


        holder.favImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!isNetworkConnected())
                {
                    Snackbar.make(v,"Your device is offline!",Snackbar.LENGTH_SHORT).show();
                }
                else
                {

                    isFav=true;

                    favorites.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if(isFav)
                            {

                                if(snapshot.child(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid()).hasChild(key))
                                {
                                    holder.favImg.setVisibility(View.GONE);
                                    favorites.child(firebaseAuth.getCurrentUser().getUid()).child(key).removeValue();
                                    isFav=false;
                                }

                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }

            }
        });


    }



    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        TextView msg,time;
        ImageView favImg,msgImg;
        CircleImageView tick;
        LinearLayout layout;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            msg=itemView.findViewById(R.id.showMsg);
            time=itemView.findViewById(R.id.textTime);
            favImg=itemView.findViewById(R.id.favChat);
            msgImg=itemView.findViewById(R.id.imgMsg);
            tick=itemView.findViewById(R.id.imgTick);
            layout=itemView.findViewById(R.id.chatLinear);

        }
    }

    @Override
    public int getItemViewType(int position) {

        firebaseAuth=FirebaseAuth.getInstance();

        if(arrayList.get(position).getSender().equals(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid()))
        {
            return MSG_RIGHT;
        }
        else
        {
            return MSG_LEFT;
        }

    }

    private boolean isNetworkConnected()
    {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    private void downloadURL(String url) {

        DatabaseReference serverTimeRef = FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset");
        serverTimeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                long offset = snapshot.getValue(Long.class);
                long estimatedServerTimeMs = System.currentTimeMillis() + offset;

                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

                DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, estimatedServerTimeMs + ".jpg");

                downloadManager.enqueue(request);

                DynamicToast.make(context,"Downloading image!",3000).show();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

}
