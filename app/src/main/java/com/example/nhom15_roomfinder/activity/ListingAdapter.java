package com.example.nhom15_roomfinder.activity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nhom15_roomfinder.R;
import com.example.nhom15_roomfinder.entity.Listing;
import java.util.List;

public class ListingAdapter extends RecyclerView.Adapter<ListingAdapter.ViewHolder> {

    public interface ListingClickListener {
        void onListingClick(Listing listing);
        void onListingEdit(Listing listing);
        void onListingDelete(Listing listing);
    }

    private List<Listing> listings;
    private ListingClickListener listener;

    public ListingAdapter(List<Listing> listings, ListingClickListener listener) {
        this.listings = listings;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_listing, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Listing listing = listings.get(position);
        holder.bind(listing);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onListingClick(listing);
        });

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onListingEdit(listing);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onListingDelete(listing);
        });
    }

    @Override
    public int getItemCount() {
        return listings.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvPrice, tvAddress, tvTime, tvViews, tvStatus;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvViews = itemView.findViewById(R.id.tvViews);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        public void bind(Listing listing) {
            tvTitle.setText(listing.getTitle());
            tvPrice.setText(listing.getFormattedPrice());
            tvAddress.setText(listing.getAddress());
            tvTime.setText(listing.getTimeAgo());
            tvViews.setText(listing.getViewCount() + " lượt xem");
            tvStatus.setText(listing.isActive() ? "Đang hiển thị" : "Đã ẩn");
        }
    }
}
