package com.example.businessidea.fragment;


import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.businessidea.Activity.AiActivity;
import com.example.businessidea.Activity.HomeActivity;
import com.example.businessidea.Activity.NotificationActivity;
import com.example.businessidea.Module.Category;
import com.example.businessidea.Module.Idea;
import com.example.businessidea.R;
import com.example.businessidea.RecycleView.CategoryAdapter;
import com.example.businessidea.RecycleView.IdeaAdapter;
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

public class HomeFragment extends Fragment {

    private FirebaseAuth auth;
    private RecyclerView recyclerView;
    private IdeaAdapter adapter;
    private List<Idea> ideaList = new ArrayList<>(); // Initialize list
    String UserId;
    int likeCount;
    private SwipeRefreshLayout swipeRefreshLayout;
    ImageView profile_image1,chatbot;
    DatabaseReference mDatabase;
    FrameLayout notification_icon;
    int commentCount;
    private List<Idea> filteredList = new ArrayList<>();
    TextView notification_badge;
    EditText search_user;
    TextView noDataText;
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1;

    public HomeFragment(){

    }
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Show confirmation dialog
                new AlertDialog.Builder(requireContext())
                        .setTitle("Exit App")
                        .setMessage("Are you sure you want to exit the app?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            requireActivity().finish(); // Close the app
                        })
                        .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                        .show();
            }
        });


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);


        auth = FirebaseAuth.getInstance();
        profile_image1 = view.findViewById(R.id.profile_image1);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        notification_icon = view.findViewById(R.id.notification_icon);
        chatbot = view.findViewById(R.id.chatbot);
        notification_badge = view.findViewById(R.id.notification_badge);
        noDataText  = view.findViewById(R.id.noDataText);
        search_user = view.findViewById(R.id.search_user);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST_CODE);
            }
        }
        chatbot.setAlpha(0f);
        chatbot.setTranslationY(50);

        // Fade-in and float-up animation
        chatbot.animate()
                .alpha(1f)
                .translationY(0)
                .setDuration(500)
                .setStartDelay(300)
                .setInterpolator(new DecelerateInterpolator())
                .start();
        ObjectAnimator floatAnim = ObjectAnimator.ofFloat(chatbot, "translationY", 0f, -15f, 0f);
        floatAnim.setDuration(1500);
        floatAnim.setRepeatMode(ValueAnimator.REVERSE);
        floatAnim.setRepeatCount(ValueAnimator.INFINITE);
        floatAnim.start();
        swipeRefreshLayout.setOnRefreshListener(() -> {
            fetchdata();
        });
        chatbot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.animate()
                        .scaleX(1.2f)
                        .scaleY(1.2f)
                        .setDuration(150)
                        .withEndAction(() -> v.animate()
                                .scaleX(1.0f)
                                .scaleY(1.0f)
                                .setDuration(150))
                        .start();
                startActivity(new Intent(getContext(), AiActivity.class));

            }
        });
        RecyclerView categoryRecyclerView = view.findViewById(R.id.categoryRecyclerView);
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        List<Category> categories = new ArrayList<>();
        categories.add(new Category("Technology"));
        categories.add(new Category("Finance"));
        categories.add(new Category("Health"));
        categories.add(new Category("Education"));
        categories.add(new Category("Startup"));
        categories.add(new Category("E-commerce"));
        categories.add(new Category("Freelance Service"));
        categories.add(new Category("Side Hustle"));

        CategoryAdapter categoryAdapter = new CategoryAdapter(categories,getContext(), getParentFragmentManager(), selectedCategory -> {
            Log.d("Category", "Selected: " + selectedCategory);
            fetchFilteredIdeas(selectedCategory, adapter, noDataText); // ✅ Correct filtering function
        });

        categoryRecyclerView.setAdapter(categoryAdapter);

        profile_image1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HomeActivity homeActivity = (HomeActivity) getActivity();
                if (homeActivity != null) {
                    homeActivity.openDrawerIfFirstFragment();
                }// Call method in HomeActivity
            }
        });
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        countUnreadNotifications(currentUserId);
        notification_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Mark all notifications as read
                markAllNotificationsAsRead(currentUserId);
                startActivity(new Intent(getContext(), NotificationActivity.class));
            }
        });
        FirebaseUser firebaseUser = auth.getCurrentUser();

        if (firebaseUser != null) {
            UserId = firebaseUser.getUid();
        } else {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return view;
        }
        mDatabase = FirebaseDatabase.getInstance().getReference("Users").child(UserId);
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String profilePicUrl = snapshot.child("profilePic").getValue(String.class);
                    if (profilePicUrl != null){
                        loadImage(profilePicUrl);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        recyclerView = view.findViewById(R.id.recycler_view1);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
            adapter = new IdeaAdapter(filteredList, getContext(),getParentFragmentManager());
            recyclerView.setAdapter(adapter);
        updateFollowAndFollowingCounts(UserId);
            fetchdata();
        // Set up listener for EditText search
        search_user.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                filterIdeas(charSequence.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable editable) {}
        });

        return view;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("Permission", "Notification permission granted.");
            } else {
                Log.w("Permission", "Notification permission denied.");
            }
        }
    }
    private void markAllNotificationsAsRead(String userId) {
        DatabaseReference notificationsRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(userId)
                .child("Notifications");

        notificationsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    snapshot.getRef().child("read").setValue(true);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.err.println("Failed to mark notifications as read: " + databaseError.getMessage());
            }
        });
    }
    private void countUnreadNotifications(String userId) {
        DatabaseReference notificationsRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(userId)
                .child("Notifications");

        notificationsRef.orderByChild("read").equalTo(false)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int unreadCount = (int) dataSnapshot.getChildrenCount();
                        Log.e("tag1", String.valueOf(unreadCount));
                        // Update the notification badge
                        updateNotificationBadge(unreadCount);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        System.err.println("Failed to count unread notifications: " + databaseError.getMessage());
                    }
                });
    }
    private void fetchFilteredIdeas(String selectedCategory, IdeaAdapter ideaAdapter, TextView noDataText) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("PublicIdeas");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Idea> filteredList = new ArrayList<>();
                for (DataSnapshot ideaSnapshot : snapshot.getChildren()) {
                    String titleIdeas = ideaSnapshot.child("titleIdeas").getValue(String.class);

                    // ✅ Filter only if a category is selected
                    if (selectedCategory == null || (titleIdeas != null && titleIdeas.equals(selectedCategory))) {
                        String ideaId = ideaSnapshot.getKey();
                        String content = ideaSnapshot.child("content").getValue(String.class);
                        String imageUrl = ideaSnapshot.child("imageUrl").getValue(String.class);
                        String userName = ideaSnapshot.child("UserName").getValue(String.class);
                        String userImage = ideaSnapshot.child("UserImage").getValue(String.class);
                        String userId = ideaSnapshot.child("UserId").getValue(String.class);
                        long timestamp = ideaSnapshot.child("timestamp").getValue(long.class);

                        List<String> likesList = new ArrayList<>();
                        int likeCount = 0, commentCount = 0;

                        DataSnapshot likesSnapshot = ideaSnapshot.child("likes");
                        if (likesSnapshot.exists()) {
                            likeCount = (int) likesSnapshot.getChildrenCount();
                            for (DataSnapshot likeSnapshot : likesSnapshot.getChildren()) {
                                likesList.add(likeSnapshot.getKey());
                            }
                        }

                        DataSnapshot commentsSnapshot = ideaSnapshot.child("comments");
                        if (commentsSnapshot.exists()) {
                            commentCount = (int) commentsSnapshot.getChildrenCount();
                        }

                        if (content != null && userName != null && userImage != null) {
                            filteredList.add(new Idea(userName, userImage, imageUrl, content, likesList, ideaId, userId, likeCount, commentCount, timestamp, titleIdeas));
                        }
                    }
                }
                if (filteredList.size() == 0 && filteredList.isEmpty()){
                    noDataText.setVisibility(View.VISIBLE);
                }else {
                    noDataText.setVisibility(View.GONE);
                }
                // Sort by latest
                Collections.reverse(filteredList);

                // ✅ Update adapter
                ideaAdapter.updateList(filteredList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database error
            }
        });
    }

    @SuppressLint("ResourceAsColor")
    private void updateNotificationBadge(int unreadCount) {
        if (unreadCount > 0) {
            notification_badge.setTextColor(R.color.back);
            notification_badge.setText(String.valueOf(unreadCount));
            notification_badge.setVisibility(View.VISIBLE); // Show badge if there are unread notifications
        } else {
            notification_badge.setVisibility(View.GONE); // Hide badge if no unread notifications
        }
    }
    // Callback interface for unread count

    private void fetchdata() {
        swipeRefreshLayout.setRefreshing(true);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("PublicIdeas");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ideaList.clear();
                for (DataSnapshot ideaSnapshot : snapshot.getChildren()) {
                    String ideaId = ideaSnapshot.getKey();
                    String content = ideaSnapshot.child("content").getValue(String.class);
                    String imageUrl = ideaSnapshot.child("imageUrl").getValue(String.class);
                    String UserName = ideaSnapshot.child("UserName").getValue(String.class);
                    String UserImage = ideaSnapshot.child("UserImage").getValue(String.class);
                    String titleIdeas = ideaSnapshot.child("titleIdeas").getValue(String.class);
                    String UserId = ideaSnapshot.child("UserId").getValue(String.class);
                    String follow = ideaSnapshot.child("follow").getValue(String.class);
                    String following = ideaSnapshot.child("following").getValue(String.class);
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
                        ideaList.add(new Idea(UserName, UserImage, imageUrl, content, likesList, ideaId,UserId,likeCount,commentCount,timestamp,titleIdeas)); // Pass ideaId
                    }
                }
                Collections.reverse(ideaList);
                filteredList.clear();
                filteredList.addAll(ideaList);  // Set the filtered list to show all data initially
                adapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to fetch data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }
    private void updateFollowAndFollowingCounts(String userId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        // Listen for changes in "Followers" for this user
        userRef.child("Followers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int followCount = (int) snapshot.getChildrenCount(); // Count followers
                userRef.child("follow").setValue(String.valueOf(followCount)); // Update follow count
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Failed to count followers: " + error.getMessage());
            }
        });

        // Listen for changes in "Following" for this user
        userRef.child("Following").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int followingCount = (int) snapshot.getChildrenCount(); // Count following
                userRef.child("following").setValue(String.valueOf(followingCount)); // Update following count
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Failed to count following: " + error.getMessage());
            }
        });
    }
    private void filterIdeas(String query) {
        filteredList.clear();
        if (TextUtils.isEmpty(query)) {
            // Show all ideas if the query is empty
            filteredList.addAll(ideaList);
        } else {
            // Filter ideas based on description
            for (Idea idea : ideaList) {
                if (idea.getContent().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(idea);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }
    private void loadImage(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.baseline_person_24)
                    .transform(new CircleCrop())
                    .error(R.drawable.baseline_person_24)
                    .into(profile_image1);
        } else {
            profile_image1.setImageResource(R.drawable.baseline_person_24);
        }
    }
}

