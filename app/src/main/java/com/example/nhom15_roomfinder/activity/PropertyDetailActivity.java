package com.example.nhom15_roomfinder.activity; // đổi đúng package của bạn

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.nhom15_roomfinder.R;

import java.util.ArrayList;
import java.util.List;

public class PropertyDetailActivity extends AppCompatActivity {

    private ViewPager2 imageViewPager;
    private TextView tvPropertyTitle, tvPrice, tvAddress, tvDescription, tvOwnerName, tvOwnerPhone;
    private Button btnCall, btnMessage;

    private PropertyImageAdapter imageAdapter;
    private List<Integer> images; // bạn sẽ thay bằng URL nếu cần

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_property_detail); // đặt đúng tên file của bạn

        initViews();
        loadImages();
        setupViewPager();
        setupButtons();
    }

    private void initViews() {
        imageViewPager   = findViewById(R.id.imageViewPager);
        tvPropertyTitle  = findViewById(R.id.tvPropertyTitle);
        tvPrice          = findViewById(R.id.tvPrice);
        tvAddress        = findViewById(R.id.tvAddress);
        tvDescription    = findViewById(R.id.tvDescription);
        tvOwnerName      = findViewById(R.id.tvOwnerName);
        tvOwnerPhone     = findViewById(R.id.tvOwnerPhone);

        btnCall          = findViewById(R.id.btnCall);
        btnMessage       = findViewById(R.id.btnMessage);
    }

    private void loadImages() {
        // demo – bạn có thể thay bằng list ảnh từ server
        images = new ArrayList<>();
        images.add(R.drawable.ic_spacious);      // Có thật
        images.add(R.drawable.ic_affordable);    // Có thật
        images.add(R.drawable.ic_image);
    }

    private void setupViewPager() {
        imageAdapter = new PropertyImageAdapter(images);
        imageViewPager.setAdapter(imageAdapter);
    }

    private void setupButtons() {

        // Gọi điện
        btnCall.setOnClickListener(v -> {
            String phone = tvOwnerPhone.getText().toString();
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phone));
            startActivity(intent);
        });

        // Nhắn tin chủ trọ → mở ChatDetailActivity
        btnMessage.setOnClickListener(v -> {
            Intent intent = new Intent(PropertyDetailActivity.this, ChatDetailActivity.class);
            intent.putExtra("ownerName", tvOwnerName.getText().toString());
            startActivity(intent);
        });
    }
}
