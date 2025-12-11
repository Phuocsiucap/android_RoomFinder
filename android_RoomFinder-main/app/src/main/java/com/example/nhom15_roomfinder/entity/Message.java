package com.example.nhom15_roomfinder.entity;

import java.io.Serializable;

/**
 * Message Entity - Đại diện cho một tin nhắn
 */
public class Message implements Serializable {
    private String id;
    private String chatId;           // ID cuộc trò chuyện
    private String senderId;         // ID người gửi
    private String senderName;       // Tên người gửi
    private String content;          // Nội dung tin nhắn
    private long timestamp;          // Thời gian gửi
    private boolean isRead;          // Đã đọc chưa
    private String messageType;      // text, image, location

    // Constructor mặc định (cần cho Firebase)
    public Message() {
        this.messageType = "text";
    }

    public Message(String senderId, String senderName, String content) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
        this.isRead = false;
        this.messageType = "text";
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getChatId() { return chatId; }
    public void setChatId(String chatId) { this.chatId = chatId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }
}
