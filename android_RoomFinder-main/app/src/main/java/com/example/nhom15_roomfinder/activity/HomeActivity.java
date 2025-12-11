package com.example.nhom15_roomfinder.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom15_roomfinder.R;
import com.example.nhom15_roomfinder.adapter.RoomAdapter;
import com.example.nhom15_roomfinder.entity.Room;
import com.example.nhom15_roomfinder.firebase.FirebaseManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    public static final String EXTRA_SEARCH_QUERY = "EXTRA_SEARCH_QUERY";
    private FirebaseManager firebaseManager;
    private String currentUserId;
    
    // UI Components
    private ImageView imgProfile;
    private ImageView imgNotification;
    private ImageView imgLocation;
    private ImageView imgSearchIcon;
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
    
    // Filter Tags
    private TextView filterCheap, filterAC, filterWifi, filterParking, filterNearSchool, filterSpacious;
    private boolean isCheapSelected, isACSelected, isWifiSelected, isParkingSelected, isNearSchoolSelected, isSpaciousSelected;

    // Adapters
    private RoomAdapter newRoomsAdapter;
    private RoomAdapter nearbyRoomsAdapter;
    private RoomAdapter suggestedRoomsAdapter;
    
    // Data
    private List<Room> newRoomsList = new ArrayList<>();
    private List<Room> nearbyRoomsList = new ArrayList<>();
    private List<Room> suggestedRoomsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        
        // Initialize Firebase
        firebaseManager = FirebaseManager.getInstance();
        currentUserId = firebaseManager.getUserId();
        
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
        imgSearchIcon = findViewById(R.id.imgSearchIcon);
        // RecyclerViews
        rvNewRooms = findViewById(R.id.rvNewRooms);
        rvNearbyRooms = findViewById(R.id.rvNearbyRooms);
        rvSuggestedRooms = findViewById(R.id.rvSuggestedRooms);
        
        // Buttons
        btnPostRoom = findViewById(R.id.btnPostRoom);
        
        // Filter Tags
        filterCheap = findViewById(R.id.filterCheap);
        filterAC = findViewById(R.id.filterAC);
        filterWifi = findViewById(R.id.filterWifi);
        filterParking = findViewById(R.id.filterParking);
        filterNearSchool = findViewById(R.id.filterNearSchool);
        filterSpacious = findViewById(R.id.filterSpacious);
        
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
            Intent intent = new Intent(HomeActivity.this, NotificationActivity.class);
            startActivity(intent);
        });
        
        // Location click
        imgLocation.setOnClickListener(v -> {
            showToast("Vị trí hiện tại");

            // Lấy nội dung search hiện tại (có thể để trống nếu không muốn)
            String searchQuery = etSearch.getText().toString().trim();

            // Chuyển sang SearchResulActivity
            Intent intent = new Intent(HomeActivity.this, SearchResulActivity.class);
            intent.putExtra(EXTRA_SEARCH_QUERY, searchQuery);
            startActivity(intent);
        });
        
        // Search functionality
        etSearch.setOnFocusChangeListener(null); // Xóa listener cũ để tránh xung đột

        // Thiết lập sự kiện click cho icon tìm kiếm
        if (imgSearchIcon != null) {
            imgSearchIcon.setOnClickListener(v -> performSearch());
        }
        
        // Category buttons
        btnCategoryAffordable.setOnClickListener(v -> {
            toggleFilter("cheap");
        });
        
        btnCategoryAC.setOnClickListener(v -> {
            toggleFilter("ac");
        });
        
        btnCategorySchool.setOnClickListener(v -> {
            toggleFilter("school");
        });
        
        btnCategorySpaciou.setOnClickListener(v -> {
            toggleFilter("spacious");
        });
        
        // Filter tags click listeners
        if (filterCheap != null) filterCheap.setOnClickListener(v -> toggleFilter("cheap"));
        if (filterAC != null) filterAC.setOnClickListener(v -> toggleFilter("ac"));
        if (filterWifi != null) filterWifi.setOnClickListener(v -> toggleFilter("wifi"));
        if (filterParking != null) filterParking.setOnClickListener(v -> toggleFilter("parking"));
        if (filterNearSchool != null) filterNearSchool.setOnClickListener(v -> toggleFilter("school"));
        if (filterSpacious != null) filterSpacious.setOnClickListener(v -> toggleFilter("spacious"));
        
        // Post room button
        btnPostRoom.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, PostRoomActivity.class);
            startActivity(intent);
        });
        
        // Bottom navigation
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            
            if (itemId == R.id.nav_home) {
                // Already on home
                return true;
            } else if (itemId == R.id.nav_search) {
                Intent searchIntent = new Intent(HomeActivity.this, SearchResulActivity.class);
                startActivity(searchIntent);
                return true;
            } else if (itemId == R.id.nav_favorites) {
                Intent favoriteIntent = new Intent(HomeActivity.this, FavoriteActivity.class);
                startActivity(favoriteIntent);
                return true;
            } else if (itemId == R.id.nav_messages) {
                Intent chatIntent = new Intent(HomeActivity.this, ChatListActivity.class);
                startActivity(chatIntent);
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
    private void performSearch() {
        String searchQuery = etSearch.getText().toString().trim();



        Intent intent = new Intent(HomeActivity.this, SearchResulActivity.class);
        // Gửi dữ liệu tìm kiếm sang activity mới
        intent.putExtra(EXTRA_SEARCH_QUERY, searchQuery);

        startActivity(intent);
    }
    /**
     * Setup RecyclerViews
     */
    private void setupRecyclerViews() {
        // Room click listener
        RoomAdapter.OnRoomClickListener roomClickListener = new RoomAdapter.OnRoomClickListener() {
            @Override
            public void onRoomClick(Room room) {
                Intent intent = new Intent(HomeActivity.this, PropertyDetailActivity.class);
                intent.putExtra("roomId", room.getId());
                intent.putExtra("room", room);
                startActivity(intent);
            }

            @Override
            public void onFavoriteClick(Room room, int position) {
                toggleFavorite(room);
            }
        };

        // Setup horizontal RecyclerView for new rooms
        rvNewRooms.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        newRoomsAdapter = new RoomAdapter(this, newRoomsList, roomClickListener);
        rvNewRooms.setAdapter(newRoomsAdapter);
        
        // Setup horizontal RecyclerView for nearby rooms
        rvNearbyRooms.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        nearbyRoomsAdapter = new RoomAdapter(this, nearbyRoomsList, roomClickListener);
        rvNearbyRooms.setAdapter(nearbyRoomsAdapter);
        
        // Setup horizontal RecyclerView for suggested rooms
        rvSuggestedRooms.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        suggestedRoomsAdapter = new RoomAdapter(this, suggestedRoomsList, roomClickListener);
        rvSuggestedRooms.setAdapter(suggestedRoomsAdapter);
    }
    
    /**
     * Load data for the home screen
     */
    private void loadData() {
        loadNewRooms();
        loadNearbyRooms();
        loadSuggestedRooms();
    }
    
    /**
     * Load new rooms data (phòng mới đăng)
     */
    private void loadNewRooms() {
        Log.d(TAG, "Loading new rooms...");
        
        firebaseManager.getFirestore()
            .collection("rooms")
            .whereEqualTo("isAvailable", true)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                newRoomsList.clear();
                Log.d(TAG, "New rooms found: " + querySnapshot.size());
                
                if (querySnapshot.isEmpty()) {
                    // Nếu không có phòng nào, tạo dữ liệu mẫu
                    createSampleRooms();
                    return;
                }
                
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    Room room = doc.toObject(Room.class);
                    if (room != null) {
                        room.setId(doc.getId());
                        newRoomsList.add(room);
                        Log.d(TAG, "Loaded room: " + room.getTitle());
                    }
                }
                newRoomsAdapter.updateData(newRoomsList);
                checkFavoriteStatus(newRoomsList, newRoomsAdapter);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error loading new rooms: " + e.getMessage());
                // Thử load không có điều kiện isAvailable
                loadAllRooms();
            });
    }
    
    /**
     * Load tất cả phòng (không filter)
     */
    private void loadAllRooms() {
        Log.d(TAG, "Loading all rooms without filter...");
        
        firebaseManager.getFirestore()
            .collection("rooms")
            .limit(10)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                Log.d(TAG, "All rooms found: " + querySnapshot.size());
                
                if (querySnapshot.isEmpty()) {
                    createSampleRooms();
                    return;
                }
                
                newRoomsList.clear();
                nearbyRoomsList.clear();
                suggestedRoomsList.clear();
                
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    Room room = doc.toObject(Room.class);
                    if (room != null) {
                        room.setId(doc.getId());
                        newRoomsList.add(room);
                        nearbyRoomsList.add(room);
                        suggestedRoomsList.add(room);
                    }
                }
                
                newRoomsAdapter.updateData(newRoomsList);
                nearbyRoomsAdapter.updateData(nearbyRoomsList);
                suggestedRoomsAdapter.updateData(suggestedRoomsList);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error loading all rooms: " + e.getMessage());
                createSampleRooms();
            });
    }
    
    /**
     * Tạo dữ liệu phòng mẫu
     */
    private void createSampleRooms() {
        Log.d(TAG, "Creating sample rooms...");
        
        List<Room> sampleRooms = new ArrayList<>();
        
        // Phòng mẫu 1
        Room room1 = new Room();
        room1.setTitle("Phòng trọ Gò Vấp");
        room1.setDescription("Phòng rộng rãi, thoáng mát, có ban công");
        room1.setPrice(3500000);
        room1.setArea(25);
        room1.setAddress("123 Nguyễn Văn Lượng");
        room1.setDistrict("Gò Vấp");
        room1.setCity("TP.HCM");
        room1.setAvailable(true);
        room1.setHasWifi(true);
        room1.setHasAC(true);
        room1.setHasParking(true);
        room1.setCreatedAt(System.currentTimeMillis());
        room1.setOwnerId(currentUserId);
        sampleRooms.add(room1);
        
        // Phòng mẫu 2
        Room room2 = new Room();
        room2.setTitle("Phòng trọ Quận 1");
        room2.setDescription("Phòng trung tâm, tiện đi lại");
        room2.setPrice(4500000);
        room2.setArea(20);
        room2.setAddress("456 Lê Lai");
        room2.setDistrict("Quận 1");
        room2.setCity("TP.HCM");
        room2.setAvailable(true);
        room2.setHasWifi(true);
        room2.setHasAC(true);
        room2.setCreatedAt(System.currentTimeMillis() - 86400000);
        room2.setOwnerId(currentUserId);
        sampleRooms.add(room2);
        
        // Phòng mẫu 3
        Room room3 = new Room();
        room3.setTitle("Phòng trọ Bình Thạnh");
        room3.setDescription("Gần chợ, thuận tiện mua sắm");
        room3.setPrice(2800000);
        room3.setArea(18);
        room3.setAddress("789 Điện Biên Phủ");
        room3.setDistrict("Bình Thạnh");
        room3.setCity("TP.HCM");
        room3.setAvailable(true);
        room3.setHasWifi(true);
        room3.setCreatedAt(System.currentTimeMillis() - 172800000);
        room3.setOwnerId(currentUserId);
        sampleRooms.add(room3);
        
        // Phòng mẫu 4
        Room room4 = new Room();
        room4.setTitle("Phòng trọ Tân Bình");
        room4.setDescription("Gần sân bay, yên tĩnh");
        room4.setPrice(3200000);
        room4.setArea(22);
        room4.setAddress("321 Cộng Hòa");
        room4.setDistrict("Tân Bình");
        room4.setCity("TP.HCM");
        room4.setAvailable(true);
        room4.setHasWifi(true);
        room4.setHasParking(true);
        room4.setCreatedAt(System.currentTimeMillis() - 259200000);
        room4.setOwnerId(currentUserId);
        sampleRooms.add(room4);
        
        // Phòng mẫu 5
        Room room5 = new Room();
        room5.setTitle("Phòng trọ Thủ Đức");
        room5.setDescription("Gần làng đại học, phù hợp sinh viên");
        room5.setPrice(2500000);
        room5.setArea(16);
        room5.setAddress("555 Võ Văn Ngân");
        room5.setDistrict("Thủ Đức");
        room5.setCity("TP.HCM");
        room5.setAvailable(true);
        room5.setHasWifi(true);
        room5.setCreatedAt(System.currentTimeMillis() - 345600000);
        room5.setOwnerId(currentUserId);
        sampleRooms.add(room5);
        
        // Lưu vào Firebase
        for (Room room : sampleRooms) {
            firebaseManager.getFirestore()
                .collection("rooms")
                .add(room)
                .addOnSuccessListener(docRef -> {
                    Log.d(TAG, "Sample room created: " + docRef.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating sample room: " + e.getMessage());
                });
        }
        
        // Hiển thị dữ liệu mẫu ngay lập tức
        newRoomsList.clear();
        newRoomsList.addAll(sampleRooms);
        newRoomsAdapter.updateData(newRoomsList);
        
        nearbyRoomsList.clear();
        nearbyRoomsList.addAll(sampleRooms);
        nearbyRoomsAdapter.updateData(nearbyRoomsList);
        
        suggestedRoomsList.clear();
        suggestedRoomsList.addAll(sampleRooms);
        suggestedRoomsAdapter.updateData(suggestedRoomsList);
        
        showToast("Đã tạo dữ liệu mẫu");
    }
    
    /**
     * Load nearby rooms data (giá rẻ)
     */
    private void loadNearbyRooms() {
        Log.d(TAG, "Loading nearby rooms...");
        
        firebaseManager.getFirestore()
            .collection("rooms")
            .whereEqualTo("isAvailable", true)
            .orderBy("price", Query.Direction.ASCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                nearbyRoomsList.clear();
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    Room room = doc.toObject(Room.class);
                    if (room != null) {
                        room.setId(doc.getId());
                        nearbyRoomsList.add(room);
                    }
                }
                nearbyRoomsAdapter.updateData(nearbyRoomsList);
                checkFavoriteStatus(nearbyRoomsList, nearbyRoomsAdapter);
            })
            .addOnFailureListener(e -> Log.e(TAG, "Error loading nearby rooms: " + e.getMessage()));
    }
    
    /**
     * Load suggested rooms data (được xem nhiều)
     */
    private void loadSuggestedRooms() {
        Log.d(TAG, "Loading suggested rooms...");
        
        firebaseManager.getFirestore()
            .collection("rooms")
            .whereEqualTo("isAvailable", true)
            .orderBy("viewCount", Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                suggestedRoomsList.clear();
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    Room room = doc.toObject(Room.class);
                    if (room != null) {
                        room.setId(doc.getId());
                        suggestedRoomsList.add(room);
                    }
                }
                suggestedRoomsAdapter.updateData(suggestedRoomsList);
                checkFavoriteStatus(suggestedRoomsList, suggestedRoomsAdapter);
            })
            .addOnFailureListener(e -> Log.e(TAG, "Error loading suggested rooms: " + e.getMessage()));
    }

    /**
     * Kiểm tra trạng thái yêu thích của các phòng
     */
    private void checkFavoriteStatus(List<Room> rooms, RoomAdapter adapter) {
        if (currentUserId == null || rooms.isEmpty()) return;
        
        List<String> roomIds = new ArrayList<>();
        for (Room room : rooms) {
            roomIds.add(room.getId());
        }
        
        firebaseManager.getFirestore()
            .collection("favorites")
            .whereEqualTo("userId", currentUserId)
            .whereIn("roomId", roomIds)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<String> favoriteIds = new ArrayList<>();
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    favoriteIds.add(doc.getString("roomId"));
                }
                
                for (Room room : rooms) {
                    room.setFavorite(favoriteIds.contains(room.getId()));
                }
                adapter.notifyDataSetChanged();
            });
    }

    /**
     * Toggle yêu thích phòng
     */
    private void toggleFavorite(Room room) {
        if (currentUserId == null) {
            showToast("Vui lòng đăng nhập");
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
                    refreshAdapters();
                    showToast("Đã xóa khỏi yêu thích");
                });
        } else {
            // Thêm vào yêu thích
            Map<String, Object> favorite = new HashMap<>();
            favorite.put("userId", currentUserId);
            favorite.put("roomId", room.getId());
            favorite.put("createdAt", System.currentTimeMillis());

            firebaseManager.getFirestore()
                .collection("favorites")
                .add(favorite)
                .addOnSuccessListener(docRef -> {
                    room.setFavorite(true);
                    refreshAdapters();
                    showToast("Đã thêm vào yêu thích");
                });
        }
    }

    private void refreshAdapters() {
        newRoomsAdapter.notifyDataSetChanged();
        nearbyRoomsAdapter.notifyDataSetChanged();
        suggestedRoomsAdapter.notifyDataSetChanged();
    }
    
    /**
     * Toggle filter selection
     */
    private void toggleFilter(String filterType) {
        switch (filterType) {
            case "cheap":
                isCheapSelected = !isCheapSelected;
                updateFilterUI(filterCheap, isCheapSelected);
                break;
            case "ac":
                isACSelected = !isACSelected;
                updateFilterUI(filterAC, isACSelected);
                break;
            case "wifi":
                isWifiSelected = !isWifiSelected;
                updateFilterUI(filterWifi, isWifiSelected);
                break;
            case "parking":
                isParkingSelected = !isParkingSelected;
                updateFilterUI(filterParking, isParkingSelected);
                break;
            case "school":
                isNearSchoolSelected = !isNearSchoolSelected;
                updateFilterUI(filterNearSchool, isNearSchoolSelected);
                break;
            case "spacious":
                isSpaciousSelected = !isSpaciousSelected;
                updateFilterUI(filterSpacious, isSpaciousSelected);
                break;
        }
        applyFilters();
    }
    
    private void updateFilterUI(TextView filterView, boolean isSelected) {
        if (filterView != null) {
            filterView.setBackgroundResource(isSelected ? R.drawable.bg_tag_selected : R.drawable.bg_tag_unselected);
            filterView.setTextColor(getResources().getColor(isSelected ? android.R.color.white : R.color.text_primary));
        }
    }
    
    /**
     * Apply filters to room lists
     */
    private void applyFilters() {
        // Mở màn hình FilteredRoomsActivity với các filter đã chọn
        Intent intent = new Intent(this, FilteredRoomsActivity.class);
        intent.putExtra("filterCheap", isCheapSelected);
        intent.putExtra("filterAC", isACSelected);
        intent.putExtra("filterWifi", isWifiSelected);
        intent.putExtra("filterParking", isParkingSelected);
        intent.putExtra("filterNearSchool", isNearSchoolSelected);
        intent.putExtra("filterSpacious", isSpaciousSelected);
        
        // Build filter title
        StringBuilder title = new StringBuilder("Lọc: ");
        if (isCheapSelected) title.append("Giá rẻ, ");
        if (isACSelected) title.append("Điều hòa, ");
        if (isWifiSelected) title.append("WiFi, ");
        if (isParkingSelected) title.append("Để xe, ");
        if (isNearSchoolSelected) title.append("Gần trường, ");
        if (isSpaciousSelected) title.append("Phòng rộng, ");
        
        String titleStr = title.toString();
        if (titleStr.endsWith(", ")) {
            titleStr = titleStr.substring(0, titleStr.length() - 2);
        }
        intent.putExtra("title", titleStr);
        
        startActivity(intent);
        
        // Reset filters after navigating
        resetFilters();
    }
    
    private void resetFilters() {
        isCheapSelected = false;
        isACSelected = false;
        isWifiSelected = false;
        isParkingSelected = false;
        isNearSchoolSelected = false;
        isSpaciousSelected = false;
        
        updateFilterUI(filterCheap, false);
        updateFilterUI(filterAC, false);
        updateFilterUI(filterWifi, false);
        updateFilterUI(filterParking, false);
        updateFilterUI(filterNearSchool, false);
        updateFilterUI(filterSpacious, false);
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