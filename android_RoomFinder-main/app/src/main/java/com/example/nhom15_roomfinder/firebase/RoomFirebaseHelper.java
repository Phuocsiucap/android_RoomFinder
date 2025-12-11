package com.example.nhom15_roomfinder.firebase;

import android.net.Uri;
import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for Room-related Firebase operations
 * This class provides methods to manage rooms in Firestore
 */
public class RoomFirebaseHelper {
    
    private static final String TAG = "RoomFirebaseHelper";
    private static final String COLLECTION_ROOMS = "rooms";
    private static final String COLLECTION_FAVORITES = "favorites";
    
    private FirebaseManager firebaseManager;
    
    public RoomFirebaseHelper() {
        this.firebaseManager = FirebaseManager.getInstance();
    }
    
    /**
     * Add a new room to Firestore
     */
    public void addRoom(String title, String description, String location, 
                       double price, String userId, Uri imageUri,
                       FirebaseCallback<String> callback) {
        
        // Create room data
        Map<String, Object> roomData = new HashMap<>();
        roomData.put("title", title);
        roomData.put("description", description);
        roomData.put("location", location);
        roomData.put("price", price);
        roomData.put("userId", userId);
        roomData.put("createdAt", System.currentTimeMillis());
        roomData.put("status", "available");
        
        // First, add the room document
        firebaseManager.addDocument(COLLECTION_ROOMS, roomData,
            documentReference -> {
                String roomId = documentReference.getId();
                Log.d(TAG, "Room added with ID: " + roomId);
                
                // If there's an image, upload it
                if (imageUri != null) {
                    uploadRoomImage(roomId, imageUri, callback);
                } else {
                    callback.onSuccess(roomId);
                }
            },
            e -> {
                Log.e(TAG, "Error adding room: " + e.getMessage(), e);
                callback.onFailure(e.getMessage());
            });
    }
    
    /**
     * Upload room image to Firebase Storage
     */
    private void uploadRoomImage(String roomId, Uri imageUri, FirebaseCallback<String> callback) {
        String storagePath = "rooms/" + roomId + "/main_image.jpg";
        
        firebaseManager.uploadImageAndGetUrl(imageUri, storagePath,
            downloadUri -> {
                // Update room document with image URL
                Map<String, Object> updates = new HashMap<>();
                updates.put("imageUrl", downloadUri.toString());
                
                firebaseManager.updateDocument(COLLECTION_ROOMS, roomId, updates,
                    aVoid -> {
                        Log.d(TAG, "Room image uploaded successfully");
                        callback.onSuccess(roomId);
                    },
                    e -> {
                        Log.e(TAG, "Error updating room with image URL: " + e.getMessage());
                        callback.onFailure(e.getMessage());
                    });
            },
            e -> {
                Log.e(TAG, "Error uploading room image: " + e.getMessage());
                callback.onFailure(e.getMessage());
            });
    }
    
