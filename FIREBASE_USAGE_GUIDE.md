# Hướng Dẫn Sử Dụng Firebase Trong RoomFinder App

## 📋 Mục Lục
1. [Giới Thiệu](#giới-thiệu)
2. [Cấu Hình Firebase](#cấu-hình-firebase)
3. [Cấu Trúc Firebase](#cấu-trúc-firebase)
4. [Hướng Dẫn Tích Hợp Login](#hướng-dẫn-tích-hợp-login)
5. [Các Tính Năng Firebase](#các-tính-năng-firebase)
6. [Ví Dụ Sử Dụng](#ví-dụ-sử-dụng)

---

## 🔥 Giới Thiệu

Project RoomFinder đã được tích hợp đầy đủ các dịch vụ Firebase:
- **Firebase Authentication**: Đăng nhập/Đăng ký người dùng
- **Cloud Firestore**: Cơ sở dữ liệu NoSQL để lưu trữ thông tin phòng, người dùng
- **Realtime Database**: Chat real-time giữa người dùng
- **Cloud Storage**: Lưu trữ hình ảnh phòng trọ
- **Firebase Analytics**: Theo dõi hành vi người dùng

---

## ⚙️ Cấu Hình Firebase

### 1. File Đã Được Cấu Hình

✅ **build.gradle.kts (Project level)**
```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    id("com.google.gms.google-services") version "4.4.4" apply false
}
```

✅ **app/build.gradle.kts**
```kotlin
plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

dependencies {
    // Firebase BOM
    implementation(platform("com.google.firebase:firebase-bom:34.5.0"))
    
    // Firebase Services
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-messaging")
    
    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:21.0.0")
}
```

✅ **google-services.json**
- File này đã có sẵn tại: `app/google-services.json`
- Chứa cấu hình kết nối với Firebase project

### 2. Permissions (AndroidManifest.xml)

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

---

## 📁 Cấu Trúc Firebase

### Classes Đã Được Tạo

```
firebase/
├── FirebaseManager.java          # Quản lý tất cả Firebase services
├── FirebaseCallback.java         # Interface cho callbacks
├── RoomFirebaseHelper.java       # Helper cho quản lý phòng trọ
└── ChatFirebaseHelper.java       # Helper cho chat real-time
```

---

## 🔐 Hướng Dẫn Tích Hợp Login

### Bước 1: Tạo LoginActivity

```java
package com.example.nhom15_roomfinder.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nhom15_roomfinder.HomeActivity;
import com.example.nhom15_roomfinder.R;
import com.example.nhom15_roomfinder.firebase.FirebaseManager;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    
    // UI Components
    private EditText etEmail, etPassword;
    private Button btnLogin, btnGoogleSignIn;
    private TextView tvRegister, tvForgotPassword;
    private ProgressBar progressBar;
    
    // Firebase
    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        // Initialize Firebase
        firebaseManager = FirebaseManager.getInstance();
        
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
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        tvRegister = findViewById(R.id.tvRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        progressBar = findViewById(R.id.progressBar);
    }
    
    private void setListeners() {
        btnLogin.setOnClickListener(v -> loginUser());
        tvRegister.setOnClickListener(v -> navigateToRegister());
        tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
        btnGoogleSignIn.setOnClickListener(v -> signInWithGoogle());
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
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!isLoading);
        btnGoogleSignIn.setEnabled(!isLoading);
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
     * Đăng nhập với Google
     */
    private void signInWithGoogle() {
        Toast.makeText(this, 
            "Tính năng đăng nhập Google sẽ được triển khai sau", 
            Toast.LENGTH_SHORT).show();
        // TODO: Implement Google Sign-In
    }
}
```

### Bước 2: Tạo RegisterActivity

```java
package com.example.nhom15_roomfinder.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nhom15_roomfinder.R;
import com.example.nhom15_roomfinder.firebase.FirebaseManager;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    
    // UI Components
    private EditText etName, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvLogin;
    private ProgressBar progressBar;
    
    // Firebase
    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        
        // Initialize Firebase
        firebaseManager = FirebaseManager.getInstance();
        
        // Initialize Views
        initializeViews();
        
        // Set Listeners
        setListeners();
    }
    
    private void initializeViews() {
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
        progressBar = findViewById(R.id.progressBar);
    }
    
    private void setListeners() {
        btnRegister.setOnClickListener(v -> registerUser());
        tvLogin.setOnClickListener(v -> finish());
    }
    
    /**
     * Đăng ký người dùng mới
     */
    private void registerUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        
        // Validate input
        if (!validateInput(name, email, password, confirmPassword)) {
            return;
        }
        
        // Show loading
        showLoading(true);
        
        // Register with Firebase
        firebaseManager.registerUser(email, password, task -> {
            if (task.isSuccessful()) {
                // Registration successful
                FirebaseUser user = firebaseManager.getCurrentUser();
                Log.d(TAG, "Registration successful: " + user.getUid());
                
                // Create user profile in Firestore
                createUserProfile(user.getUid(), email, name);
            } else {
                // Registration failed
                showLoading(false);
                String error = task.getException() != null ? 
                    task.getException().getMessage() : "Đăng ký thất bại";
                Log.e(TAG, "Registration failed: " + error);
                Toast.makeText(this, "Lỗi: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }
    
    /**
     * Tạo profile người dùng trong Firestore
     */
    private void createUserProfile(String userId, String email, String name) {
        firebaseManager.createUserProfile(userId, email, name,
            aVoid -> {
                showLoading(false);
                Log.d(TAG, "User profile created successfully");
                Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                
                // Navigate to login
                finish();
            },
            e -> {
                showLoading(false);
                Log.e(TAG, "Error creating user profile: " + e.getMessage());
                Toast.makeText(this, 
                    "Đăng ký thành công nhưng lỗi tạo profile", 
                    Toast.LENGTH_SHORT).show();
                finish();
            });
    }
    
    /**
     * Validate input
     */
    private boolean validateInput(String name, String email, String password, String confirmPassword) {
        if (name.isEmpty()) {
            etName.setError("Vui lòng nhập tên");
            etName.requestFocus();
            return false;
        }
        
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
        
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Mật khẩu không khớp");
            etConfirmPassword.requestFocus();
            return false;
        }
        
        return true;
    }
    
    /**
     * Hiển thị/Ẩn loading
     */
    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!isLoading);
    }
}
```

### Bước 3: Tạo Layout cho Login (activity_login.xml)

```xml
<?xml version="1.0" encoding="utf-8"?>
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:fillViewport="true"
    android:background="@android:color/white">

    <androidx.constraintlayout.widget.ConstraintLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:padding="24dp">

        <!-- Logo -->
        <ImageView
            android:id="@+id/ivLogo"
            android:layout_width="120dp"
            android:layout_height="120dp"
            android:layout_marginTop="40dp"
            android:src="@mipmap/ic_launcher"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintTop_toTopOf="parent" />

        <!-- Title -->
        <TextView
            android:id="@+id/tvTitle"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="16dp"
            android:text="RoomFinder"
            android:textSize="28sp"
            android:textStyle="bold"
            android:textColor="@android:color/black"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintTop_toBottomOf="@id/ivLogo" />

        <TextView
            android:id="@+id/tvSubtitle"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="8dp"
            android:text="Đăng nhập để tiếp tục"
            android:textSize="16sp"
            android:textColor="@android:color/darker_gray"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintTop_toBottomOf="@id/tvTitle" />

        <!-- Email Input -->
        <com.google.android.material.textfield.TextInputLayout
            android:id="@+id/tilEmail"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="40dp"
            android:hint="Email"
            app:layout_constraintTop_toBottomOf="@id/tvSubtitle">

            <com.google.android.material.textfield.TextInputEditText
                android:id="@+id/etEmail"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:inputType="textEmailAddress" />
        </com.google.android.material.textfield.TextInputLayout>

        <!-- Password Input -->
        <com.google.android.material.textfield.TextInputLayout
            android:id="@+id/tilPassword"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="16dp"
            android:hint="Mật khẩu"
            app:passwordToggleEnabled="true"
            app:layout_constraintTop_toBottomOf="@id/tilEmail">

            <com.google.android.material.textfield.TextInputEditText
                android:id="@+id/etPassword"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:inputType="textPassword" />
        </com.google.android.material.textfield.TextInputLayout>

        <!-- Forgot Password -->
        <TextView
            android:id="@+id/tvForgotPassword"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="8dp"
            android:text="Quên mật khẩu?"
            android:textColor="@color/colorPrimary"
            android:textStyle="bold"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintTop_toBottomOf="@id/tilPassword" />

        <!-- Login Button -->
        <Button
            android:id="@+id/btnLogin"
            android:layout_width="match_parent"
            android:layout_height="56dp"
            android:layout_marginTop="24dp"
            android:text="Đăng Nhập"
            android:textSize="16sp"
            android:textStyle="bold"
            app:layout_constraintTop_toBottomOf="@id/tvForgotPassword" />

        <!-- Divider -->
        <View
            android:id="@+id/divider"
            android:layout_width="match_parent"
            android:layout_height="1dp"
            android:layout_marginTop="24dp"
            android:background="@android:color/darker_gray"
            app:layout_constraintTop_toBottomOf="@id/btnLogin" />

        <TextView
            android:id="@+id/tvOr"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="16dp"
            android:text="HOẶC"
            android:textColor="@android:color/darker_gray"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintTop_toBottomOf="@id/divider" />

        <!-- Google Sign In Button -->
        <Button
            android:id="@+id/btnGoogleSignIn"
            android:layout_width="match_parent"
            android:layout_height="56dp"
            android:layout_marginTop="16dp"
            android:text="Đăng nhập với Google"
            android:textSize="16sp"
            app:layout_constraintTop_toBottomOf="@id/tvOr" />

        <!-- Register Link -->
        <TextView
            android:id="@+id/tvRegister"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="24dp"
            android:text="Chưa có tài khoản? Đăng ký ngay"
            android:textColor="@color/colorPrimary"
            android:textStyle="bold"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintTop_toBottomOf="@id/btnGoogleSignIn" />

        <!-- Progress Bar -->
        <ProgressBar
            android:id="@+id/progressBar"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:visibility="gone"
            app:layout_constraintBottom_toBottomOf="parent"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintTop_toTopOf="parent" />

    </androidx.constraintlayout.widget.ConstraintLayout>
</ScrollView>
```

### Bước 4: Tạo Layout cho Register (activity_register.xml)

```xml
<?xml version="1.0" encoding="utf-8"?>
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:fillViewport="true"
    android:background="@android:color/white">

    <androidx.constraintlayout.widget.ConstraintLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:padding="24dp">

        <!-- Title -->
        <TextView
            android:id="@+id/tvTitle"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="40dp"
            android:text="Đăng Ký Tài Khoản"
            android:textSize="28sp"
            android:textStyle="bold"
            android:textColor="@android:color/black"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintTop_toTopOf="parent" />

        <!-- Name Input -->
        <com.google.android.material.textfield.TextInputLayout
            android:id="@+id/tilName"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="40dp"
            android:hint="Họ và tên"
            app:layout_constraintTop_toBottomOf="@id/tvTitle">

            <com.google.android.material.textfield.TextInputEditText
                android:id="@+id/etName"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:inputType="textPersonName" />
        </com.google.android.material.textfield.TextInputLayout>

        <!-- Email Input -->
        <com.google.android.material.textfield.TextInputLayout
            android:id="@+id/tilEmail"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="16dp"
            android:hint="Email"
            app:layout_constraintTop_toBottomOf="@id/tilName">

            <com.google.android.material.textfield.TextInputEditText
                android:id="@+id/etEmail"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:inputType="textEmailAddress" />
        </com.google.android.material.textfield.TextInputLayout>

        <!-- Password Input -->
        <com.google.android.material.textfield.TextInputLayout
            android:id="@+id/tilPassword"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="16dp"
            android:hint="Mật khẩu"
            app:passwordToggleEnabled="true"
            app:layout_constraintTop_toBottomOf="@id/tilEmail">

            <com.google.android.material.textfield.TextInputEditText
                android:id="@+id/etPassword"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:inputType="textPassword" />
        </com.google.android.material.textfield.TextInputLayout>

        <!-- Confirm Password Input -->
        <com.google.android.material.textfield.TextInputLayout
            android:id="@+id/tilConfirmPassword"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="16dp"
            android:hint="Xác nhận mật khẩu"
            app:passwordToggleEnabled="true"
            app:layout_constraintTop_toBottomOf="@id/tilPassword">

            <com.google.android.material.textfield.TextInputEditText
                android:id="@+id/etConfirmPassword"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:inputType="textPassword" />
        </com.google.android.material.textfield.TextInputLayout>

        <!-- Register Button -->
        <Button
            android:id="@+id/btnRegister"
            android:layout_width="match_parent"
            android:layout_height="56dp"
            android:layout_marginTop="32dp"
            android:text="Đăng Ký"
            android:textSize="16sp"
            android:textStyle="bold"
            app:layout_constraintTop_toBottomOf="@id/tilConfirmPassword" />

        <!-- Login Link -->
        <TextView
            android:id="@+id/tvLogin"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="24dp"
            android:text="Đã có tài khoản? Đăng nhập"
            android:textColor="@color/colorPrimary"
            android:textStyle="bold"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintTop_toBottomOf="@id/btnRegister" />

        <!-- Progress Bar -->
        <ProgressBar
            android:id="@+id/progressBar"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:visibility="gone"
            app:layout_constraintBottom_toBottomOf="parent"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintTop_toTopOf="parent" />

    </androidx.constraintlayout.widget.ConstraintLayout>
</ScrollView>
```

### Bước 5: Cập nhật AndroidManifest.xml

```xml
<application ...>
    <!-- Login Activity as Launcher -->
    <activity
        android:name=".activity.LoginActivity"
        android:exported="true">
        <intent-filter>
            <action android:name="android.intent.action.MAIN" />
            <category android:name="android.intent.category.LAUNCHER" />
        </intent-filter>
    </activity>
    
    <!-- Register Activity -->
    <activity
        android:name=".activity.RegisterActivity"
        android:exported="false" />
    
    <!-- Main Activity -->
    <activity
        android:name=".MainActivity"
        android:exported="false" />
    
    <!-- Home Activity -->
    <activity
        android:name=".activity.HomeActivity"
        android:exported="false" />
</application>
```

---

## 🚀 Các Tính Năng Firebase

### 1. Authentication (Đăng nhập/Đăng ký)

```java
// Đăng ký người dùng mới
firebaseManager.registerUser(email, password, task -> {
    if (task.isSuccessful()) {
        FirebaseUser user = firebaseManager.getCurrentUser();
        // Xử lý thành công
    }
});

// Đăng nhập
firebaseManager.signInUser(email, password, task -> {
    if (task.isSuccessful()) {
        // Đăng nhập thành công
    }
});

// Đăng xuất
firebaseManager.signOut();

// Kiểm tra user đã đăng nhập chưa
boolean isLoggedIn = firebaseManager.isUserLoggedIn();

// Lấy user ID hiện tại
String userId = firebaseManager.getUserId();
```

### 2. Firestore (Lưu trữ dữ liệu)

```java
RoomFirebaseHelper roomHelper = new RoomFirebaseHelper();

// Thêm phòng mới
roomHelper.addRoom(title, description, location, price, userId, imageUri,
    new FirebaseCallback<String>() {
        @Override
        public void onSuccess(String roomId) {
            // Thêm phòng thành công
        }
        
        @Override
        public void onFailure(String error) {
            // Xử lý lỗi
        }
    });

// Lấy tất cả phòng
roomHelper.getAllRooms(new FirebaseCallback<List<Map<String, Object>>>() {
    @Override
    public void onSuccess(List<Map<String, Object>> rooms) {
        // Hiển thị danh sách phòng
    }
    
    @Override
    public void onFailure(String error) {
        // Xử lý lỗi
    }
});

// Tìm phòng theo địa điểm
roomHelper.searchRoomsByLocation("Hà Nội", callback);

// Tìm phòng theo giá
roomHelper.searchRoomsByPriceRange(1000000, 5000000, callback);

// Thêm vào yêu thích
roomHelper.addToFavorites(userId, roomId, callback);
```

### 3. Realtime Database (Chat)

```java
ChatFirebaseHelper chatHelper = new ChatFirebaseHelper();

// Tạo chat mới
chatHelper.createChat(user1Id, user1Name, user2Id, user2Name,
    new FirebaseCallback<String>() {
        @Override
        public void onSuccess(String chatId) {
            // Chat được tạo thành công
        }
        
        @Override
        public void onFailure(String error) {
            // Xử lý lỗi
        }
    });

// Gửi tin nhắn
chatHelper.sendMessage(chatId, senderId, senderName, message, callback);

// Lắng nghe tin nhắn mới
chatHelper.listenForMessages(chatId, new ChatFirebaseHelper.MessageListener() {
    @Override
    public void onMessagesReceived(List<Map<String, Object>> messages) {
        // Hiển thị tin nhắn
    }
    
    @Override
    public void onError(String error) {
        // Xử lý lỗi
    }
});

// Đánh dấu đã đọc
chatHelper.markMessagesAsRead(chatId, userId);
```

### 4. Cloud Storage (Upload ảnh)

```java
// Upload ảnh phòng
String storagePath = "rooms/" + roomId + "/image.jpg";
firebaseManager.uploadImageAndGetUrl(imageUri, storagePath,
    downloadUri -> {
        // Lưu URL vào Firestore
        String imageUrl = downloadUri.toString();
    },
    e -> {
        // Xử lý lỗi
    });

// Xóa ảnh
firebaseManager.deleteImage(storagePath, successListener, failureListener);
```

---

## 📝 Ví Dụ Sử Dụng

### Ví dụ 1: Đăng nhập và lưu thông tin user

```java
FirebaseManager firebaseManager = FirebaseManager.getInstance();

firebaseManager.signInUser("user@example.com", "password123", task -> {
    if (task.isSuccessful()) {
        String userId = firebaseManager.getUserId();
        
        // Lấy thông tin user từ Firestore
        firebaseManager.getFirestore()
            .collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                String name = documentSnapshot.getString("name");
                String email = documentSnapshot.getString("email");
                // Sử dụng thông tin user
            });
    }
});
```

### Ví dụ 2: Đăng phòng mới với ảnh

```java
RoomFirebaseHelper roomHelper = new RoomFirebaseHelper();

Uri imageUri = // URI của ảnh được chọn
String userId = firebaseManager.getUserId();

roomHelper.addRoom(
    "Phòng trọ giá rẻ",
    "Phòng đẹp, đầy đủ tiện nghi",
    "Hà Nội",
    2000000,
    userId,
    imageUri,
    new FirebaseCallback<String>() {
        @Override
        public void onSuccess(String roomId) {
            Toast.makeText(context, "Đăng phòng thành công!", Toast.LENGTH_SHORT).show();
        }
        
        @Override
        public void onFailure(String error) {
            Toast.makeText(context, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
        }
    });
```

### Ví dụ 3: Chat real-time

```java
ChatFirebaseHelper chatHelper = new ChatFirebaseHelper();

// Tạo hoặc mở chat
chatHelper.createChat(currentUserId, currentUserName, otherUserId, otherUserName,
    new FirebaseCallback<String>() {
        @Override
        public void onSuccess(String chatId) {
            // Lắng nghe tin nhắn
            chatHelper.listenForMessages(chatId, new ChatFirebaseHelper.MessageListener() {
                @Override
                public void onMessagesReceived(List<Map<String, Object>> messages) {
                    // Update RecyclerView với tin nhắn mới
                    messageAdapter.updateMessages(messages);
                }
                
                @Override
                public void onError(String error) {
                    Log.e(TAG, "Error: " + error);
                }
            });
        }
        
        @Override
        public void onFailure(String error) {
            Toast.makeText(context, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
        }
    });

// Gửi tin nhắn
btnSend.setOnClickListener(v -> {
    String message = etMessage.getText().toString();
    chatHelper.sendMessage(chatId, currentUserId, currentUserName, message, callback);
});
```

---

## 🔒 Bảo Mật

### Firestore Security Rules

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users collection
    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Rooms collection
    match /rooms/{roomId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null;
      allow update, delete: if request.auth != null && 
        request.auth.uid == resource.data.userId;
    }
    
    // Favorites collection
    match /favorites/{favoriteId} {
      allow read, write: if request.auth != null;
    }
  }
}
```

### Realtime Database Security Rules

```json
{
  "rules": {
    "chats": {
      "$chatId": {
        ".read": "auth != null",
        ".write": "auth != null"
      }
    }
  }
}
```

### Storage Security Rules

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /rooms/{roomId}/{allPaths=**} {
      allow read: if request.auth != null;
      allow write: if request.auth != null;
    }
  }
}
```

---

## 📊 Database Structure

### Firestore Collections

```
firestore/
├── users/
│   └── {userId}/
│       ├── userId: string
│       ├── email: string
│       ├── name: string
│       └── createdAt: timestamp
│
├── rooms/
│   └── {roomId}/
│       ├── title: string
│       ├── description: string
│       ├── location: string
│       ├── price: number
│       ├── userId: string
│       ├── imageUrl: string
│       ├── status: string
│       └── createdAt: timestamp
│
└── favorites/
    └── {userId}_{roomId}/
        ├── userId: string
        ├── roomId: string
        └── createdAt: timestamp
```

### Realtime Database Structure

```
database/
└── chats/
    └── {chatId}/
        ├── chatId: string
        ├── user1Id: string
        ├── user1Name: string
        ├── user2Id: string
        ├── user2Name: string
        ├── createdAt: timestamp
        ├── lastMessage: string
        ├── lastMessageTime: timestamp
        └── messages/
            └── {messageId}/
                ├── senderId: string
                ├── senderName: string
                ├── message: string
                ├── timestamp: timestamp
                └── read: boolean
```

---

## ⚠️ Lưu Ý

1. **google-services.json**: File này chứa cấu hình Firebase của bạn. Không chia sẻ file này công khai.

2. **Gradle Sync**: Sau khi thêm dependencies, nhớ sync Gradle:
   ```
   File → Sync Project with Gradle Files
   ```

3. **Internet Permission**: App cần quyền Internet để kết nối Firebase.

4. **Xử lý lỗi**: Luôn xử lý trường hợp lỗi khi làm việc với Firebase.

5. **Offline Support**: Firestore hỗ trợ offline. Dữ liệu được cache và sync khi online.

6. **Security Rules**: Nhớ cấu hình Security Rules trên Firebase Console để bảo vệ dữ liệu.

---

## 🆘 Troubleshooting

### Lỗi: "FirebaseApp is not initialized"
**Giải pháp**: Thêm `FirebaseApp.initializeApp(this)` trong `onCreate()` của Activity đầu tiên.

### Lỗi: "PERMISSION_DENIED"
**Giải pháp**: Kiểm tra Security Rules trên Firebase Console.

### Lỗi: "Failed to get document"
**Giải pháp**: Kiểm tra kết nối Internet và đảm bảo user đã đăng nhập.

### Lỗi: "com.google.android.gms.common.api.ApiException"
**Giải pháp**: Kiểm tra file `google-services.json` và package name trong `build.gradle`.

---

## 📚 Tài Liệu Tham Khảo

- [Firebase Documentation](https://firebase.google.com/docs)
- [Firebase Authentication](https://firebase.google.com/docs/auth)
- [Cloud Firestore](https://firebase.google.com/docs/firestore)
- [Realtime Database](https://firebase.google.com/docs/database)
- [Cloud Storage](https://firebase.google.com/docs/storage)

---

## 👥 Hỗ Trợ

Nếu gặp vấn đề, vui lòng liên hệ team phát triển hoặc tạo issue trên GitHub.

**Happy Coding! 🚀**
