package com.example.businessidea.Activity;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.businessidea.BaseActivity;
import com.example.businessidea.Module.Notification;
import com.example.businessidea.R;
import com.example.businessidea.RecycleView.NotificationAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotificationActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private NotificationAdapter notificationAdapter;
    private List<Notification> notificationList;
    FirebaseAuth auth;
    FirebaseUser user;
    TextView NotificationText;
    String CurrentUser;
    ImageView back_button;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        CurrentUser = user.getUid();
        NotificationText = findViewById(R.id.NotificationText);
        back_button = findViewById(R.id.back_button);
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
        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerViewNotifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NotificationActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });
        // Initialize the notification list and adapter
        notificationList = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(notificationList,this);

        // Set adapter to RecyclerView
        recyclerView.setAdapter(notificationAdapter);

        // Fetch notifications from Firebase
        fetchNotifications();
    }

    private void fetchNotifications() {
        // Firebase Database reference to fetch notifications
        DatabaseReference notificationsRef = FirebaseDatabase.getInstance().getReference("Users").child(CurrentUser).child("Notifications");

        notificationsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                notificationList.clear(); // Clear the existing notifications

                // Loop through each notification and add it to the list
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Notification notification = snapshot.getValue(Notification.class);
                    if (notification != null) {
                        notificationList.add(notification); // Add the notification to the list
                    }
                }

                // Notify the adapter that data has been updated
                Collections.reverse(notificationList);
                notificationAdapter.notifyDataSetChanged();
                if (notificationList.isEmpty()) {
                    NotificationText.setVisibility(View.VISIBLE); // Show the message
                    recyclerView.setVisibility(View.GONE); // Hide the RecyclerView
                } else {
                    NotificationText.setVisibility(View.GONE); // Hide the message
                    recyclerView.setVisibility(View.VISIBLE); // Show the RecyclerView
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(NotificationActivity.this, "Failed to load notifications", Toast.LENGTH_SHORT).show();
            }
        });
    }


}
