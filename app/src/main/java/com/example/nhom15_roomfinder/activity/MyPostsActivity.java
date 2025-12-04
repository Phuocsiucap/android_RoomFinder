package com.example.nhom15_roomfinder.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nhom15_roomfinder.R;
import com.example.nhom15_roomfinder.entity.Room;
import com.example.nhom15_roomfinder.firebase.FirebaseManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * MyPostsActivity - Màn hình hiển thị danh sách bài đăng của người dùng
 */
public class MyPostsActivity extends AppCompatActivity {

    private static final String TAG = "MyPostsActivity";

    private ImageButton btnBack;
    private TextView tvTitle, tvPostCount;
    private RecyclerView rvMyPosts;
    private LinearLayout emptyStateLayout;
    private ProgressBar progressBar;

    private FirebaseManager firebaseManager;
    private String currentUserId;
    private List<Room> myPostsList = new ArrayList<>();
    private MyPostsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);

        firebaseManager = FirebaseManager.getInstance();
        currentUserId = firebaseManager.getUserId();

        if (currentUserId == null) {
            redirectToLogin();
            return;
        }

        initViews();
        setupRecyclerView();
        setListeners();
        loadMyPosts();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvTitle = findViewById(R.id.tvTitle);
        tvPostCount = findViewById(R.id.tvPostCount);
        rvMyPosts = findViewById(R.id.rvMyPosts);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupRecyclerView() {
        adapter = new MyPostsAdapter(myPostsList, new MyPostsAdapter.OnPostClickListener() {
            @Override
            public void onPostClick(Room room) {
                openPostDetail(room);
            }

            @Override
            public void onEditClick(Room room) {
                editPost(room);
            }

            @Override
            public void onDeleteClick(Room room) {
                confirmDeletePost(room);
            }
        });

        rvMyPosts.setLayoutManager(new LinearLayoutManager(this));
        rvMyPosts.setAdapter(adapter);
    }

    private void setListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadMyPosts() {
        showLoading(true);
        
        Log.d(TAG, "Loading posts for user: " + currentUserId);

        // Query đơn giản hơn - không cần composite index
        firebaseManager.getFirestore()
            .collection("rooms")
            .whereEqualTo("ownerId", currentUserId)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                showLoading(false);
                myPostsList.clear();
                
                Log.d(TAG, "Found " + querySnapshot.size() + " posts");

                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    Room room = doc.toObject(Room.class);
                    if (room != null) {
                        room.setId(doc.getId());
                        myPostsList.add(room);
                        Log.d(TAG, "Post: " + room.getTitle());
                    }
                }
                
                // Sắp xếp theo thời gian tạo (mới nhất trước) ở client side
                myPostsList.sort((r1, r2) -> Long.compare(r2.getCreatedAt(), r1.getCreatedAt()));

                adapter.notifyDataSetChanged();
                updateUI();
            })
            .addOnFailureListener(e -> {
                showLoading(false);
                Log.e(TAG, "Error loading posts: " + e.getMessage());
                e.printStackTrace();
                Toast.makeText(this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
    }

    private void updateUI() {
        int count = myPostsList.size();
        tvPostCount.setText("Tổng: " + count + " bài đăng");
        
        if (count == 0) {
            rvMyPosts.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
        } else {
            rvMyPosts.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
        }
    }

    private void openPostDetail(Room room) {
        Intent intent = new Intent(this, PropertyDetailActivity.class);
        intent.putExtra("room", room);
        intent.putExtra("roomId", room.getId());
        startActivity(intent);
    }

    private void editPost(Room room) {
        Intent intent = new Intent(this, EditRoomActivity.class);
        intent.putExtra("room", room);
        intent.putExtra("roomId", room.getId());
        startActivityForResult(intent, 100);
    }

    private void confirmDeletePost(Room room) {
        new AlertDialog.Builder(this)
            .setTitle("Xóa bài đăng")
            .setMessage("Bạn có chắc muốn xóa bài đăng \"" + room.getTitle() + "\"?")
            .setPositiveButton("Xóa", (dialog, which) -> deletePost(room))
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void deletePost(Room room) {
        showLoading(true);
        
        firebaseManager.getFirestore()
            .collection("rooms")
            .document(room.getId())
            .delete()
            .addOnSuccessListener(aVoid -> {
                showLoading(false);
                Toast.makeText(this, "Đã xóa bài đăng", Toast.LENGTH_SHORT).show();
                myPostsList.remove(room);
                adapter.notifyDataSetChanged();
                updateUI();
            })
            .addOnFailureListener(e -> {
                showLoading(false);
                Toast.makeText(this, "Lỗi xóa bài đăng", Toast.LENGTH_SHORT).show();
            });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            // Reload posts after editing
            loadMyPosts();
        }
    }

    // ==================== ADAPTER ====================

    public static class MyPostsAdapter extends RecyclerView.Adapter<MyPostsAdapter.PostViewHolder> {

        private List<Room> posts;
        private OnPostClickListener listener;

        public interface OnPostClickListener {
            void onPostClick(Room room);
            void onEditClick(Room room);
            void onDeleteClick(Room room);
        }

        public MyPostsAdapter(List<Room> posts, OnPostClickListener listener) {
            this.posts = posts;
            this.listener = listener;
        }

        @NonNull
        @Override
        public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_my_post, parent, false);
            return new PostViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
            holder.bind(posts.get(position));
        }

        @Override
        public int getItemCount() {
            return posts.size();
        }

        class PostViewHolder extends RecyclerView.ViewHolder {
            ImageView ivThumbnail;
            TextView tvTitle, tvPrice, tvAddress, tvDate, tvStatus, tvViews;
            ImageButton btnEdit, btnDelete;

            public PostViewHolder(@NonNull View itemView) {
                super(itemView);
                ivThumbnail = itemView.findViewById(R.id.ivThumbnail);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvPrice = itemView.findViewById(R.id.tvPrice);
                tvAddress = itemView.findViewById(R.id.tvAddress);
                tvDate = itemView.findViewById(R.id.tvDate);
                tvStatus = itemView.findViewById(R.id.tvStatus);
                tvViews = itemView.findViewById(R.id.tvViews);
                btnEdit = itemView.findViewById(R.id.btnEdit);
                btnDelete = itemView.findViewById(R.id.btnDelete);
            }

            void bind(Room room) {
                tvTitle.setText(room.getTitle());
                tvPrice.setText(room.getPriceDisplay());
                tvAddress.setText(room.getFullAddress());
                tvViews.setText(room.getViewCount() + " lượt xem");

                // Format date
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                tvDate.setText(sdf.format(new Date(room.getCreatedAt())));

                // Status
                if (room.isAvailable()) {
                    tvStatus.setText("Đang hiển thị");
                    tvStatus.setBackgroundResource(R.drawable.bg_status_active);
                } else {
                    tvStatus.setText("Đã ẩn");
                    tvStatus.setBackgroundResource(R.drawable.bg_status_inactive);
                }

                // Load thumbnail
                if (room.getThumbnailUrl() != null && !room.getThumbnailUrl().isEmpty()) {
                    Glide.with(itemView.getContext())
                        .load(room.getThumbnailUrl())
                        .placeholder(R.drawable.ic_image_placeholder)
                        .error(R.drawable.ic_image_placeholder)
                        .centerCrop()
                        .into(ivThumbnail);
                } else {
                    ivThumbnail.setImageResource(R.drawable.ic_image_placeholder);
                }

                // Click listeners
                itemView.setOnClickListener(v -> listener.onPostClick(room));
                btnEdit.setOnClickListener(v -> listener.onEditClick(room));
                btnDelete.setOnClickListener(v -> listener.onDeleteClick(room));
            }
        }
    }
}
