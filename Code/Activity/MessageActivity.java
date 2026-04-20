package com.example.businessidea.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import com.android.volley.toolbox.JsonObjectRequest;

import android.view.MenuInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;

import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.businessidea.AccessToken;
import com.example.businessidea.BaseActivity;
import com.example.businessidea.Module.Chats;

import com.example.businessidea.R;
import com.example.businessidea.RecycleView.MessageAdapter;
import com.example.businessidea.fragment.MessageFragment;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MessageActivity extends BaseActivity {
ImageView back_button,chat_user_image,send_button,dotes;
TextView chat_user_name;
RecyclerView chat_recycler_view;
EditText message_input;
MessageAdapter messageAdapter;
    String currentUserId;
    String Url;
    String User;
    String userid;
    List<Chats> chats;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        back_button = findViewById(R.id.back_button);
        chat_recycler_view = findViewById(R.id.chat_recycler_view);
        chat_user_image = findViewById(R.id.chat_user_image);
        send_button = findViewById(R.id.send_button);
        chat_user_name = findViewById(R.id.chat_user_name);
        message_input = findViewById(R.id.message_input);
        dotes = findViewById(R.id.dotes);

        dotes.setOnClickListener(v -> showPopupMenu(v));
        Intent intent = getIntent();
       userid = intent.getStringExtra("UserId");

        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            getWindow().getDecorView().setSystemUiVisibility(0); // Default light icons
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.back)); // Black status bar
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.back)); // Black navigation bar
        } else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR); // Dark icons
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white)); // White status bar
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.white)); // Black navigation bar
        }
        currentUserId= Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        chat_recycler_view.setLayoutManager(linearLayoutManager);

        send_button.setOnClickListener(v -> {
            String msg = message_input.getText().toString();
            if (!msg.equals("")){
                sendMessage(currentUserId,userid,msg, System.currentTimeMillis());
            }else {
                Toast.makeText(MessageActivity.this, "empty not send", Toast.LENGTH_SHORT).show();
            }
            message_input.setText("");
        });

        back_button.setOnClickListener(v -> startActivity(new Intent(MessageActivity.this, MessageFragment.class)));
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Url = snapshot.child("profilePic").getValue(String.class);
                User = snapshot.child("username").getValue(String.class);
                chat_user_name.setText(User);
                if (!Url.equals("")){
                    Glide.with(getApplicationContext()).load(Url).transform(new CircleCrop()).into(chat_user_image);
                }
                    readMessage(currentUserId, userid, Url);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.chat_options_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();

            if (id == R.id.view_profile) {
                viewUserProfile(); // Open Profile Activity

            } else if (id == R.id.mute_notifications) {
                toggleMuteNotifications(); // Mute or Unmute Chat Notifications

            } else if (id == R.id.clear_chat) {
                clearChatHistory(); // Clear Chat Messages

            } else {
                return false;
            }

            return true;
        });

        popupMenu.show();
    }


    private void viewUserProfile() {
        Intent intent = new Intent(this, HomeActivity.class); // Assuming MainActivity hosts fragments
        intent.putExtra("fragment", "Profile");
        intent.putExtra("userId",  userid); // Pass User ID
        intent.putExtra("isCurrentUser",  userid.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())); // Change based on logic
        startActivity(intent);
    }



    private void toggleMuteNotifications() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUserId);

        userRef.child("mutedChats").child(getIntent().getStringExtra("UserId")).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isMuted = snapshot.exists(); // If data exists, chat is muted

                if (isMuted) {
                    userRef.child("mutedChats").child(getIntent().getStringExtra("UserId")).removeValue();
                    Toast.makeText(MessageActivity.this, "Notifications Unmuted", Toast.LENGTH_SHORT).show();
                } else {
                    userRef.child("mutedChats").child(getIntent().getStringExtra("UserId")).setValue(true);
                    Toast.makeText(MessageActivity.this, "Notifications Muted", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("MuteNotification", "Error toggling mute: " + error.getMessage());
            }
        });
    }

    private void clearChatHistory() {
        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chats");

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Chats chat = ds.getValue(Chats.class);

                    if (chat != null) {
                        if ((chat.getSender().equals(currentUserId) && chat.getReceiver().equals(userid)) ||
                                (chat.getSender().equals(userid) && chat.getReceiver().equals(currentUserId))) {
                            ds.getRef().removeValue(); // Delete the entire chat entry

                        }
                    }
                }


                // Clear the UI
                chats.clear();
                if (messageAdapter != null) {
                    messageAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ClearChat", "Error clearing chat: " + error.getMessage());
            }
        });
    }


    private void sendNotificationToReceiver(String token, String message, String Url, String username) {
        // Create the notification payload
        JSONObject notification = new JSONObject();
        JSONObject notificationBody = new JSONObject();
        JSONObject messageBody = new JSONObject();

        try {
            // Notification details
            notificationBody.put("title", username);
            notificationBody.put("body", message);
            notificationBody.put("image", Url);


            // Message payload
            messageBody.put("notification", notificationBody);
            messageBody.put("token", token);

            notification.put("message", messageBody);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        // Create the HTTP request to send the notification via FCM
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                "Replace Your Url", // Replace with your FCM project endpoint
                notification,
                response -> Log.d("FCM", "Notification sent successfully: " + response),
                error -> Log.e("FCM", "Failed to send notification: " + error.getMessage())
        ) {
            @NonNull
            @Override
            public Map<String, String> getHeaders() {
                // Set the necessary headers for the FCM request
                AccessToken accessToken = new AccessToken();

                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; charset=UTF-8");
                headers.put("Authorization", "Bearer " + accessToken.getAccessToken()); // Replace with a method to fetch the access token
                return headers;
            }
        };

        // Add the request to the Volley request queue
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private void sendMessage(String sender, String receiver, String message, long timestamp) {
        DatabaseReference dataReference = FirebaseDatabase.getInstance().getReference("Chats");
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");

        // Generate a unique messageId
        String messageId = dataReference.push().getKey();
        if (messageId == null) return;

        // Create a HashMap for the message
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("messageId", messageId); // Add messageId
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        hashMap.put("timestamp", timestamp);
        hashMap.put("isRead", false); // Initially, the message is unread

        // Store the message in Firebase under its unique messageId
        dataReference.child(messageId).setValue(hashMap).addOnSuccessListener(aVoid -> {
            // Optional: You can add a callback or log a message here
        });

        // Check if the receiver has muted the sender
        usersRef.child(sender).child("mutedChats").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isMuted = false;

                if (snapshot.exists()) {
                    for (DataSnapshot mutedChat : snapshot.getChildren()) {
                        String mutedUserId = mutedChat.getKey(); // Get muted user ID
                        if (mutedUserId.equals(receiver)) { // ✅ Check if receiver is muted
                            isMuted = true;
                            break; // Stop loop when found
                        }
                    }
                }

                // If not muted, send a notification
                if (!isMuted) {
                    usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String username = snapshot.child(sender).child("username").getValue(String.class);
                            String profileUrl = snapshot.child(sender).child("profilePic").getValue(String.class);
                            String token = snapshot.child(receiver).child("token").getValue(String.class);

                            if (token != null) {
                                sendNotificationToReceiver(token, message, profileUrl, username);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("FCM", "Error fetching token: " + error.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("MuteChats", "Error fetching muted chats: " + error.getMessage());
            }
        });
    }


    private void readMessage(String myId, String userId, String imageUrl) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Chats");
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Chats> chats = new ArrayList<>();
                boolean hasUnreadMessages = false; // Flag to check for unread messages

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Chats chat = ds.getValue(Chats.class);

                    if (chat != null) {
                        // Check if the chat is between the current user and the target user
                        if ((chat.getReceiver().equals(myId) && chat.getSender().equals(userId)) ||
                                (chat.getReceiver().equals(userId) && chat.getSender().equals(myId))) {

                            chats.add(chat);

                            // Update the last message and unread count for the receiver
                            if (chat.getReceiver().equals(myId) && !chat.isRead()) {
                                ds.getRef().child("isRead").setValue(true);
                            }
                        }
                    }
                }


                // Update the adapter with the new chat data
                messageAdapter = new MessageAdapter(MessageActivity.this, chats, imageUrl);
                chat_recycler_view.setAdapter(messageAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ReadMessage", "Error reading messages: " + error.getMessage());
            }
        });
    }



}