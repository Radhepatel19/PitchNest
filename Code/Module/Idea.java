package com.example.businessidea.Module;

import java.util.ArrayList;
import java.util.List;

public class Idea {

    private String UserId;
    private String UserImage;
    private String titleIdeas;

    private String UserName;// Unique ID for the idea
    private String content;          // Content of the business idea
    private long timestamp;          // Timestamp when the idea was posted
    // ID of the user who posted the idea
    private String imageUrl;         // URL of the image associated with the idea
    private List<CommentModel> comments;  // List of comments on this idea
    private List<String> likes;
    private int commentCount;
    private boolean isExpanded = false;
    private boolean isFollowed;

    // Constructor, getters, and setters
    public boolean isFollowed() {
        return isFollowed;
    }

    public void setFollowed(boolean followed) {
        isFollowed = followed;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public int getLikeCount() {
        return LikeCount;
    }

    public void setLikeCount(int likeCount) {
        LikeCount = likeCount;
    }

    private String IdeasId;
    private int  LikeCount;// List of user IDs who liked this idea
 public Idea(String UserName, String UserImage,String UserId){
     this.UserName = UserName;
     this.UserImage = UserImage;
     this.UserId = UserId;
 }
    public Idea(String UserName, String UserImage, String imageUrl, String content, List<String> likes, String IdeasId, String UserId,int  LikeCount,int commentCount,long timestamp,String titleIdeas){
        this.UserName = UserName;
        this.UserImage = UserImage;
        this.imageUrl = imageUrl;
        this.content = content;
        this.likes = likes;
        this.IdeasId = IdeasId;
        this.UserId = UserId;
        this.LikeCount = LikeCount;
        this.commentCount = commentCount;
        this.timestamp = timestamp;
        this.titleIdeas = titleIdeas;}
    public String getIdeasId() {
        return IdeasId;
    }

    public void setIdeasId(String ideasId) {
        IdeasId = ideasId;
    }

    public String getTitle() {
        return titleIdeas;
    }

    public void setTitle(String title) {
        this.titleIdeas = title;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        this.UserName = userName;
    }

    public String getUserImage() {
        return UserImage;
    }

    public void setUserImage(String userImage) {
        UserImage = userImage;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    // Constructor
    public Idea() {
        this.comments = new ArrayList<>();
        this.likes = new ArrayList<>();
    }

    // Getters and Setters
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }


    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<CommentModel> getComments() {
        return comments;
    }

    public void setComments(List<CommentModel> comments) {
        this.comments = comments;
    }

    // Method to add a comment to the idea
    public void addComment(CommentModel comment) {
        this.comments.add(comment);
    }

    // Method to add a like to the idea by a user

    public List<String> getLikes() {
        return likes;
    }

    public void setLikes(List<String> likes) {
        this.likes = likes;
    }

    // Method to check if a user has liked the idea
    public boolean hasUserLiked(String userId) {
        return this.likes.contains(userId);
    }
}
