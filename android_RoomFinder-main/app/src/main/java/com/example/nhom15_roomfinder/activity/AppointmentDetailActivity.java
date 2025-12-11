package com.example.nhom15_roomfinder.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.nhom15_roomfinder.R;
import com.example.nhom15_roomfinder.entity.Appointment;
import com.example.nhom15_roomfinder.entity.Notification;
import com.example.nhom15_roomfinder.firebase.FirebaseManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * AppointmentDetailActivity - Chi tiết lịch hẹn
 * Cho phép chủ trọ chấp nhận/từ chối lịch hẹn
 */
public class AppointmentDetailActivity extends AppCompatActivity {

    private static final String TAG = "AppointmentDetail";

    private ImageButton btnBack;
    private ImageView ivRoomThumbnail;
    private TextView tvRoomTitle, tvRequesterName, tvRequesterPhone;
    private TextView tvAppointmentDate, tvAppointmentTime, tvNote;
    private TextView tvStatus;
    private LinearLayout layoutActions;
    private Button btnAccept, btnReject, btnMessage;
    private ProgressBar progressBar;

    private FirebaseManager firebaseManager;
    private String currentUserId;
    private String appointmentId;
    private Appointment appointment;
    private Notification notification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_detail);

        firebaseManager = FirebaseManager.getInstance();
        currentUserId = firebaseManager.getUserId();

        if (currentUserId == null) {
            redirectToLogin();
            return;
        }

        getIntentData();
        initViews();
        setListeners();
        loadAppointment();
    }

    private void getIntentData() {
        appointmentId = getIntent().getStringExtra("appointmentId");
        notification = (Notification) getIntent().getSerializableExtra("notification");
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        ivRoomThumbnail = findViewById(R.id.ivRoomThumbnail);
        tvRoomTitle = findViewById(R.id.tvRoomTitle);
        tvRequesterName = findViewById(R.id.tvRequesterName);
        tvRequesterPhone = findViewById(R.id.tvRequesterPhone);
        tvAppointmentDate = findViewById(R.id.tvAppointmentDate);
        tvAppointmentTime = findViewById(R.id.tvAppointmentTime);
        tvNote = findViewById(R.id.tvNote);
        tvStatus = findViewById(R.id.tvStatus);
        layoutActions = findViewById(R.id.layoutActions);
        btnAccept = findViewById(R.id.btnAccept);
        btnReject = findViewById(R.id.btnReject);
        btnMessage = findViewById(R.id.btnMessage);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnAccept.setOnClickListener(v -> acceptAppointment());
        btnReject.setOnClickListener(v -> showRejectDialog());
        btnMessage.setOnClickListener(v -> openChat());
    }

    private void loadAppointment() {
        if (appointmentId == null) {
            Toast.makeText(this, "Không tìm thấy lịch hẹn", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        showLoading(true);

        firebaseManager.getFirestore()
            .collection("appointments")
            .document(appointmentId)
            .get()
            .addOnSuccessListener(doc -> {
                showLoading(false);
                if (doc.exists()) {
                    appointment = doc.toObject(Appointment.class);
                    if (appointment != null) {
                        appointment.setId(doc.getId());
                        displayAppointment();
                    }
                } else {
                    Toast.makeText(this, "Không tìm thấy lịch hẹn", Toast.LENGTH_SHORT).show();
                    finish();
                }
            })
            .addOnFailureListener(e -> {
                showLoading(false);
                Log.e(TAG, "Error loading appointment: " + e.getMessage());
                Toast.makeText(this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            });
    }

    private void displayAppointment() {
        tvRoomTitle.setText(appointment.getRoomTitle());
        tvRequesterName.setText("Người đặt: " + appointment.getRequesterName());
        tvRequesterPhone.setText("SĐT: " + appointment.getRequesterPhone());
        
        // Format date
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        tvAppointmentDate.setText("Ngày: " + dateFormat.format(new Date(appointment.getAppointmentDate())));
        tvAppointmentTime.setText("Giờ: " + appointment.getAppointmentTime());
        
        if (appointment.getNote() != null && !appointment.getNote().isEmpty()) {
            tvNote.setText("Ghi chú: " + appointment.getNote());
            tvNote.setVisibility(View.VISIBLE);
        } else {
            tvNote.setVisibility(View.GONE);
        }
        
        // Load thumbnail
        if (appointment.getRoomThumbnail() != null) {
            Glide.with(this)
                .load(appointment.getRoomThumbnail())
                .placeholder(R.drawable.ic_image_placeholder)
                .into(ivRoomThumbnail);
        }
        
        // Status
        updateStatusUI();
    }

    private void updateStatusUI() {
        boolean isOwner = currentUserId.equals(appointment.getOwnerId());
        
        switch (appointment.getStatus()) {
            case Appointment.STATUS_PENDING:
                tvStatus.setText("Đang chờ xác nhận");
                tvStatus.setBackgroundResource(R.drawable.bg_status_pending);
                // Chỉ chủ nhà mới thấy nút chấp nhận/từ chối
                layoutActions.setVisibility(isOwner ? View.VISIBLE : View.GONE);
                btnAccept.setVisibility(View.VISIBLE);
                btnReject.setVisibility(View.VISIBLE);
                break;
                
            case Appointment.STATUS_ACCEPTED:
                tvStatus.setText("Đã chấp nhận");
                tvStatus.setBackgroundResource(R.drawable.bg_status_active);
                layoutActions.setVisibility(View.VISIBLE);
                btnAccept.setVisibility(View.GONE);
                btnReject.setVisibility(View.GONE);
                break;
                
            case Appointment.STATUS_REJECTED:
                tvStatus.setText("Đã từ chối");
                tvStatus.setBackgroundResource(R.drawable.bg_status_inactive);
                layoutActions.setVisibility(View.VISIBLE);
                btnAccept.setVisibility(View.GONE);
                btnReject.setVisibility(View.GONE);
                break;
        }
    }

    private void acceptAppointment() {
        showLoading(true);

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", Appointment.STATUS_ACCEPTED);
        updates.put("updatedAt", System.currentTimeMillis());

        firebaseManager.getFirestore()
            .collection("appointments")
            .document(appointmentId)
            .update(updates)
            .addOnSuccessListener(aVoid -> {
                // Gửi thông báo cho người đặt
                sendNotificationToRequester(true, null);
                
                appointment.setStatus(Appointment.STATUS_ACCEPTED);
                updateStatusUI();
                showLoading(false);
                Toast.makeText(this, "Đã chấp nhận lịch hẹn", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                showLoading(false);
                Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void showRejectDialog() {
        EditText etReason = new EditText(this);
        etReason.setHint("Nhập lý do từ chối (không bắt buộc)");

        new AlertDialog.Builder(this)
            .setTitle("Từ chối lịch hẹn")
            .setMessage("Bạn có chắc muốn từ chối lịch hẹn này?")
            .setView(etReason)
            .setPositiveButton("Từ chối", (dialog, which) -> {
                String reason = etReason.getText().toString().trim();
                rejectAppointment(reason);
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void rejectAppointment(String reason) {
        showLoading(true);

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", Appointment.STATUS_REJECTED);
        updates.put("rejectReason", reason);
        updates.put("updatedAt", System.currentTimeMillis());

        firebaseManager.getFirestore()
            .collection("appointments")
            .document(appointmentId)
            .update(updates)
            .addOnSuccessListener(aVoid -> {
                // Gửi thông báo cho người đặt
                sendNotificationToRequester(false, reason);
                
                appointment.setStatus(Appointment.STATUS_REJECTED);
                updateStatusUI();
                showLoading(false);
                Toast.makeText(this, "Đã từ chối lịch hẹn", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                showLoading(false);
                Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void sendNotificationToRequester(boolean accepted, String reason) {
        Notification notif = new Notification.Builder()
            .userId(appointment.getRequesterId())
            .senderId(currentUserId)
            .senderName(appointment.getOwnerName())
            .type(accepted ? Notification.TYPE_APPOINTMENT_ACCEPTED : Notification.TYPE_APPOINTMENT_REJECTED)
            .title(accepted ? "Lịch hẹn được chấp nhận" : "Lịch hẹn bị từ chối")
            .message(accepted 
                ? "Chủ trọ đã chấp nhận lịch hẹn xem phòng \"" + appointment.getRoomTitle() + "\""
                : "Chủ trọ đã từ chối lịch hẹn xem phòng \"" + appointment.getRoomTitle() + "\"" + (reason != null && !reason.isEmpty() ? ". Lý do: " + reason : ""))
            .roomId(appointment.getRoomId())
            .roomTitle(appointment.getRoomTitle())
            .appointmentId(appointmentId)
            .build();

        firebaseManager.getFirestore()
            .collection("notifications")
            .add(notif)
            .addOnSuccessListener(docRef -> Log.d(TAG, "Notification sent"))
            .addOnFailureListener(e -> Log.e(TAG, "Error sending notification: " + e.getMessage()));
    }

    private void openChat() {
        String recipientId = currentUserId.equals(appointment.getOwnerId()) 
            ? appointment.getRequesterId() 
            : appointment.getOwnerId();
        String recipientName = currentUserId.equals(appointment.getOwnerId()) 
            ? appointment.getRequesterName() 
            : appointment.getOwnerName();

        Intent intent = new Intent(this, ChatDetailActivity.class);
        intent.putExtra("recipientId", recipientId);
        intent.putExtra("recipientName", recipientName);
        intent.putExtra("roomId", appointment.getRoomId());
        intent.putExtra("roomTitle", appointment.getRoomTitle());
        startActivity(intent);
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
}
