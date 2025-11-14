package com.example.nhom15_roomfinder.firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for Chat-related Firebase Realtime Database operations
 * This class provides methods to manage real-time chat functionality
 */
public class ChatFirebaseHelper {
    
    private static final String TAG = "ChatFirebaseHelper";
    private static final String PATH_CHATS = "chats";
    private static final String PATH_MESSAGES = "messages";
    
    private FirebaseManager firebaseManager;
    private DatabaseReference chatsRef;
    
    public ChatFirebaseHelper() {
        this.firebaseManager = FirebaseManager.getInstance();
        this.chatsRef = firebaseManager.getDatabaseReference(PATH_CHATS);
    }
    
    /**
     * Send a message in a chat
     */
    public void sendMessage(String chatId, String senderId, String senderName, 
                           String message, FirebaseCallback<Void> callback) {
        
        // Create message data
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("senderId", senderId);
        messageData.put("senderName", senderName);
        messageData.put("message", message);
        messageData.put("timestamp", System.currentTimeMillis());
        messageData.put("read", false);
        
        // Generate unique message ID
        String messageId = chatsRef.child(chatId).child(PATH_MESSAGES).push().getKey();
        
        if (messageId != null) {
            String path = PATH_CHATS + "/" + chatId + "/" + PATH_MESSAGES + "/" + messageId;
            
            firebaseManager.writeToRealtimeDb(path, messageData,
                aVoid -> {
                    Log.d(TAG, "Message sent successfully");
                    updateLastMessage(chatId, message);
                    callback.onSuccess(null);
                },
                e -> {
                    Log.e(TAG, "Error sending message: " + e.getMessage(), e);
                    callback.onFailure(e.getMessage());
                });
        } else {
            callback.onFailure("Failed to generate message ID");
        }
    }
    
    /**
     * Update last message in chat
     */
    private void updateLastMessage(String chatId, String lastMessage) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("lastMessage", lastMessage);
        updates.put("lastMessageTime", System.currentTimeMillis());
        
        String path = PATH_CHATS + "/" + chatId;
        firebaseManager.updateRealtimeDb(path, updates,
            aVoid -> Log.d(TAG, "Last message updated"),
            e -> Log.e(TAG, "Error updating last message: " + e.getMessage()));
    }
    
    /**
     * Create a new chat between two users
     */
    public void createChat(String user1Id, String user1Name, String user2Id, String user2Name,
                          FirebaseCallback<String> callback) {
        
        // Create unique chat ID (sort user IDs to ensure consistency)
        String chatId = user1Id.compareTo(user2Id) < 0 ? 
            user1Id + "_" + user2Id : user2Id + "_" + user1Id;
        
        // Create chat data
        Map<String, Object> chatData = new HashMap<>();
        chatData.put("chatId", chatId);
        chatData.put("user1Id", user1Id);
        chatData.put("user1Name", user1Name);
        chatData.put("user2Id", user2Id);
        chatData.put("user2Name", user2Name);
        chatData.put("createdAt", System.currentTimeMillis());
        chatData.put("lastMessage", "");
        chatData.put("lastMessageTime", System.currentTimeMillis());
        
        String path = PATH_CHATS + "/" + chatId;
        
        firebaseManager.writeToRealtimeDb(path, chatData,
            aVoid -> {
                Log.d(TAG, "Chat created successfully: " + chatId);
                callback.onSuccess(chatId);
            },
            e -> {
                Log.e(TAG, "Error creating chat: " + e.getMessage(), e);
                callback.onFailure(e.getMessage());
            });
    }
    
    /**
     * Listen for new messages in a chat
     */
    public void listenForMessages(String chatId, MessageListener listener) {
        DatabaseReference messagesRef = chatsRef.child(chatId).child(PATH_MESSAGES);
        
        messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Map<String, Object>> messages = new ArrayList<>();
                
                for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                    Map<String, Object> message = (Map<String, Object>) messageSnapshot.getValue();
                    if (message != null) {
                        message.put("messageId", messageSnapshot.getKey());
                        messages.add(message);
                    }
                }
                
                Log.d(TAG, "Received " + messages.size() + " messages");
                listener.onMessagesReceived(messages);
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error listening for messages: " + databaseError.getMessage());
                listener.onError(databaseError.getMessage());
            }
        });
    }
    
    /**
     * Get all chats for a user
     */
    public void getUserChats(String userId, ChatsListener listener) {
        chatsRef.orderByChild("user1Id").equalTo(userId)
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    List<Map<String, Object>> chats = new ArrayList<>();
                    
                    for (DataSnapshot chatSnapshot : dataSnapshot.getChildren()) {
                        Map<String, Object> chat = (Map<String, Object>) chatSnapshot.getValue();
                        if (chat != null) {
                            chat.put("chatId", chatSnapshot.getKey());
                            chats.add(chat);
                        }
                    }
                    
                    // Also get chats where user is user2
                    chatsRef.orderByChild("user2Id").equalTo(userId)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot chatSnapshot : dataSnapshot.getChildren()) {
                                    Map<String, Object> chat = (Map<String, Object>) chatSnapshot.getValue();
                                    if (chat != null) {
                                        chat.put("chatId", chatSnapshot.getKey());
                                        chats.add(chat);
                                    }
                                }
                                
                                Log.d(TAG, "Retrieved " + chats.size() + " chats for user");
                                listener.onChatsReceived(chats);
                            }
                            
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.e(TAG, "Error getting user chats: " + databaseError.getMessage());
                                listener.onError(databaseError.getMessage());
                            }
                        });
                }
                
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "Error getting user chats: " + databaseError.getMessage());
                    listener.onError(databaseError.getMessage());
                }
            });
    }
    
    /**
     * Mark messages as read
     */
    public void markMessagesAsRead(String chatId, String userId) {
        DatabaseReference messagesRef = chatsRef.child(chatId).child(PATH_MESSAGES);
        
        messagesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                    Map<String, Object> message = (Map<String, Object>) messageSnapshot.getValue();
                    if (message != null) {
                        String senderId = (String) message.get("senderId");
                        Boolean read = (Boolean) message.get("read");
                        
                        // Mark as read if message is from other user and not yet read
                        if (!userId.equals(senderId) && (read == null || !read)) {
                            messageSnapshot.getRef().child("read").setValue(true);
                        }
                    }
                }
                Log.d(TAG, "Messages marked as read");
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error marking messages as read: " + databaseError.getMessage());
            }
        });
    }
    
    /**
     * Delete a chat
     */
    public void deleteChat(String chatId, FirebaseCallback<Void> callback) {
        String path = PATH_CHATS + "/" + chatId;
        
        firebaseManager.deleteFromRealtimeDb(path,
            aVoid -> {
                Log.d(TAG, "Chat deleted successfully");
                callback.onSuccess(null);
            },
            e -> {
                Log.e(TAG, "Error deleting chat: " + e.getMessage(), e);
                callback.onFailure(e.getMessage());
            });
    }
    
    // Listener interfaces
    
    /**
     * Interface for listening to messages
     */
    public interface MessageListener {
        void onMessagesReceived(List<Map<String, Object>> messages);
        void onError(String error);
    }
    
    /**
     * Interface for listening to chats
     */
    public interface ChatsListener {
        void onChatsReceived(List<Map<String, Object>> chats);
        void onError(String error);
    }
}
