package com.chatid.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chatid.app.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    CircleImageView image_profile;
    ImageView id_image_profile;
    TextView username,username2,email;

    TextView verified,notVerified;

    DatabaseReference reference;
    FirebaseUser fuser;

    String userId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        image_profile = findViewById(R.id.profile_image);
        id_image_profile = findViewById(R.id.profile_id);
        username = findViewById(R.id.username);
        username2 = findViewById(R.id.username_big);
        email = findViewById(R.id.email);
        verified = findViewById(R.id.verified);
        notVerified = findViewById(R.id.not_verified);

        Bundle bundle = getIntent().getExtras();
        userId = bundle.getString("id");

        fuser = FirebaseAuth.getInstance().getCurrentUser();



        reference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                username.setText(user.getUsername());
                username2.setText(user.getUsername());
                email.setText(user.getEmail());
                if (user.getImageURL().equals("default")){
                    image_profile.setImageResource(R.mipmap.ic_launcher);
                } else {
                    Glide.with(getApplicationContext()).load(user.getImageURL()).into(image_profile);
                }
                if (user.getIdImageURL().equals("default")){
                    id_image_profile.setImageResource(R.mipmap.ic_launcher);
                    verified.setVisibility(View.INVISIBLE);
                    notVerified.setVisibility(View.VISIBLE);
                } else {
                    Glide.with(getApplicationContext()).load(user.getIdImageURL()).into(id_image_profile);
                    verified.setVisibility(View.VISIBLE);
                    notVerified.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}