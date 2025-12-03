package com.example.nhom15_roomfinder.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.nhom15_roomfinder.R;

import java.util.ArrayList;

public class ImageGalleryActivity extends AppCompatActivity {

    private ImageButton btnClose;
    private ViewPager2 viewPager;
    private TextView tvImageCounter;

    private ImageGalleryAdapter adapter;
    private ArrayList<String> imageUrls;
    private int initialPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_gallery);

        if (getIntent() != null) {
            imageUrls = getIntent().getStringArrayListExtra("imageUrls");
            initialPosition = getIntent().getIntExtra("initialPosition", 0);
        }

        if (imageUrls == null || imageUrls.isEmpty()) {
            Toast.makeText(this, "Không có ảnh để hiển thị", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupViewPager();
        setupButtons();
    }

    private void initViews() {
        btnClose = findViewById(R.id.btnClose);
        viewPager = findViewById(R.id.imageGalleryPager);
        tvImageCounter = findViewById(R.id.tvImageCounter);
    }

    private void setupViewPager() {
        // Sửa lại constructor, truyền thêm context (this)
        adapter = new ImageGalleryAdapter(this, imageUrls);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(initialPosition, false); // Set ảnh ban đầu

        updateCounter(initialPosition);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateCounter(position);
            }
        });
    }

    private void setupButtons() {
        btnClose.setOnClickListener(v -> finish());
    }

    private void updateCounter(int position) {
        tvImageCounter.setText((position + 1) + " / " + imageUrls.size());
    }
}
