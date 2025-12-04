package com.example.nhom15_roomfinder;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.nhom15_roomfinder.firebase.FirebaseCallback;
import com.example.nhom15_roomfinder.firebase.FirebaseManager;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;
import java.util.Map;

public class AdminDashboardActivity extends AppCompatActivity {

    private static final String TAG = "AdminDashboard";

    private TextView txtUserCount, txtAdCount, txtViews, txtSystemStatus;
    private Button btnManageUsers, btnManageAds, btnStatistics;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_dashboard);
        
        firebaseManager = FirebaseManager.getInstance();
        
        // Initialize views first
        initViews();
        
        // Check if user is admin before allowing access
        checkAdminAccess();
        
        setupClickListeners();
        setupSwipeRefresh();
        loadStatistics();
    }
    
    /**
     * Check if current user has admin role
     */
    private void checkAdminAccess() {
        if (!firebaseManager.isUserLoggedIn()) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        String userId = firebaseManager.getUserId();
        if (userId == null) {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        Log.d(TAG, "Checking admin access for user: " + userId);
        
        firebaseManager.getFirestore()
            .collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String role = documentSnapshot.getString("role");
                    Log.d(TAG, "User role: " + role);
                    
                    if (!"admin".equals(role)) {
                        Toast.makeText(this, "Bạn không có quyền truy cập trang quản trị", 
                            Toast.LENGTH_LONG).show();
                        Log.w(TAG, "Non-admin user attempted to access admin dashboard. Role: " + role);
                        finish();
                    } else {
                        Log.d(TAG, "Admin access granted");
                    }
                } else {
                    Log.w(TAG, "User document not found in Firestore");
                    Toast.makeText(this, "Không tìm thấy thông tin người dùng trong hệ thống", 
                        Toast.LENGTH_SHORT).show();
                    finish();
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error checking admin access: " + e.getMessage(), e);
                Toast.makeText(this, "Lỗi kiểm tra quyền truy cập: " + e.getMessage(), 
                    Toast.LENGTH_LONG).show();
                finish();
            });
    }

    private void initViews() {
        txtUserCount = findViewById(R.id.txtUserCount);
        txtAdCount = findViewById(R.id.txtAdCount);
        txtViews = findViewById(R.id.txtViews);
        txtSystemStatus = findViewById(R.id.txtSystemStatus);
        btnManageUsers = findViewById(R.id.btnManageUsers);
        btnManageAds = findViewById(R.id.btnManageAds);
        btnStatistics = findViewById(R.id.btnStatistics);
        
        // Add progress bar if not in layout
        swipeRefreshLayout = findViewById(R.id.swipeRefresh);
        if (swipeRefreshLayout == null) {
            // Will be added to layout
        }
    }

    private void setupSwipeRefresh() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(() -> {
                loadStatistics();
            });
        }
    }

    private void setupClickListeners() {
        Log.d(TAG, "Setting up click listeners");
        
        if (btnManageUsers != null) {
            Log.d(TAG, "btnManageUsers found, setting click listener");
            btnManageUsers.setOnClickListener(v -> {
                Log.d(TAG, "btnManageUsers clicked");
                try {
                    Intent intent = new Intent(AdminDashboardActivity.this, UserListActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Error starting UserListActivity: " + e.getMessage(), e);
                    Toast.makeText(this, "Lỗi mở quản lý người dùng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.e(TAG, "btnManageUsers is null!");
        }

        if (btnManageAds != null) {
            Log.d(TAG, "btnManageAds found, setting click listener");
            btnManageAds.setOnClickListener(v -> {
                Log.d(TAG, "btnManageAds clicked");
                try {
                    Intent intent = new Intent(AdminDashboardActivity.this, ActivityListAds.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Error starting ActivityListAds: " + e.getMessage(), e);
                    Toast.makeText(this, "Lỗi mở quản lý tin đăng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.e(TAG, "btnManageAds is null!");
        }

        if (btnStatistics != null) {
            Log.d(TAG, "btnStatistics found, setting click listener");
            btnStatistics.setOnClickListener(v -> {
                Log.d(TAG, "btnStatistics clicked");
                try {
                    Intent intent = new Intent(AdminDashboardActivity.this, StatisticActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Error starting StatisticActivity: " + e.getMessage(), e);
                    Toast.makeText(this, "Lỗi mở thống kê: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.e(TAG, "btnStatistics is null!");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.admin_dashboard_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_refresh) {
            loadStatistics();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showLoading(boolean show) {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(show);
        }
    }

    private void loadStatistics() {
        showLoading(true);
        
        // Load user count
        firebaseManager.getCollection("users",
            querySnapshot -> {
                int userCount = querySnapshot.size();
                txtUserCount.setText(String.valueOf(userCount));
                checkLoadingComplete();
            },
            e -> {
                Log.e(TAG, "Error loading users: " + e.getMessage());
                Toast.makeText(this, "Lỗi tải số lượng người dùng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                checkLoadingComplete();
            });

        // Load ads count (all ads, same as ActivityListAds)
        firebaseManager.getCollection("rooms",
            querySnapshot -> {
                int adCount = querySnapshot.size();
                txtAdCount.setText(String.valueOf(adCount));
                checkLoadingComplete();
            },
            e -> {
                Log.e(TAG, "Error loading ads: " + e.getMessage());
                Toast.makeText(this, "Lỗi tải số lượng tin đăng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                txtAdCount.setText("0");
                checkLoadingComplete();
            });

        // Load number of distinct cities that have room ads
        firebaseManager.getCollection("rooms",
            querySnapshot -> {
                java.util.Set<String> cities = new java.util.HashSet<>();
                for (com.google.firebase.firestore.QueryDocumentSnapshot doc : querySnapshot) {
                    String city = doc.getString("city");
                    if (city != null) {
                        city = city.trim();
                    }
                    if (city != null && !city.isEmpty()) {
                        cities.add(city);
                    }
                }
                txtViews.setText(String.valueOf(cities.size()));
                checkLoadingComplete();
            },
            e -> {
                txtViews.setText("0");
                checkLoadingComplete();
            });

        // System status (always OK for now)
        txtSystemStatus.setText("98%");
    }

    private int loadingTasks = 0;
    private final int TOTAL_LOADING_TASKS = 3;

    private void checkLoadingComplete() {
        loadingTasks++;
        if (loadingTasks >= TOTAL_LOADING_TASKS) {
            showLoading(false);
            loadingTasks = 0;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh statistics when returning to dashboard
        loadStatistics();
    }
}