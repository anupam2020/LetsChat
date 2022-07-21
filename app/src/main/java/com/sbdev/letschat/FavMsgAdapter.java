package com.sbdev.letschat;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class FavMsgAdapter extends RecyclerView.Adapter<FavMsgAdapter.FavMsgViewHolder> {

    Context context;
    ArrayList<FavMsgModel> arrayList;

    FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();

    public FavMsgAdapter(Context context, ArrayList<FavMsgModel> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public FavMsgViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FavMsgViewHolder(LayoutInflater.from(context).inflate(R.layout.chat_layout_left_fav_msg_user,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull FavMsgViewHolder holder, int position) {

        FavMsgModel favMsgModel=arrayList.get(holder.getAdapterPosition());

        Glide.with(context)
                .load(favMsgModel.getReceiverPic())
                .placeholder(R.drawable.item_user)
                .error(R.drawable.item_user)
                .into(holder.profileImg);
        if(firebaseAuth.getCurrentUser().getUid().equals(favMsgModel.getSender()))
        {
            holder.nameText.setText("You -> "+favMsgModel.getReceiverName());
            holder.layout.setBackgroundResource(R.drawable.chat_bg_left_fav_msg_user);
            holder.msgText.setTextColor(Color.WHITE);
        }
        else
        {
            holder.nameText.setText(favMsgModel.getSenderName()+" -> You");
            holder.layout.setBackgroundResource(R.drawable.chat_bg_left_fav_msg_friend);
            holder.msgText.setTextColor(Color.BLACK);
        }
        Glide.with(context)
                .load(favMsgModel.getSenderPic())
                .into(holder.profileImg);

        holder.msgText.setText(favMsgModel.getText());
        holder.msgTime.setText(favMsgModel.getTime());

        if(favMsgModel.getText().equals("--Image--"))
        {
            Glide.with(context)
                    .load(favMsgModel.getImgURI())
                    .placeholder(R.drawable.spin_coffee)
                    .error(R.drawable.spin_coffee)
                    .into(holder.favImg);
            holder.favImg.setVisibility(View.VISIBLE);
            holder.msgText.setVisibility(View.GONE);
        }
        else
        {
            holder.favImg.setVisibility(View.GONE);
            holder.msgText.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class FavMsgViewHolder extends RecyclerView.ViewHolder {

        CircleImageView profileImg;
        ImageView favImg;
        TextView nameText,msgText,msgTime;
        LinearLayout layout;

        public FavMsgViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImg=itemView.findViewById(R.id.favPic);
            nameText=itemView.findViewById(R.id.favTopText);
            msgText=itemView.findViewById(R.id.favMsg);
            msgTime=itemView.findViewById(R.id.favTime);
            layout=itemView.findViewById(R.id.favLinearLayout);
            favImg=itemView.findViewById(R.id.imgFavMsg);

        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

}
