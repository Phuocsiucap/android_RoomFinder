package com.example.nhom15_roomfinder.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom15_roomfinder.R;
import com.example.nhom15_roomfinder.entity.Listing;

import java.util.ArrayList;
import java.util.List;

public class ListingActivity extends AppCompatActivity {

    private RecyclerView rvListings;
    private Button btnAdd;
    private ProgressBar progressBar;
    private LinearLayout emptyStateLayout;
    private ListingAdapter listingAdapter;
    private List<Listing> listings = new ArrayList<>();

    // Constants
    private static final int REQUEST_CREATE_LISTING = 1001;
    private static final int REQUEST_EDIT_LISTING = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listing);

        initViews();
        setupRecyclerView();
        setupListeners();
        loadListings();
    }

    private void initViews() {
        rvListings = findViewById(R.id.rvListings);
        btnAdd = findViewById(R.id.btnAdd);
        progressBar = findViewById(R.id.progressBar);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
    }

    private void setupRecyclerView() {
        listingAdapter = new ListingAdapter(listings, new ListingAdapter.ListingClickListener() {
            @Override
            public void onListingClick(Listing listing) {
                openListingDetail(listing);
            }

            @Override
            public void onListingEdit(Listing listing) {
                editListing(listing);
            }

            @Override
            public void onListingDelete(Listing listing) {
                deleteListing(listing);
            }
        });

        rvListings.setLayoutManager(new LinearLayoutManager(this));
        rvListings.setAdapter(listingAdapter);

        // Thêm spacing giữa các item
        rvListings.addItemDecoration(new ListingItemDecoration(16));
    }

    private void setupListeners() {
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewListing();
            }
        });
    }

    private void loadListings() {
        showLoading(true);

        // Giả lập load data
        new android.os.Handler().postDelayed(() -> {
            listings.clear();
            listings.addAll(getMockListings());
            listingAdapter.notifyDataSetChanged();
            showLoading(false);
            checkEmptyState();
        }, 1000);
    }

    private void refreshListings() {
        // Giả lập refresh data
        new android.os.Handler().postDelayed(() -> {
            listings.clear();
            listings.addAll(getMockListings());
            listingAdapter.notifyDataSetChanged();
            checkEmptyState();
            Toast.makeText(ListingActivity.this, "Đã cập nhật", Toast.LENGTH_SHORT).show();
        }, 1500);
    }

    private void createNewListing() {
        Intent intent = new Intent(this, EditPostActivity.class);
        intent.putExtra("mode", "create");
        startActivityForResult(intent, REQUEST_CREATE_LISTING);
    }

    private void openListingDetail(Listing listing) {
        Toast.makeText(this, "Xem chi tiết: " + listing.getTitle(), Toast.LENGTH_SHORT).show();
        // Intent intent = new Intent(this, ListingDetailActivity.class);
        // intent.putExtra("listing", listing);
        // startActivity(intent);
    }

    private void editListing(Listing listing) {
        Intent intent = new Intent(this, EditPostActivity.class);
        intent.putExtra("mode", "edit");
        intent.putExtra("listing", listing);
        startActivityForResult(intent, REQUEST_EDIT_LISTING);
    }

    private void deleteListing(Listing listing) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xóa tin đăng")
                .setMessage("Bạn có chắc muốn xóa tin đăng '" + listing.getTitle() + "'?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    performDeleteListing(listing);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void performDeleteListing(Listing listing) {
        int position = listings.indexOf(listing);
        if (position != -1) {
            listings.remove(position);
            listingAdapter.notifyItemRemoved(position);
            checkEmptyState();
            Toast.makeText(this, "Đã xóa tin đăng", Toast.LENGTH_SHORT).show();
        }
    }

    private void showLoading(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
            rvListings.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            checkEmptyState();
        }
    }

    private void checkEmptyState() {
        if (listings.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            rvListings.setVisibility(View.GONE);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            rvListings.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CREATE_LISTING || requestCode == REQUEST_EDIT_LISTING) {
                refreshListings();
                Toast.makeText(this, "Cập nhật tin đăng thành công", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Mock data for testing
    private List<Listing> getMockListings() {
        List<Listing> mockListings = new ArrayList<>();

        Listing listing1 = new Listing();
        listing1.setId("1");
        listing1.setTitle("Cho thuê phòng trọ mới đẹp tại Quận 1");
        listing1.setDescription("Phòng trọ mới xây, đầy đủ tiện nghi: máy lạnh, wifi, chỗ để xe");
        listing1.setPrice("2,500,000");
        listing1.setAddress("123 Nguyễn Huệ, Quận 1, TP.HCM");
        listing1.setCreatedAt(System.currentTimeMillis() - 86400000); // 1 ngày trước
        listing1.setViewCount(15);
        listing1.setActive(true);

        Listing listing2 = new Listing();
        listing2.setId("2");
        listing2.setTitle("Căn hộ Studio cao cấp Quận 3");
        listing2.setDescription("Căn hộ studio full nội thất, view đẹp, an ninh 24/7");
        listing2.setPrice("4,200,000");
        listing2.setAddress("456 Lê Văn Sỹ, Quận 3, TP.HCM");
        listing2.setCreatedAt(System.currentTimeMillis() - 172800000); // 2 ngày trước
        listing2.setViewCount(28);
        listing2.setActive(true);

        Listing listing3 = new Listing();
        listing3.setId("3");
        listing3.setTitle("Nhà nguyên căn 2 phòng ngủ Quận Bình Thạnh");
        listing3.setDescription("Nhà mới xây, có sân vườn, phù hợp gia đình nhỏ");
        listing3.setPrice("6,500,000");
        listing3.setAddress("789 Xô Viết Nghệ Tĩnh, Bình Thạnh");
        listing3.setCreatedAt(System.currentTimeMillis() - 259200000); // 3 ngày trước
        listing3.setViewCount(42);
        listing3.setActive(false);

        mockListings.add(listing1);
        mockListings.add(listing2);
        mockListings.add(listing3);

        return mockListings;
    }

    // ==================== INNER CLASSES ====================

    /**
     * Adapter cho RecyclerView
     */
    public static class ListingAdapter extends RecyclerView.Adapter<ListingAdapter.ViewHolder> {
        private List<Listing> listings;
        private ListingClickListener listener;

        public interface ListingClickListener {
            void onListingClick(Listing listing);
            void onListingEdit(Listing listing);
            void onListingDelete(Listing listing);
        }

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

            // Click to view details
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onListingClick(listing);
                }
            });

            // Edit button
            holder.btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onListingEdit(listing);
                }
            });

            // Delete button
            holder.btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onListingDelete(listing);
                }
            });

            // Long click for additional options
            holder.itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    // Có thể hiển thị menu context ở đây
                    listener.onListingClick(listing);
                    return true;
                }
                return false;
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

                // Set status
                if (listing.isActive()) {
                    tvStatus.setText("Đang hiển thị");
                    tvStatus.setBackgroundColor(itemView.getContext().getColor(R.color.green_light));
                    tvStatus.setTextColor(itemView.getContext().getColor(R.color.green_dark));
                } else {
                    tvStatus.setText("Đã ẩn");
                    tvStatus.setBackgroundColor(itemView.getContext().getColor(R.color.gray_light));
                    tvStatus.setTextColor(itemView.getContext().getColor(R.color.gray_dark));
                }
            }
        }
    }

    /**
     * Item decoration đơn giản cho spacing
     */
    public static class ListingItemDecoration extends RecyclerView.ItemDecoration {
        private final int spaceHeight;

        public ListingItemDecoration(int spaceHeight) {
            this.spaceHeight = spaceHeight;
        }
    }
}