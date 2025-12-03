package com.example.nhom15_roomfinder.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.nhom15_roomfinder.R;
import com.example.nhom15_roomfinder.entity.Room;
import com.example.nhom15_roomfinder.firebase.FirebaseManager;
import com.google.android.material.chip.Chip;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * PostRoomActivity - Màn hình đăng tin phòng trọ mới
 */
public class PostRoomActivity extends AppCompatActivity {

    private static final String TAG = "PostRoomActivity";
    private static final int MAX_IMAGES = 5;

    // UI Components
    private EditText etTitle, etDescription, etPrice, etArea;
    private EditText etAddress, etDistrict, etCity;
    private Chip chipWifi, chipAC, chipParking, chipBathroom, chipKitchen, chipSecurity;
    private LinearLayout layoutImages;
    private CardView btnAddImage;
    private Button btnSubmit;
    private ProgressBar progressBar;

    private FirebaseManager firebaseManager;
    private String currentUserId;
    private List<Uri> selectedImages = new ArrayList<>();

    // Activity Result Launcher for image picker
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_room);

        firebaseManager = FirebaseManager.getInstance();
        currentUserId = firebaseManager.getUserId();

        if (currentUserId == null) {
            redirectToLogin();
            return;
        }

        initViews();
        setupImagePicker();
        setupListeners();
    }

    private void initViews() {
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etPrice = findViewById(R.id.etPrice);
        etArea = findViewById(R.id.etArea);
        etAddress = findViewById(R.id.etAddress);
        etDistrict = findViewById(R.id.etDistrict);
        etCity = findViewById(R.id.etCity);

        chipWifi = findViewById(R.id.chipWifi);
        chipAC = findViewById(R.id.chipAC);
        chipParking = findViewById(R.id.chipParking);
        chipBathroom = findViewById(R.id.chipBathroom);
        chipKitchen = findViewById(R.id.chipKitchen);
        chipSecurity = findViewById(R.id.chipSecurity);

        layoutImages = findViewById(R.id.layoutImages);
        btnAddImage = findViewById(R.id.btnAddImage);
        btnSubmit = findViewById(R.id.btnSubmit);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null && selectedImages.size() < MAX_IMAGES) {
                        selectedImages.add(imageUri);
                        addImageToLayout(imageUri);
                    }
                }
            }
        );
    }

    private void setupListeners() {
        btnAddImage.setOnClickListener(v -> {
            if (selectedImages.size() >= MAX_IMAGES) {
                Toast.makeText(this, "Tối đa " + MAX_IMAGES + " ảnh", Toast.LENGTH_SHORT).show();
                return;
            }
            openImagePicker();
        });

        btnSubmit.setOnClickListener(v -> submitRoom());
    }

    private void openImagePicker() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
            return;
        }

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void addImageToLayout(Uri imageUri) {
        // Create image view
        View imageItem = LayoutInflater.from(this)
                .inflate(R.layout.item_selected_image, layoutImages, false);
        
        ImageView imgSelected = imageItem.findViewById(R.id.imgSelected);
        ImageView btnRemove = imageItem.findViewById(R.id.btnRemove);

        Glide.with(this)
                .load(imageUri)
                .centerCrop()
                .into(imgSelected);

        int index = selectedImages.size() - 1;
        btnRemove.setOnClickListener(v -> {
            selectedImages.remove(imageUri);
            layoutImages.removeView(imageItem);
        });

        // Add before the add button
        layoutImages.addView(imageItem, layoutImages.getChildCount() - 1);
    }

    private void submitRoom() {
        // Validate input
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String areaStr = etArea.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String district = etDistrict.getText().toString().trim();
        String city = etCity.getText().toString().trim();

        if (title.isEmpty()) {
            etTitle.setError("Vui lòng nhập tiêu đề");
            etTitle.requestFocus();
            return;
        }

        if (priceStr.isEmpty()) {
            etPrice.setError("Vui lòng nhập giá");
            etPrice.requestFocus();
            return;
        }

        if (address.isEmpty()) {
            etAddress.setError("Vui lòng nhập địa chỉ");
            etAddress.requestFocus();
            return;
        }

        showLoading(true);

        // Parse values
        double price = Double.parseDouble(priceStr);
        double area = areaStr.isEmpty() ? 0 : Double.parseDouble(areaStr);

        // Create room object
        String roomId = UUID.randomUUID().toString();
        
        Map<String, Object> roomData = new HashMap<>();
        roomData.put("id", roomId);
        roomData.put("title", title);
        roomData.put("description", description);
        roomData.put("price", price);
        roomData.put("priceDisplay", String.format("%,.0f VNĐ/tháng", price));
        roomData.put("area", area);
        roomData.put("address", address);
        roomData.put("district", district);
        roomData.put("city", city);
        
        // Amenities
        roomData.put("hasWifi", chipWifi.isChecked());
        roomData.put("hasAC", chipAC.isChecked());
        roomData.put("hasParking", chipParking.isChecked());
        roomData.put("hasPrivateBathroom", chipBathroom.isChecked());
        roomData.put("hasKitchen", chipKitchen.isChecked());
        roomData.put("hasSecurity", chipSecurity.isChecked());
        
        // Owner info
        roomData.put("ownerId", currentUserId);
        roomData.put("isAvailable", true);
        roomData.put("viewCount", 0);
        roomData.put("createdAt", System.currentTimeMillis());
        roomData.put("updatedAt", System.currentTimeMillis());

        // Upload images first, then save room
        if (selectedImages.isEmpty()) {
            saveRoomToFirestore(roomId, roomData);
        } else {
            uploadImagesAndSaveRoom(roomId, roomData);
        }
    }

    private void uploadImagesAndSaveRoom(String roomId, Map<String, Object> roomData) {
        List<String> uploadedUrls = new ArrayList<>();
        
        for (int i = 0; i < selectedImages.size(); i++) {
            Uri imageUri = selectedImages.get(i);
            String imagePath = "rooms/" + roomId + "/image_" + i + ".jpg";
            
            int finalI = i;
            firebaseManager.uploadImageAndGetUrl(imageUri, imagePath,
                url -> {
                    uploadedUrls.add(url.toString());
                    
                    // Khi upload hết ảnh
                    if (uploadedUrls.size() == selectedImages.size()) {
                        roomData.put("imageUrls", uploadedUrls);
                        roomData.put("thumbnailUrl", uploadedUrls.get(0));
                        saveRoomToFirestore(roomId, roomData);
                    }
                },
                e -> {
                    Log.e(TAG, "Error uploading image: " + e.getMessage());
                    showLoading(false);
                    Toast.makeText(this, "Lỗi tải ảnh lên", Toast.LENGTH_SHORT).show();
                }
            );
        }
    }

    private void saveRoomToFirestore(String roomId, Map<String, Object> roomData) {
        firebaseManager.setDocument("rooms", roomId, roomData,
            aVoid -> {
                showLoading(false);
                Toast.makeText(this, "Đăng tin thành công!", Toast.LENGTH_SHORT).show();
                finish();
            },
            e -> {
                showLoading(false);
                Log.e(TAG, "Error saving room: " + e.getMessage());
                Toast.makeText(this, "Lỗi đăng tin", Toast.LENGTH_SHORT).show();
            }
        );
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSubmit.setEnabled(!show);
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openImagePicker();
        }
    }
}
