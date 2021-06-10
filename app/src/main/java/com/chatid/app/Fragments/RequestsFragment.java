package com.chatid.app.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chatid.app.Adapter.AcceptUserAdapter;
import com.chatid.app.Adapter.UserAdapter;
import com.chatid.app.Model.Chat;
import com.chatid.app.Model.ContactItem;
import com.chatid.app.Model.User;
import com.chatid.app.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class RequestsFragment extends Fragment {

    private RecyclerView recyclerView;
    private AcceptUserAdapter userAdapter;
    private List<User> mUsers;
    FirebaseUser fuser;
    DatabaseReference reference;
    private List<String> usersList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_requests, container, false);
        recyclerView = view.findViewById(R.id.contact_request_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fuser= FirebaseAuth.getInstance().getCurrentUser();

        usersList=new ArrayList<>();

        reference= FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()){

                    User user = snapshot1.getValue(User.class);
                    for(ContactItem contactItem : user.getContactList()){
                        if(contactItem.getStatus().equals("Request"))
                        usersList.add(contactItem.getId());
                    }
                    break;
                }
                readContacts();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        return view;
    }

    private void readContacts(){
        mUsers=new ArrayList<>();
        reference=FirebaseDatabase.getInstance().getReference("Users");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<User> mUsersLocal = new ArrayList<>();

                for(DataSnapshot snapshot1 : snapshot.getChildren()){
                    User user= snapshot1.getValue(User.class);

                    //display 1 user from chats
                    for (String id : usersList){
                        if (user.getId().equals(id)){
                            if (mUsersLocal.size()!=0){
                                for(User user1 : mUsersLocal){
                                    if(!user.getId().equals(user1.getId())){
                                        mUsersLocal.add(user);break;
                                    }
                                }
                            }else{
                                mUsersLocal.add(user);
                            }
                        }
                    }
                    mUsers.clear();
                    mUsers.addAll(mUsersLocal);


                }

                userAdapter = new AcceptUserAdapter(getContext(),mUsers, true);
                recyclerView.setAdapter(userAdapter);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
}