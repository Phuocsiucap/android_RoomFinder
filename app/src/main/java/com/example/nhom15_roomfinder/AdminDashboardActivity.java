package com.example.nhom15_roomfinder;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.nhom15_roomfinder.firebase.FirebaseCallback;
import com.example.nhom15_roomfinder.firebase.FirebaseManager;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;
import java.util.Map;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView txtUserCount, txtAdCount, txtViews;
    private Button btnManageUsers, btnManageAds, btnStatistics;
    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_dashboard);
        
        firebaseManager = FirebaseManager.getInstance();
        
        initViews();
        setupClickListeners();
        loadStatistics();
    }

    private void initViews() {
        txtUserCount = findViewById(R.id.txtUserCount);
        txtAdCount = findViewById(R.id.txtAdCount);
        txtViews = findViewById(R.id.txtViews);
        btnManageUsers = findViewById(R.id.btnManageUsers);
        btnManageAds = findViewById(R.id.btnManageAds);
        btnStatistics = findViewById(R.id.btnStatistics);
    }

    private void setupClickListeners() {
        btnManageUsers.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, UserListActivity.class);
            startActivity(intent);
        });

        btnManageAds.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, ActivityListAds.class);
            startActivity(intent);
        });

        btnStatistics.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, StatisticActivity.class);
            startActivity(intent);
        });
    }

    private void loadStatistics() {
        // Load user count
        firebaseManager.getCollection("users",
            querySnapshot -> {
                int userCount = querySnapshot.size();
                txtUserCount.setText(String.valueOf(userCount));
            },
            e -> {
                Toast.makeText(this, "Lỗi tải số lượng người dùng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });

        // Load ads count
        firebaseManager.getCollection("rooms",
            querySnapshot -> {
                int adCount = querySnapshot.size();
                txtAdCount.setText(String.valueOf(adCount));
            },
            e -> {
                Toast.makeText(this, "Lỗi tải số lượng tin đăng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });

        // Load views (placeholder - you can implement actual view tracking)
        txtViews.setText("0");
    }
}