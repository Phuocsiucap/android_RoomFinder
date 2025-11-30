package com.example.nhom15_roomfinder;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
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

import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class StatisticActivity extends AppCompatActivity {

    private Spinner spinnerTime;
    private Button btnApply;
    private TextView txtTotalAds, txtTotalViews, txtActiveUsers;
    private FirebaseManager firebaseManager;
    private String selectedTimeFilter = "all"; // all, week, month, year

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_statistic);
        
        firebaseManager = FirebaseManager.getInstance();
        
        initViews();
        setupSpinner();
        setupButton();
        loadStatistics("all");
    }

    private void initViews() {
        spinnerTime = findViewById(R.id.spinnerTime);
        btnApply = findViewById(R.id.btnApply);
        txtTotalAds = findViewById(R.id.txtTotalAds);
        txtTotalViews = findViewById(R.id.txtTotalViews);
        txtActiveUsers = findViewById(R.id.txtActiveUsers);
    }

    private void setupSpinner() {
        String[] timeFilters = {"Tất cả", "Tuần này", "Tháng này", "Năm nay"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, timeFilters);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTime.setAdapter(adapter);

        spinnerTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: selectedTimeFilter = "all"; break;
                    case 1: selectedTimeFilter = "week"; break;
                    case 2: selectedTimeFilter = "month"; break;
                    case 3: selectedTimeFilter = "year"; break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupButton() {
        btnApply.setOnClickListener(v -> loadStatistics(selectedTimeFilter));
    }

    private void loadStatistics(String timeFilter) {
        long startTime = getStartTimeForFilter(timeFilter);

        // Load total ads
        if (timeFilter.equals("all")) {
            firebaseManager.getCollection("rooms",
                querySnapshot -> {
                    int totalAds = querySnapshot.size();
                    txtTotalAds.setText(String.valueOf(totalAds));
                },
                e -> {
                    Toast.makeText(this, "Lỗi tải số lượng tin: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                });
        } else {
            // For time-filtered queries, we need to use Timestamp
            // Since createdAt might be stored as Timestamp or Long, we'll query all and filter
            firebaseManager.getCollection("rooms",
                querySnapshot -> {
                    int count = 0;
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : querySnapshot) {
                        Object createdAt = doc.get("createdAt");
                        if (createdAt != null) {
                            long createdTime = 0;
                            if (createdAt instanceof Number) {
                                createdTime = ((Number) createdAt).longValue();
                            } else if (createdAt instanceof com.google.firebase.Timestamp) {
                                createdTime = ((com.google.firebase.Timestamp) createdAt).toDate().getTime();
                            }
                            if (createdTime >= startTime) {
                                count++;
                            }
                        }
                    }
                    txtTotalAds.setText(String.valueOf(count));
                },
                e -> {
                    Toast.makeText(this, "Lỗi tải số lượng tin: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                });
        }

        // Load total users
        if (timeFilter.equals("all")) {
            firebaseManager.getCollection("users",
                querySnapshot -> {
                    int totalUsers = querySnapshot.size();
                    txtActiveUsers.setText(String.valueOf(totalUsers));
                },
                e -> {
                    Toast.makeText(this, "Lỗi tải số lượng người dùng: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                });
        } else {
            firebaseManager.getCollection("users",
                querySnapshot -> {
                    int count = 0;
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : querySnapshot) {
                        Object createdAt = doc.get("createdAt");
                        if (createdAt != null) {
                            long createdTime = 0;
                            if (createdAt instanceof Number) {
                                createdTime = ((Number) createdAt).longValue();
                            } else if (createdAt instanceof com.google.firebase.Timestamp) {
                                createdTime = ((com.google.firebase.Timestamp) createdAt).toDate().getTime();
                            }
                            if (createdTime >= startTime) {
                                count++;
                            }
                        }
                    }
                    txtActiveUsers.setText(String.valueOf(count));
                },
                e -> {
                    Toast.makeText(this, "Lỗi tải số lượng người dùng: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                });
        }

        // Load total views (calculate from all rooms)
        firebaseManager.getCollection("rooms",
            querySnapshot -> {
                int totalViews = 0;
                for (com.google.firebase.firestore.QueryDocumentSnapshot doc : querySnapshot) {
                    Object viewsObj = doc.get("views");
                    if (viewsObj instanceof Number) {
                        totalViews += ((Number) viewsObj).intValue();
                    }
                }
                txtTotalViews.setText(String.valueOf(totalViews));
            },
            e -> {
                txtTotalViews.setText("0");
            });
    }

    private long getStartTimeForFilter(String filter) {
        Calendar calendar = Calendar.getInstance();
        
        switch (filter) {
            case "week":
                calendar.add(Calendar.WEEK_OF_YEAR, -1);
                break;
            case "month":
                calendar.add(Calendar.MONTH, -1);
                break;
            case "year":
                calendar.add(Calendar.YEAR, -1);
                break;
            default:
                return 0; // All time
        }
        
        return calendar.getTimeInMillis();
    }
}