package com.example.nhom15_roomfinder.firebase;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

/**
 * FirebaseManager - Singleton class to manage all Firebase operations
 * This class provides centralized access to Firebase services:
 * - Authentication
 * - Firestore (NoSQL Database)
 * - Realtime Database
 * - Cloud Storage
 */
public class FirebaseManager {
    
    private static final String TAG = "FirebaseManager";
    private static FirebaseManager instance;
    
    // Firebase instances
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private DatabaseReference realtimeDb;
    private StorageReference storageRef;
    
    // Private constructor for Singleton pattern
    private FirebaseManager() {
        initializeFirebase();
    }
    
    /**
     * Get singleton instance of FirebaseManager
     */
    public static synchronized FirebaseManager getInstance() {
        if (instance == null) {
            instance = new FirebaseManager();
        }
        return instance;
    }
    
    /**
     * Initialize all Firebase services
     */
    private void initializeFirebase() {
        try {
            // Initialize Firebase Authentication
            mAuth = FirebaseAuth.getInstance();
            
            // Initialize Firestore
            db = FirebaseFirestore.getInstance();
            
            // Initialize Realtime Database
            realtimeDb = FirebaseDatabase.getInstance().getReference();
            
            // Initialize Cloud Storage
            storageRef = FirebaseStorage.getInstance().getReference();
            
            Log.d(TAG, "Firebase initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase: " + e.getMessage(), e);
        }
    }
    
    // ==================== Authentication Methods ====================
    
