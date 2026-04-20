package com.example.businessidea.Module;
public class AiChatsMessage {
    private String message;
    private boolean isUserMessage;
    private boolean isWaitingMessage; // New flag for "Please wait..." messages

    public AiChatsMessage(String message, boolean isUserMessage, boolean isWaitingMessage) {
        this.message = message;
        this.isUserMessage = isUserMessage;
        this.isWaitingMessage = isWaitingMessage;
    }

    public String getMessage() {
        return message;
    }

    public boolean isUserMessage() {
        return isUserMessage;
    }

    public boolean isWaitingMessage() {
        return isWaitingMessage;
    }
}

