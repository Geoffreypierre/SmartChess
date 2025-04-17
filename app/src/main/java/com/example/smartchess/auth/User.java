package com.example.smartchess.auth;

public class User {
    private String username;
    private String email;
    private String password;
    private int elo;
    private String profileImageUrl;

    public User() {
    }

    public User(String username, String email, String password, int elo, String profileImageUrl) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.elo = elo;
        this.profileImageUrl = profileImageUrl;
    }

    public User(String username, String email, String password, int elo) {
        this(username, email, password, elo, "");
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

    public int getElo() {
        return elo;
    }

    public void setElo(int elo) {
        this.elo = elo;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}