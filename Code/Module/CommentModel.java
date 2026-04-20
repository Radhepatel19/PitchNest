package com.example.businessidea.Module;

import java.util.Map;

public class CommentModel {
    private String userId;
    private String commentText;
    private String Username;
    private String UserImage;
    private long timestamp;
    private int likeCount;  // Count of likes for the comment
    private String commentId;// Unique ID for the comment
    private Map<String, Boolean> likes;
    private String ideaId;
    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        Username = username;
    }

    public String getUserImage() {
        return UserImage;
    }

    public void setUserImage(String userImage) {
        UserImage = userImage;
    }

    public CommentModel() {
        // Default constructor for Firebase
    }

    public String getIdeaId() {
        return ideaId;
    }

    public void setIdeaId(String ideaId) {
        this.ideaId = ideaId;
    }

    public Map<String, Boolean> getLikes() {
        return likes;
    }

    public void setLikes(Map<String, Boolean> likes) {
        this.likes = likes;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public CommentModel(String userId, String commentText, long timestamp, String Username, String UserImage, String commentId, int likeCount,Map<String,Boolean> likes,String ideaId) {
        this.userId = userId;
        this.commentText = commentText;
        this.timestamp = timestamp;
        this.Username = Username;
        this.UserImage = UserImage;
        this.likeCount = likeCount;
        this.commentId = commentId;
        this.likes = likes;
        this.ideaId = ideaId;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}

