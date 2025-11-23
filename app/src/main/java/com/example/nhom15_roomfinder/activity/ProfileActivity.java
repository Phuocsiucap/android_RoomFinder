package com.example.nhom15_roomfinder.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.nhom15_roomfinder.R;
import com.example.nhom15_roomfinder.activity.HomeActivity;
import com.example.nhom15_roomfinder.firebase.FirebaseManager;

public class ProfileActivity extends AppCompatActivity {
    
    private static final String TAG = "ProfileActivity";
    private FirebaseManager firebaseManager;
    private Uri selectedImageUri;
    
    // UI Components - matching with activity_profile.xml
    private TextView tvTitle;
    private ImageView imgProfilePicture;
    private ImageView ivUploadIcon;
    private TextView tvUploadChange;
    private EditText etFullName;
    private EditText etEmail;
    private EditText etPhoneNumber;
    private Spinner spinnerGender;
    private Button btnSaveChanges;
    private Button btnLogout;
    
    // Activity Result Launcher for image selection
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        
        // Initialize Firebase
        firebaseManager = FirebaseManager.getInstance();
        
        // Initialize image picker launcher
        initImagePickerLauncher();
        
        // Initialize Views
        initializeViews();
        
        // Setup Spinner
        setupGenderSpinner();
        
        // Set Listeners
        setListeners();
        
        // Load User Data
        loadUserData();
    }
    
    /**
     * Initialize image picker launcher
     */
    private void initImagePickerLauncher() {
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        // Display selected image
                        imgProfilePicture.setImageURI(selectedImageUri);
                        showToast("Ảnh đã được chọn. Nhấn 'Lưu thay đổi' để cập nhật.");
                    }
                }
            }
        );
    }
    
    /**
     * Initialize Views
     */
    private void initializeViews() {
        tvTitle = findViewById(R.id.tvTitle);
        imgProfilePicture = findViewById(R.id.imgProfilePicture);
        ivUploadIcon = findViewById(R.id.ivUploadIcon);
        tvUploadChange = findViewById(R.id.tvUploadChange);
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        spinnerGender = findViewById(R.id.spinnerGender);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        btnLogout = findViewById(R.id.btnLogout);
        
        // Set default profile picture
        imgProfilePicture.setImageResource(R.drawable.ic_profile_placeholder);
    }
    
    /**
     * Setup Gender Spinner
     */
    private void setupGenderSpinner() {
        String[] genderOptions = {"Chọn giới tính", "Nam", "Nữ", "Khác"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this, 
            android.R.layout.simple_spinner_item, 
            genderOptions
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);
    }
    
    /**
     * Set Click Listeners
     */
    private void setListeners() {
        // Profile picture click - change avatar
        imgProfilePicture.setOnClickListener(v -> openImagePicker());
        ivUploadIcon.setOnClickListener(v -> openImagePicker());
        tvUploadChange.setOnClickListener(v -> openImagePicker());
        
        // Save changes button
        btnSaveChanges.setOnClickListener(v -> saveUserProfile());
        
        // Logout button
        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }
    
    /**
     * Open image picker
     */
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        imagePickerLauncher.launch(Intent.createChooser(intent, "Chọn ảnh đại diện"));
    }
    
    /**
     * Load user data from Firebase
     */
    private void loadUserData() {
        if (firebaseManager.isUserLoggedIn()) {
            String userId = firebaseManager.getUserId();
            
            // Get current user and extract email
            String userEmail = null;
            if (firebaseManager.getCurrentUser() != null) {
                userEmail = firebaseManager.getCurrentUser().getEmail();
            }
            
            // Display basic info from Firebase Auth
            if (userEmail != null) {
                etEmail.setText(userEmail);
            }
            
            // TODO: Load additional user data from Firestore
            loadUserProfile(userId);
            
            Log.d(TAG, "Loading user data for: " + userId);
        } else {
            // User not logged in, redirect to login
            redirectToLogin();
        }
    }
    
    /**
     * Load user profile from Firestore
     */
    private void loadUserProfile(String userId) {

        // TODO: Implement Firestore query to get user profile

        // For now, set placeholder values
        if (etFullName.getText().toString().trim().isEmpty()) {
            etFullName.setHint("Nhập họ tên của bạn");
        }
        if (etPhoneNumber.getText().toString().trim().isEmpty()) {
            etPhoneNumber.setHint("Nhập số điện thoại");
        }
        
        Log.d(TAG, "Loading profile for user: " + userId);
        showToast("Đang tải thông tin người dùng...");
    }
    
    /**
     * Save user profile changes
     */
    private void saveUserProfile() {
        if (!validateInput()) {
            return;
        }
        
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        String gender = spinnerGender.getSelectedItemPosition() > 0 ? 
                      spinnerGender.getSelectedItem().toString() : "";
        
        // Show loading
        btnSaveChanges.setEnabled(false);
        btnSaveChanges.setText("Đang lưu...");
        
        try {
            // TODO: Upload image to Firebase Storage if selectedImageUri is not null
            // TODO: Save user profile to Firestore
            
            // Simulate save process
            saveUserProfileToFirestore(fullName, email, phoneNumber, gender);
            
        } catch (Exception e) {
            Log.e(TAG, "Error saving profile: " + e.getMessage(), e);
            showToast("Có lỗi xảy ra khi lưu thông tin");
            resetSaveButton();
        }
    }
    
    /**
     * Save user profile to Firestore
     */
    private void saveUserProfileToFirestore(String fullName, String email, String phone, String gender) {
        // TODO: Implement actual Firestore save
        
        // For now, just show success message
        new android.os.Handler().postDelayed(() -> {
            showToast("Đã lưu thông tin thành công!");
            resetSaveButton();
        }, 1500);
        
        Log.d(TAG, "Saving profile: " + fullName + ", " + email + ", " + phone + ", " + gender);
    }
    
    /**
     * Reset save button to original state
     */
    private void resetSaveButton() {
        btnSaveChanges.setEnabled(true);
        btnSaveChanges.setText("Lưu thay đổi");
    }
    
    /**
     * Validate user input
     */
    private boolean validateInput() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhoneNumber.getText().toString().trim();
        
        if (fullName.isEmpty()) {
            etFullName.setError("Vui lòng nhập họ tên");
            etFullName.requestFocus();
            return false;
        }
        
        if (email.isEmpty()) {
            etEmail.setError("Vui lòng nhập email");
            etEmail.requestFocus();
            return false;
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email không hợp lệ");
            etEmail.requestFocus();
            return false;
        }
        
        if (!phone.isEmpty() && phone.length() < 10) {
            etPhoneNumber.setError("Số điện thoại phải có ít nhất 10 số");
            etPhoneNumber.requestFocus();
            return false;
        }
        
        return true;
    }
    
    /**
     * Show logout confirmation dialog
     */
    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    performLogout();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
    
    /**
     * Perform logout
     */
    private void performLogout() {
        try {
            firebaseManager.signOut();
            showToast("Đã đăng xuất thành công");
            redirectToLogin();
        } catch (Exception e) {
            Log.e(TAG, "Error during logout: " + e.getMessage(), e);
            showToast("Có lỗi xảy ra khi đăng xuất");
        }
    }
    
    /**
     * Redirect to login screen
     */
    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    /**
     * Helper method to show toast messages
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Return to HomeActivity
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}
