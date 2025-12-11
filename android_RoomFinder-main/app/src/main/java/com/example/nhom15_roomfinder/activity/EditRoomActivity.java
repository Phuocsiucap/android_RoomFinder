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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * EditRoomActivity - Màn hình chỉnh sửa bài đăng phòng trọ
 */
public class EditRoomActivity extends AppCompatActivity {

    private static final String TAG = "EditRoomActivity";
    private static final int MAX_IMAGES = 5;

    // UI Components
    private ImageButton btnBack;
    private TextView tvTitle;
    private EditText etTitle, etDescription, etPrice, etArea;
    private EditText etAddress, etDistrict, etCity;
    private Chip chipWifi, chipAC, chipParking, chipBathroom, chipKitchen, chipSecurity;
    private LinearLayout layoutImages;
    private CardView btnAddImage;
    private Button btnUpdate;
    private ProgressBar progressBar;
    private Switch switchAvailable;

    private FirebaseManager firebaseManager;
    private String currentUserId;
    private Room currentRoom;
    private String roomId;
    
    // Images
    private List<Uri> newSelectedImages = new ArrayList<>();
    private List<String> existingImageUrls = new ArrayList<>();
    private List<String> deletedImageUrls = new ArrayList<>();

    // Activity Result Launcher for image picker
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_room);

        firebaseManager = FirebaseManager.getInstance();
        currentUserId = firebaseManager.getUserId();

        if (currentUserId == null) {
            redirectToLogin();
            return;
        }

        getIntentData();
        initViews();
        setupImagePicker();
        setupListeners();
        populateData();
    }

    private void getIntentData() {
        if (getIntent() != null) {
            currentRoom = (Room) getIntent().getSerializableExtra("room");
            roomId = getIntent().getStringExtra("roomId");
        }

        if (currentRoom == null && roomId != null) {
            loadRoomFromFirebase();
        }
    }

    private void loadRoomFromFirebase() {
        showLoading(true);
        firebaseManager.getFirestore()
            .collection("rooms")
            .document(roomId)
            .get()
            .addOnSuccessListener(doc -> {
                showLoading(false);
                if (doc.exists()) {
                    currentRoom = doc.toObject(Room.class);
                    if (currentRoom != null) {
                        currentRoom.setId(doc.getId());
                        populateData();
                    }
                }
            })
            .addOnFailureListener(e -> {
                showLoading(false);
                Toast.makeText(this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                finish();
            });
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvTitle = findViewById(R.id.tvTitle);
        
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
        btnUpdate = findViewById(R.id.btnUpdate);
        progressBar = findViewById(R.id.progressBar);
        switchAvailable = findViewById(R.id.switchAvailable);
    }

    private void populateData() {
        if (currentRoom == null) return;

        etTitle.setText(currentRoom.getTitle());
        etDescription.setText(currentRoom.getDescription());
        etPrice.setText(String.valueOf((long) currentRoom.getPrice()));
        etArea.setText(String.valueOf((int) currentRoom.getArea()));
        etAddress.setText(currentRoom.getAddress());
        etDistrict.setText(currentRoom.getDistrict());
        etCity.setText(currentRoom.getCity());

        // Amenities
        chipWifi.setChecked(currentRoom.isHasWifi());
        chipAC.setChecked(currentRoom.isHasAC());
        chipParking.setChecked(currentRoom.isHasParking());
        chipBathroom.setChecked(currentRoom.isHasPrivateBathroom());
        chipKitchen.setChecked(currentRoom.isHasKitchen());
        chipSecurity.setChecked(currentRoom.isHasSecurity());
        
        // Status
        switchAvailable.setChecked(currentRoom.isAvailable());

        // Load existing images
        if (currentRoom.getImageUrls() != null) {
            existingImageUrls.addAll(currentRoom.getImageUrls());
            for (String url : existingImageUrls) {
                addExistingImageToLayout(url);
            }
        }
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    int totalImages = existingImageUrls.size() + newSelectedImages.size();
                    if (imageUri != null && totalImages < MAX_IMAGES) {
                        newSelectedImages.add(imageUri);
                        addNewImageToLayout(imageUri);
                    }
                }
            }
        );
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnAddImage.setOnClickListener(v -> {
            int totalImages = existingImageUrls.size() + newSelectedImages.size();
            if (totalImages >= MAX_IMAGES) {
                Toast.makeText(this, "Tối đa " + MAX_IMAGES + " ảnh", Toast.LENGTH_SHORT).show();
                return;
            }
            openImagePicker();
        });

        btnUpdate.setOnClickListener(v -> updateRoom());
    }

    private void openImagePicker() {
        String permission = 
            android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, 100);
            return;
        }

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void addExistingImageToLayout(String imageUrl) {
        View imageItem = LayoutInflater.from(this)
                .inflate(R.layout.item_selected_image, layoutImages, false);
        
        ImageView imgSelected = imageItem.findViewById(R.id.imgSelected);
        ImageView btnRemove = imageItem.findViewById(R.id.btnRemove);

        Glide.with(this)
                .load(imageUrl)
                .centerCrop()
                .into(imgSelected);

        imageItem.setTag(imageUrl);
        
        btnRemove.setOnClickListener(v -> {
            existingImageUrls.remove(imageUrl);
            deletedImageUrls.add(imageUrl);
            layoutImages.removeView(imageItem);
        });

        layoutImages.addView(imageItem, layoutImages.getChildCount() - 1);
    }

    private void addNewImageToLayout(Uri imageUri) {
        View imageItem = LayoutInflater.from(this)
                .inflate(R.layout.item_selected_image, layoutImages, false);
        
        ImageView imgSelected = imageItem.findViewById(R.id.imgSelected);
        ImageView btnRemove = imageItem.findViewById(R.id.btnRemove);

        Glide.with(this)
                .load(imageUri)
                .centerCrop()
                .into(imgSelected);

        imageItem.setTag(imageUri);
        
        btnRemove.setOnClickListener(v -> {
            newSelectedImages.remove(imageUri);
            layoutImages.removeView(imageItem);
        });

        layoutImages.addView(imageItem, layoutImages.getChildCount() - 1);
    }

    private void updateRoom() {
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

        double price = Double.parseDouble(priceStr);
        double area = areaStr.isEmpty() ? 0 : Double.parseDouble(areaStr);

        Map<String, Object> roomData = new HashMap<>();
        roomData.put("title", title);
        roomData.put("description", description);
        roomData.put("price", price);
        roomData.put("priceDisplay", String.format("%,.0f VNĐ/tháng", price));
        roomData.put("area", area);
        roomData.put("address", address);
        roomData.put("district", district);
        roomData.put("city", city);
        roomData.put("hasWifi", chipWifi.isChecked());
        roomData.put("hasAC", chipAC.isChecked());
        roomData.put("hasParking", chipParking.isChecked());
        roomData.put("hasPrivateBathroom", chipBathroom.isChecked());
        roomData.put("hasKitchen", chipKitchen.isChecked());
        roomData.put("hasSecurity", chipSecurity.isChecked());
        roomData.put("isAvailable", switchAvailable.isChecked());
        roomData.put("updatedAt", System.currentTimeMillis());

        // Upload new images if any
        if (!newSelectedImages.isEmpty()) {
            uploadNewImagesAndUpdate(roomData);
        } else {
            // No new images, just update with existing URLs
            roomData.put("imageUrls", existingImageUrls);
            if (!existingImageUrls.isEmpty()) {
                roomData.put("thumbnailUrl", existingImageUrls.get(0));
            }
            saveRoomToFirestore(roomData);
        }
    }

    private void uploadNewImagesAndUpdate(Map<String, Object> roomData) {
        ImageUploadHelper.uploadRoomImages(this, newSelectedImages, roomId,
            new ImageUploadHelper.MultipleUploadCallback() {
                @Override
                public void onAllSuccess(List<String> imageUrls) {
                    // Combine existing and new URLs
                    List<String> allUrls = new ArrayList<>(existingImageUrls);
                    allUrls.addAll(imageUrls);
                    
                    roomData.put("imageUrls", allUrls);
                    if (!allUrls.isEmpty()) {
                        roomData.put("thumbnailUrl", allUrls.get(0));
                    }
                    saveRoomToFirestore(roomData);
                }

                @Override
                public void onProgress(int current, int total, String currentUrl) {
                    Log.d(TAG, "Upload progress: " + current + "/" + total);
                }

                @Override
                public void onError(String error, int failedIndex) {
                    showLoading(false);
                    Toast.makeText(EditRoomActivity.this, "Lỗi tải ảnh: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        );
    }

    private void saveRoomToFirestore(Map<String, Object> roomData) {
        firebaseManager.updateDocument("rooms", roomId, roomData,
            aVoid -> {
                showLoading(false);
                Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            },
            e -> {
                showLoading(false);
                Log.e(TAG, "Error updating room: " + e.getMessage());
                Toast.makeText(this, "Lỗi cập nhật", Toast.LENGTH_SHORT).show();
            }
        );
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnUpdate.setEnabled(!show);
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
