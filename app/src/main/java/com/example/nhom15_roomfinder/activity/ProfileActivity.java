package com.example.nhom15_roomfinder.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.nhom15_roomfinder.R;
import com.example.nhom15_roomfinder.activity.HomeActivity;
import com.example.nhom15_roomfinder.firebase.FirebaseManager;
import com.example.nhom15_roomfinder.utils.ImageUploadHelper;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
    
    private static final String TAG = "ProfileActivity";
    private static final String USERS_COLLECTION = "users";
    
    private FirebaseManager firebaseManager;
    private Uri selectedImageUri;
    private String currentPhotoUrl;
    
    // UI Components - matching with activity_profile.xml
    private ImageButton btnBack;
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
    private Button btnChangePassword;
    private Button btnMyPosts;
    private Button btnAdminDashboard;
    private ProgressBar progressBar;
    
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
        btnBack = findViewById(R.id.btnBack);
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
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnMyPosts = findViewById(R.id.btnMyPosts);
        btnAdminDashboard = findViewById(R.id.btnAdminDashboard);
        progressBar = findViewById(R.id.progressBar);
        
        // Set default profile picture
        imgProfilePicture.setImageResource(R.drawable.ic_profile_placeholder);
        
        // Email field should not be editable (linked to auth)
        etEmail.setEnabled(false);
        etEmail.setAlpha(0.7f);
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
        // Back button
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Profile picture click - change avatar
        imgProfilePicture.setOnClickListener(v -> openImagePicker());
        ivUploadIcon.setOnClickListener(v -> openImagePicker());
        tvUploadChange.setOnClickListener(v -> openImagePicker());
        
        // Save changes button
        btnSaveChanges.setOnClickListener(v -> saveUserProfile());
        
        // Change password button
        if (btnChangePassword != null) {
            btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        }
        
        // My Posts button
        if (btnMyPosts != null) {
            btnMyPosts.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, MyPostsActivity.class);
                startActivity(intent);
            });
        }
        
        // Admin Dashboard button
        if (btnAdminDashboard != null) {
            btnAdminDashboard.setOnClickListener(v -> navigateToAdminDashboard());
        }
        
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
        showLoading(true);
        
        firebaseManager.getFirestore()
            .collection(USERS_COLLECTION)
            .document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                showLoading(false);
                if (documentSnapshot.exists()) {
                    populateUserData(documentSnapshot);
                } else {
                    // No profile exists, create one with auth data
                    createInitialProfile(userId);
                }
            })
            .addOnFailureListener(e -> {
                showLoading(false);
                Log.e(TAG, "Error loading profile: " + e.getMessage());
                showToast("Không thể tải thông tin người dùng");
            });
    }
    
    /**
     * Populate UI with user data from Firestore
     */
    private void populateUserData(DocumentSnapshot document) {
        try {
            // Get data from document
            String name = document.getString("name");
            String phone = document.getString("phone");
            String gender = document.getString("gender");
            String photoUrl = document.getString("photoUrl");
            String role = document.getString("role");
            
            // Populate fields
            if (name != null && !name.isEmpty()) {
                etFullName.setText(name);
            }
            
            if (phone != null && !phone.isEmpty()) {
                etPhoneNumber.setText(phone);
            }
            
            // Set gender spinner
            if (gender != null && !gender.isEmpty()) {
                setGenderSpinner(gender);
            }
            
            // Load profile picture
            if (photoUrl != null && !photoUrl.isEmpty()) {
                currentPhotoUrl = photoUrl;
                loadProfileImage(photoUrl);
            }
            
            // Check and show admin dashboard button if user is admin
            checkAndShowAdminButton(role);
            
            Log.d(TAG, "Profile data loaded successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error parsing profile data: " + e.getMessage());
        }
    }
    
    /**
     * Check if user is admin and show/hide admin dashboard button
     */
    private void checkAndShowAdminButton(String role) {
        if (btnAdminDashboard != null) {
            boolean isAdmin = "admin".equals(role);
            btnAdminDashboard.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
            Log.d(TAG, "User role: " + role + ", Admin button visible: " + isAdmin);
        }
    }
    
    /**
     * Navigate to Admin Dashboard
     */
    private void navigateToAdminDashboard() {
        Intent intent = new Intent(this, com.example.nhom15_roomfinder.AdminDashboardActivity.class);
        startActivity(intent);
    }
    
    /**
     * Set gender spinner selection
     */
    private void setGenderSpinner(String gender) {
        String[] genderOptions = {"Chọn giới tính", "Nam", "Nữ", "Khác"};
        for (int i = 0; i < genderOptions.length; i++) {
            if (genderOptions[i].equals(gender)) {
                spinnerGender.setSelection(i);
                break;
            }
        }
    }
    
    /**
     * Load profile image using Glide
     */
    private void loadProfileImage(String photoUrl) {
        try {
            Glide.with(this)
                .load(photoUrl)
                .placeholder(R.drawable.ic_profile_placeholder)
                .error(R.drawable.ic_profile_placeholder)
                .circleCrop()
                .into(imgProfilePicture);
        } catch (Exception e) {
            Log.e(TAG, "Error loading profile image: " + e.getMessage());
            imgProfilePicture.setImageResource(R.drawable.ic_profile_placeholder);
        }
    }
    
    /**
     * Create initial profile for new users
     */
    private void createInitialProfile(String userId) {
        FirebaseUser user = firebaseManager.getCurrentUser();
        if (user == null) return;
        
        Map<String, Object> initialProfile = new HashMap<>();
        initialProfile.put("userId", userId);
        initialProfile.put("email", user.getEmail());
        initialProfile.put("name", user.getDisplayName() != null ? user.getDisplayName() : "");
        initialProfile.put("phone", "");
        initialProfile.put("gender", "");
        initialProfile.put("photoUrl", user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "");
        initialProfile.put("createdAt", System.currentTimeMillis());
        
        firebaseManager.setDocument(USERS_COLLECTION, userId, initialProfile,
            aVoid -> {
                Log.d(TAG, "Initial profile created");
                if (user.getDisplayName() != null) {
                    etFullName.setText(user.getDisplayName());
                }
                if (user.getPhotoUrl() != null) {
                    loadProfileImage(user.getPhotoUrl().toString());
                }
                // Check admin role for new users (default is customer)
                checkAndShowAdminButton("customer");
            },
            e -> Log.e(TAG, "Error creating initial profile: " + e.getMessage())
        );
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
        showLoading(true);
        btnSaveChanges.setEnabled(false);
        btnSaveChanges.setText("Đang lưu...");
        
        // If new image selected, upload first then save profile
        if (selectedImageUri != null) {
            uploadImageAndSaveProfile(fullName, phoneNumber, gender);
        } else {
            // No new image, just save profile data
            saveProfileToFirestore(fullName, phoneNumber, gender, currentPhotoUrl);
        }
    }
    
    /**
     * Upload image to Cloudinary and then save profile
     */
    private void uploadImageAndSaveProfile(String fullName, String phone, String gender) {
        String userId = firebaseManager.getUserId();
        
        // Sử dụng Cloudinary thay vì Firebase Storage
        ImageUploadHelper.uploadAvatar(this, selectedImageUri, userId,
            new ImageUploadHelper.SingleUploadCallback() {
                @Override
                public void onSuccess(String imageUrl) {
                    currentPhotoUrl = imageUrl;
                    saveProfileToFirestore(fullName, phone, gender, imageUrl);
                }

                @Override
                public void onError(String error) {
                    showLoading(false);
                    resetSaveButton();
                    Log.e(TAG, "Error uploading image: " + error);
                    showToast("Lỗi khi tải ảnh lên. Vui lòng thử lại.");
                }
            }
        );
    }
    
    /**
     * Save profile data to Firestore
     */
    private void saveProfileToFirestore(String fullName, String phone, String gender, String photoUrl) {
        String userId = firebaseManager.getUserId();
        
        Map<String, Object> profileData = new HashMap<>();
        profileData.put("name", fullName);
        profileData.put("phone", phone);
        profileData.put("gender", gender);
        profileData.put("photoUrl", photoUrl != null ? photoUrl : "");
        profileData.put("updatedAt", System.currentTimeMillis());
        
        firebaseManager.updateDocument(USERS_COLLECTION, userId, profileData,
            aVoid -> {
                showLoading(false);
                resetSaveButton();
                selectedImageUri = null; // Clear selected image
                showToast("Đã lưu thông tin thành công!");
                Log.d(TAG, "Profile saved successfully");
            },
            e -> {
                // If update fails (document doesn't exist), try to set
                profileData.put("userId", userId);
                profileData.put("email", firebaseManager.getCurrentUser().getEmail());
                profileData.put("createdAt", System.currentTimeMillis());
                
                firebaseManager.setDocument(USERS_COLLECTION, userId, profileData,
                    aVoid2 -> {
                        showLoading(false);
                        resetSaveButton();
                        selectedImageUri = null;
                        showToast("Đã lưu thông tin thành công!");
                    },
                    e2 -> {
                        showLoading(false);
                        resetSaveButton();
                        Log.e(TAG, "Error saving profile: " + e2.getMessage());
                        showToast("Có lỗi xảy ra khi lưu thông tin");
                    }
                );
            }
        );
    }
    
    /**
     * Show/hide loading indicator
     */
    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
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
     * Show change password dialog
     */
    private void showChangePasswordDialog() {
        FirebaseUser user = firebaseManager.getCurrentUser();
        if (user == null) {
            showToast("Vui lòng đăng nhập lại");
            return;
        }
        
        // Check if user signed in with email/password (not Google)
        boolean isEmailUser = false;
        if (user.getProviderData() != null) {
            for (com.google.firebase.auth.UserInfo profile : user.getProviderData()) {
                if ("password".equals(profile.getProviderId())) {
                    isEmailUser = true;
                    break;
                }
            }
        }
        
        if (!isEmailUser) {
            showToast("Tài khoản đăng nhập bằng Google không thể đổi mật khẩu tại đây");
            return;
        }
        
        // Create dialog layout
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 30, 50, 30);
        
        EditText etCurrentPassword = new EditText(this);
        etCurrentPassword.setHint("Mật khẩu hiện tại");
        etCurrentPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etCurrentPassword);
        
        EditText etNewPassword = new EditText(this);
        etNewPassword.setHint("Mật khẩu mới");
        etNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = 20;
        etNewPassword.setLayoutParams(params);
        layout.addView(etNewPassword);
        
        EditText etConfirmPassword = new EditText(this);
        etConfirmPassword.setHint("Xác nhận mật khẩu mới");
        etConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        etConfirmPassword.setLayoutParams(params);
        layout.addView(etConfirmPassword);
        
        new AlertDialog.Builder(this)
            .setTitle("Đổi mật khẩu")
            .setView(layout)
            .setPositiveButton("Đổi mật khẩu", (dialog, which) -> {
                String currentPassword = etCurrentPassword.getText().toString().trim();
                String newPassword = etNewPassword.getText().toString().trim();
                String confirmPassword = etConfirmPassword.getText().toString().trim();
                
                if (validatePasswordChange(currentPassword, newPassword, confirmPassword)) {
                    changePassword(currentPassword, newPassword);
                }
            })
            .setNegativeButton("Hủy", null)
            .show();
    }
    
    /**
     * Validate password change inputs
     */
    private boolean validatePasswordChange(String currentPassword, String newPassword, String confirmPassword) {
        if (currentPassword.isEmpty()) {
            showToast("Vui lòng nhập mật khẩu hiện tại");
            return false;
        }
        
        if (newPassword.isEmpty()) {
            showToast("Vui lòng nhập mật khẩu mới");
            return false;
        }
        
        if (newPassword.length() < 6) {
            showToast("Mật khẩu mới phải có ít nhất 6 ký tự");
            return false;
        }
        
        if (!newPassword.equals(confirmPassword)) {
            showToast("Mật khẩu xác nhận không khớp");
            return false;
        }
        
        if (currentPassword.equals(newPassword)) {
            showToast("Mật khẩu mới phải khác mật khẩu hiện tại");
            return false;
        }
        
        return true;
    }
    
    /**
     * Change user password
     */
    private void changePassword(String currentPassword, String newPassword) {
        showLoading(true);
        
        FirebaseUser user = firebaseManager.getCurrentUser();
        if (user == null || user.getEmail() == null) {
            showLoading(false);
            showToast("Có lỗi xảy ra. Vui lòng đăng nhập lại.");
            return;
        }
        
        // Re-authenticate user first
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);
        
        user.reauthenticate(credential)
            .addOnSuccessListener(aVoid -> {
                // Now change password
                user.updatePassword(newPassword)
                    .addOnSuccessListener(aVoid2 -> {
                        showLoading(false);
                        showToast("Đổi mật khẩu thành công!");
                        Log.d(TAG, "Password changed successfully");
                    })
                    .addOnFailureListener(e -> {
                        showLoading(false);
                        Log.e(TAG, "Error changing password: " + e.getMessage());
                        showToast("Lỗi khi đổi mật khẩu: " + e.getMessage());
                    });
            })
            .addOnFailureListener(e -> {
                showLoading(false);
                Log.e(TAG, "Re-authentication failed: " + e.getMessage());
                showToast("Mật khẩu hiện tại không đúng");
            });
    }
    
    /**
     * Show forgot password dialog (send reset email)
     */
    private void showForgotPasswordDialog() {
        FirebaseUser user = firebaseManager.getCurrentUser();
        if (user == null || user.getEmail() == null) {
            showToast("Không tìm thấy email của bạn");
            return;
        }
        
        new AlertDialog.Builder(this)
            .setTitle("Đặt lại mật khẩu")
            .setMessage("Gửi email đặt lại mật khẩu đến " + user.getEmail() + "?")
            .setPositiveButton("Gửi", (dialog, which) -> {
                sendPasswordResetEmail(user.getEmail());
            })
            .setNegativeButton("Hủy", null)
            .show();
    }
    
    /**
     * Send password reset email
     */
    private void sendPasswordResetEmail(String email) {
        showLoading(true);
        
        firebaseManager.sendPasswordResetEmail(email, task -> {
            showLoading(false);
            
            if (task.isSuccessful()) {
                showToast("Email đặt lại mật khẩu đã được gửi!");
            } else {
                String error = task.getException() != null ?
                    task.getException().getMessage() : "Gửi email thất bại";
                showToast("Lỗi: " + error);
            }
        });
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
