package com.example.businessidea.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.businessidea.Module.Users;
import com.example.businessidea.R;
import com.example.businessidea.RecycleView.FollowingAdapter;
import com.example.businessidea.RecycleView.IdeaAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.List;


public class FollowingFragment extends Fragment {

    RecyclerView FollowingRecycleView;
    FollowingAdapter adapter;
    List<Users> FollowingList;
    FirebaseAuth auth;
    EditText search_bar_following;
    String CurrentUser;
    List<Users> FilteredList;
    DatabaseReference databaseReference;
String data;
    public FollowingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_following, container, false);

        FollowingRecycleView = view.findViewById(R.id.FollowingRecycleView);
        search_bar_following = view.findViewById(R.id.search_bar_following);
        auth = FirebaseAuth.getInstance();
        FollowingList = new ArrayList<>();
        FilteredList = new ArrayList<>();
        CurrentUser = auth.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        if (CurrentUser == null) {
            Log.e("Error", "User not logged in");
            return view;
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        FollowingRecycleView.setLayoutManager(layoutManager);
        if (getArguments() != null) {
            data = getArguments().getString("key");
            fetchFollowingList(data);
            adapter = new FollowingAdapter(getContext(), FilteredList,data,getParentFragmentManager());
        }else {
            fetchFollowingList(CurrentUser);
            adapter = new FollowingAdapter(getContext(), FilteredList,CurrentUser,getParentFragmentManager());
        }


        FollowingRecycleView.setAdapter(adapter);

        // Add TextWatcher to the search bar
        search_bar_following.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterFollowingList(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No action needed
            }
        });
        return view;
    }

    private void fetchFollowingList(String UserId) {
        databaseReference.child(UserId).child("Following").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                FollowingList.clear();
                FilteredList.clear();
                if (!snapshot.exists()) {
                    Log.d("FollowingFragment", "No following users found");
                    adapter.notifyDataSetChanged(); // Update the adapter if list is empty
                    return;
                }

                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    String followingId = childSnapshot.getKey();
                    if (followingId != null) {
                        fetchUserDetails(followingId);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("FirebaseError", "Failed to fetch following list", error.toException());
            }
        });
    }

    private void fetchUserDetails(String followingId) {
        databaseReference.child(followingId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String ProfilePic = snapshot.child("profilePic").getValue(String.class);
                String UserName = snapshot.child("username").getValue(String.class);
                String Headline = snapshot.child("headline").getValue(String.class);


                Users user = new Users(ProfilePic, UserName, Headline, followingId, null);
                FollowingList.add(user);
                FilteredList.add(user);
                    adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Failed to fetch user details", error.toException());
            }
        });
    }
    private void filterFollowingList(String query) {
        FilteredList.clear();
        if (query.isEmpty()) {
            FilteredList.addAll(FollowingList);
        } else {
            for (Users user : FollowingList) {
                if (user.getUsername().toLowerCase().contains(query.toLowerCase())) {
                    FilteredList.add(user);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }
    public static FollowingFragment newInstance(String data) {
        FollowingFragment fragment = new FollowingFragment();
        Bundle args = new Bundle();
        args.putString("key", data);
        fragment.setArguments(args);
        return fragment;
    }
}