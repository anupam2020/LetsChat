package com.sbdev.letschat;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class ChatFragment extends Fragment {

    RecyclerView recyclerView;

    UserAdapter adapter;

    ArrayList<UserModel> mUsers;

    ArrayList<ChatsListModel> userList;

    FirebaseAuth firebaseAuth;

    DatabaseReference reference;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView=view.findViewById(R.id.chatsRecycler);

        mUsers=new ArrayList<>();
        adapter=new UserAdapter(mUsers,getActivity());
        recyclerView.setAdapter(adapter);

        userList=new ArrayList<>();

        firebaseAuth=FirebaseAuth.getInstance();

        reference= FirebaseDatabase.getInstance().getReference("ChatsList").child(firebaseAuth.getCurrentUser().getUid());

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
                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
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

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }
}