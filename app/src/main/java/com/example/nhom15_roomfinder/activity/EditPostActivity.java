package com.example.nhom15_roomfinder.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.example.nhom15_roomfinder.R;

public class EditPostActivity extends AppCompatActivity {

    private EditText edtTitle, edtDescription, edtPrice, edtAddress;
    private Button btnUpdate;
    private TextView btnAddMoreUtility;
    private ImageView btnBack;
    private boolean hasUnsavedChanges = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);

        initViews();
        setupClickListeners();
        setupBackPressedHandler(); // Thay thế onBackPressed()
        loadPostData();
    }

    private void initViews() {
        edtTitle = findViewById(R.id.edtTitle);
        edtDescription = findViewById(R.id.edtDescription);
        edtPrice = findViewById(R.id.edtPrice);
        edtAddress = findViewById(R.id.edtAddress);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnAddMoreUtility = findViewById(R.id.btnAddMoreUtility);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupBackPressedHandler() {
        // Tạo OnBackPressedCallback
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBackPress();
            }
        };

        // Đăng ký callback với OnBackPressedDispatcher
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void handleBackPress() {
        if (hasUnsavedChanges) {
            // Hiển thị dialog xác nhận nếu có thay đổi chưa lưu
            showUnsavedChangesDialog();
        } else {
            // Nếu không có thay đổi, thoát activity
            finish();
        }
    }

    private void showUnsavedChangesDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Thoát chỉnh sửa")
                .setMessage("Bạn có thay đổi chưa lưu. Bạn có chắc chắn muốn thoát?")
                .setPositiveButton("Thoát", (dialog, which) -> {
                    finish();
                })
                .setNegativeButton("Ở lại", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    private void setupClickListeners() {
        btnUpdate.setOnClickListener(v -> updatePost());

        btnAddMoreUtility.setOnClickListener(v -> addMoreUtility());

        // Xử lý nút back trong toolbar
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                handleBackPress();
            });
        }

        // Theo dõi thay đổi dữ liệu để đánh dấu hasUnsavedChanges
        setupTextChangeListeners();
    }

    private void setupTextChangeListeners() {
        View.OnFocusChangeListener focusChangeListener = (v, hasFocus) -> {
            if (!hasFocus) {
                checkForUnsavedChanges();
            }
        };

        edtTitle.setOnFocusChangeListener(focusChangeListener);
        edtDescription.setOnFocusChangeListener(focusChangeListener);
        edtPrice.setOnFocusChangeListener(focusChangeListener);
        edtAddress.setOnFocusChangeListener(focusChangeListener);
    }

    private void checkForUnsavedChanges() {
        // TODO: So sánh với dữ liệu gốc để xác định có thay đổi không
        // Tạm thời luôn đánh dấu có thay đổi khi người dùng edit
        hasUnsavedChanges = true;
    }

    private void loadPostData() {
        // TODO: Lấy dữ liệu bài đăng từ Intent hoặc Database
        edtTitle.setText("Căn hộ 2 phòng ngủ gần trung tâm");
        edtDescription.setText("Căn hộ cao cấp, đầy đủ tiện nghi, view đẹp...");
        edtPrice.setText("15000000");
        edtAddress.setText("📍 123 Đường ABC, Quận 1, TP.HCM");

        // Reset trạng thái unsaved changes sau khi load data
        hasUnsavedChanges = false;
    }

    private void updatePost() {
        String title = edtTitle.getText().toString().trim();
        String description = edtDescription.getText().toString().trim();
        String priceStr = edtPrice.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();

        // Validate dữ liệu
        if (title.isEmpty()) {
            showToast("Vui lòng nhập tiêu đề");
            return;
        }

        if (description.isEmpty()) {
            showToast("Vui lòng nhập mô tả");
            return;
        }

        if (priceStr.isEmpty()) {
            showToast("Vui lòng nhập giá");
            return;
        }

        if (address.isEmpty()) {
            showToast("Vui lòng nhập địa chỉ");
            return;
        }

        try {
            long price = Long.parseLong(priceStr);
            updatePostToServer(title, description, price, address);
        } catch (NumberFormatException e) {
            showToast("Giá không hợp lệ");
        }
    }

    private void updatePostToServer(String title, String description, long price, String address) {
        btnUpdate.setEnabled(false);
        btnUpdate.setText("Đang cập nhật...");

        // Giả lập cập nhật thành công
        new android.os.Handler().postDelayed(() -> {
            btnUpdate.setEnabled(true);
            btnUpdate.setText("Cập nhật tin");
            showToast("Cập nhật tin thành công");

            // Reset trạng thái unsaved changes sau khi lưu thành công
            hasUnsavedChanges = false;
            finish();
        }, 1500);
    }

    private void addMoreUtility() {
        showToast("Mở màn hình chọn tiện ích");
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}
