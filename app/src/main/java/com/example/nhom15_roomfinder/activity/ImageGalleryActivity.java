package com.example.nhom15_roomfinder.activity; // Đổi cho đúng package của bạn

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.nhom15_roomfinder.R;

import java.util.ArrayList;
import java.util.List;

public class ImageGalleryActivity extends AppCompatActivity {

    private ImageButton btnClose, btnPrevious, btnNext;
    private ViewPager2 viewPager;
    private TextView tvImageCounter;

    private ImageGalleryAdapter adapter;
    private List<Integer> images;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_gallery); // Đổi đúng tên file XML bạn upload

        initViews();
        loadImages();
        setupViewPager();
        setupButtons();
    }

    private void initViews() {
        btnClose = findViewById(R.id.btnClose);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnNext = findViewById(R.id.btnNext);
        viewPager = findViewById(R.id.imageGalleryPager);
        tvImageCounter = findViewById(R.id.tvImageCounter);
    }

    private void loadImages() {
        // Demo — sau này bạn truyền list ảnh thật vào
        images = new ArrayList<>();
        images.add(R.drawable.ic_image);
        images.add(R.drawable.ic_spacious);
        images.add(R.drawable.ic_affordable);
        images.add(R.drawable.ic_home);
        images.add(R.drawable.ic_wifi);
    }

    private void setupViewPager() {
        adapter = new ImageGalleryAdapter(images);
        viewPager.setAdapter(adapter);

        updateCounter(0);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateCounter(position);
            }
        });
    }

    private void setupButtons() {

        btnClose.setOnClickListener(v -> finish());

        btnPrevious.setOnClickListener(v -> {
            int current = viewPager.getCurrentItem();
            if (current > 0) {
                viewPager.setCurrentItem(current - 1, true);
            }
        });

        btnNext.setOnClickListener(v -> {
            int current = viewPager.getCurrentItem();
            if (current < images.size() - 1) {
                viewPager.setCurrentItem(current + 1, true);
            }
        });
    }

    private void updateCounter(int position) {
        tvImageCounter.setText((position + 1) + " / " + images.size());
    }
}
