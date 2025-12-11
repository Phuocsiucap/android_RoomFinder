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
import com.example.nhom15_roomfinder.utils.ImageUploadHelper;
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
    
    // Owner info - tự động lấy từ Firebase
    private String ownerName = "";
    private String ownerPhone = "";

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
        loadOwnerInfo();
    }
    
    /**
     * Tự động lấy thông tin người đăng (tên, số điện thoại) từ Firebase
     */
    private void loadOwnerInfo() {
        if (currentUserId == null) return;
        
        firebaseManager.getFirestore()
            .collection("users")
            .document(currentUserId)
            .get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    ownerName = doc.getString("name");
                    ownerPhone = doc.getString("phone");
                    if (ownerName == null) ownerName = "";
                    if (ownerPhone == null) ownerPhone = "";
                    Log.d(TAG, "Owner info loaded: " + ownerName + ", " + ownerPhone);
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error loading owner info: " + e.getMessage());
            });
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
        // Android 13+ dùng READ_MEDIA_IMAGES, các phiên bản cũ dùng READ_EXTERNAL_STORAGE
        String permission = 
            android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{permission}, 100);
            return;
        }

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void addImageToLayout(Uri imageUri) {
        if (layoutImages == null) return;
        
        // Create image view
        View imageItem = LayoutInflater.from(this)
                .inflate(R.layout.item_selected_image, layoutImages, false);
        
        ImageView imgSelected = imageItem.findViewById(R.id.imgSelected);
        ImageView btnRemove = imageItem.findViewById(R.id.btnRemove);

        Glide.with(this)
                .load(imageUri)
                .centerCrop()
                .into(imgSelected);

        // Store the imageUri in the view's tag for easy removal
        imageItem.setTag(imageUri);
        
        btnRemove.setOnClickListener(v -> {
            // Remove from list
            selectedImages.remove(imageUri);
            // Remove from layout
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
        
        // Owner info - tự động thêm tên và số điện thoại người đăng
        roomData.put("ownerId", currentUserId);
        roomData.put("ownerName", ownerName);
        roomData.put("ownerPhone", ownerPhone);
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
        // Sử dụng Cloudinary để upload ảnh (tối đa 5 ảnh)
        ImageUploadHelper.uploadRoomImages(this, selectedImages, roomId,
            new ImageUploadHelper.MultipleUploadCallback() {
                @Override
                public void onAllSuccess(List<String> imageUrls) {
                    roomData.put("imageUrls", imageUrls);
                    roomData.put("thumbnailUrl", imageUrls.get(0));
                    saveRoomToFirestore(roomId, roomData);
                }

                @Override
                public void onProgress(int current, int total, String currentUrl) {
                    Log.d(TAG, "Upload progress: " + current + "/" + total);
                }

                @Override
                public void onError(String error, int failedIndex) {
                    Log.e(TAG, "Error uploading image at index " + failedIndex + ": " + error);
                    showLoading(false);
                    Toast.makeText(PostRoomActivity.this, "Lỗi tải ảnh lên: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        );
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
