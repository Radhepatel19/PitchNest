package com.example.businessidea.RecycleView;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.businessidea.Module.Users;
import com.example.businessidea.R;
import com.example.businessidea.fragment.ProfileFragment;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private List<Users> userList;
    private Context context;
    private FragmentManager fragmentManager;
    public UserAdapter(List<Users> userList, Context context, FragmentManager fragmentManager) {
        this.userList = userList;
        this.context = context;
        this.fragmentManager = fragmentManager;
    }

    public void updateList(List<Users> updatedList) {
        this.userList = updatedList;
        notifyDataSetChanged();
    }

    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_pitchnest, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        Users user = userList.get(position);
        holder.userName.setText(user.getUsername());
        // Load Profile Image (Use Glide or Picasso)
        if (!user.getProfilePic().equals("")){
            Glide.with(context).load(user.getProfilePic()).placeholder(R.drawable.baseline_person_24).transform(new CircleCrop()).into(holder.userProfileImage);
        }
        holder.headline.setText(user.getHeadline());
        holder.itemView.setOnClickListener(v -> {
            ProfileFragment fragmentB = new ProfileFragment();
            Bundle bundle = new Bundle();
            bundle.putString("userId", user.getUserId());
            bundle.putBoolean("isCurrentUser", user.getUserId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()));
            fragmentB.setArguments(bundle);
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.container, fragmentB); // Replace with your container ID
            transaction.addToBackStack(null); // Optional: to add this transaction to the back stack
            transaction.commit();
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView userProfileImage;
        TextView userName,headline;
        LinearLayout itemViewLayout;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userProfileImage = itemView.findViewById(R.id.user_profile_image1);
            userName = itemView.findViewById(R.id.user_name);
            headline = itemView.findViewById(R.id.headline1);
            itemViewLayout = itemView.findViewById(R.id.itemViewLayout);
        }
    }
}
