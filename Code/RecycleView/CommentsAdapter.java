package com.example.businessidea.RecycleView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.businessidea.Module.CommentModel;
import com.example.businessidea.R;
import com.example.businessidea.fragment.ProfileFragment;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import java.util.HashMap;
import java.util.List;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {

    private List<CommentModel> comments;
    String userId;
    Context context;
    private final FirebaseAuth auth;
    private final FirebaseUser firebaseUser;
    private FragmentManager fragmentManager;
    String thatUserId;
    BottomSheetDialog bottomSheetDialog;
    public CommentsAdapter(List<CommentModel> comments,Context context,String userId,FragmentManager fragmentManager,BottomSheetDialog bottomSheetDialog) {
        this.comments = comments;
        this.context = context;
        this.userId = userId;
        this.auth = FirebaseAuth.getInstance();
        this.firebaseUser = auth.getCurrentUser();
        this.thatUserId = (firebaseUser != null) ? firebaseUser.getUid() : null;
        this.fragmentManager = fragmentManager;
        this.bottomSheetDialog = bottomSheetDialog;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dialog_add_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        CommentModel comment = comments.get(position);
        holder.commentText.setText(comment.getCommentText());
        holder.commentUsername.setText(comment.getUsername());
        holder.commentTimestamp.setText(formatTimestamp(comment.getTimestamp())); // Format the timestamp
        holder.likeCount.setText(String.valueOf(comment.getLikeCount()));
        if (!holder.userImage.equals("")) {
            Glide.with(context).load(comment.getUserImage()).transform(new CircleCrop()).into(holder.userImage);
        }
        int nightModeFlags = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        boolean isDarkMode = (nightModeFlags == Configuration.UI_MODE_NIGHT_YES);

// Update the like icon based on the current user's like status and theme
        if (comment.getLikes() != null && comment.getLikes().containsKey(thatUserId) && comment.getLikes().get(thatUserId)) {
            holder.likeIcon.setImageResource(isDarkMode ? R.drawable.baseline_favorite_24 : R.drawable.baseline_favorite_24);
        } else {
            holder.likeIcon.setImageResource(isDarkMode ? R.drawable.baseline_favorite_border_241 : R.drawable.baseline_favorite_border_24);
        }
        holder.LinearAddComment.setOnLongClickListener(v -> {
            if (comment.getUserId().equals(thatUserId)) { // Check if the logged-in user is the comment owner
                new AlertDialog.Builder(context)
                        .setTitle("Delete Comment")
                        .setMessage("Are you sure you want to delete this comment?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            deleteCommentFromFirebase(comment);
                            comments.remove(position); // Remove the comment from the local list
                            notifyItemRemoved(position); // Notify the adapter of the removal
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            } else {
                Toast.makeText(context, "You can only delete your own comments.", Toast.LENGTH_SHORT).show();
            }
            return true;
        });
        holder.LinearAddComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new android.os.Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ProfileFragment fragmentB = new ProfileFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("userId", comment.getUserId());
                        bundle.putBoolean("isCurrentUser", comment.getUserId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()));
                        fragmentB.setArguments(bundle);

                        FragmentTransaction transaction = fragmentManager.beginTransaction();
                        transaction.replace(R.id.container, fragmentB); // Replace with your container ID
                        transaction.addToBackStack(null); // Optional: to add this transaction to the back stack
                        transaction.commit();
                        if (bottomSheetDialog != null && bottomSheetDialog.isShowing()) {
                            bottomSheetDialog.dismiss();
                        }
                    }
                }, 200);
            }
        });
        // Handle like button click
        holder.likeIcon.setOnClickListener(v -> {
            boolean isLiked = comment.getLikes() != null && comment.getLikes().containsKey(thatUserId) && comment.getLikes().get(thatUserId);

            DatabaseReference publicCommentRef = FirebaseDatabase.getInstance().getReference("PublicIdeas")
                    .child(comment.getIdeaId()).child("comments").child(comment.getCommentId());
            DatabaseReference userCommentRef = FirebaseDatabase.getInstance().getReference("Users")
                    .child(userId).child("Ideas").child(comment.getIdeaId()).child("comments").child(comment.getCommentId());

            if (isLiked) {
                // Unlike the comment
                publicCommentRef.child("likes").child(thatUserId).removeValue();
                userCommentRef.child("likes").child(thatUserId).removeValue();
                publicCommentRef.child("likeCount").setValue(comment.getLikeCount() - 1);
                userCommentRef.child("likeCount").setValue(comment.getLikeCount() - 1);

                comment.getLikes().remove(userId);
                comment.setLikeCount(comment.getLikeCount() - 1);
                holder.likeIcon.setImageResource(R.drawable.baseline_favorite_border_24);
            } else {
                // Like the comment
                publicCommentRef.child("likes").child(thatUserId).setValue(true);
                userCommentRef.child("likes").child(thatUserId).setValue(true);
                publicCommentRef.child("likeCount").setValue(comment.getLikeCount() + 1);
                userCommentRef.child("likeCount").setValue(comment.getLikeCount() + 1);

                if (comment.getLikes() == null) {
                    comment.setLikes(new HashMap<>());
                }
                comment.getLikes().put(userId, true);
                comment.setLikeCount(comment.getLikeCount() + 1);
                holder.likeIcon.setImageResource(R.drawable.baseline_favorite_24);
            }

            // Update the like count text
            holder.likeCount.setText(String.valueOf(comment.getLikeCount()));
        });

    }
    private String formatTimestamp(long timestamp) {
        long currentTime = System.currentTimeMillis();
        long diff = currentTime - timestamp;

        if (diff < 60 * 60 * 1000) {
            return (diff / (60 * 1000)) + "m"; // Minutes
        } else if (diff < 24 * 60 * 60 * 1000) {
            return (diff / (60 * 60 * 1000)) + "h"; // Hours
        } else {
            return (diff / (24 * 60 * 60 * 1000)) + "d"; // Days
        }
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }
    private void deleteCommentFromFirebase(CommentModel comment) {
        DatabaseReference publicCommentRef = FirebaseDatabase.getInstance().getReference("PublicIdeas")
                .child(comment.getIdeaId()).child("comments").child(comment.getCommentId());
        DatabaseReference userCommentRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(userId).child("Ideas").child(comment.getIdeaId()).child("comments").child(comment.getCommentId());

        publicCommentRef.removeValue();
        userCommentRef.removeValue();
    }
    static class CommentViewHolder extends RecyclerView.ViewHolder {
        ImageView userImage, likeIcon;
        TextView commentText, commentUsername,commentTimestamp,likeCount;
        LinearLayout LinearAddComment;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            userImage = itemView.findViewById(R.id.comment_user_image);
            commentText = itemView.findViewById(R.id.comment_text);
            LinearAddComment = itemView.findViewById(R.id.LinearAddComment);
            commentUsername = itemView.findViewById(R.id.comment_username);
            commentTimestamp = itemView.findViewById(R.id.comment_timestamp);
            likeIcon = itemView.findViewById(R.id.comment_like_icon);
            likeCount = itemView.findViewById(R.id.comment_like_count);
        }
    }

}
