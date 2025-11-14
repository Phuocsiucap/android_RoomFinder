package com.example.nhom15_roomfinder.firebase;

/**
 * Generic callback interface for Firebase operations
 * @param <T> Type of data returned in success callback
 */
public interface FirebaseCallback<T> {
    
    /**
     * Called when Firebase operation succeeds
     * @param data The result data
     */
    void onSuccess(T data);
    
    /**
     * Called when Firebase operation fails
     * @param error The error message
     */
    void onFailure(String error);
}
