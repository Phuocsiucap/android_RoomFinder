package com.example.nhom15_roomfinder.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat; // Thêm import này
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom15_roomfinder.R;
import com.example.nhom15_roomfinder.adapter.ChatAdapter;
import com.example.nhom15_roomfinder.entity.Chat;
import com.example.nhom15_roomfinder.firebase.FirebaseManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ChatListActivity - Màn hình danh sách chat
 * Hiển thị tất cả cuộc trò chuyện của người dùng
 */
public class ChatListActivity extends AppCompatActivity {

    private static final String TAG = "ChatListActivity";
    
    private RecyclerView rvChatList;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private BottomNavigationView bottomNavigation;
    private EditText etSearch;
    private ImageButton btnFilter;

    private ChatAdapter chatAdapter;
    private List<Chat> chatList;
    private List<Chat> originalList; // Danh sách gốc để phục hồi khi tìm kiếm
    private FirebaseManager firebaseManager;
    private String currentUserId;

    private boolean isFilterUnread = false; // Trạng thái lọc chưa đọc

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        firebaseManager = FirebaseManager.getInstance();
        currentUserId = firebaseManager.getUserId();

        if (currentUserId == null) {
            redirectToLogin();
            return;
        }

        initViews();
        setupRecyclerView();
        setupBottomNavigation();
        setupSearchAndFilter();
        loadChats();
    }

    private void initViews() {
        rvChatList = findViewById(R.id.rvChatList);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        etSearch = findViewById(R.id.etSearch);
        btnFilter = findViewById(R.id.btnFilter);
    }

    private void setupRecyclerView() {
        chatList = new ArrayList<>();
        originalList = new ArrayList<>();
        
        chatAdapter = new ChatAdapter(chatList, new ChatAdapter.OnChatClickListener() {
            @Override
            public void onChatClick(Chat chat) {
                // Mở ChatDetailActivity
                Intent intent = new Intent(ChatListActivity.this, ChatDetailActivity.class);
                intent.putExtra("chatId", chat.getId());
                intent.putExtra("recipientId", chat.getRecipientId());
                intent.putExtra("recipientName", chat.getRecipientName());
                intent.putExtra("recipientAvatar", chat.getRecipientAvatar());
                if (chat.getRoomId() != null) intent.putExtra("roomId", chat.getRoomId());
                if (chat.getRoomTitle() != null) intent.putExtra("roomTitle", chat.getRoomTitle());
                
                startActivity(intent);
            }
        }, new ChatAdapter.OnChatLongClickListener() {
            @Override
            public void onChatLongClick(Chat chat, View view) {
                showChatOptions(chat, view);
            }
        });

        rvChatList.setLayoutManager(new LinearLayoutManager(this));
        rvChatList.setAdapter(chatAdapter);
    }

    private void showChatOptions(Chat chat, View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenu().add(0, 1, 0, chat.isPinned() ? "Bỏ ghim" : "Ghim cuộc trò chuyện");
        popup.getMenu().add(0, 2, 1, "Xóa cuộc trò chuyện");

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) {
                togglePinChat(chat);
                return true;
            } else if (item.getItemId() == 2) {
                confirmDeleteChat(chat);
                return true;
            }
            return false;
        });
        popup.show();
    }

    // Chức năng Ghim
    private void togglePinChat(Chat chat) {
        // Lưu trạng thái ghim vào SharedPreferences hoặc Firebase (để đơn giản ta lưu vào Firebase node riêng hoặc local)
        // Ở đây ví dụ lưu vào local list và refresh, nếu muốn lưu vĩnh viễn cần update Firebase
        boolean newStatus = !chat.isPinned();
        
        // Cập nhật Firebase (giả sử có node pinnedChats)
        firebaseManager.getDatabaseReference("pinnedChats")
            .child(currentUserId)
            .child(chat.getId())
            .setValue(newStatus ? true : null)
            .addOnSuccessListener(aVoid -> {
                chat.setPinned(newStatus);
                sortAndFilterList();
                Toast.makeText(this, newStatus ? "Đã ghim" : "Đã bỏ ghim", Toast.LENGTH_SHORT).show();
            });
    }

    // Chức năng Xóa
    private void confirmDeleteChat(Chat chat) {
        new AlertDialog.Builder(this)
            .setTitle("Xóa cuộc trò chuyện?")
            .setMessage("Bạn có chắc chắn muốn xóa cuộc trò chuyện này không?")
            .setPositiveButton("Xóa", (dialog, which) -> {
                deleteChat(chat);
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void deleteChat(Chat chat) {
        // Xóa participant (ẩn chat với user hiện tại)
        firebaseManager.getDatabaseReference("chats")
            .child(chat.getId())
            .child("participants")
            .child(currentUserId)
            .removeValue()
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Đã xóa cuộc trò chuyện", Toast.LENGTH_SHORT).show();
                // List sẽ tự update nhờ ValueEventListener
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Lỗi khi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void setupSearchAndFilter() {
        // Tìm kiếm
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterChats(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Lọc tin chưa đọc
        btnFilter.setOnClickListener(v -> {
            isFilterUnread = !isFilterUnread;
            // Sử dụng ContextCompat.getColor để tránh lỗi deprecated
            int color = isFilterUnread ? 
                ContextCompat.getColor(this, R.color.colorAccent) : // Dùng colorAccent (hồng) có sẵn
                ContextCompat.getColor(this, android.R.color.black);
                
            btnFilter.setColorFilter(color); 
            
            sortAndFilterList();
            
            if (isFilterUnread) {
                Toast.makeText(this, "Đang lọc tin chưa đọc", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterChats(String query) {
        if (originalList == null) return;
        
        List<Chat> filteredList = new ArrayList<>();
        String lowerCaseQuery = query.toLowerCase().trim();

        for (Chat chat : originalList) {
            boolean matchesSearch = chat.getRecipientName().toLowerCase().contains(lowerCaseQuery) ||
                                    (chat.getLastMessage() != null && chat.getLastMessage().toLowerCase().contains(lowerCaseQuery));
            
            boolean matchesFilter = !isFilterUnread || chat.getUnreadCount() > 0;

            if (matchesSearch && matchesFilter) {
                filteredList.add(chat);
            }
        }
        
        chatAdapter.updateData(filteredList);
        updateEmptyState();
    }

    // Hàm load và lắng nghe trạng thái ghim
    private void loadChats() {
        showLoading(true);
        
        // Lấy danh sách pinned trước
        firebaseManager.getDatabaseReference("pinnedChats").child(currentUserId)
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot pinnedSnapshot) {
                    List<String> pinnedChatIds = new ArrayList<>();
                    for (DataSnapshot snapshot : pinnedSnapshot.getChildren()) {
                        pinnedChatIds.add(snapshot.getKey());
                    }
                    
                    // Sau khi có danh sách pinned, load chats
                    loadAllChats(pinnedChatIds);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    loadAllChats(new ArrayList<>()); // Load bình thường nếu lỗi
                }
            });
    }

    private void loadAllChats(List<String> pinnedChatIds) {
        DatabaseReference chatsRef = firebaseManager.getDatabaseReference("chats");
        chatsRef.orderByChild("participants/" + currentUserId).equalTo(true)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        originalList.clear();
                        
                        for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
                            try {
                                Chat chat = parseChatFromSnapshot(chatSnapshot);
                                if (chat != null) {
                                    // Check pinned status
                                    chat.setPinned(pinnedChatIds.contains(chat.getId()));
                                    originalList.add(chat);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing chat: " + e.getMessage());
                            }
                        }
                        
                        sortAndFilterList();
                        showLoading(false);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error loading chats: " + error.getMessage());
                        showLoading(false);
                    }
                });
    }

    private void sortAndFilterList() {
        // Sắp xếp: Pinned lên đầu, sau đó đến thời gian mới nhất
        Collections.sort(originalList, (c1, c2) -> {
            if (c1.isPinned() && !c2.isPinned()) return -1;
            if (!c1.isPinned() && c2.isPinned()) return 1;
            return Long.compare(c2.getLastMessageTime(), c1.getLastMessageTime());
        });

        // Áp dụng bộ lọc hiện tại (Search text + Unread filter)
        filterChats(etSearch.getText().toString());
    }
    
    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_messages);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, HomeActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_search) {
                startActivity(new Intent(this, SearchActivity.class));
                return true;
            } else if (itemId == R.id.nav_favorites) {
                startActivity(new Intent(this, FavoriteActivity.class));
                return true;
            } else if (itemId == R.id.nav_messages) {
                return true; // Đang ở trang này
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    /**
     * Parse Chat từ DataSnapshot
     */
    private Chat parseChatFromSnapshot(DataSnapshot snapshot) {
        Chat chat = new Chat();
        chat.setId(snapshot.getKey());
        
        // Lấy thông tin người nhận (người còn lại trong cuộc trò chuyện)
        DataSnapshot participantsSnapshot = snapshot.child("participants");
        for (DataSnapshot participant : participantsSnapshot.getChildren()) {
            String oderId = participant.getKey();
            if (!oderId.equals(currentUserId)) {
                chat.setRecipientId(oderId);
                break;
            }
        }
        
        // Lấy thông tin từ recipientInfo
        DataSnapshot recipientInfo = snapshot.child("recipientInfo").child(chat.getRecipientId() != null ? chat.getRecipientId() : "");
        if (recipientInfo.exists()) {
            chat.setRecipientName(recipientInfo.child("name").getValue(String.class));
            chat.setRecipientAvatar(recipientInfo.child("avatar").getValue(String.class));
        } else {
            // Fallback nếu không tìm thấy info trong chat node
            chat.setRecipientName("User");
        }
        
        // Lấy tin nhắn cuối
        chat.setLastMessage(snapshot.child("lastMessage").getValue(String.class));
        Long lastMessageTime = snapshot.child("lastMessageTime").getValue(Long.class);
        chat.setLastMessageTime(lastMessageTime != null ? lastMessageTime : 0);
        
        // Lấy số tin chưa đọc
        Long unreadCount = snapshot.child("unreadCount").child(currentUserId).getValue(Long.class);
        chat.setUnreadCount(unreadCount != null ? unreadCount.intValue() : 0);
        
        // Lấy thông tin phòng liên quan
        chat.setRoomId(snapshot.child("roomId").getValue(String.class));
        chat.setRoomTitle(snapshot.child("roomTitle").getValue(String.class));
        
        return chat;
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void updateEmptyState() {
        if (tvEmpty != null) {
            tvEmpty.setVisibility(chatAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        }
        rvChatList.setVisibility(chatAdapter.getItemCount() == 0 ? View.GONE : View.VISIBLE);
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigation.setSelectedItemId(R.id.nav_messages);
    }
}
