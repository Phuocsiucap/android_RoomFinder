package com.example.nhom15_roomfinder.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom15_roomfinder.R;
import com.example.nhom15_roomfinder.adapter.FavoriteAdapter;
import com.example.nhom15_roomfinder.entity.Room;
import com.example.nhom15_roomfinder.firebase.FirebaseManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * FavoriteActivity - Màn hình danh sách phòng yêu thích
 */
public class FavoriteActivity extends AppCompatActivity {

    private static final String TAG = "FavoriteActivity";
    
    private RecyclerView rvFavorites;
    private ProgressBar progressBar;
    private LinearLayout layoutEmpty;
    private BottomNavigationView bottomNavigation;

    private FavoriteAdapter favoriteAdapter;
    private List<Room> favoriteRooms;
    private FirebaseManager firebaseManager;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        firebaseManager = FirebaseManager.getInstance();
        currentUserId = firebaseManager.getUserId();

        if (currentUserId == null) {
            redirectToLogin();
            return;
        }

        initViews();
        setupRecyclerView();
        setupBottomNavigation();
        loadFavorites();
    }

    private void initViews() {
        rvFavorites = findViewById(R.id.rvFavorites);
        progressBar = findViewById(R.id.progressBar);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void setupRecyclerView() {
        favoriteRooms = new ArrayList<>();
        favoriteAdapter = new FavoriteAdapter(favoriteRooms, new FavoriteAdapter.OnFavoriteClickListener() {
            @Override
            public void onRoomClick(Room room) {
                // Mở PropertyDetailActivity
                Intent intent = new Intent(FavoriteActivity.this, PropertyDetailActivity.class);
                intent.putExtra("roomId", room.getId());
                intent.putExtra("room", room);
                startActivity(intent);
            }

            @Override
            public void onRemoveFavorite(Room room, int position) {
                removeFavorite(room, position);
            }
        });

        rvFavorites.setLayoutManager(new LinearLayoutManager(this));
        rvFavorites.setAdapter(favoriteAdapter);
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_favorites);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, HomeActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_search) {
                startActivity(new Intent(this, SearchResulActivity.class));
                return true;
            } else if (itemId == R.id.nav_favorites) {
                return true; // Đang ở trang này
            } else if (itemId == R.id.nav_messages) {
                startActivity(new Intent(this, ChatListActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    /**
     * Load danh sách phòng yêu thích từ Firestore
     */
    private void loadFavorites() {
        showLoading(true);
        
        firebaseManager.getFirestore()
            .collection("favorites")
            .whereEqualTo("userId", currentUserId)
            .get()
            .addOnSuccessListener(favoriteSnapshots -> {
                // Dùng Set để loại bỏ roomId trùng lặp
                java.util.Set<String> roomIdSet = new java.util.LinkedHashSet<>();
                for (DocumentSnapshot doc : favoriteSnapshots.getDocuments()) {
                    String roomId = doc.getString("roomId");
                    if (roomId != null && !roomId.isEmpty()) {
                        roomIdSet.add(roomId);
                    }
                }
                
                List<String> roomIds = new ArrayList<>(roomIdSet);
                Log.d(TAG, "Found " + roomIds.size() + " unique favorite roomIds");
                
                if (roomIds.isEmpty()) {
                    showLoading(false);
                    favoriteRooms.clear();
                    favoriteAdapter.updateData(favoriteRooms);
                    updateEmptyState();
                    return;
                }
                
                // Lấy thông tin các phòng
                loadRoomDetails(roomIds);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error loading favorites: " + e.getMessage());
                showLoading(false);
                Toast.makeText(this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            });
    }

    /**
     * Load chi tiết các phòng từ roomIds
     */
    private void loadRoomDetails(List<String> roomIds) {
        favoriteRooms.clear();
        
        if (roomIds.isEmpty()) {
            showLoading(false);
            updateEmptyState();
            return;
        }
        
        Log.d(TAG, "Loading room details for " + roomIds.size() + " rooms: " + roomIds);
        
        // Chia nhỏ danh sách nếu quá 10 phần tử (giới hạn của whereIn)
        int batchSize = 10;
        int totalBatches = (int) Math.ceil((double) roomIds.size() / batchSize);
        final int[] completedBatches = {0};
        
        for (int i = 0; i < roomIds.size(); i += batchSize) {
            List<String> batch = roomIds.subList(i, Math.min(i + batchSize, roomIds.size()));
            
            // Tìm theo document ID
            firebaseManager.getFirestore()
                .collection("rooms")
                .whereIn(com.google.firebase.firestore.FieldPath.documentId(), batch)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Log.d(TAG, "Found " + querySnapshot.size() + " rooms by document ID");
                    
                    // Thêm các phòng tìm được
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Room room = doc.toObject(Room.class);
                        if (room != null) {
                            room.setId(doc.getId());
                            room.setFavorite(true);
                            favoriteRooms.add(room);
                        }
                    }
                    
                    completedBatches[0]++;
                    if (completedBatches[0] >= totalBatches) {
                        // Loại bỏ trùng lặp trước khi hiển thị
                        removeDuplicates();
                        Log.d(TAG, "Total favorite rooms loaded: " + favoriteRooms.size());
                        favoriteAdapter.updateData(favoriteRooms);
                        showLoading(false);
                        updateEmptyState();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading room by document ID: " + e.getMessage());
                    completedBatches[0]++;
                    if (completedBatches[0] >= totalBatches) {
                        removeDuplicates();
                        favoriteAdapter.updateData(favoriteRooms);
                        showLoading(false);
                        updateEmptyState();
                    }
                });
        }
    }
    
    /**
     * Loại bỏ các phòng trùng lặp trong danh sách
     */
    private void removeDuplicates() {
        List<Room> uniqueRooms = new ArrayList<>();
        List<String> seenIds = new ArrayList<>();
        
        for (Room room : favoriteRooms) {
            if (room.getId() != null && !seenIds.contains(room.getId())) {
                seenIds.add(room.getId());
                uniqueRooms.add(room);
            }
        }
        
        favoriteRooms.clear();
        favoriteRooms.addAll(uniqueRooms);
    }

    /**
     * Xóa phòng khỏi yêu thích
     */
    private void removeFavorite(Room room, int position) {
        firebaseManager.getFirestore()
            .collection("favorites")
            .whereEqualTo("userId", currentUserId)
            .whereEqualTo("roomId", room.getId())
            .get()
            .addOnSuccessListener(querySnapshot -> {
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    doc.getReference().delete();
                }
                
                favoriteAdapter.removeItem(position);
                updateEmptyState();
                Toast.makeText(this, "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error removing favorite: " + e.getMessage());
                Toast.makeText(this, "Lỗi xóa yêu thích", Toast.LENGTH_SHORT).show();
            });
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void updateEmptyState() {
        if (layoutEmpty != null) {
            layoutEmpty.setVisibility(favoriteRooms.isEmpty() ? View.VISIBLE : View.GONE);
        }
        rvFavorites.setVisibility(favoriteRooms.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigation.setSelectedItemId(R.id.nav_favorites);
        loadFavorites(); // Reload khi quay lại
    }
}
