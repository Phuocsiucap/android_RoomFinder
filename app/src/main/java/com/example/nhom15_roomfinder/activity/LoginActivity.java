package com.example.nhom15_roomfinder.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nhom15_roomfinder.R;
import com.example.nhom15_roomfinder.activity.HomeActivity;
import com.example.nhom15_roomfinder.auth.GoogleSignInHelper;
import com.example.nhom15_roomfinder.firebase.FirebaseManager;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    // UI Components
    private EditText etEmail, etPassword;
    private Button btnLogin, btnGoogleLogin, btnFacebookLogin;
    private Button btnLoginTab, btnRegisterTab;
    private TextView tvForgotPassword;
    private ImageView ivTogglePassword;

    // Firebase
    private FirebaseManager firebaseManager;
    
    // Google Sign-In
    private GoogleSignInHelper googleSignInHelper;

    // Password visibility flag
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase
        firebaseManager = FirebaseManager.getInstance();
        
        // Initialize Google Sign-In
        initializeGoogleSignIn();

        // Check if user is already logged in
        if (firebaseManager.isUserLoggedIn()) {
            navigateToHome();
            return;
        }

        // Initialize Views
        initializeViews();

        // Set Listeners
        setListeners();
    }

    private void initializeViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
        btnFacebookLogin = findViewById(R.id.btnFacebookLogin);
        btnLoginTab = findViewById(R.id.btnLoginTab);
        btnRegisterTab = findViewById(R.id.btnRegisterTab);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        ivTogglePassword = findViewById(R.id.ivTogglePassword);
    }

    private void setListeners() {
        btnLogin.setOnClickListener(v -> loginUser());
        btnRegisterTab.setOnClickListener(v -> navigateToRegister());
        tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
        btnGoogleLogin.setOnClickListener(v -> signInWithGoogle());
        btnFacebookLogin.setOnClickListener(v -> signInWithFacebook());
        ivTogglePassword.setOnClickListener(v -> togglePasswordVisibility());
    }

    /**
     * Toggle password visibility
     */
    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            ivTogglePassword.setImageResource(R.drawable.ic_eye);
        } else {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            ivTogglePassword.setImageResource(R.drawable.ic_eye_off);
        }
        isPasswordVisible = !isPasswordVisible;
        etPassword.setSelection(etPassword.getText().length());
    }

    /**
     * Đăng nhập với Email và Password
     */
    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validate input
        if (!validateInput(email, password)) {
            return;
        }

        // Show loading
        showLoading(true);

        // Sign in with Firebase
        firebaseManager.signInUser(email, password, task -> {
            showLoading(false);

            if (task.isSuccessful()) {
                // Login successful
                FirebaseUser user = firebaseManager.getCurrentUser();
                Log.d(TAG, "Login successful: " + user.getUid());
                Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                navigateToHome();
            } else {
                // Login failed
                String error = task.getException() != null ?
                        task.getException().getMessage() : "Đăng nhập thất bại";
                Log.e(TAG, "Login failed: " + error);
                Toast.makeText(this, "Lỗi: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Validate email và password
     */
    private boolean validateInput(String email, String password) {
        if (email.isEmpty()) {
            etEmail.setError("Vui lòng nhập email");
            etEmail.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email không hợp lệ");
            etEmail.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            etPassword.setError("Vui lòng nhập mật khẩu");
            etPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            etPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            etPassword.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Hiển thị/Ẩn loading
     */
    private void showLoading(boolean isLoading) {
        btnLogin.setEnabled(!isLoading);
        btnGoogleLogin.setEnabled(!isLoading);
        btnFacebookLogin.setEnabled(!isLoading);
        etEmail.setEnabled(!isLoading);
        etPassword.setEnabled(!isLoading);

        if (isLoading) {
            btnLogin.setText("Đang xử lý...");
        } else {
            btnLogin.setText("Login");
        }
    }

    /**
     * Chuyển đến màn hình đăng ký
     */
    private void navigateToRegister() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    /**
     * Chuyển đến màn hình Home
     */
    private void navigateToHome() {
        // TODO: Thay HomeActivity bằng activity home thực tế của bạn
//        Toast.makeText(this, "Chuyển đến Home (chưa có HomeActivity)", Toast.LENGTH_SHORT).show();

        // Uncomment khi đã có HomeActivity
         Intent intent = new Intent(this, HomeActivity.class);
         intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
         startActivity(intent);
         finish();
    }

    /**
     * Hiển thị dialog quên mật khẩu
     */
    private void showForgotPasswordDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Quên mật khẩu");

        final EditText input = new EditText(this);
        input.setHint("Nhập email của bạn");
        input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        input.setPadding(50, 30, 50, 30);
        builder.setView(input);

        builder.setPositiveButton("Gửi", (dialog, which) -> {
            String email = input.getText().toString().trim();
            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            resetPassword(email);
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    /**
     * Gửi email reset password
     */
    private void resetPassword(String email) {
        showLoading(true);

        firebaseManager.sendPasswordResetEmail(email, task -> {
            showLoading(false);

            if (task.isSuccessful()) {
                Toast.makeText(this,
                        "Email đặt lại mật khẩu đã được gửi!",
                        Toast.LENGTH_LONG).show();
            } else {
                String error = task.getException() != null ?
                        task.getException().getMessage() : "Gửi email thất bại";
                Toast.makeText(this, "Lỗi: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Initialize Google Sign-In Helper
     */
    private void initializeGoogleSignIn() {
        googleSignInHelper = new GoogleSignInHelper(this, new GoogleSignInHelper.GoogleSignInCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                Log.d(TAG, "Google Sign-In successful: " + user.getUid());
                String displayName = user.getDisplayName() != null ? user.getDisplayName() : "User";
                Toast.makeText(LoginActivity.this, 
                    "Chào mừng " + displayName + "!", 
                    Toast.LENGTH_SHORT).show();
                navigateToHome();
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Google Sign-In failed: " + error);
                Toast.makeText(LoginActivity.this, 
                    "Đăng nhập Google thất bại: " + error, 
                    Toast.LENGTH_LONG).show();
            }

            @Override
            public void onLoading(boolean isLoading) {
                showLoading(isLoading);
            }
        });
    }

    /**
     * Đăng nhập với Google
     */
    private void signInWithGoogle() {
        if (googleSignInHelper != null) {
            googleSignInHelper.signIn();
        }
    }

    /**
     * Xử lý kết quả từ Google Sign-In
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        // Handle Google Sign-In result
        if (requestCode == GoogleSignInHelper.RC_SIGN_IN) {
            if (googleSignInHelper != null) {
                googleSignInHelper.handleSignInResult(data);
            }
        }
    }
    
    /**
     * Cleanup khi Activity bị destroy
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (googleSignInHelper != null) {
            googleSignInHelper.cleanup();
        }
    }

    /**
     * Đăng nhập với Facebook
     */
    private void signInWithFacebook() {
        Toast.makeText(this,
                "Tính năng đăng nhập Facebook sẽ được triển khai sau",
                Toast.LENGTH_SHORT).show();
        // TODO: Implement Facebook Sign-In
    }
}