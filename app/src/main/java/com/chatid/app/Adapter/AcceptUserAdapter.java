package com.chatid.app.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.ButtonBarLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chatid.app.MessageActivity;
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

import java.util.List;

public class AcceptUserAdapter extends RecyclerView.Adapter<AcceptUserAdapter.ViewHolder> {
    private Context mContext;
    private List<User> mUsers;
    private boolean ischat;

    FirebaseUser fuser;
    DatabaseReference reference;
    private List<String> usersList;
    private User user;


    public AcceptUserAdapter(Context mContext, List<User> mUsers, boolean ischat){
        this.mUsers=mUsers;
        this.mContext=mContext;
        this.ischat=ischat;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.accept_user_item, parent,false);
        return new AcceptUserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = mUsers.get(position);
        holder.username.setText(user.getUsername());
        if (user.getImageURL().equals("default")){
            holder.profile_image.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(mContext).load(user.getImageURL()).into(holder.profile_image);
        }

        if (ischat){
            if (user.getStatus().equals("online")){
                holder.img_on.setVisibility(View.VISIBLE);
                holder.img_off.setVisibility(View.GONE);
            } else {
                holder.img_on.setVisibility(View.GONE);
                holder.img_off.setVisibility(View.VISIBLE);
            }
        } else {
            holder.img_on.setVisibility(View.GONE);
            holder.img_off.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, MessageActivity.class);
                intent.putExtra("userid", user.getId());
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView username;
        public ImageView profile_image;
        private ImageView img_on;
        private ImageView img_off;
        private ButtonBarLayout btn_accept;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.username = itemView.findViewById(R.id.username);
            this.profile_image = itemView.findViewById(R.id.profile_image);
            this.img_on = itemView.findViewById(R.id.img_on);
            this.img_off = itemView.findViewById(R.id.img_off);
            this.btn_accept = itemView.findViewById(R.id.btn_add_contact);

            btn_accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //remove contact from database
                    fuser = FirebaseAuth.getInstance().getCurrentUser();

                    reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());

                    reference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            user = snapshot.getValue(User.class);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    reference= FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());

                    reference.addValueEventListener(new ValueEventListener(){
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            usersList.clear();
                            for (DataSnapshot snapshot1 : snapshot.getChildren()) {

                                User user = snapshot1.getValue(User.class);
                                for (ContactItem contactItem : user.getContactList()){
                                    if (contactItem.getId().equals(mUsers.get(getAdapterPosition()).getId())){
                                        contactItem.setStatus("Friend");
                                    }
                                }

                                reference.setValue(user);
                                break;
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    //
                    //mUsers.remove(getAdapterPosition());
                    notifyItemRangeChanged(getAdapterPosition(),mUsers.size());
                    notifyItemRemoved(getAdapterPosition());
                }
            });

        }
    }

}
