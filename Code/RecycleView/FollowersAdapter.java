package com.example.businessidea.RecycleView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import android.widget.LinearLayout;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;

import com.example.businessidea.Activity.HomeActivity;
import com.example.businessidea.Activity.MessageActivity;
import com.example.businessidea.Activity.TabLayoutActivity;
import com.example.businessidea.Module.Users;
import com.example.businessidea.R;
import com.example.businessidea.fragment.ProfileFragment;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import java.util.ArrayList;
import java.util.List;

public class FollowersAdapter extends RecyclerView.Adapter<FollowersAdapter.FollowersHolder> {
    Context context;
    List<Users> FollowersList;
    FirebaseAuth auth;
    private FragmentManager fragmentManager;
    String CurrentUser;
    String data;
    public FollowersAdapter(Context context, List<Users> FollowersList,String data, FragmentManager fragmentManager) {
        this.context = context;
        this.FollowersList = FollowersList;
        this.data = data;
        auth = FirebaseAuth.getInstance();
        CurrentUser = auth.getUid();
        this.fragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public FollowersAdapter.FollowersHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_list_follow,parent,false);
        return new FollowersAdapter.FollowersHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FollowersAdapter.FollowersHolder holder, @SuppressLint("RecyclerView") int position) {
        Users users = FollowersList.get(position);

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
        }else{
            holder.UnFollow.setVisibility(View.VISIBLE);
            holder.UnFollow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showRemoveFollowerDialog(CurrentUser,users.getUserId(),position,FollowersAdapter.this);
                }
            });
        }
    }
    private void showRemoveFollowerDialog(String userId,String followersId, int position, FollowersAdapter adapter) {
        // Create BottomSheetDialog
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.bottom_dialog_followers, null);
        bottomSheetDialog.setContentView(dialogView);

        // Initialize buttons
        Button btnRemoveFollower = dialogView.findViewById(R.id.btnRemoveFollower);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        // Remove follower button logic
        btnRemoveFollower.setOnClickListener(v -> {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

            // Path to the specific follower
            DatabaseReference followerRef = databaseReference.child("Users").child(userId).child("Followers").child(followersId);
            DatabaseReference following = databaseReference.child("Users").child(followersId).child("Following");

            // Remove the follower
            followerRef.removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Successfully removed follower
                    adapter.removeFollower(position);

                    System.out.println("Follower removed successfully!");
                } else {
                    // Handle failure
                    System.err.println("Failed to remove follower: " + task.getException());
                }
            });
            following.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {

                }
            });
            bottomSheetDialog.dismiss();
        });

        // Cancel button logic
        btnCancel.setOnClickListener(v -> bottomSheetDialog.dismiss());

        // Show dialog
        bottomSheetDialog.show();
    }
    public void removeFollower(int position) {
        FollowersList.remove(position); // Remove from the list
        notifyItemRemoved(position);   // Notify the adapter to update the RecyclerView
    }
    @Override
    public int getItemCount() {
        return FollowersList.size();
    }

    public class FollowersHolder extends RecyclerView.ViewHolder{
        TextView usernameF,HeadlineF;
        ImageView profileImageF,UnFollow;
        AppCompatButton MessageF;
        LinearLayout UserToProfile;
        public FollowersHolder(@NonNull View itemView) {
            super(itemView);
            usernameF = itemView.findViewById(R.id.usernameF);
            UserToProfile = itemView.findViewById(R.id.UserToProfile);
            HeadlineF = itemView.findViewById(R.id.HeadlineF);
            profileImageF = itemView.findViewById(R.id.profileImageF);
            UnFollow = itemView.findViewById(R.id.UnFollow);
            MessageF = itemView.findViewById(R.id.MessageF);
        }
    }
}

