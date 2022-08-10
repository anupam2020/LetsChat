package com.sbdev.letschat;

import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
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

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();

    DatabaseReference statusRef=FirebaseDatabase.getInstance().getReference("Status");

    String myName,myProfilePic;

    ArrayList<UserModel> arrayList;
    Context context;
    String lastMessage, strTime, lastMsgURI;

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

        holder.profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users")
                        .child(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid());

                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        UserModel userModel=snapshot.getValue(UserModel.class);
                        assert userModel != null;
                        myName=userModel.getName();
                        myProfilePic=userModel.getProfilePic();

                        String key=statusRef.push().getKey();
                        uploadStatus(myName,myProfilePic,key,model.getUID(),"viewProfile");

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });



                ZoomageView profile=new ZoomageView(context);
                profile.setScaleType(ImageView.ScaleType.CENTER_CROP);
                profile.setAdjustViewBounds(true);

                Glide.with(context)
                        .load(model.getProfilePic())
                        .placeholder(R.drawable.item_user)
                        .error(R.drawable.item_user)
                        .into(profile);

                AlertDialog.Builder builder = new AlertDialog.Builder(context)
                        .setView(profile)
                        .setNegativeButton("",new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                Intent intent=new Intent(context,MessageActivity.class);
                                intent.putExtra("friendUID",model.getUID());
                                context.startActivity(intent);

                            }
                        })
                        .setNegativeButtonIcon(context.getDrawable(R.drawable.chat_black_new))
                        .setPositiveButton("", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                Intent intent=new Intent(context,FriendProfile.class);
                                intent.putExtra("friendUID",model.getUID());
                                context.startActivity(intent);

                            }
                        })
                        .setPositiveButtonIcon(context.getDrawable(R.drawable.info1_black_new))
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
                                        uploadStatus(myName,myProfilePic,key,model.getUID(),"downloadProfile");

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

        holder.name.setText(model.getName());

        if(model.getStatus().equals("Online"))
        {
            holder.statusIcon.setImageResource(R.drawable.circle_online);
        }
        else
        {
            holder.statusIcon.setImageResource(R.drawable.circle_offline);
        }

        lastMsg(model.getUID(), holder.lastMsg, holder.msgIcon, model.getStatus(), holder.time, holder.imgSentIcon, holder.imgInfo);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //DynamicToast.make(context,"status: "+model.getStatus(),3000).show();

                Intent intent=new Intent(context,MessageActivity.class);
                intent.putExtra("friendUID",model.getUID());
                intent.putExtra("friendToken",model.getToken());
                intent.putExtra("friendName",model.getName());
                intent.putExtra("friendPic",model.getProfilePic());
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
        ImageView statusIcon,msgIcon,imgSentIcon,imgInfo;
        TextView name,lastMsg,time;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);

            profile=itemView.findViewById(R.id.userProfilePic);
            name=itemView.findViewById(R.id.userName);
            statusIcon=itemView.findViewById(R.id.userStatusIcon);
            lastMsg=itemView.findViewById(R.id.userLastMsg);
            msgIcon=itemView.findViewById(R.id.userMsgIcon);
            time=itemView.findViewById(R.id.userTime);
            imgSentIcon=itemView.findViewById(R.id.userImgSent);
            imgInfo=itemView.findViewById(R.id.userInfo);

        }
    }

    public void lastMsg(String friendUID, TextView last_msg, ImageView msg_icon, String status, TextView time, ImageView imageIconSent, ImageView info)
    {

        lastMessage="";
        lastMsgURI="";

        FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();

        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Chats");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                last_msg.setTypeface(null);
                for(DataSnapshot dataSnapshot : snapshot.getChildren())
                {

                    MessageModel messageModel=dataSnapshot.getValue(MessageModel.class);
                    assert messageModel != null;
                    if(messageModel.getTime()!=null){

                        strTime=messageModel.getTime();
                        strTime=strTime.substring(0,strTime.indexOf(',')+1) + strTime.substring(strTime.indexOf('-')+1);

                        if(firebaseAuth.getCurrentUser()!=null)
                        {
                            if(messageModel.getSender().equals(friendUID) && messageModel.getReceiver().equals(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid()))
                            {
                                lastMessage=messageModel.getText();
                                lastMsgURI=messageModel.getImgURI();
                                time.setText(strTime);
                                msg_icon.setImageResource(R.drawable.curve_down_arrow);
                                if(messageModel.getIsSeen()==0)
                                {
                                    last_msg.setTypeface(last_msg.getTypeface(),Typeface.BOLD);
                                    time.setTypeface(last_msg.getTypeface(),Typeface.BOLD);
                                    last_msg.setTextColor(context.getResources().getColor(R.color.black));
                                    info.setVisibility(View.VISIBLE);
                                    time.setTextColor(context.getResources().getColor(R.color.black));
                                }
                                else
                                {
                                    info.setVisibility(View.GONE);
                                    time.setTypeface(null,Typeface.NORMAL);
                                    time.setTextColor(context.getResources().getColor(R.color.light_grey));
                                }
                            }
                        }

                        if(firebaseAuth.getCurrentUser()!=null)
                        {
                            if(messageModel.getSender().equals(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid()) && messageModel.getReceiver().equals(friendUID))
                            {
                                lastMessage=messageModel.getText();
                                lastMsgURI=messageModel.getImgURI();
                                time.setText(strTime);
                                msg_icon.setImageResource(R.drawable.curve_up_arrow);
                            }
                        }

                    }

                }
                if(!lastMessage.trim().isEmpty())
                {
                    last_msg.setText(lastMessage);
                    msg_icon.setVisibility(View.VISIBLE);
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
                    time.setText("");
                }

                if(lastMessage.equals("--Image--") && (lastMsgURI!=null))
                {
                    last_msg.setVisibility(View.GONE);
                    imageIconSent.setVisibility(View.VISIBLE);
                }
                else
                {
                    last_msg.setVisibility(View.VISIBLE);
                    imageIconSent.setVisibility(View.GONE);
                }

                lastMessage="";
                strTime="";
                lastMsgURI="";

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    private void downloadURL(String url, String name) {

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name+"_dp" + ".jpg");

        downloadManager.enqueue(request);

        DynamicToast.make(context, "Downloading profile picture!", context.getResources().getDrawable(R.drawable.download_1),
                context.getResources().getColor(R.color.white), context.getResources().getColor(R.color.black), 3000).show();

    }

    public void uploadStatus(String name, String profile, String key, String uid, String status)
    {
        if(status.equals("downloadProfile"))
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

}
