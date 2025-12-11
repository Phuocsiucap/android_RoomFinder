package com.example.nhom15_roomfinder.firebase;

import android.net.Uri;
import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Example usage of Firebase helpers in the RoomFinder app
 * This class demonstrates how to use FirebaseManager, RoomFirebaseHelper, and ChatFirebaseHelper
 */
public class FirebaseUsageExample {
    
    private static final String TAG = "FirebaseUsageExample";
    
    private FirebaseManager firebaseManager;
    private RoomFirebaseHelper roomHelper;
    private ChatFirebaseHelper chatHelper;
    
    public FirebaseUsageExample() {
        // Initialize Firebase helpers
        firebaseManager = FirebaseManager.getInstance();
        roomHelper = new RoomFirebaseHelper();
        chatHelper = new ChatFirebaseHelper();
    }
    
    // ==================== Authentication Examples ====================
    
    /**
     * Example: Register a new user
     */
    public void exampleRegisterUser(String email, String password, String name) {
        firebaseManager.registerUser(email, password, task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "Registration successful");
                String userId = firebaseManager.getUserId();
                
                // Create user profile in Firestore
                firebaseManager.createUserProfile(userId, email, name,
                    aVoid -> Log.d(TAG, "User profile created"),
                    e -> Log.e(TAG, "Error creating profile: " + e.getMessage()));
                    
            } else {
                Log.e(TAG, "Registration failed: " + task.getException());
            }
        });
    }
    
    /**
     * Example: Sign in existing user
     */
    public void exampleSignIn(String email, String password) {
        firebaseManager.signInUser(email, password, task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "Sign in successful");
                String userId = firebaseManager.getUserId();
                Log.d(TAG, "User ID: " + userId);
            } else {
                Log.e(TAG, "Sign in failed: " + task.getException());
            }
        });
    }
    
    /**
     * Example: Reset password
     */
    public void exampleResetPassword(String email) {
        firebaseManager.sendPasswordResetEmail(email, task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "Password reset email sent");
            } else {
                Log.e(TAG, "Failed to send reset email: " + task.getException());
            }
        });
    }
    
    // ==================== Room Management Examples ====================
    
    /**
     * Example: Add a new room
     */
    public void exampleAddRoom(String title, String description, String location, 
                              double price, Uri imageUri) {
        String userId = firebaseManager.getUserId();
        if (userId != null) {
            roomHelper.addRoom(title, description, location, price, userId, imageUri,
                new FirebaseCallback<String>() {
                    @Override
                    public void onSuccess(String roomId) {
                        Log.d(TAG, "Room added successfully with ID: " + roomId);
                    }
                    
                    @Override
                    public void onFailure(String error) {
                        Log.e(TAG, "Failed to add room: " + error);
                    }
                });
        }
    }
    
    /**
     * Example: Get all rooms
     */
    public void exampleGetAllRooms() {
        roomHelper.getAllRooms(new FirebaseCallback<List<Map<String, Object>>>() {
            @Override
            public void onSuccess(List<Map<String, Object>> rooms) {
                Log.d(TAG, "Retrieved " + rooms.size() + " rooms");
                for (Map<String, Object> room : rooms) {
                    Log.d(TAG, "Room: " + room.get("title") + " - $" + room.get("price"));
                }
            }
            
            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Failed to get rooms: " + error);
            }
        });
    }
    
    /**
     * Example: Get rooms by current user
     */
    public void exampleGetMyRooms() {
        String userId = firebaseManager.getUserId();
        if (userId != null) {
            roomHelper.getRoomsByUser(userId, new FirebaseCallback<List<Map<String, Object>>>() {
                @Override
                public void onSuccess(List<Map<String, Object>> rooms) {
                    Log.d(TAG, "My rooms count: " + rooms.size());
                }
                
                @Override
                public void onFailure(String error) {
                    Log.e(TAG, "Failed to get my rooms: " + error);
                }
            });
        }
    }
    
    /**
     * Example: Update room information
     */
    public void exampleUpdateRoom(String roomId, String newTitle, double newPrice) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("title", newTitle);
        updates.put("price", newPrice);
        updates.put("updatedAt", System.currentTimeMillis());
        
        roomHelper.updateRoom(roomId, updates, new FirebaseCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                Log.d(TAG, "Room updated successfully");
            }
            
            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Failed to update room: " + error);
            }
        });
    }
    
    /**
     * Example: Delete a room
     */
    public void exampleDeleteRoom(String roomId) {
        roomHelper.deleteRoom(roomId, new FirebaseCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                Log.d(TAG, "Room deleted successfully");
            }
            
            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Failed to delete room: " + error);
            }
        });
    }
    
    /**
     * Example: Search rooms by location
     */
    public void exampleSearchRoomsByLocation(String location) {
        roomHelper.searchRoomsByLocation(location, new FirebaseCallback<List<Map<String, Object>>>() {
            @Override
            public void onSuccess(List<Map<String, Object>> rooms) {
                Log.d(TAG, "Found " + rooms.size() + " rooms in " + location);
            }
            
            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Search failed: " + error);
            }
        });
    }
    
    /**
     * Example: Search rooms by price range
     */
    public void exampleSearchRoomsByPrice(double minPrice, double maxPrice) {
        roomHelper.searchRoomsByPriceRange(minPrice, maxPrice, 
            new FirebaseCallback<List<Map<String, Object>>>() {
                @Override
                public void onSuccess(List<Map<String, Object>> rooms) {
                    Log.d(TAG, "Found " + rooms.size() + " rooms in price range");
                }
                
                @Override
                public void onFailure(String error) {
                    Log.e(TAG, "Search failed: " + error);
                }
            });
    }
    
    // ==================== Favorites Examples ====================
    
    /**
     * Example: Add room to favorites
     */
    public void exampleAddToFavorites(String roomId) {
        String userId = firebaseManager.getUserId();
        if (userId != null) {
            roomHelper.addToFavorites(userId, roomId, new FirebaseCallback<Void>() {
                @Override
                public void onSuccess(Void data) {
                    Log.d(TAG, "Added to favorites");
                }
                
                @Override
                public void onFailure(String error) {
                    Log.e(TAG, "Failed to add to favorites: " + error);
                }
            });
        }
    }
    
    /**
     * Example: Get favorite rooms
     */
    public void exampleGetFavorites() {
        String userId = firebaseManager.getUserId();
        if (userId != null) {
            roomHelper.getFavoriteRooms(userId, new FirebaseCallback<List<String>>() {
                @Override
                public void onSuccess(List<String> favoriteRoomIds) {
                    Log.d(TAG, "Favorite rooms count: " + favoriteRoomIds.size());
                }
                
                @Override
                public void onFailure(String error) {
                    Log.e(TAG, "Failed to get favorites: " + error);
                }
            });
        }
    }
    
    // ==================== Chat Examples ====================
    
    /**
     * Example: Create a chat between two users
     */
    public void exampleCreateChat(String otherUserId, String otherUserName) {
        String currentUserId = firebaseManager.getUserId();
        if (currentUserId != null) {
            chatHelper.createChat(currentUserId, "My Name", otherUserId, otherUserName,
                new FirebaseCallback<String>() {
                    @Override
                    public void onSuccess(String chatId) {
                        Log.d(TAG, "Chat created with ID: " + chatId);
                    }
                    
                    @Override
                    public void onFailure(String error) {
                        Log.e(TAG, "Failed to create chat: " + error);
                    }
                });
        }
    }
    
    /**
     * Example: Send a message
     */
    public void exampleSendMessage(String chatId, String message) {
        String userId = firebaseManager.getUserId();
        if (userId != null) {
            chatHelper.sendMessage(chatId, userId, "My Name", message,
                new FirebaseCallback<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        Log.d(TAG, "Message sent successfully");
                    }
                    
                    @Override
                    public void onFailure(String error) {
                        Log.e(TAG, "Failed to send message: " + error);
                    }
                });
        }
    }
    
    /**
     * Example: Listen for messages in a chat
     */
    public void exampleListenForMessages(String chatId) {
        chatHelper.listenForMessages(chatId, new ChatFirebaseHelper.MessageListener() {
            @Override
            public void onMessagesReceived(List<Map<String, Object>> messages) {
                Log.d(TAG, "Received " + messages.size() + " messages");
                for (Map<String, Object> message : messages) {
                    Log.d(TAG, "Message: " + message.get("message"));
                }
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "Error listening for messages: " + error);
            }
        });
    }
    
    /**
     * Example: Get all chats for current user
     */
    public void exampleGetUserChats() {
        String userId = firebaseManager.getUserId();
        if (userId != null) {
            chatHelper.getUserChats(userId, new ChatFirebaseHelper.ChatsListener() {
                @Override
                public void onChatsReceived(List<Map<String, Object>> chats) {
                    Log.d(TAG, "User has " + chats.size() + " chats");
                }
                
                @Override
                public void onError(String error) {
                    Log.e(TAG, "Error getting chats: " + error);
                }
            });
        }
    }
    
    // ==================== Storage Examples ====================
    
    /**
     * Example: Upload an image
     */
    public void exampleUploadImage(Uri imageUri, String fileName) {
        String userId = firebaseManager.getUserId();
        String storagePath = "users/" + userId + "/" + fileName;
        
        firebaseManager.uploadImageAndGetUrl(imageUri, storagePath,
            downloadUri -> {
                Log.d(TAG, "Image uploaded. URL: " + downloadUri.toString());
            },
            e -> {
                Log.e(TAG, "Failed to upload image: " + e.getMessage());
            });
    }
}
