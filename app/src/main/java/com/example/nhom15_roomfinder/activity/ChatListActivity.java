package com.example.nhom15_roomfinder.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

    private ChatAdapter chatAdapter;
    private List<Chat> chatList;
    private FirebaseManager firebaseManager;
    private String currentUserId;

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
        loadChats();
    }

    private void initViews() {
        rvChatList = findViewById(R.id.rvChatList);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void setupRecyclerView() {
        chatList = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatList, chat -> {
            // Mở ChatDetailActivity
            Intent intent = new Intent(ChatListActivity.this, ChatDetailActivity.class);
            intent.putExtra("chatId", chat.getId());
            intent.putExtra("recipientId", chat.getRecipientId());
            intent.putExtra("recipientName", chat.getRecipientName());
            intent.putExtra("recipientAvatar", chat.getRecipientAvatar());
            startActivity(intent);
        });

        rvChatList.setLayoutManager(new LinearLayoutManager(this));
        rvChatList.setAdapter(chatAdapter);
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
     * Load danh sách chat từ Firebase Realtime Database
     */
    private void loadChats() {
        showLoading(true);
        
        DatabaseReference chatsRef = firebaseManager.getDatabaseReference("chats");
        chatsRef.orderByChild("participants/" + currentUserId).equalTo(true)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        chatList.clear();
                        
                        for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
                            try {
                                Chat chat = parseChatFromSnapshot(chatSnapshot);
                                if (chat != null) {
                                    chatList.add(chat);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing chat: " + e.getMessage());
                            }
                        }
                        
                        // Sắp xếp theo thời gian tin nhắn cuối (mới nhất lên đầu)
                        Collections.sort(chatList, (c1, c2) -> 
                            Long.compare(c2.getLastMessageTime(), c1.getLastMessageTime()));
                        
                        chatAdapter.updateData(chatList);
                        showLoading(false);
                        updateEmptyState();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error loading chats: " + error.getMessage());
                        showLoading(false);
                        Toast.makeText(ChatListActivity.this, 
                            "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                    }
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
        
        // Lấy thông tin từ recipientInfo - Lấy tên của người còn lại (không phải mình)
        String recipientId = chat.getRecipientId();
        DataSnapshot recipientInfoSnapshot = snapshot.child("recipientInfo").child(recipientId);
        if (recipientInfoSnapshot.exists()) {
            String name = recipientInfoSnapshot.child("name").getValue(String.class);
            String avatar = recipientInfoSnapshot.child("avatar").getValue(String.class);
            chat.setRecipientName(name);
            chat.setRecipientAvatar(avatar);
        }
        
        // Nếu không có tên, load từ Firestore
        if (chat.getRecipientName() == null || chat.getRecipientName().isEmpty() 
                || "You".equals(chat.getRecipientName()) || "User".equals(chat.getRecipientName())) {
            loadRecipientNameFromFirestore(chat);
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

    /**
     * Load tên người nhận từ Firestore users collection
     */
    private void loadRecipientNameFromFirestore(Chat chat) {
        if (chat.getRecipientId() == null) return;
        
        firebaseManager.getFirestore()
            .collection("users")
            .document(chat.getRecipientId())
            .get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    String name = doc.getString("name");
                    if (name == null || name.isEmpty()) {
                        name = doc.getString("email");
                    }
                    String avatar = doc.getString("photoUrl");
                    
                    chat.setRecipientName(name);
                    chat.setRecipientAvatar(avatar);
                    chatAdapter.notifyDataSetChanged();
                }
            })
            .addOnFailureListener(e -> Log.e(TAG, "Error loading recipient: " + e.getMessage()));
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void updateEmptyState() {
        if (tvEmpty != null) {
            tvEmpty.setVisibility(chatList.isEmpty() ? View.VISIBLE : View.GONE);
        }
        rvChatList.setVisibility(chatList.isEmpty() ? View.GONE : View.VISIBLE);
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
