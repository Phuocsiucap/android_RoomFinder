package com.example.nhom15_roomfinder.entity;

public class ChatMessage {
    private String senderId;
    private String message;
    private long timestamp;

    // 1. Thêm trường lưu link ảnh
    private String imageUrl;

    // Constructor mặc định (Bắt buộc cho Firebase)
    public ChatMessage() {
    }

    // Constructor cơ bản (3 tham số)
    public ChatMessage(String senderId, String message, long timestamp) {
        this.senderId = senderId;
        this.message = message;
        this.timestamp = timestamp;
    }

    // --- Các hàm Getter và Setter ---

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    // 2. Thêm Getter cho ảnh
    public String getImageUrl() {
        return imageUrl;
    }

    // 3. Thêm Setter cho ảnh (Đây là cái bạn đang thiếu)
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
