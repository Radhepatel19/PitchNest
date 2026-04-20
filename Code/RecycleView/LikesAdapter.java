package com.example.businessidea.RecycleView;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class LikesAdapter extends RecyclerView.Adapter<LikesAdapter.LikesViewHolder> {
    private List<Users> userList;
    Context context;
    FirebaseAuth auth;
    FirebaseUser firebaseUser;
    private FragmentManager fragmentManager;
    String thatUserId;
    BottomSheetDialog bottomSheetDialog;

    public LikesAdapter(List<Users> userList, Context context,FragmentManager fragmentManager,BottomSheetDialog bottomSheetDialog) {
        this.userList = userList;
        this.context = context;
        this.auth = FirebaseAuth.getInstance();
        this.firebaseUser = auth.getCurrentUser();
        this.thatUserId = (firebaseUser != null) ? firebaseUser.getUid() : null;
        this.fragmentManager = fragmentManager;
        this.bottomSheetDialog = bottomSheetDialog;
    }

    @NonNull
    @Override
    public LikesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_pitchnest, parent, false);
        return new LikesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LikesViewHolder holder, int position) {
        Users user = userList.get(position);
        holder.username.setText(user.getUsername());
        holder.headline1.setText(user.getHeadline());
        Glide.with(context).load(user.getProfilePic()).transform(new CircleCrop()).into(holder.profileImageF);
        holder.itemViewLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new android.os.Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ProfileFragment fragmentB = new ProfileFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("userId", user.getUserId());
                        bundle.putBoolean("isCurrentUser", user.getUserId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()));
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


    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class LikesViewHolder extends RecyclerView.ViewHolder {
        TextView username, headline1;
        ImageView profileImageF;
        LinearLayout itemViewLayout;

        public LikesViewHolder(@NonNull View itemView) {
            super(itemView);

            itemViewLayout = itemView.findViewById(R.id.itemViewLayout);
            profileImageF = itemView.findViewById(R.id.user_profile_image1);
            username = itemView.findViewById(R.id.user_name);
            headline1 = itemView.findViewById(R.id.headline1);
        }
    }
}

