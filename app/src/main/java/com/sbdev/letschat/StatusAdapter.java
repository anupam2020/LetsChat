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

public class StatusAdapter extends RecyclerView.Adapter<StatusAdapter.StatusViewHolder> {

    Context context;
    ArrayList<StatusModel> arrayList;

    public StatusAdapter(Context context, ArrayList<StatusModel> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public StatusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new StatusViewHolder(LayoutInflater.from(context).inflate(R.layout.status_item_layout,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull StatusViewHolder holder, int position) {

        StatusModel statusModel=arrayList.get(holder.getAdapterPosition());

        Glide.with(context)
             .load(statusModel.getImageURL())
             .placeholder(R.drawable.bw_loading1)
             .error(R.drawable.bw_loading1)
             .into(holder.img);

        holder.name.setText(statusModel.getName());
        holder.date.setText(statusModel.getDate());

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class StatusViewHolder extends RecyclerView.ViewHolder {

        CircleImageView img;
        TextView name,date;

        public StatusViewHolder(@NonNull View itemView) {
            super(itemView);

            img=itemView.findViewById(R.id.statusItemProfilePic);
            name=itemView.findViewById(R.id.statusItemName);
            date=itemView.findViewById(R.id.statusItemDate);

        }
    }

}
