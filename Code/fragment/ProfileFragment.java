package com.example.businessidea.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.businessidea.Activity.AboutUserActivity;
import com.example.businessidea.Activity.EditProfileActivity;

import com.example.businessidea.Activity.HomeActivity;
import com.example.businessidea.Activity.MessageActivity;
import com.example.businessidea.Activity.SettingActivity;
import com.example.businessidea.Activity.TabLayoutActivity;
import com.example.businessidea.Module.Idea;
import com.example.businessidea.R;

import com.example.businessidea.RecycleView.UserIdeasAdapter;
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


public class ProfileFragment extends Fragment {


    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    Button edit_profile_button,message_button,follow;
    ImageView profile_image,edit_about,setting;
    private SwipeRefreshLayout swipeRefreshLayout;
    TextView user_name,Pronouns,user_job_title,follow_count,following_count,about_me_text,see_more,gg;
    private DatabaseReference mDatabase;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseUser firebaseUser;
    boolean isCurrentUser;
    String UserId;
    private RecyclerView recyclerView;
    private UserIdeasAdapter adapter;
    private List<Idea> ideaList = new ArrayList<>(); // Initialize list
    int likeCount;
    int commentCount;
    String thatUserId;
    LinearLayout layoutAbout,LayoutFollow;
    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        follow = view.findViewById(R.id.follow);
        message_button = view.findViewById(R.id.message_button);
        see_more = view.findViewById(R.id.see_more);
        about_me_text = view.findViewById(R.id.about_me_text);
        edit_about = view.findViewById(R.id.edit_about);
        layoutAbout = view.findViewById(R.id.layoutAbout);
        edit_profile_button = view.findViewById(R.id.edit_profile_button);
        profile_image = view.findViewById(R.id.profile_image);
        user_name = view.findViewById(R.id.user_name);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        Pronouns = view.findViewById(R.id.Pronouns);
        user_job_title = view.findViewById(R.id.user_job_title);
        follow_count = view.findViewById(R.id.follow_count);
        following_count = view.findViewById(R.id.following_count);
        LayoutFollow = view.findViewById(R.id.LayoutFollow);
        setting = view.findViewById(R.id.setting);
        gg = view.findViewById(R.id.gg);

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        assert firebaseUser != null;
        UserId = firebaseUser.getUid();
        database = FirebaseDatabase.getInstance();

        recyclerView = view.findViewById(R.id.recycler_business_ideas);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        setting.setOnClickListener(v-> startActivity(new Intent(getContext(), SettingActivity.class)));

