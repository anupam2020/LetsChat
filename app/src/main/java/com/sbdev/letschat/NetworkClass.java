package com.sbdev.letschat;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pranavpandey.android.dynamic.toasts.DynamicToast;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NetworkClass extends Application {

    public static DatabaseReference connectedRef;

    public DatabaseReference chats,chatsList,statusList,usersList,favList;

    public static FirebaseListener myChats= null;

    public static DataSnapshot dataSnapshotOnSuccessChats,dataSnapshotOnSuccessChatsList,dataSnapshotOnSuccessStatusList,
            dataSnapshotOnSuccessUsersList,dataSnapshotOnSuccessFavList;

    FirebaseAuth firebaseAuth;

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");

        firebaseAuth=FirebaseAuth.getInstance();

        chats=FirebaseDatabase.getInstance().getReference("Chats");
        chatsList=FirebaseDatabase.getInstance().getReference("ChatsList");
        statusList=FirebaseDatabase.getInstance().getReference("StatusDB");
        usersList=FirebaseDatabase.getInstance().getReference("Users");
        favList= FirebaseDatabase.getInstance().getReference("Favorites");

        dataSnapshotOnSuccessChats=null;

        chats.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(myChats!=null) {
                    myChats.onChatDataChange(snapshot);
                }
                dataSnapshotOnSuccessChats=snapshot;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        firebaseAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser()!=null)
                {

                    chatsList.child(firebaseAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if(myChats!=null) {
                                myChats.onChatListDataChange(snapshot);
                            }
                            dataSnapshotOnSuccessChatsList=snapshot;
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });

                }
            }
        });

        statusList.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(myChats!=null) {
                    myChats.onStatusDataChange(snapshot);
                }
                dataSnapshotOnSuccessStatusList=snapshot;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        usersList.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(myChats!=null) {
                    myChats.onUserDataChange(snapshot);
                }
                dataSnapshotOnSuccessUsersList=snapshot;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        favList.child(firebaseAuth.getCurrentUser().getUid())
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if(myChats!=null) {
                        myChats.onFavChatsChange(snapshot);
                    }
                    dataSnapshotOnSuccessFavList=snapshot;

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });



    }

    public interface FirebaseListener
    {
        void onChatDataChange(DataSnapshot snapshot);
        void onChatListDataChange(DataSnapshot snapshot);
        void onStatusDataChange(DataSnapshot snapshot);
        void onUserDataChange(DataSnapshot snapshot);
        void onFavChatsChange(DataSnapshot snapshot);

    }

}
