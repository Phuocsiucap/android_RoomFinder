package com.example.nhom15_roomfinder.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.nhom15_roomfinder.R;

import java.util.ArrayList;
import java.util.List;

public class ReviewActivity extends AppCompatActivity {

    private ImageView backArrow, propertyImage;
    private TextView propertyName, reviewCount, yourRatingLabel, commentLabel;
    private RatingBar overallRatingBar, userRatingBar;
    private EditText commentInput;
    private Button submitButton;

    // Review tags
    private TextView tagClean, tagFriendly, tagAccurate, tagQuickSupport, tagSafe;

    private List<String> selectedTags = new ArrayList<>();
    private float userRating = 0;
    private boolean hasUnsavedChanges = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        initViews();
        setupListeners();
        setupBackPressedHandler();
        loadPropertyData();
    }

    private void initViews() {
        // Khởi tạo các view
        backArrow = findViewById(R.id.back_arrow);
        propertyImage = findViewById(R.id.property_image);
        propertyName = findViewById(R.id.property_name);
        reviewCount = findViewById(R.id.review_count);
        overallRatingBar = findViewById(R.id.overall_rating_bar);
        userRatingBar = findViewById(R.id.user_rating_bar);
        yourRatingLabel = findViewById(R.id.your_rating_label);
        commentLabel = findViewById(R.id.comment_label);
        commentInput = findViewById(R.id.comment_input);
        submitButton = findViewById(R.id.submit_button);

        // Khởi tạo tags
        tagClean = findViewById(R.id.tag_clean);
        tagFriendly = findViewById(R.id.tag_friendly);
        tagAccurate = findViewById(R.id.tag_accurate);
        tagQuickSupport = findViewById(R.id.tag_quick_support);
        tagSafe = findViewById(R.id.tag_safe);
    }

    private void setupListeners() {
        // Listener cho nút back
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleBackPressed();
            }
        });

        // Listener cho rating bar
        userRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                if (fromUser) {
                    userRating = rating;
                    hasUnsavedChanges = true;
                    updateSubmitButtonState();
                }
            }
        });

        // Listener cho comment input
        commentInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    checkForUnsavedChanges();
                }
            }
        });

        // Listeners cho các tag
        setupTagListener(tagClean, "Sạch sẽ");
        setupTagListener(tagFriendly, "Thân thiện");
        setupTagListener(tagAccurate, "Chính xác");
        setupTagListener(tagQuickSupport, "Hỗ trợ nhanh");
        setupTagListener(tagSafe, "An toàn");

        // Listener cho nút submit
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitReview();
            }
        });
    }

    private void setupTagListener(TextView tagView, String tagText) {
        tagView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleTagSelection(tagView, tagText);
            }
        });
    }

    private void toggleTagSelection(TextView tagView, String tagText) {
        if (selectedTags.contains(tagText)) {
            // Bỏ chọn tag
            selectedTags.remove(tagText);
            tagView.setBackgroundResource(R.drawable.review_tag_unselected);
            tagView.setTextColor(getResources().getColor(android.R.color.darker_gray));
        } else {
            // Chọn tag
            selectedTags.add(tagText);
            tagView.setBackgroundResource(R.drawable.review_tag_selected);
            tagView.setTextColor(getResources().getColor(android.R.color.white));
        }
        hasUnsavedChanges = true;
        updateSubmitButtonState();
    }

    private void setupBackPressedHandler() {
        // Sử dụng OnBackPressedDispatcher thay vì onBackPressed()
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBackPressed();
            }
        });
    }

    private void handleBackPressed() {
        checkForUnsavedChanges();

        if (hasUnsavedChanges) {
            showUnsavedChangesDialog();
        } else {
            finish();
        }
    }

    private void checkForUnsavedChanges() {
        String comment = commentInput.getText().toString().trim();
        hasUnsavedChanges = userRating > 0 || !comment.isEmpty() || !selectedTags.isEmpty();
    }

    private void updateSubmitButtonState() {
        boolean hasRating = userRating > 0;
        boolean hasComment = !commentInput.getText().toString().trim().isEmpty();
        boolean hasTags = !selectedTags.isEmpty();

        // Cho phép submit nếu có ít nhất một trong các điều kiện
        submitButton.setEnabled(hasRating || hasComment || hasTags);

        // Thay đổi màu nút nếu cần
        if (submitButton.isEnabled()) {
            submitButton.setAlpha(1.0f);
        } else {
            submitButton.setAlpha(0.5f);
        }
    }

    private void loadPropertyData() {
        // TODO: Load dữ liệu từ Intent hoặc database
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String propertyNameText = extras.getString("property_name", "Tên chỗ nghỉ");
            float overallRating = extras.getFloat("overall_rating", 4.5f);
            int reviewCountValue = extras.getInt("review_count", 0);
            String imageUrl = extras.getString("image_url");

            propertyName.setText(propertyNameText);
            overallRatingBar.setRating(overallRating);
            reviewCount.setText("(" + reviewCountValue + " đánh giá)");

            // Load ảnh sử dụng Glide/Picasso nếu có URL
            // Glide.with(this).load(imageUrl).into(propertyImage);
        }
    }

    private void submitReview() {
        // Lấy dữ liệu từ form
        String comment = commentInput.getText().toString().trim();

        // Validate dữ liệu
        if (userRating == 0 && comment.isEmpty() && selectedTags.isEmpty()) {
            Toast.makeText(this, "Vui lòng đánh giá hoặc nhận xét", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo đối tượng Review
        Review review = new Review();
        review.setRating(userRating);
        review.setComment(comment);
        review.setTags(new ArrayList<>(selectedTags));
        review.setTimestamp(System.currentTimeMillis());

        // Gửi đánh giá lên server
        sendReviewToServer(review);
    }

    private void sendReviewToServer(Review review) {
        // TODO: Implement logic để gửi đánh giá lên server
        // Hiển thị loading indicator
        showLoading(true);

        // Giả lập gửi thành công
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        showLoading(false);
                        // Hiển thị thông báo thành công
                        Toast.makeText(ReviewActivity.this, "Gửi đánh giá thành công!", Toast.LENGTH_SHORT).show();

                        // Đánh dấu không còn thay đổi chưa lưu
                        hasUnsavedChanges = false;

                        // Set result và finish activity
                        setResult(RESULT_OK);
                        finish();
                    }
                },
                1000);
    }

    private void showLoading(boolean show) {
        // TODO: Implement loading indicator
        if (show) {
            submitButton.setText("Đang gửi...");
            submitButton.setEnabled(false);
        } else {
            submitButton.setText("Gửi đánh giá");
            submitButton.setEnabled(true);
        }
    }

    private void showUnsavedChangesDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Thoát đánh giá")
                .setMessage("Bạn có chắc muốn thoát? Đánh giá của bạn sẽ không được lưu.")
                .setPositiveButton("Thoát", (dialog, which) -> {
                    // Thoát mà không lưu
                    finish();
                })
                .setNegativeButton("Ở lại", (dialog, which) -> {
                    // Ở lại activity
                    dialog.dismiss();
                })
                .setNeutralButton("Lưu nháp", (dialog, which) -> {
                    // TODO: Implement save draft functionality
                    saveAsDraft();
                    finish();
                })
                .show();
    }

    private void saveAsDraft() {
        // TODO: Implement save draft functionality
        Toast.makeText(this, "Đã lưu nháp", Toast.LENGTH_SHORT).show();
        hasUnsavedChanges = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Tự động lưu nháp khi activity bị pause
        if (hasUnsavedChanges) {
            saveAsDraft();
        }
    }

    // Class Review để lưu thông tin đánh giá
    public static class Review {
        private float rating;
        private String comment;
        private List<String> tags;
        private long timestamp;

        public Review() {}

        public float getRating() { return rating; }
        public void setRating(float rating) { this.rating = rating; }

        public String getComment() { return comment; }
        public void setComment(String comment) { this.comment = comment; }

        public List<String> getTags() { return tags; }
        public void setTags(List<String> tags) { this.tags = tags; }

        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }
}