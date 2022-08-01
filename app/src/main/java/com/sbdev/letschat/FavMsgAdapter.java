package com.sbdev.letschat;

import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.ArrayList;
import java.util.Objects;

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
        if(firebaseAuth.getCurrentUser()!=null)
        {
            if(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid().equals(favMsgModel.getSender()))
            {
                holder.nameText.setText("You -> "+favMsgModel.getReceiverName());
                holder.layout.setBackgroundResource(R.drawable.chat_bg_left_fav_msg_user);
                holder.msgTime.setTextColor(context.getResources().getColor(R.color.light_white_right));
                holder.msgText.setTextColor(Color.WHITE);
            }
            else
            {
                holder.nameText.setText(favMsgModel.getSenderName()+" -> You");
                holder.layout.setBackgroundResource(R.drawable.chat_bg_left_fav_msg_friend);
                holder.msgTime.setTextColor(context.getResources().getColor(R.color.light_grey_left));
                holder.msgText.setTextColor(Color.BLACK);
            }
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

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(favMsgModel.getImgURI()!=null)
                {

                    ZoomageView profile=new ZoomageView(context);
                    profile.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    profile.setAdjustViewBounds(true);

                    Glide.with(context)
                            .load(favMsgModel.getImgURI())
                            .placeholder(R.drawable.blue_img_gallery)
                            .error(R.drawable.blue_img_gallery)
                            .into(profile);

                    AlertDialog.Builder builder = new AlertDialog.Builder(context).setView(profile)
                            .setPositiveButton("", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    downloadURL(favMsgModel.getImgURI());
                                }
                            })
                            .setPositiveButtonIcon(context.getDrawable(R.drawable.download_black_new))
                            .setNeutralButton("",new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .setNeutralButtonIcon(context.getDrawable(R.drawable.return_black_new));
                    builder.show();

                }

            }
        });

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

    private void downloadURL(String url) {

        DatabaseReference serverTimeRef = FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset");
        serverTimeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                long offset = snapshot.getValue(Long.class);
                long estimatedServerTimeMs = System.currentTimeMillis() + offset;

                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

                DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, estimatedServerTimeMs + ".jpg");

                downloadManager.enqueue(request);

                DynamicToast.make(context,"Downloading file!",3000).show();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

}
