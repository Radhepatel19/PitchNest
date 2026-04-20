package com.example.businessidea.Module;

public class Users {
    private String username;
    private String mail;
    private String password;
    private String profilePic;
    private String phoneNumber;
    private String pronouns;
    private String headline;
    private String country;
    private String city;
    private String address;
    private String birthday;
    private String follow;
    private String following;
    private String about;
    private String userId;
    private String lastMessage;  // Add this field
    private int unreadCount;

    public Users(){

    }
    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getFollow() {
        return follow;
    }
    public Users(String userId, String username, String email, String Pic) {
        this.userId = userId;  // Set the userId
        this.username = username;
        this.mail = email;
        this.profilePic = Pic;
    }
    public Users(String userId, String username, String headline, String Pic,int g) {
        this.userId = userId;  // Set the userId
        this.username = username;
        this.headline = headline;
        this.profilePic = Pic;
    }
    public void setFollow(String follow) {
        this.follow = follow;
    }

    public String getFollowing() {
        return following;
    }

    public void setFollowing(String following) {
        this.following = following;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public Users(String username, String headline) {
        // Default constructor
    }
    public Users(String profilePic, String username, String headline, String userId,String pronouns) {
        this.profilePic = profilePic;
        this.username = username;
        this.headline = headline;
        this.userId = userId;
        this.pronouns=pronouns;
    }
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public  Users(String username, String email, String Pic){
        this.username = username;
        this.mail = email;
        this.profilePic = Pic;
    }

    public Users(String username, String mail, String password, String profilePic, String phoneNumber, String pronouns,
                 String headline, String country, String city, String address, String birthday, String follow, String following, String about) {
        this.username = username;
        this.mail = mail;
        this.password = password;
        this.profilePic = profilePic;
        this.phoneNumber = phoneNumber;
        this.pronouns = pronouns;
        this.headline = headline;
        this.country = country;
        this.city = city;
        this.address = address;
        this.birthday = birthday;
        this.follow = follow;
        this.following = following;
        this.about = about;
    }
    public Users(String username, String phoneNumber, String pronouns,
                 String headline, String country, String city, String address, String birthday) {
        this.username = username;
        this.phoneNumber = phoneNumber;
        this.pronouns = pronouns;
        this.headline = headline;
        this.country = country;
        this.city = city;
        this.address = address;
        this.birthday = birthday;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPronouns() {
        return pronouns;
    }

    public void setPronouns(String pronouns) {
        this.pronouns = pronouns;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }
}
