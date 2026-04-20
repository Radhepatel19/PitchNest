package com.example.businessidea.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.businessidea.BaseActivity;
import com.example.businessidea.R;
import com.example.businessidea.fragment.HomeFragment;
import com.example.businessidea.fragment.MessageFragment;
import com.example.businessidea.fragment.ProfileFragment;
import com.example.businessidea.fragment.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class HomeActivity extends BaseActivity {
    private BottomNavigationView bottomNavigationView;
    private DrawerLayout drawerLayout;
    private FragmentManager fragmentManager;
    NavigationView nav_view;
    private DatabaseReference mDatabase;
    FirebaseAuth mAuth;
    FirebaseDatabase database;
    FirebaseUser firebaseUser;
    ImageView image;
    String UserId;
    TextView UserName,HEmail,City,Country;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Set status bar color
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
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        nav_view = findViewById(R.id.nav_view);
        drawerLayout = findViewById(R.id.drawer_layout); // Correct reference to DrawerLayout
        fragmentManager = getSupportFragmentManager();

        Intent intent;
        intent = getIntent();
        boolean fragmentSet = false;
        // Check if the Intent specifies navigation to ProfileFragment
        if (intent != null && "Profile".equals(intent.getStringExtra("fragment"))) {
            String username = intent.getStringExtra("userId");
            boolean isCurrentUser = intent.getBooleanExtra("isCurrentUser", username.equals(UserId));

            // Load ProfileFragment with arguments
            ProfileFragment profileFragment = ProfileFragment.newInstance(username, isCurrentUser);
            loadFragment(profileFragment, false);
            bottomNavigationView.setSelectedItemId(R.id.nav_profile);
            fragmentSet = true;
        }

        // Set BottomNavigationView listener
        bottomNavigationView.setOnNavigationItemSelectedListener(this::handleBottomNavClick);
        View Header = nav_view.getHeaderView(0);
        image = Header.findViewById(R.id.H_image);
         UserName = Header.findViewById(R.id.H_username);
         HEmail = Header.findViewById(R.id.H_Email);
         City = Header.findViewById(R.id.H_city);
         Country = Header.findViewById(R.id.H_county);
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        UserId = firebaseUser.getUid();
        database = FirebaseDatabase.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("Users").child(UserId);
         mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String profilePicUrl = snapshot.child("profilePic").getValue(String.class);
                    String Name = snapshot.child("username").getValue(String.class);
                    String Email = snapshot.child("mail").getValue(String.class);
                    String city = snapshot.child("city").getValue(String.class);
                    String country = snapshot.child("country").getValue(String.class);
                    if (profilePicUrl != null){
                        loadImage(profilePicUrl);
                    }
                    UserName.setText(Name);
                    City.setText(city+",");
                    Country.setText(country);
                     HEmail.setText(Email);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        nav_view.setNavigationItemSelectedListener(item -> {
            int itemid = item.getItemId();
            if (itemid == R.id.Logout){
                SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
                SharedPreferences.Editor editor1 = sharedPreferences.edit();
                editor1.putBoolean("flag", false);
                editor1.apply();
                startActivity(new Intent(HomeActivity.this,LoginActivity.class));
            }else if (itemid == R.id.Setting){
                startActivity(new Intent(HomeActivity.this,SettingActivity.class));
            } else if (itemid == R.id.Profile) {
                drawerLayout.closeDrawer(nav_view);
                loadFragment(new ProfileFragment(),false);
            } else if (itemid == R.id.AboutUs) {
                startActivity(new Intent(HomeActivity.this,AboutUsActivity.class));
            }else if (itemid == R.id.SaveIdeas) {
                startActivity(new Intent(HomeActivity.this,SavedIdeasActivity.class));
            } else if (itemid == R.id.FAQs) {
                startActivity(new Intent(HomeActivity.this, Faqs.class));
            }
            return true;
        });
        // Set initial fragment (e.g., HomeFragment)
        if (!fragmentSet && savedInstanceState == null) {
            loadFragment(new HomeFragment(), false);
        } // Set default fragment
    }

    private boolean handleBottomNavClick(@NonNull MenuItem item) {
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (item.getItemId() == R.id.nav_home) {
                    loadFragment(new HomeFragment(), false);
                } else if (item.getItemId() == R.id.nav_trending) {
                    loadFragment(new SearchFragment(), false);
                } else if (item.getItemId() == R.id.nav_add) {
                                    startActivity(new Intent(HomeActivity.this, AddIdeasActivity.class));
                } else if (item.getItemId() == R.id.nav_messages) {
                    loadFragment(new MessageFragment(), false);
                } else if (item.getItemId() == R.id.nav_profile) {
                    loadFragment(new ProfileFragment(), false);
                }
            }
        }, 100);

        // Always return true to indicate the event was handled
        return true;
    }

    private void loadFragment(Fragment fragment, boolean addToBackStack) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.addToBackStack(null);
        transaction.replace(R.id.container, fragment);
        transaction.commit();
    }

    // Method to open drawer only when FirstFragment is active
    public void openDrawerIfFirstFragment() {
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.container);
        if (currentFragment instanceof HomeFragment) {  // Ensure only HomeFragment opens drawer
            drawerLayout.openDrawer(GravityCompat.START);  // Open the drawer
        }
    }
    private void loadImage(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .transform(new CircleCrop())
                    .placeholder(R.drawable.baseline_person_24)
                    .error(R.drawable.baseline_person_24)
                    .into(image);
        } else {
            image.setImageResource(R.drawable.baseline_person_24);
        }
    }

}
