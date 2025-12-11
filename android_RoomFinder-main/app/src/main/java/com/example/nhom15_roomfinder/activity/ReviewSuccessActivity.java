package com.example.nhom15_roomfinder.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.example.nhom15_roomfinder.MainActivity;
import com.example.nhom15_roomfinder.R;

public class ReviewSuccessActivity extends AppCompatActivity {

    // Views
    private ImageView successIcon;
    private TextView titleText, messageText, viewReviewsButton;
    private Button backToHomeButton;

    // Data
    private String propertyId;
    private String propertyName;
    private boolean autoNavigateAfterDelay = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_success);

        initViews();
        setupListeners();
        setupBackPressedHandler();
        loadIntentData();
        setupFallbackResources();

        // Auto navigate nếu được cấu hình
        if (autoNavigateAfterDelay) {
            setupAutoNavigate();
        }
    }

    private void initViews() {
        successIcon = findViewById(R.id.success_icon);
        titleText = findViewById(R.id.title_text);
        messageText = findViewById(R.id.message_text);
        backToHomeButton = findViewById(R.id.back_to_home_button);
        viewReviewsButton = findViewById(R.id.view_reviews_button);
    }

    private void setupListeners() {
        // Back to home button
        backToHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToHome();
            }
        });

        // View reviews button - tạm thời ẩn hoặc điều hướng về home
        viewReviewsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Tạm thời điều hướng về home vì MyReviewsActivity chưa tồn tại
                // navigateToMyReviews();
                navigateToHome();
                Toast.makeText(ReviewSuccessActivity.this, "Tính năng đang phát triển", Toast.LENGTH_SHORT).show();
            }
        });

        // Optional: Click on success icon to show celebration
        successIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCelebration();
            }
        });
    }

    private void setupBackPressedHandler() {
        // Sử dụng OnBackPressedDispatcher thay vì override onBackPressed
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBackPressed();
            }
        });
    }

    private void handleBackPressed() {
        navigateToHome();
    }

    private void loadIntentData() {
        // Load data từ Intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            propertyId = extras.getString("property_id");
            propertyName = extras.getString("property_name");
            autoNavigateAfterDelay = extras.getBoolean("auto_navigate", false);

            int successType = extras.getInt("success_type", SuccessType.REVIEW_SUBMITTED);
            boolean showViewReviews = extras.getBoolean("show_view_reviews", true);

            // Customize UI dựa trên success type
            customizeUI(successType);

            // Ẩn nút view reviews nếu cần
            if (!showViewReviews) {
                viewReviewsButton.setVisibility(View.GONE);
            }

            // Customize message nếu có property name
            if (propertyName != null && !propertyName.isEmpty()) {
                String customizedMessage = SuccessType.getMessage(successType, propertyName);
                messageText.setText(customizedMessage);
            }
        }
    }

    private void customizeUI(int successType) {
        String title = SuccessType.getTitle(successType);
        titleText.setText(title);

        // Có thể custom thêm màu sắc, icon dựa trên type
        switch (successType) {
            case SuccessType.REVIEW_UPDATED:
                // Có thể đổi màu icon nếu muốn
                break;
            case SuccessType.REVIEW_DELETED:
                // Có thể đổi icon nếu muốn
                break;
        }
    }

    private void setupFallbackResources() {
        // Fallback cho icon nếu drawable chưa tồn tại
        if (successIcon.getDrawable() == null) {
            // Sử dụng system icon hoặc tạo programmatically
            successIcon.setImageResource(android.R.drawable.ic_menu_upload);
            successIcon.setColorFilter(Color.parseColor("#4CAF50")); // Green color
        }

        // Fallback cho button background color
        if (backToHomeButton.getBackgroundTintList() == null) {
            backToHomeButton.setBackgroundColor(Color.parseColor("#2196F3")); // Blue color
        }

        // Fallback cho text color
        if (viewReviewsButton.getCurrentTextColor() == getResources().getColor(android.R.color.black)) {
            viewReviewsButton.setTextColor(Color.parseColor("#2196F3")); // Blue color
        }
    }

    private void setupAutoNavigate() {
        // Tự động điều hướng sau 3 giây
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                navigateToHome();
            }
        }, 3000);
    }

    private void navigateToHome() {
        // Tạo Intent với flags để clear back stack
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();

        // Hiệu ứng transition (tuỳ chọn)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void navigateToMyReviews() {
        // Tạm thời comment vì MyReviewsActivity chưa tồn tại
        /*
        // Đi đến màn hình danh sách review của user
        Intent intent = new Intent(this, MyReviewsActivity.class);

        // Truyền propertyId nếu có để highlight review vừa tạo
        if (propertyId != null) {
            intent.putExtra("highlight_property_id", propertyId);
        }

        startActivity(intent);
        */

        // Tạm thời điều hướng về home
        navigateToHome();
    }

    private void showCelebration() {
        // Hiệu ứng celebration đơn giản
        SuccessAnimator.playBounceAnimation(successIcon);
        Toast.makeText(this, "🎉 Cảm ơn bạn đã đánh giá!", Toast.LENGTH_SHORT).show();
    }

    // ==================== INNER CLASSES ====================

    /**
     * Helper class để quản lý success message types
     */
    public static class SuccessType {
        public static final int REVIEW_SUBMITTED = 1;
        public static final int REVIEW_UPDATED = 2;
        public static final int REVIEW_DELETED = 3;

        public static String getTitle(int type) {
            switch (type) {
                case REVIEW_SUBMITTED:
                    return "Đánh giá thành công!";
                case REVIEW_UPDATED:
                    return "Cập nhật thành công!";
                case REVIEW_DELETED:
                    return "Xóa đánh giá thành công!";
                default:
                    return "Thành công!";
            }
        }

        public static String getMessage(int type, String propertyName) {
            String baseMessage;
            switch (type) {
                case REVIEW_SUBMITTED:
                    baseMessage = "Cảm ơn bạn đã đánh giá";
                    break;
                case REVIEW_UPDATED:
                    baseMessage = "Đánh giá của bạn đã được cập nhật";
                    break;
                case REVIEW_DELETED:
                    baseMessage = "Đánh giá đã được xóa";
                    break;
                default:
                    baseMessage = "Thao tác thành công";
            }

            if (propertyName != null && !propertyName.isEmpty()) {
                return baseMessage + " " + propertyName + ". Đánh giá của bạn đã được ghi nhận và sẽ giúp ích cho cộng đồng.";
            }
            return baseMessage + ". Đánh giá của bạn đã được ghi nhận và sẽ giúp ích cho cộng đồng.";
        }
    }

    /**
     * Animation helper cho success effects
     */
    public static class SuccessAnimator {
        public static void playBounceAnimation(View view) {
            view.animate()
                    .scaleX(1.2f)
                    .scaleY(1.2f)
                    .setDuration(200)
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            view.animate()
                                    .scaleX(1.0f)
                                    .scaleY(1.0f)
                                    .setDuration(200)
                                    .start();
                        }
                    })
                    .start();
        }

        public static void playPulseAnimation(View view) {
            view.animate()
                    .alpha(0.7f)
                    .setDuration(200)
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            view.animate()
                                    .alpha(1.0f)
                                    .setDuration(200)
                                    .start();
                        }
                    })
                    .start();
        }

        public static void playSequentialAnimation(View view) {
            view.animate()
                    .scaleX(1.1f)
                    .scaleY(1.1f)
                    .setDuration(150)
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            view.animate()
                                    .scaleX(0.9f)
                                    .scaleY(0.9f)
                                    .setDuration(100)
                                    .withEndAction(new Runnable() {
                                        @Override
                                        public void run() {
                                            view.animate()
                                                    .scaleX(1.0f)
                                                    .scaleY(1.0f)
                                                    .setDuration(150)
                                                    .start();
                                        }
                                    })
                                    .start();
                        }
                    })
                    .start();
        }
    }

    /**
     * Configuration cho success screen
     */
    public static class SuccessConfig {
        private int successType;
        private String propertyName;
        private String propertyId;
        private boolean showViewReviewsButton;
        private boolean autoNavigateAfterDelay;

        public SuccessConfig() {
            this.successType = SuccessType.REVIEW_SUBMITTED;
            this.showViewReviewsButton = true;
            this.autoNavigateAfterDelay = false;
        }

        // Builder pattern methods
        public SuccessConfig setSuccessType(int successType) {
            this.successType = successType;
            return this;
        }

        public SuccessConfig setPropertyName(String propertyName) {
            this.propertyName = propertyName;
            return this;
        }

        public SuccessConfig setPropertyId(String propertyId) {
            this.propertyId = propertyId;
            return this;
        }

        public SuccessConfig setShowViewReviewsButton(boolean show) {
            this.showViewReviewsButton = show;
            return this;
        }

        public SuccessConfig setAutoNavigateAfterDelay(boolean autoNavigate) {
            this.autoNavigateAfterDelay = autoNavigate;
            return this;
        }

        // Getters
        public int getSuccessType() { return successType; }
        public String getPropertyName() { return propertyName; }
        public String getPropertyId() { return propertyId; }
        public boolean shouldShowViewReviewsButton() { return showViewReviewsButton; }
        public boolean shouldAutoNavigateAfterDelay() { return autoNavigateAfterDelay; }

        // Intent creation helper
        public Intent createIntent(android.content.Context context) {
            Intent intent = new Intent(context, ReviewSuccessActivity.class);
            intent.putExtra("success_type", successType);
            intent.putExtra("property_name", propertyName);
            intent.putExtra("property_id", propertyId);
            intent.putExtra("show_view_reviews", showViewReviewsButton);
            intent.putExtra("auto_navigate", autoNavigateAfterDelay);
            return intent;
        }
    }

    /**
     * Navigation helper để dễ sử dụng từ các activity khác
     */
    public static class NavigationHelper {
        public static void navigateToSuccess(android.content.Context context, String propertyName, String propertyId) {
            SuccessConfig config = new SuccessConfig()
                    .setPropertyName(propertyName)
                    .setPropertyId(propertyId);

            Intent intent = config.createIntent(context);
            context.startActivity(intent);
        }

        public static void navigateToSuccessWithAutoClose(android.content.Context context, String propertyName) {
            SuccessConfig config = new SuccessConfig()
                    .setPropertyName(propertyName)
                    .setAutoNavigateAfterDelay(true)
                    .setShowViewReviewsButton(false);

            Intent intent = config.createIntent(context);
            context.startActivity(intent);
        }
    }
}