    /**
     * Get current Firebase user
     */
    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }
    
    /**
     * Check if user is logged in
     */
    public boolean isUserLoggedIn() {
        return mAuth.getCurrentUser() != null;
    }
    
    /**
     * Register new user with email and password
     */
    public void registerUser(String email, String password, OnCompleteListener<AuthResult> listener) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(listener);
    }
    
    /**
     * Sign in user with email and password
     */
    public void signInUser(String email, String password, OnCompleteListener<AuthResult> listener) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(listener);
    }
    
    /**
     * Sign out current user
     */
    public void signOut() {
        mAuth.signOut();
        Log.d(TAG, "User signed out");
    }
    
    /**
     * Send password reset email
     */
    public void sendPasswordResetEmail(String email, OnCompleteListener<Void> listener) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(listener);
    }
    
    /**
     * Get user ID of current logged in user
     */
    public String getUserId() {
        FirebaseUser user = getCurrentUser();
        return user != null ? user.getUid() : null;
    }
    
    // ==================== Firestore Methods ====================
    
    /**
     * Get Firestore instance
     */
    public FirebaseFirestore getFirestore() {
        return db;
    }
    
    /**
     * Add document to Firestore collection
     */
    public void addDocument(String collection, Map<String, Object> data, 
                           OnSuccessListener<DocumentReference> successListener,
                           OnFailureListener failureListener) {
        db.collection(collection)
                .add(data)
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }
    
    /**
     * Set document in Firestore collection with specific ID
     */
    public void setDocument(String collection, String documentId, Map<String, Object> data,
                           OnSuccessListener<Void> successListener,
                           OnFailureListener failureListener) {
        db.collection(collection)
                .document(documentId)
                .set(data)
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }
    
    /**
     * Update document in Firestore
     */
    public void updateDocument(String collection, String documentId, Map<String, Object> updates,
                              OnSuccessListener<Void> successListener,
                              OnFailureListener failureListener) {
        db.collection(collection)
                .document(documentId)
                .update(updates)
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }
    
    /**
     * Delete document from Firestore
     */
    public void deleteDocument(String collection, String documentId,
                              OnSuccessListener<Void> successListener,
                              OnFailureListener failureListener) {
        db.collection(collection)
                .document(documentId)
                .delete()
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }
    
    /**
     * Get all documents from a collection
     */
    public void getCollection(String collection,
                             OnSuccessListener<QuerySnapshot> successListener,
                             OnFailureListener failureListener) {
        db.collection(collection)
                .get()
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }
    
    // ==================== Realtime Database Methods ====================
    
    /**
     * Get Realtime Database reference
     */
    public DatabaseReference getRealtimeDatabase() {
        return realtimeDb;
    }
    
    /**
     * Get reference to specific path in Realtime Database
     */
    public DatabaseReference getDatabaseReference(String path) {
        return realtimeDb.child(path);
    }
    
    /**
     * Write data to Realtime Database
     */
    public void writeToRealtimeDb(String path, Object value,
                                 OnSuccessListener<Void> successListener,
                                 OnFailureListener failureListener) {
        realtimeDb.child(path)
                .setValue(value)
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }
    
    /**
     * Update data in Realtime Database
     */
    public void updateRealtimeDb(String path, Map<String, Object> updates,
                                OnSuccessListener<Void> successListener,
                                OnFailureListener failureListener) {
        realtimeDb.child(path)
                .updateChildren(updates)
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }
    
    /**
     * Delete data from Realtime Database
     */
    public void deleteFromRealtimeDb(String path,
                                    OnSuccessListener<Void> successListener,
                                    OnFailureListener failureListener) {
        realtimeDb.child(path)
                .removeValue()
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }
    
    // ==================== Cloud Storage Methods ====================
    
    /**
     * Get Storage reference
     */
    public StorageReference getStorageReference() {
        return storageRef;
    }
    
    /**
     * Upload image to Firebase Storage
     */
    public void uploadImage(Uri imageUri, String storagePath,
                           OnSuccessListener<UploadTask.TaskSnapshot> successListener,
                           OnFailureListener failureListener) {
        StorageReference imageRef = storageRef.child(storagePath);
        imageRef.putFile(imageUri)
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }
    
    /**
     * Upload image and get download URL
     */
    public void uploadImageAndGetUrl(Uri imageUri, String storagePath,
                                    OnSuccessListener<Uri> urlSuccessListener,
                                    OnFailureListener failureListener) {
        StorageReference imageRef = storageRef.child(storagePath);
        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get download URL after successful upload
                    imageRef.getDownloadUrl()
                            .addOnSuccessListener(urlSuccessListener)
                            .addOnFailureListener(failureListener);
                })
                .addOnFailureListener(failureListener);
    }
    
    /**
     * Delete image from Firebase Storage
     */
    public void deleteImage(String storagePath,
                           OnSuccessListener<Void> successListener,
                           OnFailureListener failureListener) {
        StorageReference imageRef = storageRef.child(storagePath);
        imageRef.delete()
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }
    
    /**
     * Download image URL from Firebase Storage
     */
    public void getDownloadUrl(String storagePath,
                              OnSuccessListener<Uri> successListener,
                              OnFailureListener failureListener) {
        StorageReference imageRef = storageRef.child(storagePath);
        imageRef.getDownloadUrl()
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }
    
    // ==================== Helper Methods ====================
    
    /**
     * Create user profile in Firestore after registration
     */
    public void createUserProfile(String userId, String email, String name,
                                 OnSuccessListener<Void> successListener,
                                 OnFailureListener failureListener) {
        Map<String, Object> userProfile = new HashMap<>();
        userProfile.put("userId", userId);
        userProfile.put("email", email);
        userProfile.put("name", name);
        userProfile.put("createdAt", System.currentTimeMillis());
        
        setDocument("users", userId, userProfile, successListener, failureListener);
    }
    
    /**
     * Create user profile for Google Sign-In users
     */
    public void createGoogleUserProfile(FirebaseUser user,
                                       OnSuccessListener<Void> successListener,
                                       OnFailureListener failureListener) {
        Map<String, Object> userProfile = new HashMap<>();
        userProfile.put("userId", user.getUid());
        userProfile.put("email", user.getEmail());
        userProfile.put("name", user.getDisplayName());
        userProfile.put("photoUrl", user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null);
        userProfile.put("loginProvider", "google");
        userProfile.put("createdAt", System.currentTimeMillis());
        userProfile.put("lastLoginAt", System.currentTimeMillis());
        
        setDocument("users", user.getUid(), userProfile, successListener, failureListener);
    }
    
    /**
     * Check Firebase connection status
     */
    public boolean isFirebaseConnected() {
        return mAuth != null && db != null && realtimeDb != null && storageRef != null;
    }
    
    /**
     * Get Firebase Auth instance
     */
    public FirebaseAuth getAuth() {
        return mAuth;
    }
}
