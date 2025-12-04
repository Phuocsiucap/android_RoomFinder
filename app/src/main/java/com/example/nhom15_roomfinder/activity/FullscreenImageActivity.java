package com.example.nhom15_roomfinder.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.nhom15_roomfinder.R;
import com.example.nhom15_roomfinder.adapter.FullscreenImageAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * FullscreenImageActivity - Màn hình xem ảnh toàn màn hình
 * Cho phép vuốt qua lại để xem các ảnh khác nhau
 * Có nút X để đóng
 */
public class FullscreenImageActivity extends AppCompatActivity {

    public static final String EXTRA_IMAGE_URLS = "extra_image_urls";
    public static final String EXTRA_CURRENT_POSITION = "extra_current_position";

    private ViewPager2 viewPagerFullscreen;
    private ImageView btnClose;
    private TextView tvImageIndicator;
    private View topBar;

    private FullscreenImageAdapter adapter;
    private List<String> imageUrls;
    private int totalImages = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Fullscreen mode
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_FULLSCREEN);
        
        setContentView(R.layout.activity_fullscreen_image);

        initViews();
        getIntentData();
        setupViewPager();
        setupClickListeners();
    }

    private void initViews() {
        viewPagerFullscreen = findViewById(R.id.viewPagerFullscreen);
        btnClose = findViewById(R.id.btnClose);
        tvImageIndicator = findViewById(R.id.tvImageIndicator);
        topBar = findViewById(R.id.topBar);
    }

    private void getIntentData() {
        imageUrls = getIntent().getStringArrayListExtra(EXTRA_IMAGE_URLS);
        int currentPosition = getIntent().getIntExtra(EXTRA_CURRENT_POSITION, 0);

        if (imageUrls == null) {
            imageUrls = new ArrayList<>();
        }
        
        totalImages = imageUrls.size();

        if (totalImages > 0) {
            // Set initial position after setting up adapter
            viewPagerFullscreen.post(() -> {
                viewPagerFullscreen.setCurrentItem(currentPosition, false);
                updateIndicator(currentPosition);
            });
        }
    }

    private void setupViewPager() {
        adapter = new FullscreenImageAdapter(this, imageUrls);
        viewPagerFullscreen.setAdapter(adapter);

        // Listen for page changes to update indicator
        viewPagerFullscreen.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateIndicator(position);
            }
        });

        // Tap to toggle top bar visibility
        viewPagerFullscreen.setOnClickListener(v -> toggleTopBar());
    }

    private void setupClickListeners() {
        btnClose.setOnClickListener(v -> finish());
    }

    private void updateIndicator(int position) {
        tvImageIndicator.setText((position + 1) + " / " + totalImages);
    }

    private void toggleTopBar() {
        if (topBar.getVisibility() == View.VISIBLE) {
            topBar.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction(() -> topBar.setVisibility(View.GONE))
                    .start();
        } else {
            topBar.setVisibility(View.VISIBLE);
            topBar.setAlpha(0f);
            topBar.animate()
                    .alpha(1f)
                    .setDuration(200)
                    .start();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
