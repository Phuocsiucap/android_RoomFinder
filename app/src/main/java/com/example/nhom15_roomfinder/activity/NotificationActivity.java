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
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nhom15_roomfinder.R;
import com.example.nhom15_roomfinder.entity.Notification;
import com.example.nhom15_roomfinder.firebase.FirebaseManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * NotificationActivity - Màn hình danh sách thông báo
 * Hỗ trợ lọc theo tab: Tất cả, Booking (lịch hẹn), Chat, Duyệt tin
 */
public class NotificationActivity extends AppCompatActivity {

    private static final String TAG = "NotificationActivity";
    
    // Filter constants
    private static final String FILTER_ALL = "ALL";
    private static final String FILTER_BOOKING = "BOOKING";
    private static final String FILTER_CHAT = "CHAT";
    private static final String FILTER_APPROVE = "APPROVE";

    private ImageButton btnBack;
    private RecyclerView rvNotifications;
    private LinearLayout emptyStateLayout;
    private ProgressBar progressBar;
    private TextView tvTitle;
    
    // Tabs
    private TextView tabAll, tabBooking, tabChat, tabApprove;
    private String currentFilter = FILTER_ALL;

    private FirebaseManager firebaseManager;
    private String currentUserId;
    private List<Notification> notificationList = new ArrayList<>();
    private List<Notification> allNotifications = new ArrayList<>(); // Store all notifications for filtering
    private NotificationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        firebaseManager = FirebaseManager.getInstance();
        currentUserId = firebaseManager.getUserId();

        if (currentUserId == null) {
            redirectToLogin();
            return;
        }

