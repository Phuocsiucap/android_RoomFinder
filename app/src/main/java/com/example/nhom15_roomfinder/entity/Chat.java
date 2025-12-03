package com.example.nhom15_roomfinder.entity;

import java.io.Serializable;

/**
 * Chat Entity - Đại diện cho một cuộc trò chuyện
 */
public class Chat implements Serializable {
    private String id;
    private String recipientId;      // ID người nhận
    private String recipientName;    // Tên người nhận
    private String recipientAvatar;  // Avatar người nhận
    private String lastMessage;      // Tin nhắn cuối cùng
    private long lastMessageTime;    // Thời gian tin nhắn cuối
    private int unreadCount;         // Số tin chưa đọc
    private String roomId;           // ID phòng liên quan (nếu có)
    private String roomTitle;        // Tên phòng liên quan

    // Constructor mặc định (cần cho Firebase)
    public Chat() {}

    public Chat(String id, String recipientId, String recipientName, String lastMessage, long lastMessageTime) {
        this.id = id;
        this.recipientId = recipientId;
        this.recipientName = recipientName;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
        this.unreadCount = 0;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRecipientId() { return recipientId; }
    public void setRecipientId(String recipientId) { this.recipientId = recipientId; }

    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

    public String getRecipientAvatar() { return recipientAvatar; }
    public void setRecipientAvatar(String recipientAvatar) { this.recipientAvatar = recipientAvatar; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public long getLastMessageTime() { return lastMessageTime; }
    public void setLastMessageTime(long lastMessageTime) { this.lastMessageTime = lastMessageTime; }

    public int getUnreadCount() { return unreadCount; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getRoomTitle() { return roomTitle; }
    public void setRoomTitle(String roomTitle) { this.roomTitle = roomTitle; }
}
