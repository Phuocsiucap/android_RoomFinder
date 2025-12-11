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
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;


import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.Set;
import java.util.HashSet;
import com.example.nhom15_roomfinder.R;
import com.example.nhom15_roomfinder.entity.Room;

import com.example.nhom15_roomfinder.adapter.RoomSearchAdapter;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.MapView;
import com.mapbox.maps.Style;
import com.mapbox.maps.ImageHolder;

import androidx.annotation.NonNull;

import com.mapbox.maps.plugin.Plugin;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin;
import com.mapbox.maps.plugin.LocationPuck2D;
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener;

import android.widget.TextView;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

public class SearchResulActivity extends AppCompatActivity {
    private OnIndicatorPositionChangedListener locationListener;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final String TAG = "SearchResulActivity";

    private EditText etSearch;
    private RecyclerView recyclerViewListings;
    private MapView mapView;

    private DrawerLayout drawerLayout;
    private ImageView btnLoc;

    private Button btnSortNearest, btnSortPrice, btnSortNewest;
    private Button currentSelectedSortButton = null;

    private RoomSearchAdapter roomAdapter;
    private List<Room> displayedRoomList;
    private List<Room> allRoomsList;
    private FirebaseFirestore firestore;
    private String receivedSearchQuery;
    private BottomNavigationView bottomNavigation;

    private enum SortMode {
        RELEVANCE,
        NEAREST,
        PRICE_ASC,
        NEWEST
    }
    private SortMode currentSortMode = SortMode.RELEVANCE;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchresul);
// ================= BOTTOM NAVIGATION =================
        bottomNavigation = findViewById(R.id.bottomNavigation);

// Set tab hiện tại là Search
        bottomNavigation.setSelectedItemId(R.id.nav_search);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, HomeActivity.class));
                finish();
                return true;
            }
            else if (itemId == R.id.nav_search) {
                return true; // đang ở Search
            }
            else if (itemId == R.id.nav_favorites) {
                startActivity(new Intent(this, FavoriteActivity.class));
                finish();
                return true;
            }
            else if (itemId == R.id.nav_messages) {
                startActivity(new Intent(this, ChatListActivity.class));
                finish();
                return true;
            }
            else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                finish();
                return true;
            }
            return false;
        });
