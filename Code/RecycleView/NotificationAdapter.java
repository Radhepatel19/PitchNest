package com.example.businessidea.RecycleView;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.businessidea.Activity.HomeActivity;
import com.example.businessidea.Module.Notification;
import com.example.businessidea.R;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Notification> notificationList;
    private static Context context;

    public NotificationAdapter(List<Notification> notificationList,Context context) {
      this.notificationList = notificationList;
      this.context = context;
    }

    // Define view types for each notification type
    private static final int TYPE_LIKE = 0;
    private static final int TYPE_COMMENT = 1;
    private static final int TYPE_FOLLOW = 2;

    @Override
    public int getItemViewType(int position) {
        Notification notification = notificationList.get(position);

        // Return the type based on the notification
        switch (notification.getType()) {
            case "like":
                return TYPE_LIKE;
            case "comment":
                return TYPE_COMMENT;
            case "follow":
                return TYPE_FOLLOW;
            default:
                return -1; // Default case if no match
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;

        // Inflate the appropriate layout based on the viewType
        switch (viewType) {
            case TYPE_LIKE:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.notification_like, parent, false);
                return new LikeViewHolder(itemView);
            case TYPE_COMMENT:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.notification_comment, parent, false);
                return new CommentViewHolder(itemView);
            case TYPE_FOLLOW:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.notification_follow, parent, false);
                return new FollowViewHolder(itemView);
            default:
                throw new IllegalArgumentException("Invalid view type");
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Notification notification = notificationList.get(position);

        // Bind data based on the view type
        switch (holder.getItemViewType()) {
            case TYPE_LIKE:
                ((LikeViewHolder) holder).bind(notification,context,notificationList,position);
                break;
            case TYPE_COMMENT:
                ((CommentViewHolder) holder).bind(notification,context,notificationList,position);
                break;
            case TYPE_FOLLOW:
                ((FollowViewHolder) holder).bind(notification,context,notificationList,position);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    // ViewHolder for Like Notification
    public class LikeViewHolder extends RecyclerView.ViewHolder {
        private ImageView senderImageView, postImageView;
        private TextView timestamp,Username;
        LinearLayout LikeLayout;

        public LikeViewHolder(View itemView) {
            super(itemView);
            LikeLayout = itemView.findViewById(R.id.LikeLayout);
            senderImageView = itemView.findViewById(R.id.senderImageLike);
            Username = itemView.findViewById(R.id.UserNameLike);
            postImageView = itemView.findViewById(R.id.imageLike);
            timestamp = itemView.findViewById(R.id.timestamp);
        }

        public void bind(Notification notification,Context context,List<Notification> notificationList,int position) {
            Glide.with(senderImageView.getContext())
                    .load(notification.getSenderImage())
                    .transform(new CircleCrop())
                    .into(senderImageView);
            long postTime = notification.getTimestamp(); // Ensure this is in milliseconds
            String timeAgo = getTimeAgo(postTime);
            timestamp.setText(timeAgo);
            Username.setText(notification.getUserName());
            Glide.with(postImageView.getContext())
                    .load(notification.getImage())
                    .into(postImageView);
            long currentTime = System.currentTimeMillis();

            // Check if the notification is older than 2 weeks
            long twoWeeksInMillis = 14L * 24 * 60 * 60 * 1000; // 2 weeks in milliseconds
            if (currentTime - notification.getTimestamp() > twoWeeksInMillis) {
                // Delete the notification from Firebase
                DatabaseReference notificationRef = FirebaseDatabase.getInstance()
                        .getReference("Notifications")
                        .child(notification.getKey());

                notificationRef.removeValue().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Remove the notification from the local list and notify the adapter
                        notificationList.remove(position);
                        notifyItemRemoved(position);

                    }
                });
            }
            LikeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    boolean c = notification.getSenderId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    Intent intent = new Intent(context, HomeActivity.class);
                    intent.putExtra("fragment","Profile");
                    intent.putExtra("userId",notification.getSenderId());
                    intent.putExtra("isCurrentUser",c);
                    context.startActivity(intent);
                }
            });
        }
    }

    // ViewHolder for Comment Notification
    public class CommentViewHolder extends RecyclerView.ViewHolder {
        private ImageView senderImageView, postImageView;
        private TextView UserName,commentText, timestamp;
        LinearLayout CommentLayout;

        public CommentViewHolder(View itemView) {
            super(itemView);
            CommentLayout =itemView.findViewById(R.id.CommentLayout);
            senderImageView = itemView.findViewById(R.id.senderImageViewComment);
            postImageView = itemView.findViewById(R.id.imageComment);
            UserName = itemView.findViewById(R.id.UserNameComment);
            commentText = itemView.findViewById(R.id.comment);
            timestamp = itemView.findViewById(R.id.timestamp);
        }

        public void bind(Notification notification,Context context,List<Notification> notificationList,int position) {
            Glide.with(senderImageView.getContext())
                    .load(notification.getSenderImage())
                    .transform(new CircleCrop())
                    .into(senderImageView);
            UserName.setText(notification.getUserName());
            commentText.setText(notification.getComment()+"."); // For comments
            long postTime = notification.getTimestamp(); // Ensure this is in milliseconds
            String timeAgo = getTimeAgo(postTime);
            timestamp.setText(timeAgo);
            CommentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean c = notification.getSenderId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    Intent intent = new Intent(context, HomeActivity.class);
                    intent.putExtra("fragment","Profile");
                    intent.putExtra("userId",notification.getSenderId());
                    intent.putExtra("isCurrentUser",c);
                    context.startActivity(intent);
                }
            });
            long currentTime = System.currentTimeMillis();

            // Check if the notification is older than 2 weeks
            long twoWeeksInMillis = 14L * 24 * 60 * 60 * 1000; // 2 weeks in milliseconds
            if (currentTime - notification.getTimestamp() > twoWeeksInMillis) {
                // Delete the notification from Firebase
                DatabaseReference notificationRef = FirebaseDatabase.getInstance()
                        .getReference("Notifications")
                        .child(notification.getKey());

                notificationRef.removeValue().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Remove the notification from the local list and notify the adapter
                        notificationList.remove(position);
                        notifyItemRemoved(position);

                    }
                });
            }
            Glide.with(postImageView.getContext())
                    .load(notification.getImage())
                    .into(postImageView);
        }
    }

    // ViewHolder for Follow Notification
    public class FollowViewHolder extends RecyclerView.ViewHolder {
        private ImageView senderImageView;
        private TextView Username, timestamp;
        LinearLayout FollowLayout;

        public FollowViewHolder(View itemView) {
            super(itemView);
            FollowLayout = itemView.findViewById(R.id.FollowLayout);
            senderImageView = itemView.findViewById(R.id.senderImageFollow);
            Username = itemView.findViewById(R.id.UserNameFollow);
            timestamp = itemView.findViewById(R.id.timestamp);
        }

        public void bind(Notification notification,Context context,List<Notification> notificationList,int position) {
            Glide.with(senderImageView.getContext())
                    .load(notification.getSenderImage())
                    .transform(new CircleCrop())
                    .into(senderImageView);
            Username.setText(notification.getUserName());
            long postTime = notification.getTimestamp(); // Ensure this is in milliseconds
            String timeAgo = getTimeAgo(postTime);
            FollowLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean c = notification.getSenderId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    Intent intent = new Intent(context, HomeActivity.class);
                    intent.putExtra("fragment","Profile");
                    intent.putExtra("userId",notification.getSenderId());
                    intent.putExtra("isCurrentUser",c);
                    context.startActivity(intent);
                }
            });
            timestamp.setText(timeAgo);
            // Get the current timestamp
            long currentTime = System.currentTimeMillis();

            // Check if the notification is older than 2 weeks
            long twoWeeksInMillis = 14L * 24 * 60 * 60 * 1000; // 2 weeks in milliseconds
            if (currentTime - notification.getTimestamp() > twoWeeksInMillis) {
                // Delete the notification from Firebase
                DatabaseReference notificationRef = FirebaseDatabase.getInstance()
                        .getReference("Notifications")
                        .child(notification.getKey());

                notificationRef.removeValue().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Remove the notification from the local list and notify the adapter
                        notificationList.remove(position);
                        notifyItemRemoved(position);

                    }
                });
            }
        }
    }

    public static String getTimeAgo(long timestamp) {
        long currentTime = System.currentTimeMillis();
        long diff = currentTime - timestamp;

        if (diff < TimeUnit.HOURS.toMillis(1)) {
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
            return minutes + "m";
        } else if (diff < TimeUnit.DAYS.toMillis(1)) {
            long hours = TimeUnit.MILLISECONDS.toHours(diff);
            return hours + "h";
        } else if (diff < TimeUnit.DAYS.toMillis(7)) {
            long days = TimeUnit.MILLISECONDS.toDays(diff);
            return days + "d";
        } else if (diff < TimeUnit.DAYS.toMillis(30)) {
            long weeks = TimeUnit.MILLISECONDS.toDays(diff) / 7;
            return weeks + "w";
        } else {
            long months = TimeUnit.MILLISECONDS.toDays(diff) / 30;
            return months + "mo";
        }
    }
}
