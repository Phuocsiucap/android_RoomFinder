package com.example.nhom15_roomfinder;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.nhom15_roomfinder.firebase.FirebaseManager;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class StatisticActivity extends AppCompatActivity {

    private Spinner spinnerTime;
    private Button btnApply;
    private TextView txtTotalAds, txtActiveUsers;
    private TextView txtActiveAds;
    private TextView txtPriceLow, txtPriceMid, txtPriceHigh;
    private TextView txtTopRooms, txtTopCities;
    private TextView txtBlockedUsersSummary;
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
        txtActiveUsers = findViewById(R.id.txtActiveUsers);
        txtActiveAds = findViewById(R.id.txtActiveAds);
        txtPriceLow = findViewById(R.id.txtPriceLow);
        txtPriceMid = findViewById(R.id.txtPriceMid);
        txtPriceHigh = findViewById(R.id.txtPriceHigh);
        txtTopRooms = findViewById(R.id.txtTopRooms);
        txtTopCities = findViewById(R.id.txtTopCities);
        txtBlockedUsersSummary = findViewById(R.id.txtBlockedUsersSummary);
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

        // Load rooms statistics (total ads, views, price ranges, top views, top cities, active/blocked)
        firebaseManager.getCollection("rooms",
            querySnapshot -> {
                int totalAds = 0;
                int activeAds = 0;
                int blockedAds = 0;

                int priceLow = 0;   // < 2M
                int priceMid = 0;   // 2M - 4M
                int priceHigh = 0;  // > 4M

                java.util.List<java.util.Map.Entry<String, Integer>> roomViews = new java.util.ArrayList<>();
                java.util.Map<String, Integer> cityCounts = new java.util.HashMap<>();

                for (com.google.firebase.firestore.QueryDocumentSnapshot doc : querySnapshot) {
                    // Filter by time
                    Object createdAt = doc.get("createdAt");
                    long createdTime = 0;
                    if (createdAt instanceof Number) {
                        createdTime = ((Number) createdAt).longValue();
                    } else if (createdAt instanceof com.google.firebase.Timestamp) {
                        createdTime = ((com.google.firebase.Timestamp) createdAt).toDate().getTime();
                    }
                    if (startTime > 0 && createdTime > 0 && createdTime < startTime) {
                        continue;
                    }

                    totalAds++;

                    // Status
                    boolean isAvailable = Boolean.TRUE.equals(doc.getBoolean("isAvailable"));
                    String status = doc.getString("status");
                    boolean isBlocked = "blocked".equals(status) || !isAvailable;
                    if (isBlocked) {
                        blockedAds++;
                    } else {
                        activeAds++;
                    }

                    // Price
                    Object priceObj = doc.get("price");
                    double price = 0;
                    if (priceObj instanceof Number) {
                        price = ((Number) priceObj).doubleValue();
                    } else if (priceObj != null) {
                        try {
                            price = Double.parseDouble(priceObj.toString());
                        } catch (NumberFormatException ignored) {}
                    }
                    if (price > 0) {
                        if (price < 2_000_000) priceLow++;
                        else if (price <= 4_000_000) priceMid++;
                        else priceHigh++;
                    }

                    // Views
                    Object viewsObj = doc.get("viewCount");
                    if (!(viewsObj instanceof Number)) {
                        viewsObj = doc.get("views");
                    }
                    int views = 0;
                    if (viewsObj instanceof Number) {
                        views = ((Number) viewsObj).intValue();
                    }

                    String title = doc.getString("title");
                    if (title == null || title.isEmpty()) title = "(Không có tiêu đề)";
                    String address = doc.getString("address");
                    String city = doc.getString("city");
                    if (address == null) address = "";
                    if (city == null) city = "";
                    roomViews.add(new java.util.AbstractMap.SimpleEntry<>(
                        title + "||" + address + "||" + city, views));

                    // City
                    String roomCity = doc.getString("city");
                    if (roomCity == null || roomCity.isEmpty()) {
                        roomCity = "Khác";
                    }
                    int current = cityCounts.containsKey(roomCity) ? cityCounts.get(roomCity) : 0;
                    cityCounts.put(roomCity, current + 1);
                }

                txtTotalAds.setText(String.valueOf(totalAds));

                // Active/Blocked
                if (txtActiveAds != null) {
                    txtActiveAds.setText("Đang hoạt động: " + activeAds);
                }

                // Price ranges
                if (txtPriceLow != null) {
                    txtPriceLow.setText("< 2.000.000: " + priceLow + " tin");
                }
                if (txtPriceMid != null) {
                    txtPriceMid.setText("2.000.000 - 4.000.000: " + priceMid + " tin");
                }
                if (txtPriceHigh != null) {
                    txtPriceHigh.setText("> 4.000.000: " + priceHigh + " tin");
                }

                // Top rooms by views
                java.util.Collections.sort(roomViews, (e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()));
                StringBuilder topRoomsBuilder = new StringBuilder();
                int maxRooms = Math.min(5, roomViews.size());
                for (int i = 0; i < maxRooms; i++) {
                    java.util.Map.Entry<String, Integer> entry = roomViews.get(i);
                    if (i > 0) topRoomsBuilder.append("\n");
                    String[] parts = entry.getKey().split("\\|\\|");
                    String title = parts.length > 0 ? parts[0] : "(Không có tiêu đề)";
                    String address = parts.length > 1 ? parts[1] : "";
                    String city = parts.length > 2 ? parts[2] : "";
                    topRoomsBuilder.append(i + 1).append(". ")
                        .append(title);
                    if (!address.isEmpty() || !city.isEmpty()) {
                        topRoomsBuilder.append("\n   ");
                        if (!address.isEmpty()) topRoomsBuilder.append(address);
                        if (!city.isEmpty()) {
                            if (!address.isEmpty()) topRoomsBuilder.append(", ");
                            topRoomsBuilder.append(city);
                        }
                    }
                    topRoomsBuilder.append("\n   Lượt xem: ").append(entry.getValue());
                }
                if (txtTopRooms != null) {
                    if (topRoomsBuilder.length() == 0) {
                        txtTopRooms.setText("Chưa có dữ liệu");
                    } else {
                        String content = topRoomsBuilder.toString();
                        SpannableStringBuilder ssb = new SpannableStringBuilder(content);
                        // Mặc định màu xám cho toàn bộ nội dung
                        ssb.setSpan(
                            new ForegroundColorSpan(Color.parseColor("#424242")),
                            0,
                            content.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        );
                        // Chỉ tô màu phần tên phòng sau "1. "
                        Pattern patternRooms = Pattern.compile("(?m)^(\\d+)\\.\\s+(.*)$");
                        Matcher matcherRooms = patternRooms.matcher(content);
                        while (matcherRooms.find()) {
                            int start = matcherRooms.start(2);
                            int end = matcherRooms.end(2);
                            ssb.setSpan(
                                new ForegroundColorSpan(Color.parseColor("#1976D2")),
                                start,
                                end,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                            );
                        }
                        txtTopRooms.setText(ssb);
                    }
                }

                // Top cities by room count
                java.util.List<java.util.Map.Entry<String, Integer>> cityList =
                    new java.util.ArrayList<>(cityCounts.entrySet());
                java.util.Collections.sort(cityList, (c1, c2) -> Integer.compare(c2.getValue(), c1.getValue()));
                StringBuilder topCitiesBuilder = new StringBuilder();
                int maxCities = Math.min(5, cityList.size());
                for (int i = 0; i < maxCities; i++) {
                    java.util.Map.Entry<String, Integer> entry = cityList.get(i);
                    if (i > 0) topCitiesBuilder.append("\n");
                    topCitiesBuilder.append(i + 1).append(". ")
                        .append(entry.getKey()).append(" (").append(entry.getValue()).append(" tin)");
                }
                if (txtTopCities != null) {
                    if (topCitiesBuilder.length() == 0) {
                        txtTopCities.setText("Chưa có dữ liệu");
                    } else {
                        String contentCities = topCitiesBuilder.toString();
                        SpannableStringBuilder ssbCities = new SpannableStringBuilder(contentCities);
                        // Mặc định xám cho toàn bộ
                        ssbCities.setSpan(
                            new ForegroundColorSpan(Color.parseColor("#424242")),
                            0,
                            contentCities.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        );
                        // Tô màu xanh dương cho tên khu vực (giữa "1. " và " (")
                        Pattern patternCities = Pattern.compile("(?m)^(\\d+)\\.\\s+([^\\(]+)");
                        Matcher matcherCities = patternCities.matcher(contentCities);
                        while (matcherCities.find()) {
                            int start = matcherCities.start(2);
                            int end = matcherCities.end(2);
                            ssbCities.setSpan(
                                new ForegroundColorSpan(Color.parseColor("#1976D2")),
                                start,
                                end,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                            );
                        }
                        txtTopCities.setText(ssbCities);
                    }
                }
            },
            e -> {
                Toast.makeText(this, "Lỗi tải thống kê tin: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });

        // Load users: active count + blocked count + chi tiết blocked
        firebaseManager.getCollection("users",
            querySnapshot -> {
                int activeCount = 0;
                int blockedCount = 0;
                java.util.List<String> blockedDetails = new ArrayList<>();

                for (com.google.firebase.firestore.QueryDocumentSnapshot doc : querySnapshot) {
                    Object createdAt = doc.get("createdAt");
                    long createdTime = 0;
                    if (createdAt instanceof Number) {
                        createdTime = ((Number) createdAt).longValue();
                    } else if (createdAt instanceof com.google.firebase.Timestamp) {
                        createdTime = ((com.google.firebase.Timestamp) createdAt).toDate().getTime();
                    }
                    if (startTime > 0 && createdTime > 0 && createdTime < startTime) {
                        continue;
                    }

                    boolean isBlocked = Boolean.TRUE.equals(doc.getBoolean("isBlocked"));
                    String name = doc.getString("name");
                    String email = doc.getString("email");
                    if (name == null || name.isEmpty()) name = "(Không tên)";
                    if (email == null) email = "(Không email)";

                    if (isBlocked) {
                        blockedCount++;
                        blockedDetails.add(name + " - " + email);
                    } else {
                        activeCount++;
                    }
                }

                txtActiveUsers.setText(String.valueOf(activeCount));
                if (txtBlockedUsersSummary != null) {
                    txtBlockedUsersSummary.setText("Số người dùng bị khóa: " + blockedCount + " (Xem chi tiết)");
                    java.util.List<String> finalBlockedDetails = blockedDetails;
                    txtBlockedUsersSummary.setOnClickListener(v -> {
                        if (finalBlockedDetails.isEmpty()) {
                            Toast.makeText(this, "Không có người dùng bị khóa", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < finalBlockedDetails.size(); i++) {
                            if (i > 0) sb.append("\n");
                            sb.append(i + 1).append(". ").append(finalBlockedDetails.get(i));
                        }
                        new androidx.appcompat.app.AlertDialog.Builder(this)
                            .setTitle("Người dùng bị khóa")
                            .setMessage(sb.toString())
                            .setPositiveButton("Đóng", null)
                            .show();
                    });
                }
            },
            e -> {
                Toast.makeText(this, "Lỗi tải thống kê người dùng: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
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