package com.example.nhom15_roomfinder.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom15_roomfinder.R;
import com.example.nhom15_roomfinder.adapter.RoomAdapter;
import com.example.nhom15_roomfinder.entity.Room;
import com.example.nhom15_roomfinder.firebase.FirebaseManager;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * FilteredRoomsActivity - Hiển thị danh sách phòng đã lọc
 */
public class FilteredRoomsActivity extends AppCompatActivity {

    private static final String TAG = "FilteredRoomsActivity";

    private ImageButton btnBack;
    private TextView tvTitle, tvResultCount;
    private RecyclerView rvRooms;
    private LinearLayout emptyStateLayout;
    private ProgressBar progressBar;

    private FirebaseManager firebaseManager;
    private List<Room> roomList = new ArrayList<>();
    private List<Room> allRooms = new ArrayList<>();
    private RoomAdapter adapter;

    // Filter flags
    private boolean filterCheap, filterAC, filterWifi, filterParking, filterNearSchool, filterSpacious;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filtered_rooms);

        firebaseManager = FirebaseManager.getInstance();

        getIntentData();
        initViews();
        setupRecyclerView();
        setListeners();
        loadRooms();
    }

    private void getIntentData() {
        Intent intent = getIntent();
        filterCheap = intent.getBooleanExtra("filterCheap", false);
        filterAC = intent.getBooleanExtra("filterAC", false);
        filterWifi = intent.getBooleanExtra("filterWifi", false);
        filterParking = intent.getBooleanExtra("filterParking", false);
        filterNearSchool = intent.getBooleanExtra("filterNearSchool", false);
        filterSpacious = intent.getBooleanExtra("filterSpacious", false);
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvTitle = findViewById(R.id.tvTitle);
        tvResultCount = findViewById(R.id.tvResultCount);
        rvRooms = findViewById(R.id.rvRooms);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        progressBar = findViewById(R.id.progressBar);

        // Set title based on filters
        String title = getIntent().getStringExtra("title");
        if (title != null && !title.isEmpty()) {
            tvTitle.setText(title);
        } else {
            tvTitle.setText("Kết quả lọc");
        }
    }

    private void setupRecyclerView() {
        adapter = new RoomAdapter(this, roomList, new RoomAdapter.OnRoomClickListener() {
            @Override
            public void onRoomClick(Room room) {
                Intent intent = new Intent(FilteredRoomsActivity.this, PropertyDetailActivity.class);
                intent.putExtra("room", room);
                startActivity(intent);
            }

            @Override
            public void onFavoriteClick(Room room, int position) {
                toggleFavorite(room, position);
            }
        });

        rvRooms.setLayoutManager(new LinearLayoutManager(this));
        rvRooms.setAdapter(adapter);
    }

    private void toggleFavorite(Room room, int position) {
        String currentUserId = firebaseManager.getUserId();
        if (currentUserId == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        if (room.isFavorite()) {
            // Xóa khỏi yêu thích
            firebaseManager.getFirestore()
                .collection("favorites")
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("roomId", room.getId())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        doc.getReference().delete();
                    }
                    room.setFavorite(false);
                    adapter.notifyItemChanged(position);
                    Toast.makeText(this, "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();
                });
        } else {
            // Thêm vào yêu thích
            java.util.Map<String, Object> favorite = new java.util.HashMap<>();
            favorite.put("userId", currentUserId);
            favorite.put("roomId", room.getId());
            favorite.put("createdAt", System.currentTimeMillis());

            firebaseManager.getFirestore()
                .collection("favorites")
                .add(favorite)
                .addOnSuccessListener(docRef -> {
                    room.setFavorite(true);
                    adapter.notifyItemChanged(position);
                    Toast.makeText(this, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
                });
        }
    }

    private void setListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadRooms() {
        showLoading(true);

        firebaseManager.getFirestore()
            .collection("rooms")
            .whereEqualTo("status", "active")
            .get()
            .addOnSuccessListener(querySnapshot -> {
                allRooms.clear();

                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    Room room = doc.toObject(Room.class);
                    if (room != null) {
                        room.setId(doc.getId());
                        allRooms.add(room);
                    }
                }

                applyFilters();
                showLoading(false);
            })
            .addOnFailureListener(e -> {
                showLoading(false);
                Log.e(TAG, "Error loading rooms: " + e.getMessage());
                Toast.makeText(this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            });
    }

    private void applyFilters() {
        roomList.clear();

        for (Room room : allRooms) {
            boolean match = true;

            // Filter by cheap price (< 3,000,000 VND)
            if (filterCheap && room.getPrice() >= 3000000) {
                match = false;
            }

            // Filter by AC
            if (filterAC && !room.isHasAC()) {
                match = false;
            }

            // Filter by WiFi
            if (filterWifi && !room.isHasWifi()) {
                match = false;
            }

            // Filter by Parking
            if (filterParking && !room.isHasParking()) {
                match = false;
            }

            // Filter by spacious (>= 20m2)
            if (filterSpacious && room.getArea() < 20) {
                match = false;
            }

            // Filter near school - check description/address contains school-related keywords
            if (filterNearSchool) {
                String address = room.getAddress() != null ? room.getAddress().toLowerCase() : "";
                String description = room.getDescription() != null ? room.getDescription().toLowerCase() : "";
                boolean nearSchool = address.contains("trường") || address.contains("đại học") 
                    || address.contains("cao đẳng") || description.contains("gần trường")
                    || description.contains("gần đại học");
                if (!nearSchool) {
                    match = false;
                }
            }

            if (match) {
                roomList.add(room);
            }
        }

        adapter.notifyDataSetChanged();
        updateUI();
    }

    private void updateUI() {
        tvResultCount.setText("Tìm thấy " + roomList.size() + " phòng");

        if (roomList.isEmpty()) {
            rvRooms.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
        } else {
            rvRooms.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
