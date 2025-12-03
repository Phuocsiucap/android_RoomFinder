package com.example.nhom15_roomfinder.entity;

import java.io.Serializable;

/**
 * User Entity - Đại diện cho thông tin người dùng
 * Roles: "customer" (mặc định), "landlord" (chủ trọ), "admin"
 */
public class User implements Serializable {
    private String id;
    private String email;
    private String name;
    private String phone;
    private String avatarUrl;
    private String role; // customer, landlord, admin
    private long createdAt;
    private long lastLoginAt;

    // Constructor mặc định (cần cho Firebase)
    public User() {
        this.role = "customer"; // Mặc định là customer
    }

    public User(String id, String email, String name) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.role = "customer"; // Mặc định là customer
        this.createdAt = System.currentTimeMillis();
    }

    public User(String id, String email, String name, String phone, String avatarUrl, String role) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.avatarUrl = avatarUrl;
        this.role = role != null ? role : "customer";
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(long lastLoginAt) { this.lastLoginAt = lastLoginAt; }

    // Helper methods
    public boolean isCustomer() {
        return "customer".equals(role);
    }

    public boolean isLandlord() {
        return "landlord".equals(role);
    }

    public boolean isAdmin() {
        return "admin".equals(role);
    }
}
