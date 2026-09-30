package com.example.bakebliss_bakery.models;

public class User {
    private String username;
    private String email;
    private String password;
    private String phone;
    private String address;
    private String profileImage;

    public User() {
    }

    public User(String username, String email, String password, String phone) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.address = "Not Set";
        this.profileImage = "";
    }

    public User(String username, String email, String phone, String address, String profileImage) {
        this.username = username;
        this.email = email;
        this.password = "";
        this.phone = phone;
        this.address = address;
        this.profileImage = profileImage;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address != null ? address : "Not Set";
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getProfileImage() {
        return profileImage != null ? profileImage : "";
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}