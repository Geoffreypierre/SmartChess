package com.example.smartchess.play;

import java.util.Date;

public class FriendRequestModel {
    private String id;
    private String senderId;
    private String senderUsername;
    private String senderProfilePicture;
    private String receiverId;
    private String status; // "pending", "accepted", "declined"
    private Date createdAt;

    public FriendRequestModel() {}

    public FriendRequestModel(String id, String senderId, String senderUsername,
                              String senderProfilePicture, String receiverId) {
        this.id = id;
        this.senderId = senderId;
        this.senderUsername = senderUsername;
        this.senderProfilePicture = senderProfilePicture;
        this.receiverId = receiverId;
        this.status = "pending";
        this.createdAt = new Date();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getSenderProfilePicture() {
        return senderProfilePicture;
    }

    public void setSenderProfilePicture(String senderProfilePicture) {
        this.senderProfilePicture = senderProfilePicture;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}