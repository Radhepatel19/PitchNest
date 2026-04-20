package com.example.businessidea.Module;

    public class Chats {
        private String message;
        private String sender;
        private String receiver;
        private long timestamp;
        private boolean isRead;
        private String messageId;  // Unique ID for deleting messages

        public Chats() {
        }

        public Chats(String message, String sender, String receiver, long timestamp, String messageId) {
            this.message = message;
            this.sender = sender;
            this.receiver = receiver;
            this.timestamp = timestamp;
            this.messageId = messageId;
        }

        // Getters and Setters
        public String getMessageId() {
            return messageId;
        }

        public void setMessageId(String messageId) {
            this.messageId = messageId;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getSender() {
            return sender;
        }

        public void setSender(String sender) {
            this.sender = sender;
        }

        public String getReceiver() {
            return receiver;
        }

        public void setReceiver(String receiver) {
            this.receiver = receiver;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public boolean isRead() {
            return isRead;
        }

        public void setRead(boolean read) {
            isRead = read;
        }
    }