        if (getArguments() != null) {
            thatUserId = getArguments().getString("userId");
            isCurrentUser = getArguments().getBoolean("isCurrentUser");
            swipeRefreshLayout.setOnRefreshListener(() -> {
                if (isCurrentUser) {
                    getData(UserId);
                    fetchdata(UserId);
                    updateFollowAndFollowingCounts(UserId);
                } else {
                    getData(thatUserId);
                    fetchdata(thatUserId);
                    updateFollowAndFollowingCounts(thatUserId);
                }
            });
            if (isCurrentUser) {
                setting.setVisibility(View.VISIBLE);
                follow.setVisibility(View.GONE);
                message_button.setVisibility(View.GONE);
                edit_profile_button.setVisibility(View.VISIBLE);
                edit_about.setVisibility(View.VISIBLE);
                getData(UserId);
                fetchdata(UserId);
                updateFollowAndFollowingCounts(UserId);
                adapter = new UserIdeasAdapter(getContext(),ideaList,"",getParentFragmentManager());
                recyclerView.setAdapter(adapter);
                edit_profile_button.setOnClickListener(v -> startActivity(new Intent(getContext(), EditProfileActivity.class)));
                layoutAbout.setOnClickListener(v -> startActivity(new Intent(getContext(), AboutUserActivity.class)));
                LayoutFollow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getContext(),TabLayoutActivity.class);
                        intent.putExtra("UserId",UserId);
                        startActivity(intent);
                    }
                });
            } else {
                setting.setVisibility(View.GONE);
                edit_profile_button.setVisibility(View.GONE);
                follow.setVisibility(View.VISIBLE);
                message_button.setVisibility(View.VISIBLE);
                edit_about.setVisibility(View.GONE);
                getData(thatUserId);
                fetchdata(thatUserId);
                updateFollowAndFollowingCounts(thatUserId);

                adapter = new UserIdeasAdapter(getContext(),ideaList,thatUserId,getParentFragmentManager());
                recyclerView.setAdapter(adapter);
                DatabaseReference currentUserRef = FirebaseDatabase.getInstance()
                        .getReference("Users")
                        .child(UserId) // Current user's ID
                        .child("Following")
                        .child(thatUserId); // ID of the user being checked

                currentUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            follow.setText("Following");
                        } else {
                            follow.setText("Follow");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                follow.setOnClickListener(v -> {
                    if (firebaseUser != null) {
                        DatabaseReference targetUserRef = FirebaseDatabase.getInstance()
                                .getReference("Users")
                                .child(thatUserId) // ID of the user being followed
                                .child("Followers")
                                .child(UserId); // Current user's ID
                        final Animation scaleAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.btn_anim);
                        // Check if already following
                        currentUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    // If following, remove from both lists
                                    currentUserRef.removeValue();
                                    targetUserRef.removeValue();
                                    follow.setText("Follow");
                                } else {
                                    // If not following, add to both lists
                                    currentUserRef.setValue(true);
                                    targetUserRef.setValue(true);
                                    follow.setText("Following");
                                }
                                follow.startAnimation(scaleAnimation);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                });
                message_button.setOnClickListener(v -> {
                    Intent intent = new Intent(getContext(), MessageActivity.class);
                    intent.putExtra("UserId",thatUserId);
                    startActivity(intent);
                });
                LayoutFollow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getContext(),TabLayoutActivity.class);
                        intent.putExtra("UserId",thatUserId);
                        startActivity(intent);
                    }
                });
            }
        }else {
            setting.setVisibility(View.VISIBLE);
            follow.setVisibility(View.GONE);
            message_button.setVisibility(View.GONE);
            edit_profile_button.setVisibility(View.VISIBLE);
            edit_about.setVisibility(View.VISIBLE);
            getData(UserId);
            updateFollowAndFollowingCounts(UserId);
            fetchdata(UserId);
            swipeRefreshLayout.setOnRefreshListener(() -> {
                getData(UserId); // Refresh current user's data
                fetchdata(UserId);
                updateFollowAndFollowingCounts(UserId);
            });
            adapter = new UserIdeasAdapter(getContext(),ideaList,"",getParentFragmentManager());
            recyclerView.setAdapter(adapter);
            edit_profile_button.setOnClickListener(v -> startActivity(new Intent(getContext(), EditProfileActivity.class)));
            layoutAbout.setOnClickListener(v -> startActivity(new Intent(getContext(), AboutUserActivity.class)));
            LayoutFollow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(),TabLayoutActivity.class);
                    intent.putExtra("UserId",UserId);
                    startActivity(intent);
                }
            });
        }
        return view;
    }
    private void fetchdata(String NavUserId) {
        swipeRefreshLayout.setRefreshing(true);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(NavUserId).child("Ideas");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ideaList.clear();
                for (DataSnapshot ideaSnapshot : snapshot.getChildren()) {
                    String ideaId = ideaSnapshot.getKey();
                    String content = ideaSnapshot.child("content").getValue(String.class);
                    String imageUrl = ideaSnapshot.child("imageUrl").getValue(String.class);
                    String IdeasTitle = ideaSnapshot.child("titleIdeas").getValue(String.class);
                    String UserName = ideaSnapshot.child("UserName").getValue(String.class);
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
                        ideaList.add(new Idea(UserName, UserImage, imageUrl, content, likesList, ideaId,UserId,likeCount,commentCount,timestamp,IdeasTitle)); // Pass ideaId
                    }
                }
                Collections.reverse(ideaList);
                adapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
                if (ideaList.isEmpty()) {
                    gg.setVisibility(View.VISIBLE); // Show the message
                    recyclerView.setVisibility(View.GONE); // Hide the RecyclerView
                } else {
                    gg.setVisibility(View.GONE); // Hide the message
                    recyclerView.setVisibility(View.VISIBLE); // Show the RecyclerView
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }
    private void loadImage(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.baseline_person_24)
                    .transform(new CircleCrop())
                    .error(R.drawable.baseline_person_24)
                    .into(profile_image);
        } else {
            profile_image.setImageResource(R.drawable.baseline_person_24);
        }
    }
    public static ProfileFragment newInstance(String username, boolean email) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString("userId", username);
        args.putBoolean("isCurrentUser", email);
        fragment.setArguments(args);
        return fragment;
    }
    private void getData(String userIdGet) {
        swipeRefreshLayout.setRefreshing(true);
        mDatabase = FirebaseDatabase.getInstance().getReference("Users").child(userIdGet);
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String profilePicUrl = snapshot.child("profilePic").getValue(String.class);
                    String Name = snapshot.child("username").getValue(String.class);
                    String headline = snapshot.child("headline").getValue(String.class);
                    String pronouns = snapshot.child("pronouns").getValue(String.class);
                    String About = snapshot.child("about").getValue(String.class);
                    String follow = snapshot.child("follow").getValue(String.class);
                    String following = snapshot.child("following").getValue(String.class);

                    // Load Profile Picture
                    if (profilePicUrl != null) {
                        loadImage(profilePicUrl); // Assume this method loads the image
                    }

                    // Handle "About" section visibility and line count
                    if (About == null || About.equals("")) {
                        about_me_text.setVisibility(View.GONE);
                        see_more.setVisibility(View.GONE); // Hide "See More" button if About is empty
                    } else {
                        about_me_text.setVisibility(View.VISIBLE);
                        about_me_text.setText(About);
                        about_me_text.setMaxLines(Integer.MAX_VALUE);
                        about_me_text.setEllipsize(TextUtils.TruncateAt.END); // Set ellipsis at the end

                        // Post to ensure the layout is fully measured before checking line count
                        about_me_text.post(() -> {
                            if (about_me_text.getLineCount() > 4) {
                                about_me_text.setMaxLines(4); // Limit to 4 lines initially
                                see_more.setVisibility(View.VISIBLE);
                            } else {
                                about_me_text.setMaxLines(Integer.MAX_VALUE); // Show full text if it's small
                                see_more.setVisibility(View.GONE);
                            }
                        });
                        see_more.setOnClickListener(v -> {
                            if (about_me_text.getMaxLines() == 4) {
                                about_me_text.setMaxLines(Integer.MAX_VALUE); // Expand text
                                about_me_text.setEllipsize(null); // Remove ellipsis
                                see_more.setText("See Less");
                            } else {
                                about_me_text.setMaxLines(4); // Collapse to 4 lines
                                about_me_text.setEllipsize(TextUtils.TruncateAt.END);  // Set ellipsis again
                                see_more.setText("See More");
                            }
                        });

                    }

                    // Set other user data
                    user_name.setText(Name);
                    user_job_title.setText(headline);
                    Pronouns.setText("(" + pronouns + ")");
                    follow_count.setText(follow + " Followers");
                    following_count.setText(following + " Following");
                }
                swipeRefreshLayout.setRefreshing(false); // Hide the swipe refresh indicator
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                swipeRefreshLayout.setRefreshing(false); // Hide the swipe refresh indicator in case of an error
            }
        });
    }
    private void updateFollowAndFollowingCounts(String userId) {
        swipeRefreshLayout.setRefreshing(true);
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
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        Intent intent = new Intent(requireContext(), HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        requireActivity().finish(); // Finish current activity
                    }
                });
    }

}