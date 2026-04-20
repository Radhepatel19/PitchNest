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
import com.example.businessidea.RecycleView.FollowersAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FollowersFragment extends Fragment {

    RecyclerView FollowersRecycleView;
    FollowersAdapter adapter;
    List<Users> FollowersList;
    List<Users> FilteredList;
    FirebaseAuth auth;
    EditText search_bar;
    String CurrentUser;
    DatabaseReference databaseReference;
String data;
    public FollowersFragment() {
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_followers, container, false);

        FollowersRecycleView = view.findViewById(R.id.FollowersRecycleView);
        search_bar = view.findViewById(R.id.search_bar);
        auth = FirebaseAuth.getInstance();
        FollowersList = new ArrayList<>();
        FilteredList = new ArrayList<>();
        CurrentUser = auth.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        if (CurrentUser == null) {
            Log.e("Error", "User not logged in");
            return view;
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        FollowersRecycleView.setLayoutManager(layoutManager);
        if (getArguments() != null) {
            data = getArguments().getString("key");
            fetchFollowersList(data);
            adapter = new FollowersAdapter(getContext(), FilteredList,data,getParentFragmentManager());
        }else {
            fetchFollowersList(CurrentUser);
            adapter = new FollowersAdapter(getContext(), FilteredList,CurrentUser,getParentFragmentManager());
        }

        FollowersRecycleView.setAdapter(adapter);


        // Add TextWatcher to the search bar
        search_bar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterFollowersList(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No action needed
            }
        });

        return view;
    }

    private void fetchFollowersList(String data1) {
        databaseReference.child(data1).child("Followers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                FollowersList.clear();
                FilteredList.clear();
                if (!snapshot.exists()) {
                    adapter.notifyDataSetChanged(); // Update the adapter if list is empty
                    return;
                }

                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    String followersId = childSnapshot.getKey();
                    if (followersId != null) {
                        fetchUserDetails(followersId);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("FirebaseError", "Failed to fetch following list", error.toException());
            }
        });
    }

    private void fetchUserDetails(String followersId) {
        databaseReference.child(followersId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String ProfilePic = snapshot.child("profilePic").getValue(String.class);
                String UserName = snapshot.child("username").getValue(String.class);
                String Headline = snapshot.child("headline").getValue(String.class);


                    Users user = new Users(ProfilePic, UserName, Headline, followersId, null);
                    FollowersList.add(user);
                    FilteredList.add(user);
                    adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Failed to fetch user details", error.toException());
            }
        });
    }

    private void filterFollowersList(String query) {
        FilteredList.clear();
        if (query.isEmpty()) {
            FilteredList.addAll(FollowersList);
        } else {
            for (Users user : FollowersList) {
                if (user.getUsername().toLowerCase().contains(query.toLowerCase())) {
                    FilteredList.add(user);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }
    public static FollowersFragment newInstance(String data) {
        FollowersFragment fragment = new FollowersFragment();
        Bundle args = new Bundle();
        args.putString("key", data);
        fragment.setArguments(args);
        return fragment;
    }
}
