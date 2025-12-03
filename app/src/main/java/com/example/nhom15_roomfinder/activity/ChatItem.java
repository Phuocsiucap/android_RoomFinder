package com.example.nhom15_roomfinder.activity; // ĐỔI cho đúng package

public class ChatItem {
    private String title;
    private String lastMessage;
    private String time;
    private int unreadCount;

    public ChatItem(String title, String lastMessage, String time, int unreadCount) {
        this.title = title;
        this.lastMessage = lastMessage;
        this.time = time;
        this.unreadCount = unreadCount;
    }

    public String getTitle() {
        return title;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public String getTime() {
        return time;
    }

    public int getUnreadCount() {
        return unreadCount;
    }
}
