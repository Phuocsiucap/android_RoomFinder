package com.example.nhom15_roomfinder.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.nhom15_roomfinder.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class CreatePostActivity extends AppCompatActivity {

    private EditText etTitle, etDescription, etPrice, etAddress;
    private ChipGroup chipGroupAmenities;
    private Button btnAddMore, btnPost;
    private FrameLayout btnAddImage;
    private ImageView ivImage1, ivImage2;

    private List<Uri> selectedImages = new ArrayList<>();
    private static final int PICK_IMAGE_REQUEST = 1;
    private int currentImageSlot = 1; // 1 hoặc 2 để xác định đang chọn ảnh cho slot nào

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post); // Đảm bảo layout file name đúng

        initViews();
        setupListeners();
    }

    private void initViews() {
        // Khởi tạo EditText
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etPrice = findViewById(R.id.etPrice);
        etAddress = findViewById(R.id.etAddress);

        // Khởi tạo ChipGroup
        chipGroupAmenities = findViewById(R.id.chipGroupAmenities);

        // Khởi tạo Buttons
        btnAddMore = findViewById(R.id.btnAddMore);
        btnPost = findViewById(R.id.btnPost);

        // Khởi tạo Image views và button
        btnAddImage = findViewById(R.id.btnAddImage);
        ivImage1 = findViewById(R.id.ivImage1);
        ivImage2 = findViewById(R.id.ivImage2);
    }

    private void setupListeners() {
        // Listener cho nút thêm ảnh
        btnAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker();
            }
        });

        // Listener cho từng image view để thay đổi ảnh
        ivImage1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentImageSlot = 1;
                openImagePicker();
            }
        });

        ivImage2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentImageSlot = 2;
                openImagePicker();
            }
        });

        // Listener cho nút thêm tiện ích
        btnAddMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddAmenityDialog();
            }
        });

        // Listener cho nút đăng tin
        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAndPost();
            }
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            Uri imageUri = data.getData();

            if (imageUri != null) {
                // Hiển thị ảnh đã chọn lên ImageView tương ứng
                if (currentImageSlot == 1) {
                    ivImage1.setImageURI(imageUri);
                    // Ẩn icon camera khi có ảnh
                    ivImage1.setBackground(null);
                } else if (currentImageSlot == 2) {
                    ivImage2.setImageURI(imageUri);
                    // Ẩn icon camera khi có ảnh
                    ivImage2.setBackground(null);
                }

                // Lưu URI ảnh vào danh sách
                if (selectedImages.size() < 2) {
                    selectedImages.add(imageUri);
                }
            }
        }
    }

    private void showAddAmenityDialog() {
        // Tạo dialog hoặc bottom sheet để thêm tiện ích mới
        // Ở đây có thể implement theo nhu cầu cụ thể
        Toast.makeText(this, "Chức năng thêm tiện ích mới", Toast.LENGTH_SHORT).show();
    }

    private void validateAndPost() {
        // Lấy dữ liệu từ form
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String price = etPrice.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        // Lấy danh sách tiện ích đã chọn
        List<String> selectedAmenities = getSelectedAmenities();

        // Validate dữ liệu
        if (TextUtils.isEmpty(title)) {
            etTitle.setError("Vui lòng nhập tiêu đề");
            etTitle.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(description)) {
            etDescription.setError("Vui lòng nhập mô tả");
            etDescription.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(price)) {
            etPrice.setError("Vui lòng nhập giá");
            etPrice.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(address)) {
            etAddress.setError("Vui lòng nhập địa chỉ");
            etAddress.requestFocus();
            return;
        }

        // Tạo đối tượng Post
        Post post = new Post();
        post.setTitle(title);
        post.setDescription(description);
        post.setPrice(price);
        post.setAddress(address);
        post.setAmenities(selectedAmenities);
        post.setImageUris(selectedImages);

        // Gửi dữ liệu lên server hoặc lưu vào database
        submitPost(post);
    }

    private List<String> getSelectedAmenities() {
        List<String> amenities = new ArrayList<>();

        for (int i = 0; i < chipGroupAmenities.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupAmenities.getChildAt(i);
            if (chip.isChecked()) {
                amenities.add(chip.getText().toString());
            }
        }

        return amenities;
    }

    private void submitPost(Post post) {
        // TODO: Implement logic để gửi bài đăng lên server
        // Có thể sử dụng Retrofit, Volley, hoặc Firebase tùy theo backend

        // Hiển thị thông báo thành công
        Toast.makeText(this, "Đăng tin thành công!", Toast.LENGTH_SHORT).show();

        // Quay lại màn hình trước đó hoặc làm mới form
        finish();
    }

    // Class Post để lưu thông tin bài đăng
    public static class Post {
        private String title;
        private String description;
        private String price;
        private String address;
        private List<String> amenities;
        private List<Uri> imageUris;

        // Constructor, getters và setters
        public Post() {}

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getPrice() { return price; }
        public void setPrice(String price) { this.price = price; }

        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }

        public List<String> getAmenities() { return amenities; }
        public void setAmenities(List<String> amenities) { this.amenities = amenities; }

        public List<Uri> getImageUris() { return imageUris; }
        public void setImageUris(List<Uri> imageUris) { this.imageUris = imageUris; }
    }
}
