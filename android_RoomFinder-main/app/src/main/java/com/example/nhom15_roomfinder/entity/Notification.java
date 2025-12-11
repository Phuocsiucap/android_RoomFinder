package com.example.nhom15_roomfinder.entity;

import java.io.Serializable;

/**
 * Notification Entity - Thông báo cho người dùng
 * 
 * Types:
 * - APPOINTMENT_REQUEST: Yêu cầu đặt lịch xem phòng
 * - APPOINTMENT_ACCEPTED: Lịch hẹn được chấp nhận
 * - APPOINTMENT_REJECTED: Lịch hẹn bị từ chối
 * - NEW_MESSAGE: Tin nhắn mới
 * - SYSTEM: Thông báo hệ thống
 */
public class Notification implements Serializable {
    
    public static final String TYPE_APPOINTMENT_REQUEST = "APPOINTMENT_REQUEST";
    public static final String TYPE_APPOINTMENT_ACCEPTED = "APPOINTMENT_ACCEPTED";
    public static final String TYPE_APPOINTMENT_REJECTED = "APPOINTMENT_REJECTED";
    public static final String TYPE_NEW_MESSAGE = "NEW_MESSAGE";
    public static final String TYPE_SYSTEM = "SYSTEM";
    
    private String id;
    private String userId;           // Người nhận thông báo
    private String senderId;         // Người gửi (nếu có)
    private String senderName;
    private String senderAvatar;
    private String type;             // Loại thông báo
    private String title;            // Tiêu đề
    private String message;          // Nội dung
    private String roomId;           // Phòng liên quan (nếu có)
    private String roomTitle;
    private String appointmentId;    // ID lịch hẹn (nếu có)
    private long createdAt;
    private boolean isRead;
    private String actionUrl;        // URL/Action khi click

    // Constructor mặc định (Firebase)
    public Notification() {
        this.createdAt = System.currentTimeMillis();
        this.isRead = false;
    }

    public Notification(String userId, String type, String title, String message) {
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.createdAt = System.currentTimeMillis();
        this.isRead = false;
    }

    // Builder pattern for easier creation
    public static class Builder {
        private Notification notification;
        
        public Builder() {
            notification = new Notification();
        }
        
        public Builder userId(String userId) {
            notification.userId = userId;
            return this;
        }
        
        public Builder senderId(String senderId) {
            notification.senderId = senderId;
            return this;
        }
        
        public Builder senderName(String senderName) {
            notification.senderName = senderName;
            return this;
        }
        
        public Builder type(String type) {
            notification.type = type;
            return this;
        }
        
        public Builder title(String title) {
            notification.title = title;
            return this;
        }
        
        public Builder message(String message) {
            notification.message = message;
            return this;
        }
        
        public Builder roomId(String roomId) {
            notification.roomId = roomId;
            return this;
        }
        
        public Builder roomTitle(String roomTitle) {
            notification.roomTitle = roomTitle;
            return this;
        }
        
        public Builder appointmentId(String appointmentId) {
            notification.appointmentId = appointmentId;
            return this;
        }
        
        public Notification build() {
            return notification;
        }
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getSenderAvatar() { return senderAvatar; }
    public void setSenderAvatar(String senderAvatar) { this.senderAvatar = senderAvatar; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getRoomTitle() { return roomTitle; }
    public void setRoomTitle(String roomTitle) { this.roomTitle = roomTitle; }

    public String getAppointmentId() { return appointmentId; }
    public void setAppointmentId(String appointmentId) { this.appointmentId = appointmentId; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public String getActionUrl() { return actionUrl; }
    public void setActionUrl(String actionUrl) { this.actionUrl = actionUrl; }
}
