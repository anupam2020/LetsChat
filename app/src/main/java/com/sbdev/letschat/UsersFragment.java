package com.sbdev.letschat;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pranavpandey.android.dynamic.toasts.DynamicToast;

import java.util.ArrayList;

public class UsersFragment extends Fragment {

    RecyclerView recyclerView;

    UserAdapter adapter;

    ArrayList<UserModel> arrayList;

    FirebaseAuth firebaseAuth;

    DatabaseReference reference,connectedRef;

    ProgressDialog progressDialog;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView=view.findViewById(R.id.usersRecyclerView);

        arrayList=new ArrayList<>();
        adapter=new UserAdapter(arrayList,getActivity());
        recyclerView.setAdapter(adapter);

        progressDialog=new ProgressDialog(getActivity());

        firebaseAuth=FirebaseAuth.getInstance();

        reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.keepSynced(true);
        connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");

        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog_dots);
        progressDialog.setCancelable(true);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        checkRealTimeNetwork(view);

        usersList();

        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected){
                    if(!((Activity) getContext()).isFinishing())
                    {
                        progressDialog.show();
                        usersList();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                DynamicToast.make(getActivity(),error.getMessage(),3000).show();
            }
        });

    }

    public void usersList()
    {

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                arrayList.clear();

                for(DataSnapshot dataSnapshot : snapshot.getChildren())
                {
                    UserModel userModel=dataSnapshot.getValue(UserModel.class);

                    if(!userModel.getUID().equals(firebaseAuth.getCurrentUser().getUid()))
                    {
                        arrayList.add(userModel);
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

                connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
                connectedRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean connected = snapshot.getValue(Boolean.class);
                        if (connected) {
                            if(!((Activity) getContext()).isFinishing())
                            {
                                progressDialog.show();
                                usersList();
                            }
                        }
                        else {
                            Snackbar.make(view,"Your device is offline!",Snackbar.LENGTH_SHORT).show();
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
        return inflater.inflate(R.layout.fragment_users, container, false);
    }
}