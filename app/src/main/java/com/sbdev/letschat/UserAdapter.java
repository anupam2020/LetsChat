package com.sbdev.letschat;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pranavpandey.android.dynamic.toasts.DynamicToast;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    ArrayList<UserModel> arrayList;
    Context context;
    String lastMessage;

    public UserAdapter(ArrayList<UserModel> arrayList, Context context) {
        this.arrayList = arrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UserViewHolder(LayoutInflater.from(context).inflate(R.layout.user_item_layout,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {

        UserModel model=arrayList.get(holder.getAdapterPosition());

        Glide.with(context)
                .load(model.getProfilePic())
                .placeholder(R.drawable.item_user)
                .error(R.drawable.item_user)
                .into(holder.profile);

        holder.name.setText(model.getName());

        if(model.getStatus().equals("Online"))
        {
            holder.statusIcon.setImageResource(R.drawable.circle_online);
        }
        else
        {
            holder.statusIcon.setImageResource(R.drawable.circle_offline);
        }

        lastMsg(model.getUID(), holder.lastMsg, holder.msgIcon, model.getStatus());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //DynamicToast.make(context,"status: "+model.getStatus(),3000).show();

                Intent intent=new Intent(context,MessageActivity.class);
                intent.putExtra("friendUID",model.getUID());
                context.startActivity(intent);

            }
        });

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class UserViewHolder extends RecyclerView.ViewHolder {

        CircleImageView profile;
        ImageView statusIcon,msgIcon;
        TextView name,lastMsg;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);

            profile=itemView.findViewById(R.id.userProfilePic);
            name=itemView.findViewById(R.id.userName);
            statusIcon=itemView.findViewById(R.id.userStatusIcon);
            lastMsg=itemView.findViewById(R.id.userLastMsg);
            msgIcon=itemView.findViewById(R.id.userMsgIcon);

        }
    }

    public void lastMsg(String friendUID, TextView last_msg, ImageView msg_icon, String status)
    {

        lastMessage="";

        FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();

        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Chats");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot dataSnapshot : snapshot.getChildren())
                {

                    MessageModel messageModel=dataSnapshot.getValue(MessageModel.class);

                    if(messageModel.getSender().equals(friendUID) && messageModel.getReceiver().equals(firebaseAuth.getCurrentUser().getUid()))
                    {
                        lastMessage=messageModel.getText();
                        msg_icon.setImageResource(R.drawable.curve_down_arrow);
                        msg_icon.setVisibility(View.VISIBLE);
                    }
                    if(messageModel.getSender().equals(firebaseAuth.getCurrentUser().getUid()) && messageModel.getReceiver().equals(friendUID))
                    {
                        lastMessage=messageModel.getText();
                        msg_icon.setImageResource(R.drawable.curve_up_arrow);
                        msg_icon.setVisibility(View.VISIBLE);
                    }
//                    else
//                    {
//                        if(status.equals("Offline") || status.equals("Online"))
//                        {
//                            last_msg.setText(status);
//                        }
//                        else
//                        {
//                            last_msg.setText("Last seen "+status);
//                        }
//                        msg_icon.setVisibility(View.GONE);
//                    }

                }
                if(!lastMessage.trim().isEmpty())
                {
                    last_msg.setText(lastMessage);
                    //msg_icon.setVisibility(View.VISIBLE);
                }
                else
                {
                    if(status.equals("Online") || status.equals("Offline"))
                    {
                        last_msg.setText(status);
                    }
                    else
                    {
                        last_msg.setText("Last seen "+status);
                    }
                    msg_icon.setVisibility(View.GONE);
                }

                lastMessage="";

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

}
