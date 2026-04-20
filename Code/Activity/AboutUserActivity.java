package com.example.businessidea.Activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.businessidea.BaseActivity;
import com.example.businessidea.R;
import com.example.businessidea.fragment.ProfileFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AboutUserActivity extends BaseActivity {
ImageView about_close,Image_profile_about;
TextView about_user_name;
Button Add_btn;
EditText about_post_content;
FirebaseAuth auth;
DatabaseReference mDatabase;
String UserId;
FirebaseUser firebaseUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_user);

        about_close = findViewById(R.id.about_close);
        Image_profile_about = findViewById(R.id.Image_profile_about);
        about_user_name = findViewById(R.id.about_user_name);
        Add_btn = findViewById(R.id.Add_btn);
        about_post_content = findViewById(R.id.about_post_content);

        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            getWindow().getDecorView().setSystemUiVisibility(0); // Default light icons
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.back)); // Black status bar
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.back)); // Black navigation bar
        } else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR); // Dark icons
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white)); // White status bar
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.white)); // Black navigation bar
        }
        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        assert firebaseUser != null;
        UserId = firebaseUser.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference("Users").child(UserId);
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String profilePicUrl = snapshot.child("profilePic").getValue(String.class);
                    String Name = snapshot.child("username").getValue(String.class);
                    String about = snapshot.child("about").getValue(String.class);
                    if (profilePicUrl != null){
                        loadImage(profilePicUrl);
                    }
                    if (!about.equals("")){
                        about_post_content.setText(about);
                    }
                    about_user_name.setText(Name);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        about_close.setOnClickListener(v -> startActivity(new Intent(AboutUserActivity.this, ProfileFragment.class)));

        Add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatabase.child("about").setValue(about_post_content.getText().toString());
                startActivity(new Intent(AboutUserActivity.this, ProfileFragment.class));
                finish();
            }
        });
    }
    private void loadImage(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(getApplicationContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.baseline_person_24)
                    .transform(new CircleCrop())
                    .error(R.drawable.baseline_person_24)
                    .into(Image_profile_about);
        } else {
            Image_profile_about.setImageResource(R.drawable.baseline_person_24);
        }
    }
}