// =====================================================

        initViews();
        NavigationView navigationView = findViewById(R.id.filter_navigation_view);
        View filterView = navigationView;

        setupPriceSlider(filterView);

        setupRecyclerView();

        firestore = FirebaseFirestore.getInstance();

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE
            );

        } else {

            setupMapView();
        }

        setupListeners();

        setupOnBackPressed();

        handleIncomingIntent();

        fetchAllRoomsAndFilter();

    }

    private void initViews() {
        etSearch = findViewById(R.id.etSearch);
        recyclerViewListings = findViewById(R.id.recyclerViewListings);
        drawerLayout = findViewById(R.id.drawer_layout);
        btnLoc = findViewById(R.id.btn_loc);
        mapView = findViewById(R.id.mapView);

        btnSortNearest = findViewById(R.id.btn_gan_nhat);
        btnSortPrice = findViewById(R.id.btn_gia);
        btnSortNewest = findViewById(R.id.btn_moi_nhat);
        ImageView imgSearchIcon = findViewById(R.id.imgSearchIcon);
        imgSearchIcon.setOnClickListener(v -> {
            String query = etSearch.getText().toString().trim();
            if (!query.isEmpty()) {
                Intent intent = new Intent(SearchResulActivity.this, SearchResulActivity.class);
                intent.putExtra(HomeActivity.EXTRA_SEARCH_QUERY, query);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(SearchResulActivity.this, "Vui lòng nhập từ khóa tìm kiếm", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRecyclerView() {
        displayedRoomList = new ArrayList<>();
        allRoomsList = new ArrayList<>();
        recyclerViewListings.setLayoutManager(new LinearLayoutManager(this));


        RoomSearchAdapter.OnRoomClickListener listener = new RoomSearchAdapter.OnRoomClickListener() {
            @Override
            public void onRoomClick(Room room) {

                Intent intent = new Intent(SearchResulActivity.this, PropertyDetailActivity.class);
                intent.putExtra("roomId", room.getId()); // Gửi ID của phòng
                intent.putExtra("room", room);
                startActivity(intent);
            }


        };

        roomAdapter = new RoomSearchAdapter(this, displayedRoomList, listener);
        recyclerViewListings.setAdapter(roomAdapter);
    }

    private void setupMapView() {
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS, style -> {

            LocationComponentPlugin locationPlugin =
                    mapView.getPlugin(Plugin.MAPBOX_LOCATION_COMPONENT_PLUGIN_ID);

            if (locationPlugin == null) {
                Log.e(TAG, "Location plugin is NULL");
                return;
            }


            locationPlugin.updateSettings(settings -> {
                settings.setEnabled(true);
                settings.setPulsingEnabled(true);
                LocationPuck2D puck = new LocationPuck2D(
                        ImageHolder.from(R.drawable.mapbox_user_icon),
                        ImageHolder.from(R.drawable.mapbox_user_stroke_icon),
                        null
                );

                return null;
            });

            // ✅ TẠO LISTENER
            locationListener = point -> {

                double lat = point.latitude();
                double lng = point.longitude();

                mapView.getMapboxMap().setCamera(
                        new CameraOptions.Builder()
                                .center(Point.fromLngLat(lng, lat))
                                .zoom(14.0)
                                .build()
                );

                locationPlugin.removeOnIndicatorPositionChangedListener(locationListener);
            };
            locationPlugin.addOnIndicatorPositionChangedListener(locationListener);
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
        Button btnHanoi = navigationView.findViewById(R.id.Hanoi);
        Button btnHcm = navigationView.findViewById(R.id.Hcm);
        Button btnHaiphong = navigationView.findViewById(R.id.Haiphong);
        Button btnDannang = navigationView.findViewById(R.id.Dannang);
        Button btnHue = navigationView.findViewById(R.id.Hue);

        Button btnS20 = navigationView.findViewById(R.id.s20);
        Button btnS2050 = navigationView.findViewById(R.id.s2050);
        Button btnS50100 = navigationView.findViewById(R.id.s50100);
        Button btnS100200 = navigationView.findViewById(R.id.s100200);
        Button btnS200 = navigationView.findViewById(R.id.s200);

        Button btnWifi = navigationView.findViewById(R.id.wifi);
        Button btnDieuhoa = navigationView.findViewById(R.id.dieuhoa);
        Button btnDexe = navigationView.findViewById(R.id.dexe);
        Button btnPhongtam = navigationView.findViewById(R.id.phongtam);
        Button btnBep = navigationView.findViewById(R.id.bep);
        Button btnBaove = navigationView.findViewById(R.id.baove);


        View.OnClickListener toggleSelectedListener = v -> v.setSelected(!v.isSelected());

        btnHanoi.setOnClickListener(toggleSelectedListener);
        btnHcm.setOnClickListener(toggleSelectedListener);
        btnHaiphong.setOnClickListener(toggleSelectedListener);
        btnDannang.setOnClickListener(toggleSelectedListener);
        btnHue.setOnClickListener(toggleSelectedListener);

        btnS20.setOnClickListener(toggleSelectedListener);
        btnS2050.setOnClickListener(toggleSelectedListener);
        btnS50100.setOnClickListener(toggleSelectedListener);
        btnS100200.setOnClickListener(toggleSelectedListener);
        btnS200.setOnClickListener(toggleSelectedListener);

        btnWifi.setOnClickListener(toggleSelectedListener);
        btnDieuhoa.setOnClickListener(toggleSelectedListener);
        btnDexe.setOnClickListener(toggleSelectedListener);
        btnPhongtam.setOnClickListener(toggleSelectedListener);
        btnBep.setOnClickListener(toggleSelectedListener);
        btnBaove.setOnClickListener(toggleSelectedListener);

        Button btnCancel  = navigationView.findViewById(R.id.button_cancel);
        Button btnApprove = navigationView.findViewById(R.id.button_approve);

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> closeDrawer());
        }

        if (btnApprove != null) {
            btnApprove.setOnClickListener(v -> {

                RangeSlider priceSlider = navigationView.findViewById(R.id.priceRangeSlider);
                float minPrice = 0f, maxPrice = Float.MAX_VALUE;
                if (priceSlider != null) {
                    minPrice = priceSlider.getValues().get(0);
                    maxPrice = priceSlider.getValues().get(1);
                }

                Set<String> selectedCities = new HashSet<>();
                if (navigationView.findViewById(R.id.Hanoi).isSelected()) selectedCities.add("hà nội");
                if (navigationView.findViewById(R.id.Hcm).isSelected()) selectedCities.add("TP.HCM");
                if (navigationView.findViewById(R.id.Haiphong).isSelected()) selectedCities.add("Hải Phòng");
                if (navigationView.findViewById(R.id.Dannang).isSelected()) selectedCities.add("Đà Nẵng");
                if (navigationView.findViewById(R.id.Hue).isSelected()) selectedCities.add("Huế");
                if (navigationView.findViewById(R.id.Khac).isSelected()) selectedCities.add("Khác");

                double minArea = 0, maxArea = Double.MAX_VALUE;
                if (navigationView.findViewById(R.id.s20).isSelected()) { minArea = 0; maxArea = 20; }
                if (navigationView.findViewById(R.id.s2050).isSelected()) { minArea = 20; maxArea = 50; }
                if (navigationView.findViewById(R.id.s50100).isSelected()) { minArea = 50; maxArea = 100; }
                if (navigationView.findViewById(R.id.s100200).isSelected()) { minArea = 100; maxArea = 200; }
                if (navigationView.findViewById(R.id.s200).isSelected()) { minArea = 200; maxArea = Double.MAX_VALUE; }

                Set<String> selectedAmenities = new HashSet<>();
                if (navigationView.findViewById(R.id.wifi).isSelected()) selectedAmenities.add("wifi");
                if (navigationView.findViewById(R.id.dieuhoa).isSelected()) selectedAmenities.add("ac");
                if (navigationView.findViewById(R.id.dexe).isSelected()) selectedAmenities.add("parking");
                if (navigationView.findViewById(R.id.phongtam).isSelected()) selectedAmenities.add("bathroom");
                if (navigationView.findViewById(R.id.bep).isSelected()) selectedAmenities.add("kitchen");
                if (navigationView.findViewById(R.id.baove).isSelected()) selectedAmenities.add("security");

                displayedRoomList.clear();
                displayedRoomList.addAll(
                        filterRooms(allRoomsList,
                                minPrice, maxPrice,
                                selectedCities,
                                minArea, maxArea,
                                selectedAmenities)
                );

                roomAdapter.notifyDataSetChanged();

                // 7. Đóng drawer
                closeDrawer();

                Toast.makeText(this, "Bộ lọc đã được áp dụng!", Toast.LENGTH_SHORT).show();
            });
        }



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
    private void updateSortButtonsUI(Button selectedButton) {

        if (currentSelectedSortButton != null) {
            currentSelectedSortButton.setSelected(false);
        }

        // 2. Cập nhật nút mới được chọn
        selectedButton.setSelected(true);
        currentSelectedSortButton = selectedButton;
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

                    room.setId(doc.getId());
                    allRoomsList.add(room);
                }
                Log.d(TAG, "Đã tải thành công " + allRoomsList.size() + " phòng vào bộ nhớ.");
                filterAndSortResults();
            } else {
                Toast.makeText(SearchResulActivity.this, "Lỗi khi tải dữ liệu.", Toast.LENGTH_LONG).show();
                Log.e(TAG, "Lỗi kết nối Firestore: ", task.getException());
            }
        });
    }

    private void filterAndSortResults() {

        List<Room> filteredList = new ArrayList<>();
        if (receivedSearchQuery == null || receivedSearchQuery.trim().isEmpty()) {

            filteredList.addAll(allRoomsList);
        } else {

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

            Collections.sort(scoredRooms, (o1, o2) -> Integer.compare(o2.score, o1.score));
            for (ScoredRoom scoredRoom : scoredRooms) {
                filteredList.add(scoredRoom.room);
            }
        }


        switch (currentSortMode) {
            case PRICE_ASC:
                // Sắp xếp theo giá tăng dần
                Collections.sort(filteredList, Comparator.comparingDouble(Room::getPrice));
                break;
            case NEWEST:
                // Sắp xếp theo ngày đăng mới nhất (cần trường 'createdAt' trong Room)
                // Collections.sort(filteredList, (o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()));

                break;
            case NEAREST:

                break;
            case RELEVANCE:

                break;
        }


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


        priceRangeSlider.setValueFrom(0f);
        priceRangeSlider.setValueTo(40_000_000f);
        priceRangeSlider.setStepSize(0f);

        priceRangeSlider.setValues(0f, 40_000_000f);
        updatePriceText(0f, 40_000_000f, priceRangeText);

        priceRangeSlider.addOnChangeListener((slider, value, fromUser) -> {
            float min = slider.getValues().get(0);
            float max = slider.getValues().get(1);
            updatePriceText(min, max, priceRangeText);
        });
    }



    private void updatePriceText(float min, float max, TextView textView) {
        String minStr = String.format("%,.0f", min).replace(",", ".");
        String maxStr = String.format("%,.0f", max).replace(",", ".");
        textView.setText(minStr + " VNĐ        " + maxStr + " VNĐ");
    }
    public static List<Room> filterRooms(List<Room> rooms,
                                         double minPrice, double maxPrice,
                                         Set<String> selectedCities,
                                         double minArea, double maxArea,
                                         Set<String> selectedAmenities) {
        List<Room> result = new ArrayList<>();

        for (Room room : rooms) {
            // Lọc theo giá
            if (room.getPrice() < minPrice || room.getPrice() > maxPrice) {
                continue;
            }

            // Lọc theo thành phố (nếu có chọn)
            if (selectedCities != null && !selectedCities.isEmpty() &&
                    !selectedCities.contains(room.getCity())) {
                continue;
            }

            // Lọc theo diện tích
            if (room.getArea() < minArea || room.getArea() > maxArea) {
                continue;
            }

            // Lọc theo tiện ích (multi-selection)
            boolean amenityMatch = true;
            if (selectedAmenities != null && !selectedAmenities.isEmpty()) {
                for (String amenity : selectedAmenities) {
                    switch (amenity.toLowerCase()) {
                        case "wifi":
                            if (!room.isHasWifi()) amenityMatch = false;
                            break;
                        case "ac":
                            if (!room.isHasAC()) amenityMatch = false;
                            break;
                        case "parking":
                            if (!room.isHasParking()) amenityMatch = false;
                            break;
                        case "bathroom":
                            if (!room.isHasPrivateBathroom()) amenityMatch = false;
                            break;
                        case "kitchen":
                            if (!room.isHasKitchen()) amenityMatch = false;
                            break;
                        case "security":
                            if (!room.isHasSecurity()) amenityMatch = false;
                            break;
                    }
                    if (!amenityMatch) break; // đã không hợp lệ, bỏ qua phòng này
                }
            }

            if (!amenityMatch) continue;

            // Nếu tất cả điều kiện thỏa, thêm phòng vào kết quả
            result.add(room);
        }

        return result;
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // ✅ Người dùng đã cho phép GPS → bật Mapbox
                setupMapView();

            } else {
                Toast.makeText(this,
                        "Bạn cần bật GPS để sử dụng bản đồ",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    //OK
}
