package com.example.smartchess.auth;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String id;
    private String username;
    private int elo;
    private String profilePicture;
    private List<String> friends;

    public User() {
        this.friends = new ArrayList<>();
    }

    public User(String id, String username, int elo, String profilePicture) {
        this.id = id;
        this.username = username;
        this.elo = elo;
        this.profilePicture = profilePicture;
        this.friends = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getElo() {
        return elo;
    }

    public void setElo(int elo) {
        this.elo = elo;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public List<String> getFriends() {
        return friends;
    }

    public void setFriends(List<String> friends) {
        this.friends = friends;
    }

    public void addFriend(String friendId) {
        if (this.friends == null) {
            this.friends = new ArrayList<>();
        }
        this.friends.add(friendId);
    }

    public void removeFriend(String friendId) {
        if (this.friends != null) {
            this.friends.remove(friendId);
        }
    }
}