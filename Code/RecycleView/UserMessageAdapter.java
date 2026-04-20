package com.example.businessidea.RecycleView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.businessidea.Activity.MessageActivity;
import com.example.businessidea.Module.Chats;
import com.example.businessidea.Module.Users;  // Change from Idea to Users
import com.example.businessidea.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class UserMessageAdapter extends RecyclerView.Adapter<UserMessageAdapter.UserHolder> {
    private final Context context;
    private final List<Users> mUsers;
    private final FirebaseAuth auth;
    private final FirebaseUser firebaseUser;
    private final DatabaseReference chatsRef;
    private final String currentUserId;

    // Constructor
    public UserMessageAdapter(Context context, List<Users> mUsers) {
        this.context = context;
        this.mUsers = mUsers;
        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        currentUserId = firebaseUser.getUid();
        chatsRef = FirebaseDatabase.getInstance().getReference("Chats");
    }

    @NonNull
    @Override
    public UserMessageAdapter.UserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_list, parent, false);
        return new UserHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserMessageAdapter.UserHolder holder, int position) {
        Users user = mUsers.get(position);

        // Set user name
        holder.user_name.setText(user.getUsername());

        // Load profile image
        String imageUrl = user.getProfilePic();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context).load(imageUrl).transform(new CircleCrop()).into(holder.user_profile_image);
        }

        // Fetch and display the last message for this user
        fetchLastMessage(user.getUserId(), holder.last_message);

        fetchUnreadCount(holder,user.getUserId());
        // Navigate to MessageActivity on item click
        holder.itemViewLayout.setOnClickListener(v -> {
            Intent intent = new Intent(context, MessageActivity.class);

            intent.putExtra("UserId", user.getUserId());
            context.startActivity(intent);
        });
        holder.itemViewLayout.setOnLongClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete")
                    .setMessage("Are you sure you want to delete this item?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Remove the item from the list
                        mUsers.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, mUsers.size());

                        // Optionally, delete from Firebase
                    })
                    .setNegativeButton("No", null)
                    .show();
            return true; // Returning true means the long press is consumed
        });
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public static class UserHolder extends RecyclerView.ViewHolder {
        ImageView user_profile_image;
        TextView user_name, last_message, unread_count;
        LinearLayout itemViewLayout;

        public UserHolder(@NonNull View itemView) {
            super(itemView);
            itemViewLayout = itemView.findViewById(R.id.itemViewLayout);
            user_profile_image = itemView.findViewById(R.id.user_profile_image);
            user_name = itemView.findViewById(R.id.user_name);
            last_message = itemView.findViewById(R.id.last_message);
            unread_count = itemView.findViewById(R.id.unread_count);
        }
    }

    // Fetch the last message between the current user and the selected user
    private void fetchLastMessage(String userId, TextView lastMessageView) {
        chatsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String lastMessage = "No Message";

                for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
                    Chats chat = chatSnapshot.getValue(Chats.class);
                    if (chat != null) {
                        boolean isChatBetweenUsers =
                                (chat.getSender().equals(currentUserId) && chat.getReceiver().equals(userId)) ||
                                        (chat.getSender().equals(userId) && chat.getReceiver().equals(currentUserId));
                        if (isChatBetweenUsers) {
                            lastMessage = chat.getMessage();
                        }
                    }
                }
                lastMessageView.setText(lastMessage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                lastMessageView.setText("Error fetching message");
            }
        });
    }
    private void fetchUnreadCount(UserMessageAdapter.UserHolder holder, String chatUserId) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String currentUserId = currentUser.getUid();
        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chats");

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int unreadCount = 0;

                for (DataSnapshot ds : snapshot.getChildren()) {
                    if (ds.exists()) {
                        String receiverId = ds.child("receiver").getValue(String.class);
                        String senderId = ds.child("sender").getValue(String.class);
                        Boolean isRead = ds.child("isRead").getValue(Boolean.class);

                        // Check if message is sent to the current user, from the chat user, and is unread
                        if (receiverId != null && senderId != null && isRead != null) {
                            if (receiverId.equals(currentUserId) && senderId.equals(chatUserId) && !isRead) {
                                unreadCount++; // Count unread messages
                            }
                        }
                    }
                }

                // Update UI based on the unread count
                if (unreadCount > 0) {
                    holder.unread_count.setVisibility(View.VISIBLE);
                    holder.unread_count.setText(String.valueOf(unreadCount)); // Display unread count
                } else {
                    holder.unread_count.setVisibility(View.GONE); // Hide if no unread messages
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("UnreadCount", "Error fetching unread messages: " + error.getMessage());
            }
        });
    }



}