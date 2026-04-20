package com.example.businessidea.Activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;

import android.view.View;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.businessidea.BaseActivity;
import com.example.businessidea.Module.Idea;
import com.example.businessidea.R;

import com.example.businessidea.RecycleView.IdeaAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

import java.util.Collections;
import java.util.List;

public class SavedIdeasActivity extends BaseActivity {
    private RecyclerView recyclerView;
    private TextView noIdeasTextView;
    ImageView ArrowSetting;
    private DatabaseReference saveIdeasRef;
    private DatabaseReference publicIdeasRef;
    int likeCount,commentCount;
    private List<Idea> savedIdeasList = new ArrayList<>();
    private IdeaAdapter savedIdeasAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_ideas);
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
        // Initialize views
        recyclerView = findViewById(R.id.recycler_saved_ideas);
        noIdeasTextView = findViewById(R.id.gg);
        ArrowSetting = findViewById(R.id.ArrowSetting);
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        // Initialize Firebase references
        saveIdeasRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUserId).child("SavedIdeas");
        publicIdeasRef = FirebaseDatabase.getInstance().getReference("PublicIdeas");

        ArrowSetting.setOnClickListener(v -> {
            startActivity(new Intent(SavedIdeasActivity.this,SettingActivity.class));
            finish();
        });
        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        savedIdeasAdapter = new IdeaAdapter(savedIdeasList,this);
        fetchSavedIdeas();
        recyclerView.setAdapter(savedIdeasAdapter);

    }

    private void fetchSavedIdeas() {
        // Fetch saved idea IDs from SaveIdeas
        saveIdeasRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot ideaSnapshot : snapshot.getChildren()) {
                        String ideaId = ideaSnapshot.getValue(String.class); // Get the idea ID (value)

                        if (ideaId != null) {
                            fetchIdeaDetails(ideaId); // Fetch details from PublicIdeas
                        }
                    }
                } else {
                  showEmptyState();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SavedIdeasActivity.this, "Error fetching saved ideas: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchIdeaDetails(String ideaId) {
        publicIdeasRef.child(ideaId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot ideaSnapshot) {
                    String content = ideaSnapshot.child("content").getValue(String.class);
                    String imageUrl = ideaSnapshot.child("imageUrl").getValue(String.class);
                    String UserName = ideaSnapshot.child("UserName").getValue(String.class);
                String IdeasTitle = ideaSnapshot.child("titleIdeas").getValue(String.class);
                    String UserImage = ideaSnapshot.child("UserImage").getValue(String.class);
                    String UserId = ideaSnapshot.child("UserId").getValue(String.class);
                    long timestamp = ideaSnapshot.child("timestamp").getValue(long.class);
                    DataSnapshot likesSnapshot = ideaSnapshot.child("likes");
                    DataSnapshot commentsSnapshot = ideaSnapshot.child("comments");
                    commentCount = 0;
                    if (commentsSnapshot.exists()){
                        commentCount = (int) commentsSnapshot.getChildrenCount();
                    }
                    List<String> likesList = new ArrayList<>();
                    likeCount = 0;
                    if (likesSnapshot.exists()) { // Check if likes exist
                        likeCount = (int) likesSnapshot.getChildrenCount(); // Count total likes
                        for (DataSnapshot likeSnapshot : likesSnapshot.getChildren()) {
                            String userId = likeSnapshot.getKey(); // User ID of the person who liked
                            likesList.add(userId);
                        }
                    }

                    if (content != null  && UserName != null && UserImage != null) {
                        // Create a new Idea object with the data
                        savedIdeasList.add(new Idea(UserName, UserImage, imageUrl, content, likesList, ideaId,UserId,likeCount,commentCount,timestamp,IdeasTitle)); // Pass ideaId
                        showRecyclerView();
                    }
                savedIdeasAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showRecyclerView() {
        recyclerView.setVisibility(View.VISIBLE);
        noIdeasTextView.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        recyclerView.setVisibility(View.GONE);
        noIdeasTextView.setVisibility(View.VISIBLE);
    }
}