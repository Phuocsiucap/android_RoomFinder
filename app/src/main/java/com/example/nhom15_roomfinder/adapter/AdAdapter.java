package com.example.nhom15_roomfinder.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
        private Button btnEdit, btnDelete;

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
            String location = (String) ad.get("location");
            Object priceObj = ad.get("price");
            String status = (String) ad.get("status");
            String imageUrl = (String) ad.get("imageUrl");
            String adId = (String) ad.get("roomId");

            txtAdTitle.setText(title != null ? title : "Không có tiêu đề");
            txtAdLocation.setText(location != null ? location : "Không có địa chỉ");

            if (priceObj != null) {
                if (priceObj instanceof Number) {
                    double price = ((Number) priceObj).doubleValue();
                    txtAdPrice.setText(String.format("%.0f VNĐ", price));
                } else {
                    txtAdPrice.setText(priceObj.toString() + " VNĐ");
                }
            } else {
                txtAdPrice.setText("0 VNĐ");
            }

            if (status != null) {
                txtAdStatus.setText(status.equals("available") ? "Đang hoạt động" : "Đã khóa");
                txtAdStatus.setTextColor(status.equals("available") ? 
                    0xFF4CAF50 : 0xFFF44336);
            } else {
                txtAdStatus.setText("Không xác định");
            }

            // Load image - placeholder for now
            // TODO: Add image loading library (Picasso/Glide) to load images from URL
            imgAd.setImageResource(android.R.drawable.ic_menu_gallery);

            btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(ad);
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (adId != null && listener != null) {
                    listener.onDeleteClick(adId);
                }
            });
        }
    }
}

