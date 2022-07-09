package com.sbdev.letschat;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.pranavpandey.android.dynamic.toasts.DynamicToast;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class ChatFragment extends Fragment {

    RecyclerView recyclerView;

    UserAdapter adapter;

    ArrayList<UserModel> mUsers;

    ArrayList<ChatsListModel> userList;

    FirebaseAuth firebaseAuth;

    DatabaseReference reference,usersRef;

    ProgressDialog progressDialog;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView=view.findViewById(R.id.chatsRecycler);

        mUsers=new ArrayList<>();
        adapter=new UserAdapter(mUsers,getActivity());
        recyclerView.setAdapter(adapter);

        progressDialog=new ProgressDialog(getActivity());

        userList=new ArrayList<>();

        firebaseAuth=FirebaseAuth.getInstance();

        usersRef= FirebaseDatabase.getInstance().getReference("Users").child(firebaseAuth.getCurrentUser().getUid());
        usersRef.keepSynced(true);
        reference= FirebaseDatabase.getInstance().getReference("ChatsList").child(firebaseAuth.getCurrentUser().getUid());
        reference.keepSynced(true);

        //connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");

        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog_dots);
        progressDialog.setCancelable(true);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        checkRealTimeNetwork(view);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                userList.clear();

                for(DataSnapshot dataSnapshot : snapshot.getChildren())
                {
                    ChatsListModel chatsListModel=dataSnapshot.getValue(ChatsListModel.class);
                    userList.add(chatsListModel);
                }

                chatsList();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        NetworkClass.connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected){
                    usersRef.child("status").setValue("Online");
                    //usersRef.child("status").onDisconnect().setValue("Offline");

                    usersRef.child("status").onDisconnect().setValue("Offline").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful())
                            {
                                DatabaseReference serverTimeRef = FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset");
                                serverTimeRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                                        long offset = snapshot.getValue(Long.class);
                                        long estimatedServerTimeMs = System.currentTimeMillis() + offset;

                                        Timestamp timestamp=new Timestamp(estimatedServerTimeMs);
                                        Date date=timestamp;
                                        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("MMM dd, hh:mm a");
                                        String strDateTime=simpleDateFormat.format(date);

                                        usersRef.child("status").onDisconnect().setValue(strDateTime);

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }

                        }
                    });

                    chatsList();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                DynamicToast.make(getActivity(),error.getMessage(),3000).show();
            }
        });


    }

    private void chatsList()
    {

        reference=FirebaseDatabase.getInstance().getReference("Users");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                mUsers.clear();

                for(DataSnapshot dataSnapshot : snapshot.getChildren())
                {
                    UserModel userModel=dataSnapshot.getValue(UserModel.class);

                    for(ChatsListModel chatsListModel : userList)
                    {
                        if(userModel.getUID().equals(chatsListModel.getUID()))
                        {
                            mUsers.add(userModel);
                        }
                    }
                }

                adapter.notifyDataSetChanged();
                progressDialog.dismiss();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void checkRealTimeNetwork(View view)
    {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                NetworkClass.connectedRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean connected = snapshot.getValue(Boolean.class);
                        if (connected) {
                            chatsList();
                        }
                        else {
                            progressDialog.dismiss();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressDialog.dismiss();
                        DynamicToast.make(getActivity(),error.getMessage(),3000).show();
                    }
                });

            }
        },2000);
    }



    private boolean isNetworkConnected()
    {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }
}