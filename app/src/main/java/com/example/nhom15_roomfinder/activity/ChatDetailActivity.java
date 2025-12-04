package com.example.nhom15_roomfinder.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nhom15_roomfinder.R;
import com.example.nhom15_roomfinder.entity.ChatMessage;
import com.example.nhom15_roomfinder.firebase.FirebaseManager;
import com.example.nhom15_roomfinder.utils.ImageUploadHelper;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ChatDetailActivity extends AppCompatActivity {

    private static final String TAG = "ChatDetailActivity";

    private ImageButton btnBack, btnSend, btnAttach;
    private EditText etMessage;
    private RecyclerView rvMessages;
    private TextView tvChatTitle;

    private ChatMessageAdapter adapter;
    private List<ChatMessage> messageList;
    private FirebaseManager firebaseManager;
    private String currentUserId;
    
    // Chat Info
    private String chatId;
    private String recipientId;
    private String recipientName;
    private String roomId;
    private String roomTitle;

    // Image Picker
    private ActivityResultLauncher<String> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);

        firebaseManager = FirebaseManager.getInstance();
        currentUserId = firebaseManager.getUserId();

        if (currentUserId == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        getIntentData();
        initViews();
        setupRecyclerView();
        setupImagePicker();
        
        if (chatId != null) {
            loadMessages(chatId);
        } else {
            findChatId();
        }

        setListeners();
    }

    private void getIntentData() {
        if (getIntent() != null) {
            chatId = getIntent().getStringExtra("chatId");
            recipientId = getIntent().getStringExtra("recipientId");
            recipientName = getIntent().getStringExtra("recipientName");
            roomId = getIntent().getStringExtra("roomId");
            roomTitle = getIntent().getStringExtra("roomTitle");
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnSend = findViewById(R.id.btnSend);
        btnAttach = findViewById(R.id.btnAttach);
        etMessage = findViewById(R.id.etMessage);
        rvMessages = findViewById(R.id.rvMessages);
        tvChatTitle = findViewById(R.id.tvChatTitle);
        
        if (recipientName != null && !recipientName.isEmpty()) {
            tvChatTitle.setText(recipientName);
        } else if (recipientId != null) {
            // Load tên người nhận từ Firestore
            loadRecipientName();
        }
    }

    /**
     * Load tên người nhận từ Firestore users collection
     */
    private void loadRecipientName() {
        firebaseManager.getFirestore()
            .collection("users")
            .document(recipientId)
            .get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    recipientName = doc.getString("name");
                    if (recipientName == null || recipientName.isEmpty()) {
                        recipientName = doc.getString("email");
                    }
                    if (recipientName != null) {
                        tvChatTitle.setText(recipientName);
                    }
                }
            })
            .addOnFailureListener(e -> Log.e(TAG, "Error loading recipient name: " + e.getMessage()));
    }

    private void setupRecyclerView() {
        messageList = new ArrayList<>();
        adapter = new ChatMessageAdapter(messageList, currentUserId);
        
        // Thêm listener để xem ảnh full khi click
        adapter.setOnImageClickListener(imageUrl -> showFullImageDialog(imageUrl));
        
        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        rvMessages.setAdapter(adapter);
    }

    /**
     * Hiển thị dialog xem ảnh full với khả năng zoom
     */
    private void showFullImageDialog(String imageUrl) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_image_viewer);
        
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        
        PhotoView photoView = dialog.findViewById(R.id.photoView);
        ImageButton btnClose = dialog.findViewById(R.id.btnClose);
        
        // Load ảnh với Glide
        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_image_placeholder)
                .into(photoView);
        
        btnClose.setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }

    private void setupImagePicker() {
        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                uploadImageAndSend(uri);
            }
        });
    }

    private void uploadImageAndSend(Uri imageUri) {
        Toast.makeText(this, "Đang gửi ảnh...", Toast.LENGTH_SHORT).show();
        
        // Sử dụng Cloudinary thay vì Firebase Storage
        String chatFolder = chatId != null ? chatId : "temp_" + System.currentTimeMillis();
        
        ImageUploadHelper.uploadChatImage(this, imageUri, chatFolder, 
            new ImageUploadHelper.SingleUploadCallback() {
                @Override
                public void onSuccess(String imageUrl) {
                    // Upload thành công, gửi tin nhắn chứa link ảnh
                    if (chatId == null) {
                        createNewChatWithImage(imageUrl);
                    } else {
                        sendImageMessage(chatId, imageUrl);
                    }
                    Toast.makeText(ChatDetailActivity.this, "Đã gửi ảnh", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(ChatDetailActivity.this, "Lỗi gửi ảnh: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        );
    }

    private void findChatId() {
        if (recipientId == null) return;

        DatabaseReference chatsRef = firebaseManager.getDatabaseReference("chats");
        chatsRef.orderByChild("participants/" + currentUserId).equalTo(true)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
                            if (chatSnapshot.child("participants").hasChild(recipientId)) {
                                chatId = chatSnapshot.getKey();
                                loadMessages(chatId);
                                return;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error finding chat: " + error.getMessage());
                    }
                });
    }

    private void loadMessages(String chatId) {
        DatabaseReference messagesRef = firebaseManager.getDatabaseReference("chats/" + chatId + "/messages");
        messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot msgSnapshot : snapshot.getChildren()) {
                    ChatMessage msg = msgSnapshot.getValue(ChatMessage.class);
                    if (msg != null) {
                        messageList.add(msg);
                    }
                }
                adapter.notifyDataSetChanged();
                if (!messageList.isEmpty()) {
                    rvMessages.smoothScrollToPosition(messageList.size() - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading messages: " + error.getMessage());
            }
        });
    }

    private void setListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnSend.setOnClickListener(v -> {
            String msgContent = etMessage.getText().toString().trim();
            if (TextUtils.isEmpty(msgContent)) return;

            if (chatId == null) {
                createNewChat(msgContent);
            } else {
                sendMessage(chatId, msgContent);
            }
            etMessage.setText("");
        });
        
        btnAttach.setOnClickListener(v -> {
            pickImageLauncher.launch("image/*");
        });
    }

    private void createNewChat(String firstMessage) {
        initChatCreation(firstMessage, null);
    }
    
    private void createNewChatWithImage(String imageUrl) {
        initChatCreation("", imageUrl);
    }

    private void initChatCreation(String messageContent, String imageUrl) {
        DatabaseReference chatsRef = firebaseManager.getDatabaseReference("chats");
        chatId = chatsRef.push().getKey();
        
        Map<String, Object> chatData = new HashMap<>();
        
        Map<String, Boolean> participants = new HashMap<>();
        participants.put(currentUserId, true);
        participants.put(recipientId, true);
        chatData.put("participants", participants);
        
        if (roomId != null) chatData.put("roomId", roomId);
        if (roomTitle != null) chatData.put("roomTitle", roomTitle);
        
        Map<String, Object> recipientInfo = new HashMap<>();
        Map<String, String> user1 = new HashMap<>();
        user1.put("name", "You"); 
        
        Map<String, String> user2 = new HashMap<>();
        user2.put("name", recipientName != null ? recipientName : "User");
        
        recipientInfo.put(currentUserId, user1);
        recipientInfo.put(recipientId, user2);
        chatData.put("recipientInfo", recipientInfo);
        
        chatsRef.child(chatId).setValue(chatData).addOnSuccessListener(aVoid -> {
            if (imageUrl != null) {
                sendImageMessage(chatId, imageUrl);
            } else {
                sendMessage(chatId, messageContent);
            }
            loadMessages(chatId);
        });
    }

    private void sendMessage(String chatId, String messageContent) {
        DatabaseReference chatRef = firebaseManager.getDatabaseReference("chats/" + chatId);
        String msgId = chatRef.child("messages").push().getKey();
        ChatMessage chatMessage = new ChatMessage(currentUserId, messageContent, System.currentTimeMillis());
        
        if (msgId != null) {
            chatRef.child("messages").child(msgId).setValue(chatMessage);
            updateLastMessage(chatRef, messageContent);
        }
    }

    private void sendImageMessage(String chatId, String imageUrl) {
        DatabaseReference chatRef = firebaseManager.getDatabaseReference("chats/" + chatId);
        String msgId = chatRef.child("messages").push().getKey();

        // Gửi tin nhắn chỉ có ảnh (không có text)
        ChatMessage chatMessage = new ChatMessage(currentUserId, "", System.currentTimeMillis());
        chatMessage.setImageUrl(imageUrl);

        if (msgId != null) {
            chatRef.child("messages").child(msgId).setValue(chatMessage);
            updateLastMessage(chatRef, "📷 Hình ảnh");
        }
    }


    private void updateLastMessage(DatabaseReference chatRef, String content) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("lastMessage", content);
        updates.put("lastMessageTime", System.currentTimeMillis());
        chatRef.updateChildren(updates);
    }
}
