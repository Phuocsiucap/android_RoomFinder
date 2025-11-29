package com.example.nhom15_roomfinder.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom15_roomfinder.R;
import com.example.nhom15_roomfinder.firebase.FirebaseCallback;
import com.example.nhom15_roomfinder.firebase.FirebaseManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdAdapter extends RecyclerView.Adapter<AdAdapter.AdViewHolder> {

    private List<Map<String, Object>> adsList;
    private OnAdActionListener listener;
    private FirebaseManager firebaseManager;

    public interface OnAdActionListener {
        void onEditClick(Map<String, Object> ad);
        void onDeleteClick(String adId);
    }

    public AdAdapter(List<Map<String, Object>> adsList, OnAdActionListener listener) {
        this.adsList = adsList != null ? adsList : new ArrayList<>();
        this.listener = listener;
        this.firebaseManager = FirebaseManager.getInstance();
    }

    @NonNull
    @Override
    public AdViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ad_admin, parent, false);
        return new AdViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdViewHolder holder, int position) {
        Map<String, Object> ad = adsList.get(position);
        holder.bind(ad);
    }

    @Override
    public int getItemCount() {
        return adsList.size();
    }

    public void updateList(List<Map<String, Object>> newList) {
        this.adsList = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    class AdViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgAd;
        private TextView txtAdTitle, txtAdLocation, txtAdPrice, txtAdStatus;
        private ImageButton btnEdit, btnDelete;

        public AdViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAd = itemView.findViewById(R.id.imgAd);
            txtAdTitle = itemView.findViewById(R.id.txtAdTitle);
            txtAdLocation = itemView.findViewById(R.id.txtAdLocation);
            txtAdPrice = itemView.findViewById(R.id.txtAdPrice);
            txtAdStatus = itemView.findViewById(R.id.txtAdStatus);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        public void bind(Map<String, Object> ad) {
            String title = (String) ad.get("title");
            String address = (String) ad.get("address");
            String district = (String) ad.get("district");
            String city = (String) ad.get("city");
            Object priceObj = ad.get("price");
            String priceDisplay = (String) ad.get("priceDisplay");
            Object isAvailableObj = ad.get("isAvailable");
            boolean isAvailable = isAvailableObj instanceof Boolean ? (Boolean) isAvailableObj : false;
            String status = (String) ad.get("status");
            String imageUrl = (String) ad.get("imageUrl");
            String thumbnailUrl = (String) ad.get("thumbnailUrl");
            String roomId = (String) ad.get("roomId");
            String idField = (String) ad.get("id");
            final String adId = roomId != null ? roomId : idField;

            // Set title
            txtAdTitle.setText(title != null ? title : "Không có tiêu đề");

            // Build location string from address, district, city
            StringBuilder locationBuilder = new StringBuilder();
            if (address != null && !address.isEmpty()) {
                locationBuilder.append(address);
            }
            if (district != null && !district.isEmpty()) {
                if (locationBuilder.length() > 0) locationBuilder.append(", ");
                locationBuilder.append(district);
            }
            if (city != null && !city.isEmpty()) {
                if (locationBuilder.length() > 0) locationBuilder.append(", ");
                locationBuilder.append(city);
            }
            String locationStr = locationBuilder.length() > 0 ? locationBuilder.toString() : "Không có địa chỉ";
            txtAdLocation.setText(locationStr);

            // Set price - prefer priceDisplay if available
            if (priceDisplay != null && !priceDisplay.isEmpty()) {
                txtAdPrice.setText(priceDisplay);
            } else if (priceObj != null) {
                if (priceObj instanceof Number) {
                    double price = ((Number) priceObj).doubleValue();
                    txtAdPrice.setText(String.format("%,.0f VNĐ/tháng", price));
                } else {
                    txtAdPrice.setText(priceObj.toString() + " VNĐ");
                }
            } else {
                txtAdPrice.setText("0 VNĐ");
            }

            // Set status - check both isAvailable and status field
            boolean isActive = isAvailable && !"blocked".equals(status);
            if (isActive) {
                txtAdStatus.setText("Đang hoạt động");
                txtAdStatus.setTextColor(0xFF4CAF50);
            } else if ("blocked".equals(status)) {
                txtAdStatus.setText("Đã khóa");
                txtAdStatus.setTextColor(0xFFF44336);
            } else {
                txtAdStatus.setText("Không khả dụng");
                txtAdStatus.setTextColor(0xFFFF9800);
            }

            // Load image using Glide if available
            if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {
                com.bumptech.glide.Glide.with(itemView.getContext())
                    .load(thumbnailUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .centerCrop()
                    .into(imgAd);
            } else if (imageUrl != null && !imageUrl.isEmpty()) {
                com.bumptech.glide.Glide.with(itemView.getContext())
                    .load(imageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .centerCrop()
                    .into(imgAd);
            } else {
                imgAd.setImageResource(android.R.drawable.ic_menu_gallery);
            }

            // Set click listeners
            if (btnEdit != null) {
                btnEdit.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onEditClick(ad);
                    }
                });
            }

            if (btnDelete != null) {
                btnDelete.setOnClickListener(v -> {
                    if (adId != null && listener != null) {
                        listener.onDeleteClick(adId);
                    }
                });
            }
        }
    }
}

