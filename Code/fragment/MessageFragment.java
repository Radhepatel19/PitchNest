package com.example.businessidea.fragment;

import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.businessidea.Module.Chats;

import com.example.businessidea.Module.Users;
import com.example.businessidea.R;
import com.example.businessidea.RecycleView.UserMessageAdapter;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MessageFragment extends Fragment {

    private RecyclerView recyclerView;
    private UserMessageAdapter userAdapter;
    private List<Users> userList; // List of user details to display
    private Set<String> userIds; // Unique IDs of users involved in chats
    EditText search_bar;
    private DatabaseReference chatsReference;
    private DatabaseReference usersReference;
    private String currentUserId;

    private static final String TAG = "MessageFragment"; // Tag for logging

    public MessageFragment() {
        // Required empty public constructor

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_message, container, false);

        recyclerView = view.findViewById(R.id.recycler_user_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        search_bar = view.findViewById(R.id.search_bar);
        userList = new ArrayList<>();
        userIds = new HashSet<>();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();


        chatsReference = FirebaseDatabase.getInstance().getReference("Chats");
        usersReference = FirebaseDatabase.getInstance().getReference("Users");

        fetchChatUsers();

        return view;
    }

    private void fetchChatUsers() {


        chatsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {


                userIds.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Chats chat = ds.getValue(Chats.class);
                    if (chat != null) {
                        if (chat.getSender().equals(currentUserId)) {
                            userIds.add(chat.getReceiver());

                        }
                        if (chat.getReceiver().equals(currentUserId)) {
                            userIds.add(chat.getSender());

                        }
                    }
                }


                fetchUserDetails();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void fetchUserDetails() {
        usersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {


                userList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Users user = ds.getValue(Users.class);
                    if (user != null) {

                        if (userIds.contains(user.getUserId())) {
                            userList.add(user);
                            Log.d(TAG, "Added user: " + user.getUserId());
                        }
                    }
                }

                Log.d(TAG, "User list size: " + userList.size());

                    userAdapter = new UserMessageAdapter(getContext(), userList);
                    recyclerView.setAdapter(userAdapter);
                    userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error fetching users: " + error.getMessage());
            }
        });
    }

}
