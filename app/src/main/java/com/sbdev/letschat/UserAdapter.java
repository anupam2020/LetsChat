package com.sbdev.letschat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

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
                .load(model.ProfilePic)
                .into(holder.profile);

        holder.name.setText(model.Name);
        holder.email.setText(model.Email);

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class UserViewHolder extends RecyclerView.ViewHolder {

        CircleImageView profile;
        TextView name,email;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);

            profile=itemView.findViewById(R.id.userProfilePic);
            name=itemView.findViewById(R.id.userName);
            email=itemView.findViewById(R.id.userEmail);

        }
    }

}