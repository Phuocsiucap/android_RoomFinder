package com.example.nhom15_roomfinder;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.widget.Button;

import com.example.nhom15_roomfinder.activity.PostRoomActivity;
import com.example.nhom15_roomfinder.adapter.AdAdapter;
import com.example.nhom15_roomfinder.firebase.FirebaseCallback;
import com.example.nhom15_roomfinder.firebase.FirebaseManager;
import com.example.nhom15_roomfinder.firebase.RoomFirebaseHelper;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class ActivityListAds extends AppCompatActivity implements AdAdapter.OnAdActionListener {

    private static final String TAG = "ActivityListAds";

    private RecyclerView rvAds;
    private EditText edtSearch;
    private EditText edtMinPrice, edtMaxPrice;
    private Spinner spinnerFilter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Button btnAddAd;
    private AdAdapter adAdapter;
    private List<Map<String, Object>> allAdsList;
    private List<Map<String, Object>> filteredAdsList;
    private FirebaseManager firebaseManager;
    private RoomFirebaseHelper roomFirebaseHelper;
    private String currentFilter = "all"; // all, available, pending, blocked

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_list_ads);
        
        firebaseManager = FirebaseManager.getInstance();
        roomFirebaseHelper = new RoomFirebaseHelper();
        allAdsList = new ArrayList<>();
        filteredAdsList = new ArrayList<>();
        
        initViews();
        setupRecyclerView();
        setupSearch();
        setupFilter();
        setupSwipeRefresh();
        loadAds();
    }

    private void initViews() {
        rvAds = findViewById(R.id.rvAds);
        edtSearch = findViewById(R.id.edtSearch);
        edtMinPrice = findViewById(R.id.edtMinPrice);
        edtMaxPrice = findViewById(R.id.edtMaxPrice);
        spinnerFilter = findViewById(R.id.spinnerFilter);
        swipeRefreshLayout = findViewById(R.id.swipeRefresh);
        btnAddAd = findViewById(R.id.btnAddAd);
        
        // Set click listener for Add Ad button
        if (btnAddAd != null) {
            btnAddAd.setOnClickListener(v -> {
                Intent intent = new Intent(ActivityListAds.this, PostRoomActivity.class);
                startActivity(intent);
            });
        }
    }

    private void setupSwipeRefresh() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(() -> {
                loadAds();
            });
        }
    }

    private void setupFilter() {
        if (spinnerFilter != null) {
            String[] filterOptions = {"Tất cả", "Đang hoạt động", "Đang chờ", "Đã khóa"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, filterOptions);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerFilter.setAdapter(adapter);
            
            spinnerFilter.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                    switch (position) {
                        case 0: currentFilter = "all"; break;
                        case 1: currentFilter = "available"; break;
                        case 2: currentFilter = "pending"; break;
                        case 3: currentFilter = "blocked"; break;
                    }
                    applyFilters();
                }

                @Override
                public void onNothingSelected(android.widget.AdapterView<?> parent) {}
            });
        }
    }

    private void setupRecyclerView() {
        adAdapter = new AdAdapter(filteredAdsList, this);
        rvAds.setLayoutManager(new LinearLayoutManager(this));
        rvAds.setAdapter(adAdapter);
    }

    private void setupSearch() {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        edtSearch.addTextChangedListener(watcher);
        if (edtMinPrice != null) {
            edtMinPrice.addTextChangedListener(watcher);
        }
        if (edtMaxPrice != null) {
            edtMaxPrice.addTextChangedListener(watcher);
        }
    }

    private void applyFilters() {
        String query = edtSearch.getText().toString().toLowerCase();

        // Parse price range
        double minPrice = -1;
        double maxPrice = -1;
        try {
            if (edtMinPrice != null) {
                String minStr = edtMinPrice.getText().toString().trim();
                if (!minStr.isEmpty()) {
                    minPrice = Double.parseDouble(minStr);
                }
            }
            if (edtMaxPrice != null) {
                String maxStr = edtMaxPrice.getText().toString().trim();
                if (!maxStr.isEmpty()) {
                    maxPrice = Double.parseDouble(maxStr);
                }
            }
        } catch (NumberFormatException e) {
            // Nếu nhập sai định dạng, bỏ qua filter giá
            minPrice = -1;
            maxPrice = -1;
        }
        filteredAdsList.clear();

        for (Map<String, Object> ad : allAdsList) {
            // Apply status filter - check both status and isAvailable
            String status = (String) ad.get("status");
            Object isAvailableObj = ad.get("isAvailable");
            boolean isAvailable = isAvailableObj instanceof Boolean ? (Boolean) isAvailableObj : false;
            
            boolean statusMatch = false;
            switch (currentFilter) {
                case "all":
                    statusMatch = true;
                    break;
                case "available":
                    statusMatch = isAvailable && !"blocked".equals(status);
                    break;
                case "pending":
                    statusMatch = !isAvailable || "pending".equals(status);
                    break;
                case "blocked":
                    statusMatch = "blocked".equals(status) || !isAvailable;
                    break;
            }

            // Apply search filter
            boolean searchMatch = query.isEmpty();
            if (!searchMatch) {
                String title = (String) ad.get("title");
                String address = (String) ad.get("address");
                String district = (String) ad.get("district");
                String city = (String) ad.get("city");
                searchMatch = (title != null && title.toLowerCase().contains(query)) ||
                             (address != null && address.toLowerCase().contains(query)) ||
                             (district != null && district.toLowerCase().contains(query)) ||
                             (city != null && city.toLowerCase().contains(query));
            }

            // Apply price filter
            boolean priceMatch = true;
            if (minPrice >= 0 || maxPrice >= 0) {
                Object priceObj = ad.get("price");
                double price = 0;
                if (priceObj instanceof Number) {
                    price = ((Number) priceObj).doubleValue();
                } else if (priceObj != null) {
                    try {
                        price = Double.parseDouble(priceObj.toString());
                    } catch (NumberFormatException ignored) {}
                }

                if (minPrice >= 0 && price < minPrice) priceMatch = false;
                if (maxPrice >= 0 && price > maxPrice) priceMatch = false;
            }

            if (statusMatch && searchMatch && priceMatch) {
                filteredAdsList.add(ad);
            }
        }

        // Sort by creation date (newest first)
        Collections.sort(filteredAdsList, (ad1, ad2) -> {
            Object time1 = ad1.get("createdAt");
            Object time2 = ad2.get("createdAt");
            if (time1 instanceof Number && time2 instanceof Number) {
                return Long.compare(((Number) time2).longValue(), ((Number) time1).longValue());
            }
            return 0;
        });

        adAdapter.updateList(filteredAdsList);
    }

    private void loadAds() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(true);
        }

        // Load directly from Firestore to get document IDs
        firebaseManager.getCollection("rooms",
            querySnapshot -> {
                allAdsList.clear();
                for (com.google.firebase.firestore.QueryDocumentSnapshot document : querySnapshot) {
                    Map<String, Object> roomData = document.getData();
                    // Add document ID as roomId
                    roomData.put("roomId", document.getId());
                    // Also ensure id field exists
                    if (!roomData.containsKey("id")) {
                        roomData.put("id", document.getId());
                    }
                    allAdsList.add(roomData);
                }
                applyFilters();
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            },
            e -> {
                Log.e(TAG, "Error loading ads: " + e.getMessage());
                Toast.makeText(ActivityListAds.this, "Lỗi tải danh sách tin: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.admin_ads_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_refresh) {
            loadAds();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onEditClick(Map<String, Object> ad) {
        String adId = (String) ad.get("roomId");
        if (adId == null) {
            // Try to get from id field
            adId = (String) ad.get("id");
        }
        
        if (adId == null) {
            Toast.makeText(this, "Không tìm thấy ID tin đăng", Toast.LENGTH_SHORT).show();
            return;
        }

        // Open EditRoomActivity
        Intent intent = new Intent(ActivityListAds.this, EditRoomActivity.class);
        intent.putExtra("roomId", adId);
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(String adId) {
        new AlertDialog.Builder(this)
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc chắn muốn xóa tin đăng này? Hành động này không thể hoàn tác.")
            .setPositiveButton("Xóa", (dialog, which) -> {
                // Delete from Firestore
                firebaseManager.deleteDocument("rooms", adId,
                    aVoid -> {
                        Toast.makeText(ActivityListAds.this, "Xóa tin đăng thành công", 
                            Toast.LENGTH_SHORT).show();
                        loadAds();
                    },
                    e -> {
                        Toast.makeText(ActivityListAds.this, "Lỗi xóa tin: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    });
            })
            .setNegativeButton("Hủy", null)
            .show();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh list when returning from EditRoomActivity or PostRoomActivity
        loadAds();
    }
}