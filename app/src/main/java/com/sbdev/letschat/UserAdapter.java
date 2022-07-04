package com.sbdev.letschat;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.pranavpandey.android.dynamic.toasts.DynamicToast;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    ArrayList<UserModel> arrayList;
    Context context;

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

        if(model.getStatus().equals("online"))
        {
            holder.statusIcon.setImageResource(R.drawable.circle_online);
        }
        else
        {
            holder.statusIcon.setImageResource(R.drawable.circle_offline);
        }

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
        ImageView statusIcon;
        TextView name;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);

            profile=itemView.findViewById(R.id.userProfilePic);
            name=itemView.findViewById(R.id.userName);
            statusIcon=itemView.findViewById(R.id.userStatusIcon);

        }
    }

}
