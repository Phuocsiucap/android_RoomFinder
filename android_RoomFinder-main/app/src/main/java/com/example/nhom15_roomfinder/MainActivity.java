package com.example.nhom15_roomfinder;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.nhom15_roomfinder.firebase.FirebaseManager;
import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        // Initialize Firebase
        initializeFirebase();
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
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
                Toast.makeText(this, "Firebase connected", Toast.LENGTH_SHORT).show();
                
                // Check if user is already logged in
                if (firebaseManager.isUserLoggedIn()) {
                    Log.d(TAG, "User is logged in: " + firebaseManager.getUserId());
                } else {
                    Log.d(TAG, "No user logged in");
                }
            } else {
                Log.e(TAG, "Firebase connection failed");
                Toast.makeText(this, "Firebase connection failed", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase: " + e.getMessage(), e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}