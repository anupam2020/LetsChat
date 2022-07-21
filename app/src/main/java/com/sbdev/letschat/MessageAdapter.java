package com.sbdev.letschat;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.pranavpandey.android.dynamic.toasts.DynamicToast;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();

    DatabaseReference favorites= FirebaseDatabase.getInstance().getReference("Favorites");
    DatabaseReference chatsRef= FirebaseDatabase.getInstance().getReference("Chats");
    StorageReference storageReference= FirebaseStorage.getInstance().getReference("Pictures");

    public static final int MSG_LEFT=0;
    public static final int MSG_RIGHT=1;

    boolean isFav;

    Animation animation;

    ArrayList<MessageModel> arrayList;
    Context context;
    Uri imgUri;
    View view;

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
        if(messageModel.getText().equals("--Image--"))
        {
            holder.msg.setVisibility(View.GONE);
            Glide.with(context)
                    .load(messageModel.getImgURI())
                    .placeholder(R.drawable.bw_loading1)
                    .error(R.drawable.bw_loading1)
                    .into(holder.msgImg);
            holder.msgImg.setVisibility(View.VISIBLE);
        }

        holder.favImg.setVisibility(View.GONE);
        favorites.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //holder.favImg.setVisibility(View.INVISIBLE);
                if(snapshot.child(firebaseAuth.getCurrentUser().getUid()).hasChild(key))
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

        if(messageModel.isSeen)
        {
            holder.tick.setImageResource(R.drawable.double_tick);
        }
        else
        {
            holder.tick.setImageResource(R.drawable.single_tick);
        }

        holder.itemView.setOnTouchListener(new View.OnTouchListener() {

            GestureDetector gestureDetector=new GestureDetector(context,new GestureDetector.SimpleOnGestureListener(){

                @Override
                public void onLongPress(MotionEvent e) {
                    super.onLongPress(e);

                    if(firebaseAuth.getCurrentUser().getUid().equals(messageModel.getSender()))
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

                                if(!snapshot.child(firebaseAuth.getCurrentUser().getUid()).hasChild(key)) {
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




        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // On Click

                Toast.makeText(context, "Clicked!", Toast.LENGTH_SHORT).show();

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

            }
        });



        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                // Long-click

                return false;
            }
        });


    }



    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        TextView msg,time;
        ImageView favImg,msgImg,tick;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            msg=itemView.findViewById(R.id.showMsg);
            time=itemView.findViewById(R.id.textTime);
            favImg=itemView.findViewById(R.id.favChat);
            msgImg=itemView.findViewById(R.id.imgMsg);
            tick=itemView.findViewById(R.id.imgTick);

        }
    }

    @Override
    public int getItemViewType(int position) {

        firebaseAuth=FirebaseAuth.getInstance();

        if(arrayList.get(position).sender.equals(firebaseAuth.getCurrentUser().getUid()))
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

}
