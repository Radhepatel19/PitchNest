package com.example.businessidea.RecycleView;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.businessidea.Activity.HomeActivity;
import com.example.businessidea.Activity.MessageActivity;
import com.example.businessidea.Module.Users;
import com.example.businessidea.R;
import com.example.businessidea.fragment.ProfileFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class FollowingAdapter extends RecyclerView.Adapter<FollowingAdapter.FollowingHolder> {
    Context context;
    List<Users> FollowingList;
    String data;
    FirebaseAuth auth;
    private FragmentManager fragmentManager;
    String CurrentUser;
    public FollowingAdapter(Context context, List<Users> FollowingList,String data, FragmentManager fragmentManager) {
        this.context = context;
        this.FollowingList = FollowingList;
        this.data = data;
        auth = FirebaseAuth.getInstance();
        CurrentUser = auth.getUid();
        this.fragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public FollowingAdapter.FollowingHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_list_follow,parent,false);
        return new FollowingAdapter.FollowingHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FollowingAdapter.FollowingHolder holder, @SuppressLint("RecyclerView") int position) {
        Users users = FollowingList.get(position);

        holder.usernameF.setText(users.getUsername());
        holder.HeadlineF.setText(users.getHeadline());
        holder.UserToProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, HomeActivity.class);
                intent.putExtra("userId",users.getUserId());
                intent.putExtra("fragment","Profile");
                context.startActivity(intent);
            }
        });
        if (users.getProfilePic() != null && !users.getProfilePic().isEmpty()) {
            Glide.with(context)
                    .load(users.getProfilePic())
                    .transform(new CircleCrop())
                    .into(holder.profileImageF);
        } else {
            holder.profileImageF.setImageResource(R.drawable.baseline_person_24); // Use a default image
        }
        holder.MessageF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MessageActivity.class);
                intent.putExtra("UserId",users.getUserId());
                context.startActivity(intent);
            }
        });
        if (!CurrentUser.equals(data)){
            holder.UnFollow.setVisibility(View.GONE);
        }else {
            holder.UnFollow.setVisibility(View.VISIBLE);
            holder.UnFollow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popupMenu = new PopupMenu(context, holder.UnFollow);
                    popupMenu.inflate(R.menu.menu_user_option); // Inflate menu from XML
                    // Set the popup menu style dynamically based on the theme
                    popupMenu.setOnMenuItemClickListener(item -> {
                        // Using if-else if instead of switch
                        if (item.getItemId() == R.id.unfollow) {
                            unfollowUser(users,position,FollowingAdapter.this);
                            return true;
                        } else if (item.getItemId() == R.id.report) {
                            reportUser(users);
                            return true;
                        } else {
                            return false;
                        }
                    });
                    popupMenu.show();
                }
            });
        }
    }
    private void unfollowUser(Users user,int position,FollowingAdapter adapter) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Get current user's ID
        String unfollowUserId = user.getUserId(); // Get the user to unfollow's ID

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        // Path to the current user's following list
        DatabaseReference currentUserFollowingRef = databaseReference.child("Users").child(currentUserId).child("Following").child(unfollowUserId);

        // Path to the unfollowed user's followers list
        DatabaseReference unfollowUserFollowersRef = databaseReference.child("Users").child(unfollowUserId).child("Followers").child(currentUserId);

        // Remove the user from the current user's following list
        currentUserFollowingRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Remove the current user from the unfollowed user's followers list
                adapter.removeFollower(position);
                unfollowUserFollowersRef.removeValue().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        // Notify the adapter or update the UI if necessary
                        // Example: adapter.removeFollower(position);
                    }
                });
            }
        });
    }
    public void removeFollower(int position) {
        FollowingList.remove(position); // Remove from the list
        notifyItemRemoved(position);   // Notify the adapter to update the RecyclerView
    }
    private void reportUser(Users user) {
        Toast.makeText(context, "Reported " + user.getUsername(), Toast.LENGTH_SHORT).show();
        // Add report logic here
    }

    @Override
    public int getItemCount() {
        return FollowingList.size();
    }

    public class FollowingHolder extends RecyclerView.ViewHolder{
        TextView usernameF,HeadlineF;
        ImageView profileImageF,UnFollow;
        AppCompatButton MessageF;
        LinearLayout UserToProfile;
        public FollowingHolder(@NonNull View itemView) {
            super(itemView);
            usernameF = itemView.findViewById(R.id.usernameF);
            HeadlineF = itemView.findViewById(R.id.HeadlineF);
            profileImageF = itemView.findViewById(R.id.profileImageF);
            UserToProfile = itemView.findViewById(R.id.UserToProfile);
            UnFollow = itemView.findViewById(R.id.UnFollow);
            MessageF = itemView.findViewById(R.id.MessageF);
        }
    }
}
