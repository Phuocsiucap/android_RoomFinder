package com.example.nhom15_roomfinder.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom15_roomfinder.R;
import com.example.nhom15_roomfinder.firebase.FirebaseManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    private FirebaseManager firebaseManager;
    
    // UI Components
    private ImageView imgProfile;
    private ImageView imgNotification;
    private ImageView imgLocation;
    private EditText etSearch;
    private LinearLayout btnCategoryAffordable;
    private LinearLayout btnCategoryAC;
    private LinearLayout btnCategorySchool;
    private LinearLayout btnCategorySpaciou;
    private RecyclerView rvNewRooms;
    private RecyclerView rvNearbyRooms;
    private RecyclerView rvSuggestedRooms;
    private Button btnPostRoom;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        
        // Initialize Firebase
        firebaseManager = FirebaseManager.getInstance();
        
        // Check if user is logged in
        if (!firebaseManager.isUserLoggedIn()) {
            redirectToLogin();
            return;
        }
        
        // Initialize Views
        initializeViews();
        
        // Set Listeners
        setListeners();
        
        // Setup RecyclerViews
        setupRecyclerViews();
        
        // Load data
        loadData();
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    
    /**
     * Initialize Views
     */
    private void initializeViews() {
        imgProfile = findViewById(R.id.imgProfile);
        imgNotification = findViewById(R.id.imgNotification);
        imgLocation = findViewById(R.id.imgLocation);
        etSearch = findViewById(R.id.etSearch);
        
        // Category buttons
        btnCategoryAffordable = findViewById(R.id.btnCategoryAffordable);
        btnCategoryAC = findViewById(R.id.btnCategoryAC);
        btnCategorySchool = findViewById(R.id.btnCategorySchool);
        btnCategorySpaciou = findViewById(R.id.btnCategorySpaciou);
        
        // RecyclerViews
        rvNewRooms = findViewById(R.id.rvNewRooms);
        rvNearbyRooms = findViewById(R.id.rvNearbyRooms);
        rvSuggestedRooms = findViewById(R.id.rvSuggestedRooms);
        
        // Buttons
        btnPostRoom = findViewById(R.id.btnPostRoom);
        
        // Bottom Navigation
        bottomNavigation = findViewById(R.id.bottomNavigation);
        
        // Set home tab as selected
        bottomNavigation.setSelectedItemId(R.id.nav_home);
    }
    
    /**
     * Set Listeners
     */
    private void setListeners() {
        // Profile click
        imgProfile.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
        
        // Notification click
        imgNotification.setOnClickListener(v -> {
            showToast("Thông báo");
            // TODO: Navigate to NotificationActivity
        });
        
        // Location click
        imgLocation.setOnClickListener(v -> {
            showToast("Vị trí hiện tại");
            // TODO: Get current location and update search
        });
        
        // Search functionality
        etSearch.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                // TODO: Navigate to SearchActivity
                showToast("Tìm kiếm");
            }
        });
        
        // Category buttons
        btnCategoryAffordable.setOnClickListener(v -> {
            showToast("Tìm phòng giá rẻ");
            // TODO: Filter rooms by affordable price
        });
        
        btnCategoryAC.setOnClickListener(v -> {
            showToast("Tìm phòng có máy lạnh");
            // TODO: Filter rooms with AC
        });
        
        btnCategorySchool.setOnClickListener(v -> {
            showToast("Tìm phòng gần trường");
            // TODO: Filter rooms near schools
        });
        
        btnCategorySpaciou.setOnClickListener(v -> {
            showToast("Tìm phòng rộng");
            // TODO: Filter spacious rooms
        });
        
        // Post room button
        btnPostRoom.setOnClickListener(v -> {
            showToast("Đăng tin phòng");
            // TODO: Navigate to PostRoomActivity
        });
        
        // Bottom navigation
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            
            if (itemId == R.id.nav_home) {
                // Already on home
                return true;
            } else if (itemId == R.id.nav_search) {
                showToast("Tìm kiếm");
                // TODO: Navigate to SearchActivity
                return true;
            } else if (itemId == R.id.nav_favorites) {
                showToast("Yêu thích");
                // TODO: Navigate to FavoritesActivity
                return true;
            } else if (itemId == R.id.nav_messages) {
                showToast("Tin nhắn");
                // TODO: Navigate to MessagesActivity
                return true;
            } else if (itemId == R.id.nav_profile) {
                // Navigate to ProfileActivity
                Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                startActivity(intent);
                return true;
            }
            
            return false;
        });
    }
    
    /**
     * Setup RecyclerViews
     */
    private void setupRecyclerViews() {
        // Setup horizontal RecyclerView for new rooms
        rvNewRooms.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvNewRooms.setHasFixedSize(true);
        
        // Setup vertical RecyclerView for nearby rooms
        rvNearbyRooms.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rvNearbyRooms.setHasFixedSize(true);
        
        // Setup vertical RecyclerView for suggested rooms
        rvSuggestedRooms.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rvSuggestedRooms.setHasFixedSize(true);
        
        // TODO: Set adapters when data models are ready
    }
    
    /**
     * Load data for the home screen
     */
    private void loadData() {
        // TODO: Load rooms data from Firebase
        loadNewRooms();
        loadNearbyRooms();
        loadSuggestedRooms();
    }
    
    /**
     * Load new rooms data
     */
    private void loadNewRooms() {
        // TODO: Implement Firebase query for new rooms
        Log.d(TAG, "Loading new rooms...");
    }
    
    /**
     * Load nearby rooms data
     */
    private void loadNearbyRooms() {
        // TODO: Implement Firebase query for nearby rooms based on user location
        Log.d(TAG, "Loading nearby rooms...");
    }
    
    /**
     * Load suggested rooms data
     */
    private void loadSuggestedRooms() {
        // TODO: Implement Firebase query for suggested rooms based on user preferences
        Log.d(TAG, "Loading suggested rooms...");
    }
    
    /**
     * Redirect to login if user not authenticated
     */
    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    /**
     * Helper method to show toast messages
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Check authentication status when returning to activity
        if (!firebaseManager.isUserLoggedIn()) {
            redirectToLogin();
        } else {
            // Reset bottom navigation to home tab
            bottomNavigation.setSelectedItemId(R.id.nav_home);
        }
    }
}