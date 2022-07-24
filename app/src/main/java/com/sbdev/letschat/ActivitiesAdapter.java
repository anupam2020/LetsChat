package com.sbdev.letschat;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ActivitiesAdapter extends RecyclerView.Adapter<ActivitiesAdapter.ActivitiesViewHolder> {

    FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
    DatabaseReference statusRef= FirebaseDatabase.getInstance().getReference("Status");

    ArrayList<ActivitiesModel> arrayList;
    Context context;

    public ActivitiesAdapter(ArrayList<ActivitiesModel> arrayList, Context context) {
        this.arrayList = arrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public ActivitiesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ActivitiesViewHolder(LayoutInflater.from(context).inflate(R.layout.activities_item_layout,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ActivitiesViewHolder holder, int position) {

        ActivitiesModel activitiesModel=arrayList.get(holder.getAdapterPosition());

        Glide.with(context)
                .load(activitiesModel.getProfilePic())
                .into(holder.circleProfile);

        if(activitiesModel.getStatus().equals("view"))
        {
            holder.name.setText(activitiesModel.getName()+" viewed your profile picture.");
        }
        else
        {
            holder.name.setText(activitiesModel.getName()+" downloaded your profile picture.");
        }

        holder.dateTime.setText(activitiesModel.getDateTime());

        holder.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PopupMenu popupMenu=new PopupMenu(context,v);
                popupMenu.getMenuInflater().inflate(R.menu.delete,popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        if(item.getItemId()==R.id.deleteItem)
                        {
                            arrayList.remove(holder.getAdapterPosition());
                            notifyItemRemoved(holder.getAdapterPosition());

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    statusRef.child(activitiesModel.getUID())
                                            .child(activitiesModel.getKey())
                                            .removeValue();

                                }
                            },500);

                        }

                        return false;
                    }
                });

                popupMenu.show();

            }
        });

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class ActivitiesViewHolder extends RecyclerView.ViewHolder {

        CircleImageView circleProfile;
        TextView name,dateTime;
        ImageView more;

        public ActivitiesViewHolder(@NonNull View itemView) {
            super(itemView);

            circleProfile=itemView.findViewById(R.id.activitiesCircleImg);
            name=itemView.findViewById(R.id.activitiesName);
            more=itemView.findViewById(R.id.activitiesMore);
            dateTime=itemView.findViewById(R.id.activitiesDataTime);

        }
    }

}
