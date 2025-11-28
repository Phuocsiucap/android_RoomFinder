package com.example.nhom15_roomfinder.entity;

import java.io.Serializable;

/**
 * Favorite Entity - Đại diện cho phòng yêu thích
 */
public class Favorite implements Serializable {
    private String id;
    private String oderId;           // ID người dùng
    private String roomId;           // ID phòng
    private long createdAt;          // Thời gian thêm vào yêu thích

    // Constructor mặc định (cần cho Firebase)
    public Favorite() {}

    public Favorite(String userId, String roomId) {
        this.oderId = oderId;
        this.roomId = roomId;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return oderId; }
    public void setUserId(String oderId) { this.oderId = oderId; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
