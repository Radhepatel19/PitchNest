package com.example.businessidea.Module;

public class Notification {
    private String type;
    private String senderId;
    private String receiverId; // New field for receiver ID
    private String senderImage;
    private String image;
    private long timestamp;
    private String userName;
    private String comment;
    private String ideasId;
    boolean isRead;
    String key;

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Notification(String type, String senderId, String receiverId, String senderImage, String image, long timestamp, String userName, String comment, String ideasId, boolean isRead, String key) {
        this.type = type;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.senderImage = senderImage;
        this.image = image;
        this.timestamp = timestamp;
        this.userName = userName;
        this.comment = comment;
        this.ideasId = ideasId;
        this.isRead = isRead; // Default as unread
        this.key = key;
    }

    public Notification(String type, String senderId, String receiverId, String senderImage, long timestamp, String userName,boolean isRead,String key) {
        this.type = type;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.senderImage = senderImage;
        this.timestamp = timestamp;
        this.userName = userName;
        this.isRead = isRead; // Default as unread
        this.key = key;
    }

    public String getComment() {
        return comment;
    }

    public String getIdeasId() {
        return ideasId;
    }

    public void setIdeasId(String ideasId) {
        this.ideasId = ideasId;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Notification() {}

    public Notification(String type, String senderId, String receiverId, String senderImage, String image, long timestamp, String userName,String ideasId,boolean isRead,String key) {
        this.type = type;
        this.senderId = senderId;
        this.receiverId = receiverId; // Initialize the receiverId
        this.senderImage = senderImage;
        this.image = image;
        this.timestamp = timestamp;
        this.userName = userName;
        this.ideasId = ideasId;
        this.isRead = isRead; // Default as unread
        this.key = key;
    }

    // Getters and setters for each field
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId; // Getter for receiverId
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId; // Setter for receiverId
    }

    public String getSenderImage() {
        return senderImage;
    }

    public void setSenderImage(String senderImage) {
        this.senderImage = senderImage;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