    /**
     * Get all available rooms
     */
    public void getAllRooms(FirebaseCallback<List<Map<String, Object>>> callback) {
        firebaseManager.getCollection(COLLECTION_ROOMS,
            queryDocumentSnapshots -> {
                List<Map<String, Object>> roomsList = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Map<String, Object> roomData = document.getData();
                    roomData.put("roomId", document.getId());
                    roomsList.add(roomData);
                }
                Log.d(TAG, "Retrieved " + roomsList.size() + " rooms");
                callback.onSuccess(roomsList);
            },
            e -> {
                Log.e(TAG, "Error getting rooms: " + e.getMessage(), e);
                callback.onFailure(e.getMessage());
            });
    }
    
    /**
     * Get rooms by user ID
     */
    public void getRoomsByUser(String userId, FirebaseCallback<List<Map<String, Object>>> callback) {
        firebaseManager.getFirestore()
            .collection(COLLECTION_ROOMS)
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Map<String, Object>> roomsList = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Map<String, Object> roomData = document.getData();
                    roomData.put("roomId", document.getId());
                    roomsList.add(roomData);
                }
                Log.d(TAG, "Retrieved " + roomsList.size() + " rooms for user: " + userId);
                callback.onSuccess(roomsList);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error getting user rooms: " + e.getMessage(), e);
                callback.onFailure(e.getMessage());
            });
    }
    
    /**
     * Update room information
     */
    public void updateRoom(String roomId, Map<String, Object> updates, 
                          FirebaseCallback<Void> callback) {
        firebaseManager.updateDocument(COLLECTION_ROOMS, roomId, updates,
            aVoid -> {
                Log.d(TAG, "Room updated successfully");
                callback.onSuccess(null);
            },
            e -> {
                Log.e(TAG, "Error updating room: " + e.getMessage(), e);
                callback.onFailure(e.getMessage());
            });
    }
    
    /**
     * Delete room
     */
    public void deleteRoom(String roomId, FirebaseCallback<Void> callback) {
        firebaseManager.deleteDocument(COLLECTION_ROOMS, roomId,
            aVoid -> {
                Log.d(TAG, "Room deleted successfully");
                // Also delete associated images
                deleteRoomImages(roomId);
                callback.onSuccess(null);
            },
            e -> {
                Log.e(TAG, "Error deleting room: " + e.getMessage(), e);
                callback.onFailure(e.getMessage());
            });
    }
    
    /**
     * Delete room images from Storage
     */
    private void deleteRoomImages(String roomId) {
        String storagePath = "rooms/" + roomId + "/main_image.jpg";
        firebaseManager.deleteImage(storagePath,
            aVoid -> Log.d(TAG, "Room images deleted"),
            e -> Log.e(TAG, "Error deleting room images: " + e.getMessage()));
    }
    
    /**
     * Add room to favorites
     */
    public void addToFavorites(String userId, String roomId, FirebaseCallback<Void> callback) {
        String favoriteId = userId + "_" + roomId;
        Map<String, Object> favoriteData = new HashMap<>();
        favoriteData.put("userId", userId);
        favoriteData.put("roomId", roomId);
        favoriteData.put("createdAt", System.currentTimeMillis());
        
        firebaseManager.setDocument(COLLECTION_FAVORITES, favoriteId, favoriteData,
            aVoid -> {
                Log.d(TAG, "Room added to favorites");
                callback.onSuccess(null);
            },
            e -> {
                Log.e(TAG, "Error adding to favorites: " + e.getMessage(), e);
                callback.onFailure(e.getMessage());
            });
    }
    
    /**
     * Remove room from favorites
     */
    public void removeFromFavorites(String userId, String roomId, FirebaseCallback<Void> callback) {
        String favoriteId = userId + "_" + roomId;
        
        firebaseManager.deleteDocument(COLLECTION_FAVORITES, favoriteId,
            aVoid -> {
                Log.d(TAG, "Room removed from favorites");
                callback.onSuccess(null);
            },
            e -> {
                Log.e(TAG, "Error removing from favorites: " + e.getMessage(), e);
                callback.onFailure(e.getMessage());
            });
    }
    
    /**
     * Get user's favorite rooms
     */
    public void getFavoriteRooms(String userId, FirebaseCallback<List<String>> callback) {
        firebaseManager.getFirestore()
            .collection(COLLECTION_FAVORITES)
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<String> favoriteRoomIds = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    String roomId = document.getString("roomId");
                    if (roomId != null) {
                        favoriteRoomIds.add(roomId);
                    }
                }
                Log.d(TAG, "Retrieved " + favoriteRoomIds.size() + " favorite rooms");
                callback.onSuccess(favoriteRoomIds);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error getting favorite rooms: " + e.getMessage(), e);
                callback.onFailure(e.getMessage());
            });
    }
    
    /**
     * Search rooms by location
     */
    public void searchRoomsByLocation(String location, FirebaseCallback<List<Map<String, Object>>> callback) {
        firebaseManager.getFirestore()
            .collection(COLLECTION_ROOMS)
            .whereGreaterThanOrEqualTo("location", location)
            .whereLessThanOrEqualTo("location", location + "\uf8ff")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Map<String, Object>> roomsList = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Map<String, Object> roomData = document.getData();
                    roomData.put("roomId", document.getId());
                    roomsList.add(roomData);
                }
                Log.d(TAG, "Found " + roomsList.size() + " rooms in location: " + location);
                callback.onSuccess(roomsList);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error searching rooms: " + e.getMessage(), e);
                callback.onFailure(e.getMessage());
            });
    }
    
    /**
     * Search rooms by price range
     */
    public void searchRoomsByPriceRange(double minPrice, double maxPrice, 
                                       FirebaseCallback<List<Map<String, Object>>> callback) {
        firebaseManager.getFirestore()
            .collection(COLLECTION_ROOMS)
            .whereGreaterThanOrEqualTo("price", minPrice)
            .whereLessThanOrEqualTo("price", maxPrice)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Map<String, Object>> roomsList = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Map<String, Object> roomData = document.getData();
                    roomData.put("roomId", document.getId());
                    roomsList.add(roomData);
                }
                Log.d(TAG, "Found " + roomsList.size() + " rooms in price range");
                callback.onSuccess(roomsList);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error searching rooms by price: " + e.getMessage(), e);
                callback.onFailure(e.getMessage());
            });
    }
}
