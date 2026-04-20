package com.example.businessidea.Activity;


import android.Manifest;
import android.annotation.SuppressLint;

import android.content.Intent;
import android.content.SharedPreferences;


import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.businessidea.BaseActivity;
import com.example.businessidea.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SettingActivity extends BaseActivity {
    private LinearLayout notificationOptions, accountOptions, followersFollowingOptions,Saved;
    private ImageView toggleArrow1, toggleArrow2, toggleArrow3,ArrowSetting;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch notificationSwitch;
    private Button changePasswordButton, logoutButton, viewFollowersButton, viewFollowingButton;

    String UserId;
    private static final String PREF_NAME = "AppSettings";
    private static final String KEY_NOTIFICATIONS_ENABLED = "notifications_enabled";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        // Initialize Views
        notificationOptions = findViewById(R.id.notification_options);
        accountOptions = findViewById(R.id.account_options);
        followersFollowingOptions = findViewById(R.id.followers_following_options);
        ArrowSetting = findViewById(R.id.ArrowSetting);
        Saved = findViewById(R.id.Saved);

        toggleArrow1 = findViewById(R.id.toggleArrow1);
        toggleArrow2 = findViewById(R.id.toggleArrow2);
        toggleArrow3 = findViewById(R.id.toggleArrow3);
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        notificationSwitch = findViewById(R.id.notification_switch);
        changePasswordButton = findViewById(R.id.ChangePassword);
        logoutButton = findViewById(R.id.logout_button);
        viewFollowersButton = findViewById(R.id.view_followers_button);
        viewFollowingButton = findViewById(R.id.view_following_button);
        UserId =  FirebaseAuth.getInstance().getCurrentUser().getUid();

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
        // Set Listeners for Toggles
        setToggleListener(toggleArrow1, notificationOptions);
        setToggleListener(toggleArrow2, followersFollowingOptions);
        setToggleListener(toggleArrow3, accountOptions);
        // Initialize SharedPreferences
        Saved.setOnClickListener(v -> startActivity(new Intent(SettingActivity.this, SavedIdeasActivity.class)));
        ArrowSetting.setOnClickListener(v-> startActivity(new Intent(SettingActivity.this, HomeActivity.class)));
        loadNotificationSettings();
        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(UserId);

            userRef.child("EnableNotification").setValue(isChecked).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {

                }
            });
        });




        // Button Listeners
        changePasswordButton.setOnClickListener(v ->
               startActivity(new Intent(SettingActivity.this, ForgetPasswordActivity.class))
        );

        logoutButton.setOnClickListener(v -> {
            SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
            SharedPreferences.Editor editor1 = sharedPreferences.edit();
            editor1.putBoolean("flag", false);
            editor1.apply();
            startActivity(new Intent(SettingActivity.this,LoginActivity.class));
            finish();
        });

        viewFollowersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingActivity.this, TabLayoutActivity.class);
                intent.putExtra("UserId",currentUserId);
                startActivity(intent);
            }
        });

        viewFollowingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingActivity.this, TabLayoutActivity.class);
                intent.putExtra("UserId",currentUserId);
                startActivity(intent);
            }
        });
    }
    private void setToggleListener(ImageView toggleArrow, LinearLayout optionsLayout) {
        toggleArrow.setOnClickListener(v -> {
            if (optionsLayout.getVisibility() == View.VISIBLE) {
                optionsLayout.setVisibility(View.GONE);
                toggleArrow.setImageResource(R.drawable.baseline_arrow_downward_24); // Set down arrow
            } else {
                optionsLayout.setVisibility(View.VISIBLE);
                toggleArrow.setImageResource(R.drawable.baseline_arrow_upward_24); // Set up arrow
            }
        });
    }

    private void loadNotificationSettings() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(UserId);

        userRef.child("EnableNotification").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    boolean isMuted = snapshot.getValue(Boolean.class);
                    notificationSwitch.setChecked(isMuted); // Set switch state
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("NotificationSettings", "Error fetching settings: " + error.getMessage());
            }
        });
    }
}