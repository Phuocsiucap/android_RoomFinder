package com.example.nhom15_roomfinder.auth;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.example.nhom15_roomfinder.firebase.FirebaseManager;

/**
 * Helper class để xử lý Google Sign-In với Firebase Authentication
 */
public class GoogleSignInHelper {
    
    private static final String TAG = "GoogleSignInHelper";
    public static final int RC_SIGN_IN = 9001;
    
    private Activity activity;
    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth firebaseAuth;
    private GoogleSignInCallback callback;
    
    /**
     * Interface callback cho Google Sign-In result
     */
    public interface GoogleSignInCallback {
        void onSuccess(FirebaseUser user);
        void onFailure(String error);
        void onLoading(boolean isLoading);
    }
    
    /**
     * Constructor
     */
    public GoogleSignInHelper(Activity activity, GoogleSignInCallback callback) {
        this.activity = activity;
        this.callback = callback;
        this.firebaseAuth = FirebaseAuth.getInstance();
        
        configureGoogleSignIn();
    }
    
    /**
     * Cấu hình Google Sign-In options
     */
    private void configureGoogleSignIn() {
        // Lấy Web Client ID từ google-services.json
        String webClientId = "793277639685-ve0hfgsu5l0bgpoh9957v9ui93gajlua.apps.googleusercontent.com";
        
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .requestProfile()
                .build();
        
        googleSignInClient = GoogleSignIn.getClient(activity, gso);
    }
    
    /**
     * Bắt đầu quá trình đăng nhập Google
     */
    public void signIn() {
        if (callback != null) {
            callback.onLoading(true);
        }
        
        Intent signInIntent = googleSignInClient.getSignInIntent();
        activity.startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    
    /**
     * Xử lý kết quả đăng nhập từ onActivityResult
     */
    public void handleSignInResult(Intent data) {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        
        try {
            // Google Sign In thành công, authenticate với Firebase
            GoogleSignInAccount account = task.getResult(ApiException.class);
            Log.d(TAG, "Google sign in successful: " + account.getId());
            
            // Authenticate với Firebase
            firebaseAuthWithGoogle(account.getIdToken());
            
        } catch (ApiException e) {
            // Google Sign In thất bại
            Log.w(TAG, "Google sign in failed", e);
            if (callback != null) {
                callback.onLoading(false);
                callback.onFailure("Đăng nhập Google thất bại: " + e.getMessage());
            }
        }
    }
    
    /**
     * Xác thực với Firebase sử dụng Google ID Token
     */
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(activity, new OnCompleteListener<com.google.firebase.auth.AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<com.google.firebase.auth.AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in thành công
                            Log.d(TAG, "Firebase sign in with Google successful");
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            
                            if (user != null) {
                                // Tạo user profile trong Firestore
                                createUserProfileInFirestore(user);
                            }
                            
                        } else {
                            // Sign in thất bại
                            Log.w(TAG, "Firebase sign in with Google failed", task.getException());
                            if (callback != null) {
                                callback.onLoading(false);
                                String error = task.getException() != null ? 
                                    task.getException().getMessage() : 
                                    "Xác thực Firebase thất bại";
                                callback.onFailure(error);
                            }
                        }
                    }
                });
    }
    
    /**
     * Tạo user profile trong Firestore
     */
    private void createUserProfileInFirestore(FirebaseUser user) {
        FirebaseManager firebaseManager = FirebaseManager.getInstance();
        
        firebaseManager.createGoogleUserProfile(user,
            // Success listener
            aVoid -> {
                Log.d(TAG, "User profile created successfully");
                if (callback != null) {
                    callback.onLoading(false);
                    callback.onSuccess(user);
                }
            },
            // Failure listener
            e -> {
                Log.w(TAG, "Failed to create user profile", e);
                // Vẫn cho phép đăng nhập thành công ngay cả khi không tạo được profile
                if (callback != null) {
                    callback.onLoading(false);
                    callback.onSuccess(user);
                }
            });
    }
    
    /**
     * Đăng xuất khỏi Google và Firebase
     */
    public void signOut() {
        // Firebase sign out
        firebaseAuth.signOut();
        
        // Google sign out
        googleSignInClient.signOut()
                .addOnCompleteListener(activity, task -> {
                    Log.d(TAG, "Google sign out completed");
                });
    }
    
    /**
     * Hủy kết nối Google account
     */
    public void revokeAccess() {
        // Firebase sign out
        firebaseAuth.signOut();
        
        // Google revoke access
        googleSignInClient.revokeAccess()
                .addOnCompleteListener(activity, task -> {
                    Log.d(TAG, "Google revoke access completed");
                });
    }
    
    /**
     * Kiểm tra xem user đã đăng nhập chưa
     */
    public boolean isSignedIn() {
        return firebaseAuth.getCurrentUser() != null;
    }
    
    /**
     * Lấy thông tin user hiện tại từ Firebase
     */
    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }
    
    /**
     * Cleanup khi Activity bị destroy
     */
    public void cleanup() {
        this.activity = null;
        this.callback = null;
    }
}