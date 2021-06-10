package com.chatid.app.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chatid.app.Adapter.AddUserAdapter;
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

public class AddContactFragment extends Fragment {

    private RecyclerView recyclerView;
    private AddUserAdapter addUserAdapter;
    private List<User> mUsers;
    FirebaseUser fuser;
    DatabaseReference reference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_contact, container, false);
        recyclerView = view.findViewById(R.id.add_contact_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fuser= FirebaseAuth.getInstance().getCurrentUser();

        mUsers = new ArrayList<>();

        reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot snapshot1 : snapshot.getChildren()){
                        User user = snapshot1.getValue(User.class);
                        if (!user.getId().equals(fuser.getUid())){
                            boolean lock = false;
                            for (ContactItem contactItem : user.getContactList()){
                                if (contactItem.getStatus().equals("Friend")){
                                    lock = true;
                                }
                                else if (contactItem.getStatus().equals("Sent")){
                                    lock = true;
                                }
                                else if (contactItem.getStatus().equals("Request")){
                                    lock = true;
                                }
                            }
                            if (!lock){
                                mUsers.add(user);
                            }
                        }
                    }

                    addUserAdapter = new AddUserAdapter(getContext(),mUsers, true);
                    recyclerView.setAdapter(addUserAdapter);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        return view;
    }
}