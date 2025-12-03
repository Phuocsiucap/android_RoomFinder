package com.example.nhom15_roomfinder;

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
import com.example.nhom15_roomfinder.firebase.FirebaseManager;
import com.google.android.material.chip.Chip;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * EditRoomActivity - Màn hình sửa thông tin phòng trọ
 */
public class EditRoomActivity extends AppCompatActivity {

    private static final String TAG = "EditRoomActivity";
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
    private String roomId;
    private List<Uri> selectedImages = new ArrayList<>();
    private List<String> existingImageUrls = new ArrayList<>();

    // Activity Result Launcher for image picker
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_room);

        // Get roomId from intent
        roomId = getIntent().getStringExtra("roomId");
        if (roomId == null) {
            Toast.makeText(this, "Không tìm thấy ID phòng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        firebaseManager = FirebaseManager.getInstance();
        initViews();
        setupImagePicker();
        setupListeners();
        loadRoomData();
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
        
        if (btnSubmit != null) {
            btnSubmit.setText("Cập nhật");
        }
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
        if (btnAddImage != null) {
            btnAddImage.setOnClickListener(v -> {
                if (selectedImages.size() >= MAX_IMAGES) {
                    Toast.makeText(this, "Tối đa " + MAX_IMAGES + " ảnh", Toast.LENGTH_SHORT).show();
                    return;
                }
                openImagePicker();
            });
        }

        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> updateRoom());
        }
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

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        imagePickerLauncher.launch(Intent.createChooser(intent, "Chọn ảnh"));
    }

    private void addImageToLayout(Uri imageUri) {
        if (layoutImages == null) return;
        
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
    
    private void addExistingImageToLayout(String imageUrl) {
        if (layoutImages == null) return;
        
        View imageItem = LayoutInflater.from(this)
                .inflate(R.layout.item_selected_image, layoutImages, false);
        
        ImageView imgSelected = imageItem.findViewById(R.id.imgSelected);
        ImageView btnRemove = imageItem.findViewById(R.id.btnRemove);

        Glide.with(this)
                .load(imageUrl)
                .centerCrop()
                .into(imgSelected);

        btnRemove.setOnClickListener(v -> {
            // Xóa ảnh khỏi danh sách
            existingImageUrls.remove(imageUrl);
            layoutImages.removeView(imageItem);
            
            // Xóa file trên Firebase Storage nếu có thể
            deleteImageFromStorage(imageUrl);
        });

        // Add before the add button
        layoutImages.addView(imageItem, layoutImages.getChildCount() - 1);
    }
    
    private void deleteImageFromStorage(String imageUrl) {
        try {
            // Lấy path từ URL
            // URL format: https://firebasestorage.googleapis.com/v0/b/.../o/rooms%2F...%2Fimage.jpg?alt=media&token=...
            if (imageUrl == null || imageUrl.isEmpty()) return;
            
            // Extract path từ URL
            String path = extractPathFromUrl(imageUrl);
            if (path == null || path.isEmpty()) return;
            
            // Xóa file từ Firebase Storage
            firebaseManager.getStorageReference()
                .child(path)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Image deleted from Storage: " + path);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting image from Storage: " + e.getMessage());
                    // Không hiển thị lỗi cho user vì ảnh đã được xóa khỏi UI
                });
        } catch (Exception e) {
            Log.e(TAG, "Error parsing image URL: " + e.getMessage());
        }
    }
    
    private String extractPathFromUrl(String url) {
        try {
            // URL format: https://firebasestorage.googleapis.com/v0/b/BUCKET/o/PATH?alt=media&token=TOKEN
            // Cần decode URL encoding (%2F -> /)
            if (url.contains("/o/")) {
                int startIndex = url.indexOf("/o/") + 3;
                int endIndex = url.indexOf("?");
                if (endIndex == -1) endIndex = url.length();
                
                String encodedPath = url.substring(startIndex, endIndex);
                // Decode URL encoding
                return java.net.URLDecoder.decode(encodedPath, "UTF-8");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error extracting path from URL: " + e.getMessage());
        }
        return null;
    }

    private void loadRoomData() {
        showLoading(true);
        
        firebaseManager.getFirestore()
            .collection("rooms")
            .document(roomId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                showLoading(false);
                if (documentSnapshot.exists()) {
                    populateRoomData(documentSnapshot);
                } else {
                    Toast.makeText(this, "Không tìm thấy thông tin phòng", Toast.LENGTH_SHORT).show();
                    finish();
                }
            })
            .addOnFailureListener(e -> {
                showLoading(false);
                Log.e(TAG, "Error loading room: " + e.getMessage());
                Toast.makeText(this, "Lỗi tải thông tin phòng", Toast.LENGTH_SHORT).show();
                finish();
            });
    }

    private void populateRoomData(DocumentSnapshot document) {
        // Basic info
        etTitle.setText(document.getString("title"));
        etDescription.setText(document.getString("description"));
        
        // Price
        Object priceObj = document.get("price");
        if (priceObj instanceof Number) {
            etPrice.setText(String.valueOf(((Number) priceObj).longValue()));
        }
        
        // Area
        Object areaObj = document.get("area");
        if (areaObj instanceof Number) {
            etArea.setText(String.valueOf(((Number) areaObj).intValue()));
        }
        
        // Address
        etAddress.setText(document.getString("address"));
        etDistrict.setText(document.getString("district"));
        etCity.setText(document.getString("city"));
        
        // Amenities
        chipWifi.setChecked(Boolean.TRUE.equals(document.getBoolean("hasWifi")));
        chipAC.setChecked(Boolean.TRUE.equals(document.getBoolean("hasAC")));
        chipParking.setChecked(Boolean.TRUE.equals(document.getBoolean("hasParking")));
        chipBathroom.setChecked(Boolean.TRUE.equals(document.getBoolean("hasPrivateBathroom")));
        chipKitchen.setChecked(Boolean.TRUE.equals(document.getBoolean("hasKitchen")));
        chipSecurity.setChecked(Boolean.TRUE.equals(document.getBoolean("hasSecurity")));
        
        // Load existing images
        @SuppressWarnings("unchecked")
        List<String> imageUrls = (List<String>) document.get("imageUrls");
        if (imageUrls != null && !imageUrls.isEmpty()) {
            existingImageUrls = new ArrayList<>(imageUrls);
            for (String url : existingImageUrls) {
                addExistingImageToLayout(url);
            }
        } else {
            String thumbnailUrl = document.getString("thumbnailUrl");
            if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {
                existingImageUrls.add(thumbnailUrl);
                addExistingImageToLayout(thumbnailUrl);
            }
        }
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

        // Parse values
        double price = Double.parseDouble(priceStr);
        double area = areaStr.isEmpty() ? 0 : Double.parseDouble(areaStr);

        // Create update data
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("title", title);
        updateData.put("description", description);
        updateData.put("price", price);
        updateData.put("priceDisplay", String.format("%,.0f VNĐ/tháng", price));
        updateData.put("area", area);
        updateData.put("address", address);
        updateData.put("district", district);
        updateData.put("city", city);
        
        // Amenities
        updateData.put("hasWifi", chipWifi.isChecked());
        updateData.put("hasAC", chipAC.isChecked());
        updateData.put("hasParking", chipParking.isChecked());
        updateData.put("hasPrivateBathroom", chipBathroom.isChecked());
        updateData.put("hasKitchen", chipKitchen.isChecked());
        updateData.put("hasSecurity", chipSecurity.isChecked());
        
        updateData.put("updatedAt", System.currentTimeMillis());

        // Upload new images if any, then update room
        if (selectedImages.isEmpty()) {
            // Keep existing images
            if (!existingImageUrls.isEmpty()) {
                updateData.put("imageUrls", existingImageUrls);
                if (existingImageUrls.size() > 0) {
                    updateData.put("thumbnailUrl", existingImageUrls.get(0));
                }
            }
            updateRoomInFirestore(updateData);
        } else {
            uploadImagesAndUpdateRoom(updateData);
        }
    }

    private void uploadImagesAndUpdateRoom(Map<String, Object> updateData) {
        List<String> uploadedUrls = new ArrayList<>(existingImageUrls);
        
        for (int i = 0; i < selectedImages.size(); i++) {
            Uri imageUri = selectedImages.get(i);
            String imagePath = "rooms/" + roomId + "/image_" + System.currentTimeMillis() + "_" + i + ".jpg";
            
            int finalI = i;
            firebaseManager.uploadImageAndGetUrl(imageUri, imagePath,
                url -> {
                    uploadedUrls.add(url.toString());
                    
                    // When all images uploaded
                    if (uploadedUrls.size() == (existingImageUrls.size() + selectedImages.size())) {
                        updateData.put("imageUrls", uploadedUrls);
                        if (uploadedUrls.size() > 0) {
                            updateData.put("thumbnailUrl", uploadedUrls.get(0));
                        }
                        updateRoomInFirestore(updateData);
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

    private void updateRoomInFirestore(Map<String, Object> updateData) {
        firebaseManager.updateDocument("rooms", roomId, updateData,
            aVoid -> {
                showLoading(false);
                Toast.makeText(this, "Cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show();
                finish();
            },
            e -> {
                showLoading(false);
                Log.e(TAG, "Error updating room: " + e.getMessage());
                Toast.makeText(this, "Lỗi cập nhật thông tin", Toast.LENGTH_SHORT).show();
            }
        );
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (btnSubmit != null) {
            btnSubmit.setEnabled(!show);
        }
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

