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
import com.example.nhom15_roomfinder.adapter.RoomSearchAdapter;
import com.example.nhom15_roomfinder.entity.Room;
import com.example.nhom15_roomfinder.firebase.FirebaseManager;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

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

    private RoomSearchAdapter adapter;

    // Filter flags
    private boolean filterCheap, filterAC, filterWifi, filterParking, filterNear, filterSpacious;

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
        filterNear = intent.getBooleanExtra("filterNear", false);
        filterSpacious = intent.getBooleanExtra("filterSpacious", false);
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvTitle = findViewById(R.id.tvTitle);
        tvResultCount = findViewById(R.id.tvResultCount);
        rvRooms = findViewById(R.id.rvRooms);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        progressBar = findViewById(R.id.progressBar);

        String title = getIntent().getStringExtra("title");
        tvTitle.setText(title != null ? title : "Kết quả lọc");
    }

    private void setupRecyclerView() {
        adapter = new RoomSearchAdapter(
                this,
                roomList,
                room -> {
                    Intent intent = new Intent(FilteredRoomsActivity.this, PropertyDetailActivity.class);
                    intent.putExtra("room", room);
                    startActivity(intent);
                }
        );

        rvRooms.setLayoutManager(new LinearLayoutManager(this));
        rvRooms.setAdapter(adapter);
    }

    private void setListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadRooms() {
        showLoading(true);

        firebaseManager.getFirestore()
                .collection("rooms")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    allRooms.clear();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {

                        Room room = doc.toObject(Room.class);

                        if (room != null) {
                            room.setId(doc.getId());

                            if (!room.isAvailable()) continue;

                            allRooms.add(room);
                        }
                    }

                    applyFilters();
                    showLoading(false);
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Log.e(TAG, "Firebase error: " + e.getMessage());
                    Toast.makeText(this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                });
    }

    private void applyFilters() {
        roomList.clear();

        for (Room room : allRooms) {

            boolean match = true;

            if (filterCheap && room.getPrice() >= 3000000) match = false;

            if (filterAC && !safeBool(room.isHasAC())) match = false;

            if (filterWifi && !safeBool(room.isHasWifi())) match = false;

            if (filterParking && !safeBool(room.isHasParking())) match = false;

            if (filterSpacious && room.getArea() < 20) match = false;

            if (filterNear) {

                String addr = safeString(room.getAddress());
                String city = safeString(room.getCity());
                String desc = safeString(room.getDescription());

                boolean near = false;

                if (city.contains("hcm") || city.contains("ho chi minh") ||
                        city.contains("tp.hcm") || city.contains("sài gòn"))
                    near = true;

                if (addr.contains("gần") || desc.contains("gần"))
                    near = true;

                if (!near) match = false;
            }

            if (match) roomList.add(room);
        }

        adapter.notifyDataSetChanged();
        updateUI();
    }

    private String safeString(String s) {
        return s == null ? "" : s.toLowerCase().trim();
    }

    private boolean safeBool(Boolean b) {
        return b != null && b;
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
