package com.example.businessidea;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.businessidea.Module.AiChatsMessage;

import java.util.ArrayList;
import java.util.List;

public class ChatViewModel extends ViewModel {
    private final MutableLiveData<List<AiChatsMessage>> chatMessages = new MutableLiveData<>(new ArrayList<>());

    public LiveData<List<AiChatsMessage>> getChatMessages() {
        return chatMessages;
    }

    public void addMessage(AiChatsMessage message) {
        List<AiChatsMessage> currentMessages = new ArrayList<>(chatMessages.getValue());
        currentMessages.add(message);
        chatMessages.setValue(currentMessages);
    }
    public void setMessages(List<AiChatsMessage> messages) {
        chatMessages.setValue(new ArrayList<>(messages));
    }
}