        initViews();
        setupRecyclerView();
        setListeners();
        loadNotifications();
    }

    private void initViews() {
        // Using existing layout IDs
        rvNotifications = findViewById(R.id.recyclerNotification);
        
        // Tabs for filtering
        tabAll = findViewById(R.id.tabAll);
        tabBooking = findViewById(R.id.tabBooking);
        tabChat = findViewById(R.id.tabChat);
        tabApprove = findViewById(R.id.tabApprove);
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter(notificationList, notification -> {
            // Mark as read
            markAsRead(notification);
            
            // Handle click based on type
            handleNotificationClick(notification);
        });

        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        rvNotifications.setAdapter(adapter);
    }

    private void setListeners() {
        // Tab click listeners
        if (tabAll != null) {
            tabAll.setOnClickListener(v -> {
                setActiveTab(FILTER_ALL);
                applyFilter();
            });
        }
        
        if (tabBooking != null) {
            tabBooking.setOnClickListener(v -> {
                setActiveTab(FILTER_BOOKING);
                applyFilter();
            });
        }
        
        if (tabChat != null) {
            tabChat.setOnClickListener(v -> {
                setActiveTab(FILTER_CHAT);
                applyFilter();
            });
        }
        
        if (tabApprove != null) {
            tabApprove.setOnClickListener(v -> {
                setActiveTab(FILTER_APPROVE);
                applyFilter();
            });
        }
    }
    
    /**
     * Cập nhật UI cho tab đang active
     */
    private void setActiveTab(String filter) {
        currentFilter = filter;
        
        // Reset all tabs
        if (tabAll != null) tabAll.setBackgroundResource(R.drawable.bg_tab_unselected);
        if (tabBooking != null) tabBooking.setBackgroundResource(R.drawable.bg_tab_unselected);
        if (tabChat != null) tabChat.setBackgroundResource(R.drawable.bg_tab_unselected);
        if (tabApprove != null) tabApprove.setBackgroundResource(R.drawable.bg_tab_unselected);
        
        // Set active tab
        switch (filter) {
            case FILTER_ALL:
                if (tabAll != null) tabAll.setBackgroundResource(R.drawable.bg_tab_selected);
                break;
            case FILTER_BOOKING:
                if (tabBooking != null) tabBooking.setBackgroundResource(R.drawable.bg_tab_selected);
                break;
            case FILTER_CHAT:
                if (tabChat != null) tabChat.setBackgroundResource(R.drawable.bg_tab_selected);
                break;
            case FILTER_APPROVE:
                if (tabApprove != null) tabApprove.setBackgroundResource(R.drawable.bg_tab_selected);
                break;
        }
    }
    
    /**
     * Lọc thông báo theo tab đang chọn
     */
    private void applyFilter() {
        notificationList.clear();
        
        for (Notification notification : allNotifications) {
            boolean shouldShow = false;
            
            switch (currentFilter) {
                case FILTER_ALL:
                    shouldShow = true;
                    break;
                case FILTER_BOOKING:
                    // Hiển thị các thông báo về lịch hẹn
                    shouldShow = notification.getType() != null && (
                        notification.getType().equals(Notification.TYPE_APPOINTMENT_REQUEST) ||
                        notification.getType().equals(Notification.TYPE_APPOINTMENT_ACCEPTED) ||
                        notification.getType().equals(Notification.TYPE_APPOINTMENT_REJECTED)
                    );
                    break;
                case FILTER_CHAT:
                    // Hiển thị các thông báo tin nhắn
                    shouldShow = notification.getType() != null &&
                        notification.getType().equals(Notification.TYPE_NEW_MESSAGE);
                    break;
                case FILTER_APPROVE:
                    // Hiển thị các thông báo hệ thống
                    shouldShow = notification.getType() != null &&
                        notification.getType().equals(Notification.TYPE_SYSTEM);
                    break;
            }
            
            if (shouldShow) {
                notificationList.add(notification);
            }
        }
        
        adapter.notifyDataSetChanged();
        updateUI();
    }

    private void loadNotifications() {
        showLoading(true);

        firebaseManager.getFirestore()
            .collection("notifications")
            .whereEqualTo("userId", currentUserId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(50)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                showLoading(false);
                allNotifications.clear();
                notificationList.clear();

                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    Notification notification = doc.toObject(Notification.class);
                    if (notification != null) {
                        notification.setId(doc.getId());
                        allNotifications.add(notification);
                    }
                }

                // Apply current filter
                applyFilter();
            })
            .addOnFailureListener(e -> {
                showLoading(false);
                Log.e(TAG, "Error loading notifications: " + e.getMessage());
                
                // Nếu lỗi do index, thử load không có orderBy
                loadNotificationsWithoutOrder();
            });
    }
    
    /**
     * Load thông báo không có orderBy (phòng trường hợp chưa có index)
     */
    private void loadNotificationsWithoutOrder() {
        firebaseManager.getFirestore()
            .collection("notifications")
            .whereEqualTo("userId", currentUserId)
            .limit(50)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                allNotifications.clear();
                notificationList.clear();

                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    Notification notification = doc.toObject(Notification.class);
                    if (notification != null) {
                        notification.setId(doc.getId());
                        allNotifications.add(notification);
                    }
                }
                
                // Sort on client side
                allNotifications.sort((n1, n2) -> Long.compare(n2.getCreatedAt(), n1.getCreatedAt()));

                // Apply current filter
                applyFilter();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error loading notifications without order: " + e.getMessage());
            });
    }

    private void markAsRead(Notification notification) {
        if (notification.isRead()) return;

        firebaseManager.getFirestore()
            .collection("notifications")
            .document(notification.getId())
            .update("isRead", true)
            .addOnSuccessListener(aVoid -> {
                notification.setRead(true);
                adapter.notifyDataSetChanged();
            });
    }

    private void handleNotificationClick(Notification notification) {
        switch (notification.getType()) {
            case Notification.TYPE_APPOINTMENT_REQUEST:
                // Mở chi tiết lịch hẹn
                Intent appointmentIntent = new Intent(this, AppointmentDetailActivity.class);
                appointmentIntent.putExtra("appointmentId", notification.getAppointmentId());
                appointmentIntent.putExtra("notification", notification);
                startActivity(appointmentIntent);
                break;
                
            case Notification.TYPE_APPOINTMENT_ACCEPTED:
            case Notification.TYPE_APPOINTMENT_REJECTED:
                // Mở chi tiết lịch hẹn
                Intent intent2 = new Intent(this, AppointmentDetailActivity.class);
                intent2.putExtra("appointmentId", notification.getAppointmentId());
                startActivity(intent2);
                break;
                
            case Notification.TYPE_NEW_MESSAGE:
                // Mở chat
                Intent chatIntent = new Intent(this, ChatDetailActivity.class);
                chatIntent.putExtra("recipientId", notification.getSenderId());
                chatIntent.putExtra("recipientName", notification.getSenderName());
                startActivity(chatIntent);
                break;
                
            default:
                // Mở chi tiết phòng nếu có roomId
                if (notification.getRoomId() != null) {
                    Intent roomIntent = new Intent(this, PropertyDetailActivity.class);
                    roomIntent.putExtra("roomId", notification.getRoomId());
                    startActivity(roomIntent);
                }
                break;
        }
    }

    private void updateUI() {
        // No empty state layout in existing layout
        // RecyclerView is always visible
    }

    private void showLoading(boolean show) {
        // No progress bar in existing layout
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // ==================== ADAPTER ====================

    public static class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

        private List<Notification> notifications;
        private OnNotificationClickListener listener;

        public interface OnNotificationClickListener {
            void onClick(Notification notification);
        }

        public NotificationAdapter(List<Notification> notifications, OnNotificationClickListener listener) {
            this.notifications = notifications;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_notification, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.bind(notifications.get(position));
        }

        @Override
        public int getItemCount() {
            return notifications.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivIcon;
            View ivUnread;
            TextView tvTitle, tvMessage, tvTime;
            View rootView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                rootView = itemView;
                ivIcon = itemView.findViewById(R.id.icon);
                ivUnread = itemView.findViewById(R.id.unreadDot);
                tvTitle = itemView.findViewById(R.id.title);
                tvMessage = itemView.findViewById(R.id.message);
                tvTime = itemView.findViewById(R.id.time);
            }

            void bind(Notification notification) {
                tvTitle.setText(notification.getTitle());
                tvMessage.setText(notification.getMessage());
                
                // Format time
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());
                tvTime.setText(sdf.format(new Date(notification.getCreatedAt())));
                
                // Unread indicator
                ivUnread.setVisibility(notification.isRead() ? View.GONE : View.VISIBLE);
                
                // Background based on read status
                if (!notification.isRead()) {
                    rootView.setBackgroundColor(itemView.getContext().getColor(R.color.notification_unread_bg));
                } else {
                    rootView.setBackgroundColor(itemView.getContext().getColor(android.R.color.white));
                }
                
                // Set icon based on type
                switch (notification.getType()) {
                    case Notification.TYPE_APPOINTMENT_REQUEST:
                        ivIcon.setImageResource(R.drawable.ic_calendar);
                        break;
                    case Notification.TYPE_APPOINTMENT_ACCEPTED:
                        ivIcon.setImageResource(R.drawable.ic_check_circle);
                        break;
                    case Notification.TYPE_APPOINTMENT_REJECTED:
                        ivIcon.setImageResource(R.drawable.ic_cancel);
                        break;
                    case Notification.TYPE_NEW_MESSAGE:
                        ivIcon.setImageResource(R.drawable.ic_message);
                        break;
                    default:
                        ivIcon.setImageResource(R.drawable.ic_notification);
                        break;
                }
                
                rootView.setOnClickListener(v -> listener.onClick(notification));
            }
        }
    }
}
