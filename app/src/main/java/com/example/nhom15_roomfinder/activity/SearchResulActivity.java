package com.example.nhom15_roomfinder.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.android.material.slider.RangeSlider;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom15_roomfinder.R;
import com.example.nhom15_roomfinder.entity.Room;
// Sửa lại import này để nhất quán
import com.example.nhom15_roomfinder.adapter.RoomAdapter;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.MapView;
import com.mapbox.maps.Style;
import android.widget.TextView;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

public class SearchResulActivity extends AppCompatActivity {

    private static final String TAG = "SearchResulActivity";

    // --- Views chính ---
    private EditText etSearch;
    private RecyclerView recyclerViewListings;
    private MapView mapView;

    // --- Drawer & filter ---
    private DrawerLayout drawerLayout;
    private ImageView btnLoc;

    // --- CÁC NÚT SẮP XẾP ---
    private Button btnSortNearest, btnSortPrice, btnSortNewest;
    private Button currentSelectedSortButton = null;

    // --- Adapter & dữ liệu ---
    private RoomAdapter roomAdapter;
    private List<Room> displayedRoomList;
    private List<Room> allRoomsList;
    private FirebaseFirestore firestore;
    private String receivedSearchQuery;

    // --- BIẾN TRẠNG THÁI SẮP XẾP ---
    private enum SortMode {
        RELEVANCE, // Mặc định: theo độ liên quan
        NEAREST,
        PRICE_ASC, // Giá tăng dần
        NEWEST
    }
    private SortMode currentSortMode = SortMode.RELEVANCE;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchresul);

        // 1. Ánh xạ tất cả các view
        initViews();
        NavigationView navigationView = findViewById(R.id.filter_navigation_view);
        View filterView = navigationView;

        setupPriceSlider(filterView);

        // 2. Khởi tạo RecyclerView
        setupRecyclerView();

        // 3. Khởi tạo Firestore
        firestore = FirebaseFirestore.getInstance();

        // 4. Thiết lập MapView
        setupMapView();

        // 5. Thiết lập các listener (bao gồm cả các nút sắp xếp)
        setupListeners();

        // 6. Xử lý nút back
        setupOnBackPressed();

        // 7. Lấy dữ liệu từ intent
        handleIncomingIntent();

        // 8. Lấy danh sách phòng và lọc
        fetchAllRoomsAndFilter();

    }

    private void initViews() {
        etSearch = findViewById(R.id.etSearch);
        recyclerViewListings = findViewById(R.id.recyclerViewListings);
        drawerLayout = findViewById(R.id.drawer_layout);
        btnLoc = findViewById(R.id.btn_loc);
        mapView = findViewById(R.id.mapView);

        // Ánh xạ các nút sắp xếp
        btnSortNearest = findViewById(R.id.btn_gan_nhat);
        btnSortPrice = findViewById(R.id.btn_gia);
        btnSortNewest = findViewById(R.id.btn_moi_nhat);
    }

    private void setupRecyclerView() {
        displayedRoomList = new ArrayList<>();
        allRoomsList = new ArrayList<>();
        recyclerViewListings.setLayoutManager(new LinearLayoutManager(this));

        // --- SỬA LỖI Ở ĐÂY ---
        // Vì OnRoomClickListener có 2 phương thức, ta phải triển khai đầy đủ, không dùng lambda được.
        RoomAdapter.OnRoomClickListener listener = new RoomAdapter.OnRoomClickListener() {
            @Override
            public void onRoomClick(Room room) {
                // Khi người dùng nhấn vào một phòng, chuyển sang trang chi tiết
                Intent intent = new Intent(SearchResulActivity.this, PropertyDetailActivity.class);
                intent.putExtra("roomId", room.getId()); // Gửi ID của phòng
                // Bạn cũng có thể gửi cả đối tượng Room nếu nó là Serializable hoặc Parcelable
                intent.putExtra("room", room);
                startActivity(intent);
            }

            @Override
            public void onFavoriteClick(Room room, int position) {
                // Xử lý khi người dùng nhấn vào nút yêu thích
                // Ví dụ: thay đổi trạng thái và cập nhật UI/Firebase
                // (Bạn có thể sao chép logic từ HomeActivity nếu muốn)
                Toast.makeText(SearchResulActivity.this, "Đã nhấn yêu thích: " + room.getTitle(), Toast.LENGTH_SHORT).show();
                // TODO: Bổ sung logic xử lý yêu thích ở đây
            }
        };

        // Khởi tạo adapter với listener đã được tạo
        roomAdapter = new RoomAdapter(this, displayedRoomList, listener);
        recyclerViewListings.setAdapter(roomAdapter);
    }


    private void setupMapView() {
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS, style -> {
            // Thiết lập vị trí mặc định (ví dụ: Hà Nội)
            double lat = 21.0278;
            double lng = 105.8342;
            mapView.getMapboxMap().setCamera(new CameraOptions.Builder()
                    .center(Point.fromLngLat(lng, lat))
                    .zoom(12.0)
                    .build());
        });
    }

    private void setupListeners() {
        drawerLayout.setScrimColor(0x99000000);

        btnLoc.setOnClickListener(v -> {
            if (!drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.openDrawer(GravityCompat.END);
            }
        });

        NavigationView navigationView = findViewById(R.id.filter_navigation_view);

        Button btnCancel  = navigationView.findViewById(R.id.button_cancel);
        Button btnApprove = navigationView.findViewById(R.id.button_approve);

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> closeDrawer());
        }

        if (btnApprove != null) {
            btnApprove.setOnClickListener(v -> {
                Toast.makeText(this, "Áp dụng bộ lọc!", Toast.LENGTH_SHORT).show();
                closeDrawer();
            });
        }

        // ----- SORT BUTTONS -----
        btnSortNearest.setOnClickListener(v -> {
            currentSortMode = SortMode.NEAREST;
            updateSortButtonsUI(btnSortNearest);
            filterAndSortResults();
        });

        btnSortPrice.setOnClickListener(v -> {
            currentSortMode = SortMode.PRICE_ASC;
            updateSortButtonsUI(btnSortPrice);
            filterAndSortResults();
        });

        btnSortNewest.setOnClickListener(v -> {
            currentSortMode = SortMode.NEWEST;
            updateSortButtonsUI(btnSortNewest);
            filterAndSortResults();
        });
    }




    // --- HÀM MỚI: CẬP NHẬT GIAO DIỆN NÚT SẮP XẾP ---
    private void updateSortButtonsUI(Button selectedButton) {
        // 1. Reset nút đang được chọn cũ (nếu có) về trạng thái mặc định
        if (currentSelectedSortButton != null) {
            currentSelectedSortButton.setSelected(false);
        }

        // 2. Cập nhật nút mới được chọn
        selectedButton.setSelected(true);
        currentSelectedSortButton = selectedButton; // Lưu lại nút vừa được chọn
    }


    private void setupOnBackPressed() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                    closeDrawer();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    private void closeDrawer() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END);
        }
    }

    private void handleIncomingIntent() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(HomeActivity.EXTRA_SEARCH_QUERY)) {
            receivedSearchQuery = intent.getStringExtra(HomeActivity.EXTRA_SEARCH_QUERY);
            if (etSearch != null && receivedSearchQuery != null) {
                etSearch.setText(receivedSearchQuery);
            }
        }
    }

    private void fetchAllRoomsAndFilter() {
        Log.d(TAG, "Bắt đầu lấy TOÀN BỘ dữ liệu từ Firestore...");
        firestore.collection("rooms").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                allRoomsList.clear();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    Room room = doc.toObject(Room.class);
                    // Gán ID cho phòng, rất quan trọng cho các thao tác sau này
                    room.setId(doc.getId());
                    allRoomsList.add(room);
                }
                Log.d(TAG, "Đã tải thành công " + allRoomsList.size() + " phòng vào bộ nhớ.");
                // Lọc và sắp xếp với tiêu chí mặc định
                filterAndSortResults();
            } else {
                Toast.makeText(SearchResulActivity.this, "Lỗi khi tải dữ liệu.", Toast.LENGTH_LONG).show();
                Log.e(TAG, "Lỗi kết nối Firestore: ", task.getException());
            }
        });
    }

    private void filterAndSortResults() {
        // --- BƯỚC 1: LỌC THEO TỪ KHÓA TÌM KIẾM ---
        List<Room> filteredList = new ArrayList<>();
        if (receivedSearchQuery == null || receivedSearchQuery.trim().isEmpty()) {
            // Nếu không có từ khóa, lấy tất cả phòng
            filteredList.addAll(allRoomsList);
        } else {
            // Nếu có từ khóa, lọc theo điểm liên quan
            String normalizedQuery = normalizeString(receivedSearchQuery);
            List<ScoredRoom> scoredRooms = new ArrayList<>();

            for (Room room : allRoomsList) {
                String normalizedTitle = normalizeString(room.getTitle());
                String normalizedAddress = normalizeString(room.getAddress());
                String normalizedDistrict = normalizeString(room.getDistrict());
                String normalizedCity = normalizeString(room.getCity());

                int score = 0;
                if (normalizedTitle.contains(normalizedQuery)) score += 20;
                if (normalizedAddress.contains(normalizedQuery)) score += 15;
                if (normalizedDistrict.contains(normalizedQuery)) score += 10;
                if (normalizedCity.contains(normalizedQuery)) score += 5;

                if (score > 0) {
                    scoredRooms.add(new ScoredRoom(room, score));
                }
            }
            // Sắp xếp theo điểm số để lấy danh sách đã lọc
            Collections.sort(scoredRooms, (o1, o2) -> Integer.compare(o2.score, o1.score));
            for (ScoredRoom scoredRoom : scoredRooms) {
                filteredList.add(scoredRoom.room);
            }
        }

        // --- BƯỚC 2: SẮP XẾP DANH SÁCH ĐÃ LỌC THEO TIÊU CHÍ HIỆN TẠI ---
        switch (currentSortMode) {
            case PRICE_ASC:
                // Sắp xếp theo giá tăng dần
                Collections.sort(filteredList, Comparator.comparingDouble(Room::getPrice));
                break;
            case NEWEST:
                // Sắp xếp theo ngày đăng mới nhất (cần trường 'createdAt' trong Room)
                // Collections.sort(filteredList, (o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()));
                Toast.makeText(this, "Chức năng sắp xếp 'Mới nhất' chưa được cài đặt", Toast.LENGTH_SHORT).show();
                break;
            case NEAREST:
                // Sắp xếp theo khoảng cách gần nhất (cần logic tính khoảng cách)
                Toast.makeText(this, "Chức năng sắp xếp 'Gần nhất' chưa được cài đặt", Toast.LENGTH_SHORT).show();
                break;
            case RELEVANCE:
                // Mặc định đã được sắp xếp theo độ liên quan ở bước lọc
                break;
        }

        // --- BƯỚC 3: CẬP NHẬT RECYCLERVIEW ---
        displayedRoomList.clear();
        displayedRoomList.addAll(filteredList);
        roomAdapter.notifyDataSetChanged();
    }


    private String normalizeString(String input) {
        if (input == null) return "";
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized).replaceAll("").toLowerCase();
    }

    private static class ScoredRoom {
        Room room;
        int score;
        ScoredRoom(Room room, int score) {
            this.room = room;
            this.score = score;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
    private void setupPriceSlider(View filterView) {
        RangeSlider priceRangeSlider = filterView.findViewById(R.id.priceRangeSlider);
        TextView priceRangeText = filterView.findViewById(R.id.priceRangeText);

        if (priceRangeSlider == null || priceRangeText == null) return;

        // Thiết lập min, max, step
        priceRangeSlider.setValueFrom(0f);      // Giá trị nhỏ nhất
        priceRangeSlider.setValueTo(100_000_000f);       // Giá trị lớn nhất
        priceRangeSlider.setStepSize(100_000f);       // Bước tăng

        priceRangeSlider.setValues(0f, 100_000_000f);
        updatePriceText(0f, 100_000_000f, priceRangeText);

        priceRangeSlider.addOnChangeListener((slider, value, fromUser) -> {
            float min = slider.getValues().get(0);
            float max = slider.getValues().get(1);
            updatePriceText(min, max, priceRangeText);
        });
    }
    //ok


    private void updatePriceText(float min, float max, TextView textView) {
        String minStr = String.format("%,.0f", min).replace(",", ".");
        String maxStr = String.format("%,.0f", max).replace(",", ".");
        textView.setText(minStr + " VNĐ        " + maxStr + " VNĐ");
    }
}
