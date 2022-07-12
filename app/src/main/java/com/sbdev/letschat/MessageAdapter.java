package com.sbdev.letschat;

import android.content.Context;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();

    DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Favorites");

    public static final int MSG_LEFT=0;
    public static final int MSG_RIGHT=1;

    boolean isFav;

    ArrayList<MessageModel> arrayList;
    Context context;

    public MessageAdapter(ArrayList<MessageModel> arrayList, Context context) {
        this.arrayList = arrayList;
        this.context = context;
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

        MessageModel messageModel=arrayList.get(holder.getAdapterPosition());

        String key=messageModel.getKey();

        holder.msg.setText(messageModel.getText());

        holder.time.setText(messageModel.getTime());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

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



        holder.itemView.setOnTouchListener(new View.OnTouchListener() {

            GestureDetector gestureDetector=new GestureDetector(context,new GestureDetector.SimpleOnGestureListener(){

                @Override
                public boolean onDoubleTap(MotionEvent e) {

                    isFav=true;

                    reference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if(isFav)
                            {

                                if(!snapshot.child(firebaseAuth.getCurrentUser().getUid()).child(key).hasChild("favorite"))
                                {
                                    HashMap map=new HashMap();
                                    map.put("sender",messageModel.getSender());
                                    map.put("receiver",messageModel.getReceiver());
                                    map.put("text",messageModel.getText());
                                    map.put("time",messageModel.getTime());
                                    map.put("key",messageModel.getKey());
                                    map.put("favorite","true");

                                    holder.favImg.setImageResource(R.drawable.heart_1);
                                    holder.favImg.setVisibility(View.VISIBLE);
                                    reference.child(firebaseAuth.getCurrentUser().getUid()).child(key).setValue(map);
                                }
                                isFav=false;

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

                gestureDetector.onTouchEvent(event);
                return false;
            }
        });




        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // On Click

            }
        });

        holder.favImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                isFav=true;

                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if(isFav)
                        {

                            if(snapshot.child(firebaseAuth.getCurrentUser().getUid()).child(key).hasChild("favorite"))
                            {
                                holder.favImg.setVisibility(View.GONE);
                                reference.child(firebaseAuth.getCurrentUser().getUid()).child(key).removeValue();
                            }
                            isFav=false;

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });



        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                PopupMenu popupMenu=new PopupMenu(context,v);
                popupMenu.getMenuInflater().inflate(R.menu.text_options,popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        switch (item.getItemId())
                        {
                            case R.id.deleteMsg:
                                reference.child(key).removeValue();
                                arrayList.remove(holder.getAdapterPosition());
                                notifyItemRemoved(holder.getAdapterPosition());
                                break;

                            case R.id.copyMsg:
                                Toast.makeText(context, "Copy!", Toast.LENGTH_SHORT).show();
                        }

                        return false;
                    }
                });

                popupMenu.show();

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
        ImageView favImg;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            msg=itemView.findViewById(R.id.showMsg);
            time=itemView.findViewById(R.id.textTime);
            favImg=itemView.findViewById(R.id.favChat);

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

}
