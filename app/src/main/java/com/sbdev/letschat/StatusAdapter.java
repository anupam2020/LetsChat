package com.sbdev.letschat;

import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jsibbold.zoomage.ZoomageView;
import com.pranavpandey.android.dynamic.toasts.DynamicToast;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class StatusAdapter extends RecyclerView.Adapter<StatusAdapter.StatusViewHolder> {

    FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();

    DatabaseReference statusRef=FirebaseDatabase.getInstance().getReference("Status");

    String myName,myProfilePic;

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


        holder.img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users")
                        .child(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid());

                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        UserModel userModel=snapshot.getValue(UserModel.class);
                        assert userModel != null;
                        myName=userModel.getName();
                        myProfilePic=userModel.getProfilePic();

                        String key=statusRef.push().getKey();
                        uploadStatus(myName,myProfilePic,key,statusModel.getUid(),"viewStatus");

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });



                ZoomageView profile=new ZoomageView(context);
                profile.setScaleType(ImageView.ScaleType.CENTER_CROP);
                profile.setAdjustViewBounds(true);

                Glide.with(context)
                        .load(statusModel.getImageURL())
                        .placeholder(R.drawable.blue_img_gallery)
                        .error(R.drawable.blue_img_gallery)
                        .into(profile);

                AlertDialog.Builder builder = new AlertDialog.Builder(context)
                    .setView(profile)
                    .setNegativeButton("",new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            Intent intent=new Intent(context,MessageActivity.class);
                            intent.putExtra("friendUID",statusModel.getUid());
                            context.startActivity(intent);

                        }
                    })
                    .setNegativeButtonIcon(context.getDrawable(R.drawable.chat_black_new))
                    .setNeutralButton("",new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users")
                                    .child(firebaseAuth.getCurrentUser().getUid());

                            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {


                                    UserModel userModel=snapshot.getValue(UserModel.class);
                                    assert userModel != null;
                                    myName=userModel.getName();
                                    myProfilePic=userModel.getProfilePic();
                                    String key=statusRef.push().getKey();
                                    uploadStatus(myName,myProfilePic,key,statusModel.getUid(),"downloadStatus");

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    })
                    .setNeutralButtonIcon(context.getDrawable(R.drawable.download_black_new));

                builder.show();

            }
        });


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

    public void uploadStatus(String name, String profile, String key, String uid, String status)
    {
        if(status.equals("downloadStatus"))
        {
            downloadURL(profile, name);
        }

        DatabaseReference serverTimeRef = FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset");
        serverTimeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                long offset = snapshot.getValue(Long.class);
                long estimatedServerTimeMs = System.currentTimeMillis() + offset;

                Timestamp timestamp=new Timestamp(estimatedServerTimeMs);
                Date date=timestamp;
                SimpleDateFormat simpleDateFormat=new SimpleDateFormat("MMMM dd, yyyy - hh:mm a");
                String strDateTime=simpleDateFormat.format(date);

                HashMap map=new HashMap();
                map.put("name",name);
                map.put("profilePic",profile);
                map.put("UID",uid);
                map.put("status",status);
                map.put("key",key);
                map.put("dateTime",strDateTime);
                map.put("timestamp",Long.toString(estimatedServerTimeMs));
                statusRef.child(uid).child(key).setValue(map);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void downloadURL(String url, String name) {

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name+"_status" + ".jpg");

        downloadManager.enqueue(request);

        DynamicToast.make(context, "Downloading status!", context.getResources().getDrawable(R.drawable.download_1),
                context.getResources().getColor(R.color.white), context.getResources().getColor(R.color.black), 3000).show();

    }

}
