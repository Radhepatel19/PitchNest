package com.example.businessidea.RecycleView;



import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.businessidea.Activity.AddIdeasActivity;
import com.example.businessidea.Module.CommentModel;
import com.example.businessidea.Module.Idea;
import com.example.businessidea.Module.Users;
import com.example.businessidea.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserIdeasAdapter extends RecyclerView.Adapter<UserIdeasAdapter.UserIdeaHolder>{
    List<Idea> ideasList;
    Context context;
    private final FirebaseAuth auth;
    private final FirebaseUser firebaseUser;
    private final String userId;
    private FragmentManager fragmentManager;
    String thatUserId;
    public UserIdeasAdapter(Context context, List<Idea> ideasList,String thatUserId,FragmentManager fragmentManager) {
        this.context = context;
        this.ideasList = ideasList;
        this.auth = FirebaseAuth.getInstance();
        this.firebaseUser = auth.getCurrentUser();
        this.thatUserId = thatUserId;
        this.userId = (firebaseUser != null) ? firebaseUser.getUid() : null;
        if (userId == null) {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show();
        }
        this.fragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public UserIdeasAdapter.UserIdeaHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_trending_idea,parent,false);
        return new UserIdeaHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserIdeasAdapter.UserIdeaHolder holder, int position) {
        Idea idea = ideasList.get(position);
        // Set text dynamically
        holder.idea_title.setText(idea.getContent());

        // Initially set maxLines to 5
        holder.idea_title.setMaxLines(idea.isExpanded() ? Integer.MAX_VALUE : 5);

        // Add a post runnable to check if the text exceeds 5 lines after the layout is completed
        holder.idea_title.post(() -> {
            if ( holder.idea_title.getLineCount() > 5) {
                holder.see_more.setVisibility(View.VISIBLE);
            } else {
                holder.see_more.setVisibility(View.GONE);
            }
        });

        holder.like_count.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLikesBottomSheet(idea.getIdeasId());
            }
        });
        holder.IdeasTitle.setText(idea.getTitle());
        // Set the initial "See More"/"See Less" text based on the expanded state
        holder.see_more.setText(idea.isExpanded() ? "See Less" : "See More");

        // Handle the "See More"/"See Less" button click event
        holder.see_more.setOnClickListener(v -> {
            // Toggle the expanded state
            boolean expanded = !idea.isExpanded();
            idea.setExpanded(expanded);

            // Update maxLines and "See More"/"See Less" text based on the expanded state
            holder.idea_title.setMaxLines(expanded ? Integer.MAX_VALUE : 5);
            holder.see_more.setText(expanded ? "See Less" : "See More");

            // After the toggle, we need to recheck if the text exceeds 5 lines
            holder.idea_title.post(() -> {
                if (holder.idea_title.getLineCount() > 5) {
                    holder.see_more.setVisibility(View.VISIBLE);
                } else {
                    holder.see_more.setVisibility(View.GONE);
                }
            });
        });
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
        if (idea.getUserImage() != null && !idea.getUserImage().isEmpty()){
            Glide.with(context).load(idea.getUserImage()).transform(new CircleCrop()).into(holder.user_profile_image);
        }else {
            holder.user_profile_image.setImageResource(R.drawable.baseline_person_24);
        }
        if (!idea.getImageUrl().isEmpty()) {
            holder.idea_image.setVisibility(View.VISIBLE);
            Glide.with(context).load(idea.getImageUrl()).into(holder.idea_image);
        } else {
            holder.idea_image.setVisibility(View.GONE);
        }

        holder.user_name.setText(idea.getUserName());
        holder.like_count.setText(String.valueOf(idea.getLikeCount()));
        holder.comment_count.setText(String.valueOf(idea.getCommentCount()));
        long postTime = idea.getTimestamp(); // Ensure this is in milliseconds
        CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(
                postTime,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
        );
        if (userId.equals(idea.getUserId())) {
            holder.dotes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openBottomDialog(idea);
                }
            });
        }else{
            holder.dotes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openBottomDialogReport(idea);
                }
            });
        }
        holder.post_date.setText(relativeTime);
        DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference("PublicIdeas")
                .child(idea.getIdeasId()).child("likes");
        DatabaseReference userLikesRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(idea.getUserId()).child("Ideas").child(idea.getIdeasId()).child("likes");
        setupLikeFeature(holder, likesRef, userLikesRef);
        setupCommentFeature(holder, idea.getIdeasId(),idea);
        if (idea.getUserId().equals(userId)) {
            holder.btn_follow.setVisibility(View.GONE);
            holder.dotes.setVisibility(View.VISIBLE);// Hide the button
        } else {
            holder.btn_follow.setVisibility(View.VISIBLE); // Show the button for others
            holder.dotes.setVisibility(View.VISIBLE);// Hide the button
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
                            .child(thatUserId) // ID of the user being followed
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
                                updateFollowState(idea.getUserId(), false);
                            } else {
                                // If not following, add to both lists
                                currentUserRef.setValue(true);
                                targetUserRef.setValue(true);
                                holder.btn_follow.setText("Following");
                                updateFollowState(idea.getUserId(), false);
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
        return (ideasList != null) ? ideasList.size() : 0;
    }
    private void setupLikeFeature(UserIdeasAdapter.UserIdeaHolder holder, DatabaseReference likesRef, DatabaseReference userLikesRef) {
        likesRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isLiked = snapshot.exists() && snapshot.getValue(Boolean.class);
                updateLikeIcon(holder.like_icon, isLiked);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        holder.like_icon.setOnClickListener(v -> likesRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    likesRef.child(userId).removeValue();
                    userLikesRef.child(userId).removeValue();
                    updateLikeCount(holder, -1);
                    updateLikeIcon(holder.like_icon, false);
                } else {
                    likesRef.child(userId).setValue(true);
                    userLikesRef.child(userId).setValue(true);
                    updateLikeCount(holder, 1);
                    animateLikeButton(holder.like_icon);
                    updateLikeIcon(holder.like_icon, true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        }));
    }
    private void updateFollowState(String userId, boolean isFollowing) {
        for (int i = 0; i < ideasList.size(); i++) {
            Idea idea = ideasList.get(i);
            if (idea.getUserId().equals(userId)) {
                idea.setFollowed(isFollowing); // Update the follow state in the dataset
                notifyItemChanged(i); // Notify the adapter to refresh this item
            }
        }
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
        if (saveButton) {
            save.setImageResource(R.drawable.baseline_bookmark_24); // Liked state
        } else {
            save.setImageResource(R.drawable.baseline_bookmark_border_24); // Not liked state

        }
    }
    private void setupCommentFeature(UserIdeasAdapter.UserIdeaHolder holder, String ideaId, Idea idea) {
        holder.comment_icon.setOnClickListener(v -> showCommentBottomSheet(ideaId, holder, idea));
    }

    private void showLikesBottomSheet(String ideaId) {
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
    private void showCommentBottomSheet(final String ideaId, UserIdeasAdapter.UserIdeaHolder holder, Idea idea) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        bottomSheetDialog.setContentView(LayoutInflater.from(context).inflate(R.layout.item_comment, null));
        ImageView userImage = bottomSheetDialog.findViewById(R.id.dialog_user_image);
        EditText commentInput = bottomSheetDialog.findViewById(R.id.message_input);
        ImageView addCommentButton = bottomSheetDialog.findViewById(R.id.buttonSend);
        RecyclerView recyclerViewComments = bottomSheetDialog.findViewById(R.id.recyclerViewComments);

        // Initialize RecyclerView
        recyclerViewComments.setLayoutManager(new LinearLayoutManager(context));
        List<CommentModel> commentsList = new ArrayList<>();
        CommentsAdapter commentsAdapter;
        if (userId.equals(idea.getUserId())) {
            commentsAdapter = new CommentsAdapter(commentsList, context, userId,fragmentManager,bottomSheetDialog);
        }else{
            commentsAdapter = new CommentsAdapter(commentsList, context, thatUserId,fragmentManager,bottomSheetDialog);
        }
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
        Map<String, Boolean> likes = new HashMap<>();
        String commentId = commentRef.push().getKey();
        CommentModel comment = new CommentModel(userId, commentText, System.currentTimeMillis(), username, profilePicUrl,commentId,0,likes,ideaId);

        if (commentId != null) {
            commentRef.child(commentId).setValue(comment);
            userCommentRef.child(commentId).setValue(comment);
        }
    }

    private void updateLikeIcon(ImageView likeIcon, boolean isLiked) {
        int nightModeFlags = likeIcon.getContext().getResources().getConfiguration().uiMode &
                android.content.res.Configuration.UI_MODE_NIGHT_MASK;

        if (nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
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

    private void updateLikeCount(UserIdeasAdapter.UserIdeaHolder holder, int delta) {
        int currentCount = Integer.parseInt(holder.like_count.getText().toString());
        holder.like_count.setText(String.valueOf(currentCount + delta));
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
    private void openBottomDialog(Idea idea) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.bottom_dialog_layout, null);
        bottomSheetDialog.setContentView(dialogView);

        TextView btnEdit = dialogView.findViewById(R.id.btn_edit);
        TextView btnDelete = dialogView.findViewById(R.id.btn_delete);

        btnEdit.setOnClickListener(v -> {
            // Handle Edit action
            Intent intent = new Intent(context, AddIdeasActivity.class);
            intent.putExtra("ideaDescription", idea.getContent());
            intent.putExtra("ideaId", idea.getIdeasId());// Pass the description
            context.startActivity(intent); // Start the EditIdeaActivity
            bottomSheetDialog.dismiss(); // Close the bottom sheet dialog
        });

        btnDelete.setOnClickListener(v -> {
            DatabaseReference ideasRef = FirebaseDatabase.getInstance().getReference("PublicIdeas")
                    .child(idea.getIdeasId());
            DatabaseReference saveRef = FirebaseDatabase.getInstance().getReference("Users")
                    .child(idea.getUserId()).child("SavedIdeas");




            // Reference to the user's "Ideas" node
            DatabaseReference userIdeasRef = FirebaseDatabase.getInstance().getReference("Users")
                    .child(idea.getUserId()).child("Ideas").child(idea.getIdeasId());

            // Show confirmation before deleting
            new AlertDialog.Builder(context)
                    .setTitle("Delete Idea")
                    .setMessage("Are you sure you want to delete this idea?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Delete the idea from both nodes
                        ideasRef.removeValue().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                userIdeasRef.removeValue().addOnCompleteListener(userTask -> {
                                    saveRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists()) {
                                                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                                    String storedIdeaId = childSnapshot.getValue(String.class); // Get value

                                                    if (storedIdeaId != null && storedIdeaId.equals(idea.getIdeasId())) {
                                                        // Delete the key where the value matches ideaId
                                                        childSnapshot.getRef().removeValue();

                                                    }
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                });
                            }
                        });
                        bottomSheetDialog.dismiss();
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        bottomSheetDialog.show();
    }
    @SuppressLint("MissingInflatedId")
    private void openBottomDialogReport(Idea idea) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.bottom_dialog_report, null);
        bottomSheetDialog.setContentView(dialogView);

       TextView btnReport = dialogView.findViewById(R.id.btnReport);

        btnReport.setOnClickListener(v -> {

            DatabaseReference userIdeasRef = FirebaseDatabase.getInstance().getReference("Report")
                    .child(idea.getUserId()).child(idea.getIdeasId());
            // Show confirmation before deleting
            new AlertDialog.Builder(context)
                    .setTitle("Report Idea")
                    .setMessage("Are you sure you want to Report this idea?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Delete the idea from both nodes
                        userIdeasRef.setValue(true).addOnCompleteListener(task -> {

                        });
                        bottomSheetDialog.dismiss();
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        bottomSheetDialog.show();
    }
    private void loadImage(ImageView imageView, String url, int placeholder) {
        Glide.with(context)
                .load(url)
                .placeholder(placeholder)
                .transform(new CircleCrop())
                .error(placeholder)
                .into(imageView);
    }
    public class UserIdeaHolder extends RecyclerView.ViewHolder {
        TextView user_name, idea_title, like_count, comment_count,post_date,see_more,IdeasTitle;
        ImageView like_icon, comment_icon, idea_image, user_profile_image,dotes,save;
        AppCompatButton btn_follow;
        public UserIdeaHolder(@NonNull View itemView) {
            super(itemView);
            user_name = itemView.findViewById(R.id.user_name);
            idea_image = itemView.findViewById(R.id.idea_image);
            idea_title = itemView.findViewById(R.id.idea_title);
            like_count = itemView.findViewById(R.id.like_count);
            comment_count = itemView.findViewById(R.id.comment_count);
            save = itemView.findViewById(R.id.save);
            post_date = itemView.findViewById(R.id.post_date);
            like_icon = itemView.findViewById(R.id.like_icon);
            comment_icon = itemView.findViewById(R.id.comment_icon);
            user_profile_image = itemView.findViewById(R.id.user_profile_image);
            dotes = itemView.findViewById(R.id.dotes);
            see_more = itemView.findViewById(R.id.see_more);
            IdeasTitle = itemView.findViewById(R.id.Ideas);
            btn_follow = itemView.findViewById(R.id.btn_follow);
        }
    }
}
