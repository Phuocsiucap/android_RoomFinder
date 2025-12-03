package com.example.nhom15_roomfinder.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.nhom15_roomfinder.R;
import com.example.nhom15_roomfinder.adapter.PropertyImageAdapter;
import com.example.nhom15_roomfinder.entity.Room;
import com.example.nhom15_roomfinder.firebase.FirebaseManager;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PropertyDetailActivity - Màn hình chi tiết phòng trọ
 * Hiển thị thông tin đầy đủ về phòng và cho phép liên hệ chủ trọ
 */
public class PropertyDetailActivity extends AppCompatActivity {

    private static final String TAG = "PropertyDetailActivity";

    // UI Components
    private ViewPager2 imageViewPager;
    private TextView tvPropertyTitle, tvPrice, tvAddress, tvDescription;
    private TextView tvOwnerName, tvOwnerPhone, tvArea;
    private LinearLayout layoutWifi, layoutAC, layoutParking;
    private ImageView imgFavorite;
    private ImageButton btnBack;
    private Button btnCall, btnMessage, btnBooking;
    private ProgressBar progressBar;

    private PropertyImageAdapter imageAdapter;
    private FirebaseManager firebaseManager;
    private String currentUserId;
    
    private Room currentRoom;
    private String roomId;
    private boolean isFavorite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_property_detail);

        firebaseManager = FirebaseManager.getInstance();
        currentUserId = firebaseManager.getUserId();

        initViews();
        getIntentData();
        setupButtons();
    }

    private void initViews() {
        imageViewPager = findViewById(R.id.imageViewPager);
        tvPropertyTitle = findViewById(R.id.tvPropertyTitle);
        tvPrice = findViewById(R.id.tvPrice);
        tvAddress = findViewById(R.id.tvAddress);
        tvDescription = findViewById(R.id.tvDescription);
        tvOwnerName = findViewById(R.id.tvOwnerName);
        tvOwnerPhone = findViewById(R.id.tvOwnerPhone);
        btnCall = findViewById(R.id.btnCall);
        btnMessage = findViewById(R.id.btnMessage);
        btnBooking = findViewById(R.id.btnBooking);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);
        imgFavorite = findViewById(R.id.imgFavorite);
        
        // Ánh xạ các view tiện ích
        layoutWifi = findViewById(R.id.layoutWifi);
        layoutAC = findViewById(R.id.layoutAC);
        layoutParking = findViewById(R.id.layoutParking);
    }

    private void getIntentData() {
        Intent intent = getIntent();
        
        // Lấy Room object từ intent (nếu có)
        if (intent.hasExtra("room")) {
            currentRoom = (Room) intent.getSerializableExtra("room");
            displayRoomData(currentRoom);
            checkFavoriteStatus();
            incrementViewCount(); // Cập nhật view count khi truyền object
        }
        
        // Hoặc lấy roomId để load từ Firebase
        if (intent.hasExtra("roomId")) {
            roomId = intent.getStringExtra("roomId");
            if (currentRoom == null) {
                loadRoomFromFirebase(roomId);
            }
        }
    }

    /**
     * Load thông tin phòng từ Firebase
     */
    private void loadRoomFromFirebase(String roomId) {
        showLoading(true);
        
        firebaseManager.getFirestore()
            .collection("rooms")
            .document(roomId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                showLoading(false);
                if (documentSnapshot.exists()) {
                    currentRoom = documentSnapshot.toObject(Room.class);
                    if (currentRoom != null) {
                        currentRoom.setId(documentSnapshot.getId());
                        displayRoomData(currentRoom);
                        checkFavoriteStatus();
                        incrementViewCount();
                    }
                } else {
                    Toast.makeText(this, "Không tìm thấy thông tin phòng", Toast.LENGTH_SHORT).show();
                    finish(); // Đóng màn hình nếu không tìm thấy phòng
                }
            })
            .addOnFailureListener(e -> {
                showLoading(false);
                Log.e(TAG, "Error loading room: " + e.getMessage());
                Toast.makeText(this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                finish(); // Đóng màn hình nếu lỗi
            });
    }

    /**
     * Hiển thị dữ liệu phòng lên UI
     */
    private void displayRoomData(Room room) {
        tvPropertyTitle.setText(room.getTitle());
        tvPrice.setText(room.getPriceDisplay());
        tvAddress.setText(room.getFullAddress());
        tvDescription.setText(room.getDescription());
        tvOwnerName.setText(room.getOwnerName() != null ? room.getOwnerName() : "Chủ trọ");
        tvOwnerPhone.setText(room.getOwnerPhone() != null ? room.getOwnerPhone() : "Chưa cập nhật");

        // Setup image gallery
        setupImageViewPager(room.getImageUrls());
        
        // Hiển thị tiện ích
        if (layoutWifi != null) {
            layoutWifi.setVisibility(room.isHasWifi() ? View.VISIBLE : View.GONE);
        }
        if (layoutAC != null) {
            layoutAC.setVisibility(room.isHasAC() ? View.VISIBLE : View.GONE);
        }
        if (layoutParking != null) {
            layoutParking.setVisibility(room.isHasParking() ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * Setup ViewPager cho gallery ảnh
     */
    private void setupImageViewPager(List<String> imageUrls) {
        final List<String> displayUrls;
        if (imageUrls == null || imageUrls.isEmpty()) {
            displayUrls = new ArrayList<>();
            displayUrls.add(""); // Placeholder
        } else {
            displayUrls = imageUrls;
        }
        
        imageAdapter = new PropertyImageAdapter(this, displayUrls);
        
        // Xử lý sự kiện click vào ảnh -> Mở ImageGalleryActivity
        imageAdapter.setOnImageClickListener(position -> {
            // Nếu là ảnh placeholder thì không mở
            if (imageUrls == null || imageUrls.isEmpty()) return;

            Intent intent = new Intent(PropertyDetailActivity.this, ImageGalleryActivity.class);
            intent.putStringArrayListExtra("imageUrls", new ArrayList<>(displayUrls));
            intent.putExtra("initialPosition", position);
            startActivity(intent);
        });

        imageViewPager.setAdapter(imageAdapter);
    }

    /**
     * Kiểm tra phòng có trong danh sách yêu thích không
     */
    private void checkFavoriteStatus() {
        if (currentUserId == null || currentRoom == null) return;

        firebaseManager.getFirestore()
            .collection("favorites")
            .whereEqualTo("userId", currentUserId)
            .whereEqualTo("roomId", currentRoom.getId())
            .get()
            .addOnSuccessListener(querySnapshot -> {
                isFavorite = !querySnapshot.isEmpty();
                updateFavoriteIcon();
            });
    }

    private void updateFavoriteIcon() {
        if (imgFavorite != null) {
            imgFavorite.setImageResource(isFavorite ? 
                R.drawable.ic_favorite_filled : R.drawable.ic_favorite);
        }
    }

    /**
     * Tăng lượt xem phòng
     */
    private void incrementViewCount() {
        if (currentRoom == null) return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("viewCount", currentRoom.getViewCount() + 1);
        
        firebaseManager.updateDocument("rooms", currentRoom.getId(), updates,
            aVoid -> Log.d(TAG, "View count updated"),
            e -> Log.e(TAG, "Error updating view count: " + e.getMessage())
        );
    }

    private void setupButtons() {
        // Back button
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Gọi điện
        btnCall.setOnClickListener(v -> {
            String phone = tvOwnerPhone.getText().toString();
            if (phone.equals("Chưa cập nhật")) {
                Toast.makeText(this, "Chưa có số điện thoại", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phone));
            startActivity(intent);
        });

        // Nhắn tin chủ trọ
        btnMessage.setOnClickListener(v -> {
            if (currentUserId == null) {
                Toast.makeText(this, "Vui lòng đăng nhập để nhắn tin", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (currentRoom == null) return;
            
            Intent intent = new Intent(PropertyDetailActivity.this, ChatDetailActivity.class);
            intent.putExtra("recipientId", currentRoom.getOwnerId());
            intent.putExtra("recipientName", currentRoom.getOwnerName());
            intent.putExtra("roomId", currentRoom.getId());
            intent.putExtra("roomTitle", currentRoom.getTitle());
            startActivity(intent);
        });

        // Đặt lịch
        btnBooking.setOnClickListener(v -> {
            if (currentUserId == null) {
                Toast.makeText(this, "Vui lòng đăng nhập để đặt lịch", Toast.LENGTH_SHORT).show();
                return;
            }

            if (currentRoom == null) return;

            Intent intent = new Intent(PropertyDetailActivity.this, BookingActivity.class);
            intent.putExtra("roomId", currentRoom.getId());
            intent.putExtra("roomTitle", currentRoom.getTitle());
            intent.putExtra("ownerId", currentRoom.getOwnerId());
            startActivity(intent);
        });

        // Yêu thích
        if (imgFavorite != null) {
            imgFavorite.setOnClickListener(v -> toggleFavorite());
        }
    }

    /**
     * Thêm/Xóa khỏi yêu thích
     */
    private void toggleFavorite() {
        if (currentUserId == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (currentRoom == null) return;

        if (isFavorite) {
            // Xóa khỏi yêu thích
            firebaseManager.getFirestore()
                .collection("favorites")
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("roomId", currentRoom.getId())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        doc.getReference().delete();
                    }
                    isFavorite = false;
                    updateFavoriteIcon();
                    Toast.makeText(this, "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();
                });
        } else {
            // Thêm vào yêu thích
            Map<String, Object> favorite = new HashMap<>();
            favorite.put("userId", currentUserId);
            favorite.put("roomId", currentRoom.getId());
            favorite.put("createdAt", System.currentTimeMillis());
            
            firebaseManager.getFirestore()
                .collection("favorites")
                .add(favorite)
                .addOnSuccessListener(docRef -> {
                    isFavorite = true;
                    updateFavoriteIcon();
                    Toast.makeText(this, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
                });
        }
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}
