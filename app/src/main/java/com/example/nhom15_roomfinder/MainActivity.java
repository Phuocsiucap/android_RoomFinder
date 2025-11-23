package com.example.nhom15_roomfinder;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.nhom15_roomfinder.activity.HomeActivity;
import com.example.nhom15_roomfinder.activity.LoginActivity;
import com.example.nhom15_roomfinder.firebase.FirebaseManager;
import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        
        // Initialize Firebase first
        initializeFirebase();
        
        // Check authentication and navigate accordingly
        checkAuthenticationAndNavigate();
    }
    
    /**
     * Initialize Firebase and check connection status
     */
    private void initializeFirebase() {
        try {
            // Initialize Firebase App
            FirebaseApp.initializeApp(this);
            
            // Get FirebaseManager instance
            firebaseManager = FirebaseManager.getInstance();
            
            // Check if Firebase is properly connected
            if (firebaseManager.isFirebaseConnected()) {
                Log.d(TAG, "Firebase connected successfully");
            } else {
                Log.e(TAG, "Firebase connection failed");
                Toast.makeText(this, "Firebase connection failed", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase: " + e.getMessage(), e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Check user authentication and navigate to appropriate screen
     */
    private void checkAuthenticationAndNavigate() {
        if (firebaseManager != null) {
            if (firebaseManager.isUserLoggedIn()) {
                // User is logged in, navigate to HomeActivity
                String userId = firebaseManager.getUserId();
                Log.d(TAG, "User is logged in: " + userId);
                navigateToHome();
            } else {
                // User is not logged in, navigate to LoginActivity
                Log.d(TAG, "No user logged in, redirecting to login");
                navigateToLogin();
            }
        } else {
            // Firebase not initialized properly, show error and go to login
            Log.e(TAG, "FirebaseManager is null");
            Toast.makeText(this, "Initialization error", Toast.LENGTH_SHORT).show();
            navigateToLogin();
        }
    }
    
    /**
     * Navigate to HomeActivity
     */
    private void navigateToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    /**
     * Navigate to LoginActivity
     */
    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}