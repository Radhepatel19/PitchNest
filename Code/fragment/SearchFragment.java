package com.example.businessidea.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.businessidea.Module.Users;
import com.example.businessidea.R;
import com.example.businessidea.RecycleView.UserAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SearchFragment extends Fragment {

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    RecyclerView recyclerView;
    EditText search_bar;
    List<Users> userList,randomUserList;
    UserAdapter userAdapter;
    TextView noUsersFoundText;
    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        recyclerView = view.findViewById(R.id.recycler_search_users);
        search_bar = view.findViewById(R.id.search_bar);
       noUsersFoundText = view.findViewById(R.id.no_users_found_text);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        userList = new ArrayList<>();
        randomUserList = new ArrayList<>();
        userAdapter = new UserAdapter(randomUserList, getContext(),getParentFragmentManager());
        recyclerView.setAdapter(userAdapter);

        // Load all users
        loadUsers();
        // Add Text Change Listener to Filter Results
        search_bar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().isEmpty()) {
                    // Show random 15 users when search is empty
                    showRandomUsers();
                } else {
                    // Search and filter users
                    searchUsers(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        return view;
    }
    private void searchUsers(String query) {
        List<Users> filteredList = new ArrayList<>();
        for (Users user : userList) {
            if (user.getUsername().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(user);
            }
        }
        userAdapter.updateList(filteredList);
        // Show or hide "No users found" text based on filtered results
        if (filteredList.isEmpty()) {
            noUsersFoundText.setVisibility(View.VISIBLE); // Show message
        } else {
            noUsersFoundText.setVisibility(View.GONE); // Hide message
        }
    }
    private void loadUsers() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Users user = data.getValue(Users.class);
                    userList.add(user);
                }
                // Show random 15 users initially
                showRandomUsers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error loading users", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void showRandomUsers() {
        randomUserList.clear();
        if (userList.size() > 15) {
            // Shuffle and pick 15 random users
            Collections.shuffle(userList);
            randomUserList.addAll(userList.subList(0, 15));
        } else {
            // If less than 15 users, show all
            randomUserList.addAll(userList);
        }
        userAdapter.notifyDataSetChanged();
    }
}