package com.example.businessidea.Activity;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.businessidea.ChatViewModel;
import com.example.businessidea.Module.AiChatsMessage;
import com.example.businessidea.R;

import com.example.businessidea.RecycleView.AiChatAdapter;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.List;


public class AiActivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private ChatViewModel chatViewModel;
    private EditText messageInput;
    private ImageView sendButton,iv_back;
    private AiChatAdapter chatAdapter;
    private List<AiChatsMessage> chatList;
    RelativeLayout textPlan;


    @SuppressLint({"WrongViewCast", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai);

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
        chatRecyclerView = findViewById(R.id.chat_recycler_view);
        messageInput = findViewById(R.id.message_input);
        textPlan = findViewById(R.id.textPlan);
        sendButton = findViewById(R.id.send_button);
        iv_back = findViewById(R.id.iv_back);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AiActivity.this,HomeActivity.class));
            }
        });


        chatList = new ArrayList<>();
        chatAdapter = new AiChatAdapter(AiActivity.this,chatList);

        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);
        chatViewModel = new ViewModelProvider(this).get(ChatViewModel.class);

        // Observe data
        chatViewModel.getChatMessages().observe(this, messages -> {
            chatAdapter.updateMessages(messages);
            updateUI();
        });
        sendButton.setOnClickListener(v -> sendMessage());

    }
    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();
        if (!messageText.isEmpty()) {
            // Add user message to chat
            chatViewModel.addMessage(new AiChatsMessage(messageText, true,false));
            chatAdapter.notifyItemInserted(chatList.size() - 1);
            chatRecyclerView.scrollToPosition(chatList.size() - 1);
            messageInput.setText("");
            updateUI();

            // Call AI for response
            modelCall(messageText);
        }
    }


    public void modelCall(String message) {
        AiChatsMessage waitingMessage = new AiChatsMessage("Please wait...", false,true);
        chatList.add(waitingMessage);
        chatAdapter.notifyItemInserted(chatList.size() - 1);
        chatRecyclerView.scrollToPosition(chatList.size() - 1);
        updateUI();

            // Initialize GenerativeModel with the correct API key
            GenerativeModel gm = new GenerativeModel("gemini-1.5-pro", "your api key");
            GenerativeModelFutures model = GenerativeModelFutures.from(gm);

            // Create content
            Content content = new Content.Builder()
                    .addText(message)
                    .build();

            // Generate response asynchronously
            ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
                    @Override
                    public void onSuccess(GenerateContentResponse result) {
                        String aiResponse = result.getText();
                        chatViewModel.addMessage(new AiChatsMessage(aiResponse, false,false));
                        runOnUiThread(() -> {
                            chatAdapter.notifyItemInserted(chatList.size() - 1);
                            chatRecyclerView.scrollToPosition(chatList.size() - 1);
                            updateUI();
                        });
                    }

                    @Override
                    public void onFailure(@NonNull Throwable t) {
                        t.printStackTrace();
                    }
                }, getMainExecutor());
            }
        }

    private void updateUI() {
        if (chatList.isEmpty()) {
            textPlan.setVisibility(View.VISIBLE);  // ✅ Show welcome message
            chatRecyclerView.setVisibility(View.GONE);
        } else {
            textPlan.setVisibility(View.GONE);  // ✅ Hide when messages exist
            chatRecyclerView.setVisibility(View.VISIBLE);
        }
    }

}

