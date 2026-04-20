package com.example.businessidea.RecycleView;


import android.content.Context;

import android.content.res.Configuration;

import android.os.Bundle;

import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.businessidea.AccessToken;
import com.example.businessidea.Module.CommentModel;
import com.example.businessidea.Module.Idea;
import com.example.businessidea.Module.Notification;
import com.example.businessidea.Module.Users;
import com.example.businessidea.R;
import com.example.businessidea.fragment.ProfileFragment;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IdeaAdapter extends RecyclerView.Adapter<IdeaAdapter.IdeaViewHolder> {
    private List<Idea> ideaList;
    private final Context context;
    private final FirebaseAuth auth;
    private final FirebaseUser firebaseUser;
    private final String userId;
    private FragmentManager fragmentManager;
    String UrlImageUser,UserName;
    public IdeaAdapter(List<Idea> ideaList, Context context, FragmentManager fragmentManager) {
        this.ideaList = (ideaList != null) ? ideaList : new ArrayList<>();
        this.context = context;
        this.auth = FirebaseAuth.getInstance();
        this.firebaseUser = auth.getCurrentUser();
        this.userId = (firebaseUser != null) ? firebaseUser.getUid() : null;
        if (userId == null) {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show();
        }
        this.fragmentManager = fragmentManager;
    }
    public IdeaAdapter(List<Idea> ideaList, Context context) {
        this.ideaList = (ideaList != null) ? ideaList : new ArrayList<>();
        this.context = context;
        this.auth = FirebaseAuth.getInstance();
        this.firebaseUser = auth.getCurrentUser();
        this.userId = (firebaseUser != null) ? firebaseUser.getUid() : null;
        if (userId == null) {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }

    @NonNull
    @Override
    public IdeaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_idea_card, parent, false);
        return new IdeaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IdeaViewHolder holder, int position) {
        Idea idea = ideaList.get(position);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.equals("")){
                    UrlImageUser = snapshot.child("profilePic").getValue(String.class);
                    UserName = snapshot.child("username").getValue(String.class);
                }else{
                    UrlImageUser = "";
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        // Load user and idea images with Picasso
        loadImage(holder.UserImage, idea.getUserImage(), R.drawable.baseline_person_24);
        if (!idea.getImageUrl().isEmpty()) {
            holder.ImageIdeas.setVisibility(View.VISIBLE);
            Glide.with(context).load(idea.getImageUrl()).into(holder.ImageIdeas);
        } else {
            holder.ImageIdeas.setVisibility(View.GONE);
        }
        holder.IdeasTitle.setText(idea.getTitle());
        holder.ideaDescription.setText(idea.getContent());

        // Initially set maxLines to 5
        holder.ideaDescription.setMaxLines(idea.isExpanded() ? Integer.MAX_VALUE : 5);

        // Add a post runnable to check if the text exceeds 5 lines after the layout is completed
        holder.ideaDescription.post(() -> {
            if ( holder.ideaDescription.getLineCount() > 5) {
                holder.see_more1.setVisibility(View.VISIBLE);
            } else {
                holder.see_more1.setVisibility(View.GONE);
            }
        });
        holder.ProfileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add a delay of 1 second (1000 milliseconds)
                new android.os.Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ProfileFragment fragmentB = new ProfileFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("userId", idea.getUserId());
                        bundle.putBoolean("isCurrentUser", idea.getUserId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()));
                        fragmentB.setArguments(bundle);

                        FragmentTransaction transaction = fragmentManager.beginTransaction();
                        transaction.replace(R.id.container, fragmentB); // Replace with your container ID
                        transaction.addToBackStack(null); // Optional: to add this transaction to the back stack
                        transaction.commit();
                    }
                }, 200); // Delay in milliseconds (1 second)
            }
        });
        holder.likeCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLikesBottomSheet(idea.getIdeasId(), holder, idea);
            }
        });
        // Set the initial "See More"/"See Less" text based on the expanded state
        holder.see_more1.setText(idea.isExpanded() ? "See Less" : "See More");

        // Handle the "See More"/"See Less" button click event
        holder.see_more1.setOnClickListener(v -> {
            // Toggle the expanded state
            boolean expanded = !idea.isExpanded();
            idea.setExpanded(expanded);

            // Update maxLines and "See More"/"See Less" text based on the expanded state
            holder.ideaDescription.setMaxLines(expanded ? Integer.MAX_VALUE : 5);
            holder.see_more1.setText(expanded ? "See Less" : "See More");

            // After the toggle, we need to recheck if the text exceeds 5 lines
            holder.ideaDescription.post(() -> {
                if (holder.ideaDescription.getLineCount() > 5) {
                    holder.see_more1.setVisibility(View.VISIBLE);
                } else {
                    holder.see_more1.setVisibility(View.GONE);
                }
            });
        });
        // Set idea details
        holder.userName.setText(idea.getUserName());
        holder.likeCount.setText(String.valueOf(idea.getLikeCount()));
        holder.commentCounts.setText(String.valueOf(idea.getCommentCount()));
        long postTime = idea.getTimestamp(); // Ensure this is in milliseconds
        CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(
                postTime,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
        );
        holder.date.setText(relativeTime);
        checkIfIdeaIsSaved(idea, holder.save);
        holder.save.setOnClickListener(v -> {
            if (userId != null) {
                // Reference to user's saved ideas
                DatabaseReference userIdeasRef = FirebaseDatabase.getInstance().getReference("Users");
                DatabaseReference savedIdeasRef = userIdeasRef.child(userId).child("SavedIdeas");

                // Check if the idea is already saved
                savedIdeasRef.orderByValue().equalTo(idea.getIdeasId()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // Idea already saved, remove it (unsave)
                            for (DataSnapshot ideaSnapshot : snapshot.getChildren()) {
                                ideaSnapshot.getRef().removeValue()
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                toggleButtonColor(holder.save, false);
                                            }
                                        });
                            }
                        } else {
                            // Idea not saved, save it
                            saveIdeaForUser(userId, idea, holder.save);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle the error if necessary
                    }
                });
            }
        });

        // Like and Comment functionalities
        DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference("PublicIdeas")
                .child(idea.getIdeasId()).child("likes");
        DatabaseReference userLikesRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(idea.getUserId()).child("Ideas").child(idea.getIdeasId()).child("likes");

        setupLikeFeature(holder, likesRef, userLikesRef,idea);
        setupCommentFeature(holder, idea.getIdeasId(),idea);

        if (idea.getUserId().equals(userId)) {
            holder.btn_follow.setVisibility(View.GONE); // Hide the button
        } else {
            holder.btn_follow.setVisibility(View.VISIBLE); // Show the button for others
            DatabaseReference currentUserRef = FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(userId) // Current user's ID
                    .child("Following")
                    .child(idea.getUserId()); // ID of the user being checked

            currentUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        holder.btn_follow.setText("Following");
                    } else {
                        holder.btn_follow.setText("Follow");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(context, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            holder.btn_follow.setOnClickListener(v -> {
                if (firebaseUser != null) {
                    DatabaseReference targetUserRef = FirebaseDatabase.getInstance()
                            .getReference("Users")
                            .child(idea.getUserId()) // ID of the user being followed
                            .child("Followers")
                            .child(userId); // Current user's ID
                    final Animation scaleAnimation = AnimationUtils.loadAnimation(context, R.anim.btn_anim);
                    // Check if already following
                    currentUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                // If following, remove from both lists
                                currentUserRef.removeValue();
                                targetUserRef.removeValue();
                                holder.btn_follow.setText("Follow");
                                removeFollowNotification(idea.getUserId(),userId);
                                updateFollowState(idea.getUserId(), false);
                            } else {
                                // If not following, add to both lists
                                currentUserRef.setValue(true);
                                targetUserRef.setValue(true);
                                holder.btn_follow.setText("Following");
                                updateFollowState(idea.getUserId(), true);
                                sendFollowNotification(userId,UrlImageUser,idea.getUserId(),System.currentTimeMillis(),UserName);
                                getDataForTheNotification(userId,idea.getUserId(),"follow");
                            }
                            holder.btn_follow.startAnimation(scaleAnimation);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(context, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(context, "Please log in to follow users.", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return (ideaList != null) ? ideaList.size() : 0;
    }
    private void updateFollowState(String userId, boolean isFollowing) {
        for (int i = 0; i < ideaList.size(); i++) {
            Idea idea = ideaList.get(i);
            if (idea.getUserId().equals(userId)) {
                idea.setFollowed(isFollowing); // Update the follow state in the dataset
                notifyItemChanged(i); // Notify the adapter to refresh this item
            }
        }
    }

    private void setupLikeFeature(IdeaViewHolder holder, DatabaseReference likesRef, DatabaseReference userLikesRef,Idea idea) {
        likesRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isLiked = snapshot.exists() && snapshot.getValue(Boolean.class);
                updateLikeIcon(holder.likeIcon, isLiked);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        holder.likeIcon.setOnClickListener(v -> likesRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    likesRef.child(userId).removeValue();
                    userLikesRef.child(userId).removeValue();
                    updateLikeCount(holder, -1);
                    removeNotification(idea.getIdeasId(),idea.getUserId(),"like");
                    updateLikeIcon(holder.likeIcon, false);
                } else {
                    likesRef.child(userId).setValue(true);
                    userLikesRef.child(userId).setValue(true);
                    updateLikeCount(holder, 1);
                    sendLikeNotification(userId,UrlImageUser,idea.getUserId(),idea.getImageUrl(), System.currentTimeMillis(),UserName, idea.getIdeasId());
                    getDataForTheNotification(userId,idea.getUserId(),"like");
                    animateLikeButton(holder.likeIcon);
                    updateLikeIcon(holder.likeIcon, true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        }));
    }
    private void setupCommentFeature(IdeaViewHolder holder, String ideaId, Idea idea) {
        holder.commentIcon.setOnClickListener(v -> showCommentBottomSheet(ideaId, holder, idea));
    }
    private void checkIfIdeaIsSaved(Idea idea, ImageView saveButton) {
        if (userId != null) {
            DatabaseReference userIdeasRef = FirebaseDatabase.getInstance().getReference("Users");
            DatabaseReference savedIdeasRef = userIdeasRef.child(userId).child("SavedIdeas");
            savedIdeasRef.orderByValue().equalTo(idea.getIdeasId()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // Idea is already saved
                        toggleButtonColor(saveButton, true);
                    } else {
                        // Idea is not saved
                        toggleButtonColor(saveButton, false);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle error if needed
                }
            });
        }
    }

    private void saveIdeaForUser(String userId, Idea idea, ImageView saveButton) {
        DatabaseReference userIdeasRef = FirebaseDatabase.getInstance().getReference("Users");
        DatabaseReference savedIdeasRef = userIdeasRef.child(userId).child("SavedIdeas");
        String ideaId = idea.getIdeasId(); // Use the existing idea's ID
        if (ideaId != null) {
            savedIdeasRef.push().setValue(ideaId) // Only save the ideasId
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            toggleButtonColor(saveButton, true);
                        }
                    });
        }
    }

    private void toggleButtonColor(ImageView save, boolean saveButton) {
        int nightModeFlags = save.getContext().getResources().getConfiguration().uiMode &
                Configuration.UI_MODE_NIGHT_MASK;

        // Liked state
        // Not liked state
        if (saveButton) {
            save.setImageResource(R.drawable.baseline_bookmark_24); // Liked state
        } else {
            save.setImageResource(R.drawable.baseline_bookmark_border_24); // Not liked state

        }
    }

    private void showLikesBottomSheet(String ideaId, IdeaViewHolder holder, Idea idea) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        bottomSheetDialog.setContentView(LayoutInflater.from(context).inflate(R.layout.item_like, null));

        EditText searchBar = bottomSheetDialog.findViewById(R.id.search_bar);
        RecyclerView recyclerViewLikes = bottomSheetDialog.findViewById(R.id.recyclerViewLikes);

        // Initialize RecyclerView
        recyclerViewLikes.setLayoutManager(new LinearLayoutManager(context));
        ArrayList<Users> LikesList = new ArrayList<>();
        ArrayList<Users> filteredList = new ArrayList<>(); // New list for search results
        LikesAdapter LikeAdapter = new LikesAdapter(filteredList, context, fragmentManager, bottomSheetDialog);
        recyclerViewLikes.setAdapter(LikeAdapter);

        // Load user details
        loadLikes(ideaId, LikesList, filteredList, LikeAdapter);

        // Implement search filter
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                filterLikes(charSequence.toString(), LikesList, filteredList, LikeAdapter);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        bottomSheetDialog.show();
    }

    // Load likes into both lists (full and filtered)
    private void loadLikes(String ideaId, List<Users> LikesList, List<Users> filteredList, LikesAdapter LikeAdapter) {
        DatabaseReference commentRef = FirebaseDatabase.getInstance().getReference("PublicIdeas")
                .child(ideaId).child("likes");
        commentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                LikesList.clear();
                filteredList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String likeId = dataSnapshot.getKey();
                    fetchUserDetails(likeId, LikesList, filteredList, LikeAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // Fetch user details and add to both lists
    private void fetchUserDetails(String userId, List<Users> LikesList, List<Users> filteredList, LikesAdapter LikeAdapter) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String username = snapshot.child("username").getValue(String.class);
                    String headline = snapshot.child("headline").getValue(String.class);
                    String ProfilePic = snapshot.child("profilePic").getValue(String.class);

                    Users user = new Users(userId, username, headline, ProfilePic, 0);
                    LikesList.add(user);
                    filteredList.add(user); // Also add to filteredList initially
                    LikeAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FetchUserDetailsError", error.getMessage());
            }
        });
    }

    // Filter function to search in username or headline
    private void filterLikes(String query, List<Users> LikesList, List<Users> filteredList, LikesAdapter LikeAdapter) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(LikesList); // Show all users if search is empty
        } else {
            for (Users user : LikesList) {
                if (user.getUsername().toLowerCase().contains(query.toLowerCase()) ||
                        user.getHeadline().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(user);
                }
            }
        }
        LikeAdapter.notifyDataSetChanged();
    }


    private void showCommentBottomSheet(final String ideaId, IdeaViewHolder holder, Idea idea) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        bottomSheetDialog.setContentView(LayoutInflater.from(context).inflate(R.layout.item_comment, null));
        ImageView userImage = bottomSheetDialog.findViewById(R.id.dialog_user_image);
        EditText commentInput = bottomSheetDialog.findViewById(R.id.message_input);
        ImageView addCommentButton = bottomSheetDialog.findViewById(R.id.buttonSend);
        RecyclerView recyclerViewComments = bottomSheetDialog.findViewById(R.id.recyclerViewComments);

        // Initialize RecyclerView
        recyclerViewComments.setLayoutManager(new LinearLayoutManager(context));
        List<CommentModel> commentsList = new ArrayList<>();
        CommentsAdapter commentsAdapter = new CommentsAdapter(commentsList,context,idea.getUserId(),fragmentManager,bottomSheetDialog);
        recyclerViewComments.setAdapter(commentsAdapter);

        // Load user details
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String profilePicUrl = snapshot.child("profilePic").getValue(String.class);
                    String username = snapshot.child("username").getValue(String.class);

                    // Load profile picture
                    loadImage(userImage, profilePicUrl, R.drawable.baseline_person_24);

                    // Handle add comment button click
                    addCommentButton.setOnClickListener(v -> {
                        String commentText = commentInput.getText().toString().trim();
                        if (commentText.isEmpty()) {
                            Toast.makeText(context, "Please enter a comment", Toast.LENGTH_SHORT).show();
                        } else {
                            addCommentToFirebase(ideaId, commentText, username, profilePicUrl, idea);
                            sendCommentNotification(userId,UrlImageUser,idea.getUserId(),idea.getImageUrl(),System.currentTimeMillis(),UserName,commentText,idea.getIdeasId());
                            getDataForTheNotification(userId,idea.getUserId(),"comment");
                            commentInput.setText(""); // Clear input
                        }
                    });

                    // Load existing comments
                    loadComments(ideaId, commentsList, commentsAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        bottomSheetDialog.show();
    }

    private void loadComments(String ideaId, List<CommentModel> commentsList, CommentsAdapter commentsAdapter) {
        DatabaseReference commentRef = FirebaseDatabase.getInstance().getReference("PublicIdeas")
                .child(ideaId).child("comments");
        commentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentsList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    CommentModel comment = dataSnapshot.getValue(CommentModel.class);
                    commentsList.add(comment);
                }
                commentsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }


    private void addCommentToFirebase(String ideaId, String commentText, String username, String profilePicUrl, Idea idea) {
        DatabaseReference commentRef = FirebaseDatabase.getInstance().getReference("PublicIdeas")
                .child(ideaId).child("comments");
        DatabaseReference userCommentRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(idea.getUserId()).child("Ideas").child(ideaId).child("comments");
        String commentId = commentRef.push().getKey();
        // Initialize the likes map
        Map<String, Boolean> likes = new HashMap<>();
        CommentModel comment = new CommentModel(userId, commentText, System.currentTimeMillis(), username, profilePicUrl,commentId,0,likes,ideaId);

        if (commentId != null) {
            commentRef.child(commentId).setValue(comment);
            userCommentRef.child(commentId).setValue(comment);
        }
    }

    private void updateLikeIcon(ImageView likeIcon, boolean isLiked) {
        int nightModeFlags = likeIcon.getContext().getResources().getConfiguration().uiMode &
                Configuration.UI_MODE_NIGHT_MASK;

        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            // Dark mode
            if (isLiked) {
                likeIcon.setImageResource(R.drawable.baseline_thumb_up_24); // Liked state
            } else {
                likeIcon.setImageResource(R.drawable.baseline_thumb_up_off_alt_241); // Not liked state

            }
        } else {
            // Light mode
            if (isLiked) {
                likeIcon.setImageResource(R.drawable.baseline_thumb_up_24); // Liked state
            } else {
                likeIcon.setImageResource(R.drawable.baseline_thumb_up_off_alt_24); // Not liked state
            }
        }
    }

    private void updateLikeCount(IdeaViewHolder holder, int delta) {
        int currentCount = Integer.parseInt(holder.likeCount.getText().toString());
        holder.likeCount.setText(String.valueOf(currentCount + delta));
    }

    private void sendLikeNotification(String senderId, String senderImage, String receiverId, String ideaImage, long timestamp, String userName,String IdeasId) {
        DatabaseReference notificationsRef = FirebaseDatabase.getInstance().getReference("Users").child(receiverId).child("Notifications");
        if (!senderId.equals(receiverId)) {
            // Create a new notification reference to generate a unique key
            String key = notificationsRef.push().getKey();
            // Create a new notification
            Notification notification = new Notification(
                    "like",  // Type of notification
                    senderId,  // Sender ID (the user who liked the idea)
                    receiverId,
                    senderImage,  // Sender's image URL
                    ideaImage,  // Image related to the idea
                    timestamp,  // Timestamp
                    userName, // Adding the user name to the notification
                    IdeasId,
                    false,
                    key
            );

            // Push the notification to Firebase
            notificationsRef.child(key).setValue(notification)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d("Notification", "Notification sent successfully.");
                        } else {
                            Log.e("Notification", "Failed to send notification.");
                        }
                    });
        }
    }
    private void sendCommentNotification(String senderId, String senderImage, String receiverId, String ideaImage, long timestamp, String userName,String comment,String IdeasId) {
        DatabaseReference notificationsRef = FirebaseDatabase.getInstance().getReference("Users").child(receiverId).child("Notifications");
        if (!senderId.equals(receiverId)) {
            String key = notificationsRef.push().getKey();
            // Create a new notification
            Notification notification = new Notification(
                    "comment",  // Type of notification
                    senderId,  // Sender ID (the user who liked the idea)
                    receiverId,
                    senderImage,  // Sender's image URL
                    ideaImage,  // Image related to the idea
                    timestamp,  // Timestamp
                    userName,
                    comment,
                    IdeasId,
                    false,// Adding the user name to the notification
                    key
            );

            // Push the notification to Firebase
            notificationsRef.child(key).setValue(notification)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d("Notification", "Notification sent successfully.");
                        } else {
                            Log.e("Notification", "Failed to send notification.");
                        }
                    });
        }
    }
    private void sendFollowNotification(String senderId, String senderImage, String receiverId, long timestamp, String userName) {
        DatabaseReference notificationsRef = FirebaseDatabase.getInstance().getReference("Users").child(receiverId).child("Notifications");
        if (!senderId.equals(receiverId)) {
            String key = notificationsRef.push().getKey();
            // Create a new notification
            Notification notification = new Notification(
                    "follow",  // Type of notification
                    senderId,  // Sender ID (the user who liked the idea)
                    receiverId,
                    senderImage,  // Sender's image URL
                    timestamp,  // Timestamp
                    userName,// Adding the user name to the notification
                    false,
                    key
            );

            // Push the notification to Firebase
            notificationsRef.child(key).setValue(notification)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d("Notification", "Notification sent successfully.");
                        } else {
                            Log.e("Notification", "Failed to send notification.");
                        }
                    });
        }
    }
    public void removeNotification(String ideaId, String senderId,String type) {
        // Query notifications by ideaId
        DatabaseReference notificationsRef = FirebaseDatabase.getInstance().getReference("Users").child(senderId).child("Notifications");
        notificationsRef.orderByChild("ideasId").equalTo(ideaId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Iterate through all notifications related to the given idea
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String notificationTypes = snapshot.child("type").getValue(String.class);
                            String sender = snapshot.child("receiverId").getValue(String.class);

                            // Check if the notification is of type "like" and matches the senderId
                            if (type.equals(notificationTypes) && senderId.equals(sender)) {
                                // Remove the notification from Firebase
                                snapshot.getRef().removeValue()
                                        .addOnSuccessListener(unused -> {
                                            System.out.println("Notification removed successfully.");
                                        })
                                        .addOnFailureListener(e -> {
                                            System.err.println("Failed to remove notification: " + e.getMessage());
                                        });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Handle database errors
                        System.err.println("Error querying notifications: " + databaseError.getMessage());
                    }
                });
    }
    public void removeFollowNotification(String receiverId, String senderId) {
        DatabaseReference notificationsRef = FirebaseDatabase.getInstance().getReference("Users").child(receiverId).child("Notifications");
        notificationsRef.orderByChild("receiverId").equalTo(receiverId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String notificationType = snapshot.child("type").getValue(String.class);
                            String sender = snapshot.child("senderId").getValue(String.class);

                            // Check if the notification is a "follow" and matches the senderId
                            if ("follow".equals(notificationType) && senderId.equals(sender)) {
                                // Remove the notification
                                snapshot.getRef().removeValue()
                                        .addOnSuccessListener(unused -> {
                                            System.out.println("Follow notification removed successfully.");
                                        })
                                        .addOnFailureListener(e -> {
                                            System.err.println("Failed to remove follow notification: " + e.getMessage());
                                        });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Handle database errors
                        System.err.println("Error querying notifications: " + databaseError.getMessage());
                    }
                });
    }
    private void sendNotificationToReceiver(String token, String message, String imageUrl,String username) {
        // Create the notification payload
        JSONObject notification = new JSONObject();
        JSONObject notificationBody = new JSONObject();
        JSONObject messageBody = new JSONObject();

        try {
            // Notification details
            notificationBody.put("title", username);
            notificationBody.put("body", message);
            notificationBody.put("image", imageUrl);


            // Message payload
            messageBody.put("notification", notificationBody);
            messageBody.put("token", token);

            notification.put("message", messageBody);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        // Create the HTTP request to send the notification via FCM
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                "Replace With Url", // Replace with your FCM project endpoint
                notification,
                response -> Log.d("FCM", "Notification sent successfully: " + response),
                error -> Log.e("FCM", "Failed to send notification: " + error.getMessage())
        ) {
            @NonNull
            @Override
            public Map<String, String> getHeaders() {
                // Set the necessary headers for the FCM request
                AccessToken accessToken = new AccessToken();

                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; charset=UTF-8");
                headers.put("Authorization", "Bearer " + accessToken.getAccessToken()); // Replace with a method to fetch the access token
                return headers;
            }
        };

        // Add the request to the Volley request queue
        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(request);
    }
    public void updateList(List<Idea> newList) {
        this.ideaList = newList;
        notifyDataSetChanged();
    }


    public void getDataForTheNotification(String Sender,String ReceiverId1,String type){
        DatabaseReference notificationsRef = FirebaseDatabase.getInstance().getReference("Users").child(Sender).child("Notifications");

        if (!Sender.equals(ReceiverId1)) {
            notificationsRef.orderByChild("type").equalTo(type).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String image = snapshot.child("senderImage").getValue(String.class);
                        String username = snapshot.child("userName").getValue(String.class);
                        DatabaseReference receiverTokenRef = FirebaseDatabase.getInstance().getReference("Users").child(ReceiverId1).child("token");
                        receiverTokenRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot tokenSnapshot) {
                                String token = tokenSnapshot.getValue(String.class);
                                if (token != null) {
                                    String notificationMessage = generateNotificationMessage(type, username);
                                    sendNotificationToReceiver(token, notificationMessage, image, username);
                                } else {
                                    Log.w("FirebaseWarning", "Token not found for receiver: " + ReceiverId1);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.e("FirebaseError", "Error fetching token: " + databaseError.getMessage());
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("FirebaseError", "Error: " + databaseError.getMessage());
                }
            });
        }
    }

    private String generateNotificationMessage(String type, String username) {
        switch (type) {
            case "like":
                return username + " liked your post.";
            case "comment":
                return username + " commented on your post.";
            case "follow":
                return username + " started following you.";
            default:
                return username + " sent you a notification.";
        }
    }
    private void animateLikeButton(ImageView likeIcon) {
        likeIcon.animate()
                .scaleX(1.5f)
                .scaleY(1.5f)
                .setDuration(50)
                .withEndAction(() -> likeIcon.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(50));
    }

    private void loadImage(ImageView imageView, String url, int placeholder) {
        Glide.with(context)
                .load(url)
                .placeholder(placeholder)
                .transform(new CircleCrop())
                .error(placeholder)
                .into(imageView);
    }


    public static class IdeaViewHolder extends RecyclerView.ViewHolder {
        TextView userName, ideaDescription, likeCount, commentCounts,date,see_more1,IdeasTitle;
        ImageView likeIcon, commentIcon, ImageIdeas, UserImage,save;
       Button btn_follow;

        LinearLayout ProfileView;

        public IdeaViewHolder(View itemView) {
            super(itemView);
            save = itemView.findViewById(R.id.save);
            ProfileView = itemView.findViewById(R.id.ProfileView);
            btn_follow = itemView.findViewById(R.id.btn_follow);
            UserImage = itemView.findViewById(R.id.user_profile_image);
            ImageIdeas = itemView.findViewById(R.id.idea_image);
            userName = itemView.findViewById(R.id.user_name);
            ideaDescription = itemView.findViewById(R.id.idea_description);
            IdeasTitle = itemView.findViewById(R.id.Ideas);
            likeCount = itemView.findViewById(R.id.like_count);
            likeIcon = itemView.findViewById(R.id.like_icon);
            commentIcon = itemView.findViewById(R.id.comment_icon);
            commentCounts = itemView.findViewById(R.id.comment_count);

            date = itemView.findViewById(R.id.post_date);
            see_more1 = itemView.findViewById(R.id.see_more1);
        }
    }
}
