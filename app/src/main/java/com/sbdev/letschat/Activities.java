package com.sbdev.letschat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Activities extends AppCompatActivity {

    ImageView back;

    RecyclerView recyclerView;

    ActivitiesAdapter adapter;

    ArrayList<ActivitiesModel> arrayList;

    FirebaseAuth firebaseAuth;

    DatabaseReference reference,statusRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activities);

        back=findViewById(R.id.activitiesBack);
        recyclerView=findViewById(R.id.activitiesRecycler);

        arrayList=new ArrayList<>();
        adapter=new ActivitiesAdapter(arrayList,this);
        recyclerView.setAdapter(adapter);

        firebaseAuth=FirebaseAuth.getInstance();

        reference= FirebaseDatabase.getInstance().getReference("Users");

        statusRef=FirebaseDatabase.getInstance().getReference("Status");

        statusRef.child(firebaseAuth.getCurrentUser().getUid())
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    arrayList.clear();

                    for(DataSnapshot dataSnapshot : snapshot.getChildren())
                    {
                        ActivitiesModel activitiesModel=dataSnapshot.getValue(ActivitiesModel.class);
                        arrayList.add(activitiesModel);
                    }

                    if(arrayList.size()>0)
                    {
                        Collections.sort(arrayList, new Comparator<ActivitiesModel>() {
                            @Override
                            public int compare(ActivitiesModel o1, ActivitiesModel o2) {
                                return o2.getTimestamp().compareTo(o1.getTimestamp());
                            }
                        });
                    }

                    adapter.notifyDataSetChanged();

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

    }
}