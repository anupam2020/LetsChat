package com.sbdev.letschat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    FirebaseAuth firebaseAuth;

    public static final int MSG_LEFT=0;
    public static final int MSG_RIGHT=1;

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

        MessageModel messageModel=arrayList.get(position);

        holder.msg.setText(messageModel.text);

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        TextView msg;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            msg=itemView.findViewById(R.id.showMsg);

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
