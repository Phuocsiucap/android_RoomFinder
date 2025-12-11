package com.example.nhom15_roomfinder.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.nhom15_roomfinder.R;
import com.example.nhom15_roomfinder.adapter.PropertyImageAdapter;
import com.example.nhom15_roomfinder.entity.Appointment;
import com.example.nhom15_roomfinder.entity.Notification;
import com.example.nhom15_roomfinder.entity.Room;
import com.example.nhom15_roomfinder.firebase.FirebaseManager;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * PropertyDetailActivity - Màn hình chi tiết phòng trọ
 * Hiển thị thông tin đầy đủ về phòng và cho phép liên hệ chủ trọ
 */
public class PropertyDetailActivity extends AppCompatActivity {

    private static final String TAG = "PropertyDetailActivity";

    // UI Components
    private ViewPager2 imageViewPager;
    private TextView tvPropertyTitle, tvPrice, tvAddress, tvDescription;
    private TextView tvOwnerName, tvOwnerPhone, tvArea;
    private LinearLayout layoutWifi, layoutAC, layoutParking;
    private ImageView imgFavorite;
    private Button btnCall, btnMessage, btnBookAppointment;
    private ProgressBar progressBar;

    private PropertyImageAdapter imageAdapter;
    private FirebaseManager firebaseManager;
    private String currentUserId;
    
    private Room currentRoom;
    private String roomId;
    private boolean isFavorite = false;
    
    // For appointment booking
    private long selectedDate = 0;
    private String selectedTime = "";
    private String currentUserName = "";
    private String currentUserPhone = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_property_detail);

        firebaseManager = FirebaseManager.getInstance();
        currentUserId = firebaseManager.getUserId();

        initViews();
        getIntentData();
        setupButtons();
        loadCurrentUserInfo();
    }
    
    private void loadCurrentUserInfo() {
        if (currentUserId == null) return;
        
        firebaseManager.getFirestore()
            .collection("users")
            .document(currentUserId)
            .get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    currentUserName = doc.getString("name");
                    currentUserPhone = doc.getString("phone");
                    if (currentUserName == null) currentUserName = "";
                    if (currentUserPhone == null) currentUserPhone = "";
                }
            });
    }

    private void initViews() {
        imageViewPager = findViewById(R.id.imageViewPager);
        tvPropertyTitle = findViewById(R.id.tvPropertyTitle);
        tvPrice = findViewById(R.id.tvPrice);
        tvAddress = findViewById(R.id.tvAddress);
        tvDescription = findViewById(R.id.tvDescription);
        tvOwnerName = findViewById(R.id.tvOwnerName);
        tvOwnerPhone = findViewById(R.id.tvOwnerPhone);
        btnCall = findViewById(R.id.btnCall);
        btnMessage = findViewById(R.id.btnMessage);
        btnBookAppointment = findViewById(R.id.btnBookAppointment);
        progressBar = findViewById(R.id.progressBar);
        imgFavorite = findViewById(R.id.imgFavorite);
    }

    private void getIntentData() {
        Intent intent = getIntent();
        
        // Lấy Room object từ intent (nếu có)
        if (intent.hasExtra("room")) {
            currentRoom = (Room) intent.getSerializableExtra("room");
            displayRoomData(currentRoom);
            checkFavoriteStatus();
        }
        
        // Hoặc lấy roomId để load từ Firebase
        if (intent.hasExtra("roomId")) {
            roomId = intent.getStringExtra("roomId");
            if (currentRoom == null) {
                loadRoomFromFirebase(roomId);
            }
        }
    }

    /**
     * Load thông tin phòng từ Firebase
     */
    private void loadRoomFromFirebase(String roomId) {
        showLoading(true);
        
        firebaseManager.getFirestore()
            .collection("rooms")
            .document(roomId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                showLoading(false);
                if (documentSnapshot.exists()) {
                    currentRoom = documentSnapshot.toObject(Room.class);
                    if (currentRoom != null) {
                        currentRoom.setId(documentSnapshot.getId());
                        displayRoomData(currentRoom);
                        checkFavoriteStatus();
                        incrementViewCount();
                    }
                } else {
                    Toast.makeText(this, "Không tìm thấy thông tin phòng", Toast.LENGTH_SHORT).show();
                }
            })
            .addOnFailureListener(e -> {
                showLoading(false);
                Log.e(TAG, "Error loading room: " + e.getMessage());
                Toast.makeText(this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            });
    }

    /**
     * Hiển thị dữ liệu phòng lên UI
     */
    private void displayRoomData(Room room) {
        tvPropertyTitle.setText(room.getTitle());
        tvPrice.setText(room.getPriceDisplay());
        tvAddress.setText(room.getFullAddress());
        tvDescription.setText(room.getDescription());
        tvOwnerName.setText(room.getOwnerName() != null ? room.getOwnerName() : "Chủ trọ");
        tvOwnerPhone.setText(room.getOwnerPhone() != null ? room.getOwnerPhone() : "Chưa cập nhật");

        // Setup image gallery
        setupImageViewPager(room.getImageUrls());
    }

    /**
     * Setup ViewPager cho gallery ảnh
     */
    private void setupImageViewPager(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            imageUrls = new ArrayList<>();
            imageUrls.add(""); // Placeholder
        }
        
        final List<String> finalImageUrls = imageUrls;
        imageAdapter = new PropertyImageAdapter(this, imageUrls);
        
        // Set click listener để mở fullscreen image viewer
        imageAdapter.setOnImageClickListener(position -> {
            openFullscreenImageViewer(finalImageUrls, position);
        });
        
        imageViewPager.setAdapter(imageAdapter);
    }

    /**
     * Mở màn hình xem ảnh toàn màn hình
     */
    private void openFullscreenImageViewer(List<String> imageUrls, int position) {
        Intent intent = new Intent(this, FullscreenImageActivity.class);
        intent.putStringArrayListExtra(FullscreenImageActivity.EXTRA_IMAGE_URLS, 
                new ArrayList<>(imageUrls));
        intent.putExtra(FullscreenImageActivity.EXTRA_CURRENT_POSITION, position);
        startActivity(intent);
    }

    /**
     * Kiểm tra phòng có trong danh sách yêu thích không
     */
    private void checkFavoriteStatus() {
        if (currentUserId == null || currentRoom == null) return;

        firebaseManager.getFirestore()
            .collection("favorites")
            .whereEqualTo("userId", currentUserId)
            .whereEqualTo("roomId", currentRoom.getId())
            .get()
            .addOnSuccessListener(querySnapshot -> {
                isFavorite = !querySnapshot.isEmpty();
                updateFavoriteIcon();
            });
    }

    private void updateFavoriteIcon() {
        if (imgFavorite != null) {
            imgFavorite.setImageResource(isFavorite ? 
                R.drawable.ic_favorite_filled : R.drawable.ic_favorite);
        }
    }

    /**
     * Tăng lượt xem phòng
     */
    private void incrementViewCount() {
        if (currentRoom == null) return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("viewCount", currentRoom.getViewCount() + 1);
        
        firebaseManager.updateDocument("rooms", currentRoom.getId(), updates,
            aVoid -> Log.d(TAG, "View count updated"),
            e -> Log.e(TAG, "Error updating view count: " + e.getMessage())
        );
    }

    private void setupButtons() {
        // Gọi điện
        btnCall.setOnClickListener(v -> {
            String phone = tvOwnerPhone.getText().toString();
            if (phone.equals("Chưa cập nhật")) {
                Toast.makeText(this, "Chưa có số điện thoại", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phone));
            startActivity(intent);
        });

        // Nhắn tin chủ trọ
        btnMessage.setOnClickListener(v -> {
            if (currentRoom == null) return;
            
            Intent intent = new Intent(PropertyDetailActivity.this, ChatDetailActivity.class);
            intent.putExtra("recipientId", currentRoom.getOwnerId());
            intent.putExtra("recipientName", currentRoom.getOwnerName());
            intent.putExtra("roomId", currentRoom.getId());
            intent.putExtra("roomTitle", currentRoom.getTitle());
            startActivity(intent);
        });

        // Yêu thích
        if (imgFavorite != null) {
            imgFavorite.setOnClickListener(v -> toggleFavorite());
        }
        
        // Đặt lịch hẹn
        if (btnBookAppointment != null) {
            btnBookAppointment.setOnClickListener(v -> showBookingDialog());
        }
    }
    
    /**
     * Hiển thị dialog đặt lịch hẹn
     */
    private void showBookingDialog() {
        if (currentUserId == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để đặt lịch", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (currentRoom == null) return;
        
        // Không cho phép đặt lịch hẹn với phòng của chính mình
        if (currentUserId.equals(currentRoom.getOwnerId())) {
            Toast.makeText(this, "Không thể đặt lịch hẹn với phòng của bạn", Toast.LENGTH_SHORT).show();
            return;
        }
        
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_book_appointment, null);
        
        TextView tvDate = dialogView.findViewById(R.id.tvDate);
        TextView tvTime = dialogView.findViewById(R.id.tvTime);
        EditText etNote = dialogView.findViewById(R.id.etNote);
        
        // Date picker
        tvDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            DatePickerDialog datePicker = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedCal = Calendar.getInstance();
                    selectedCal.set(year, month, dayOfMonth);
                    selectedDate = selectedCal.getTimeInMillis();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    tvDate.setText(sdf.format(new Date(selectedDate)));
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));
            datePicker.getDatePicker().setMinDate(System.currentTimeMillis());
            datePicker.show();
        });
        
        // Time picker
        tvTime.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            TimePickerDialog timePicker = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                    tvTime.setText(selectedTime);
                },
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true);
            timePicker.show();
        });
        
        new AlertDialog.Builder(this)
            .setTitle("Đặt lịch hẹn xem phòng")
            .setView(dialogView)
            .setPositiveButton("Đặt lịch", (dialog, which) -> {
                if (selectedDate == 0) {
                    Toast.makeText(this, "Vui lòng chọn ngày", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (selectedTime.isEmpty()) {
                    Toast.makeText(this, "Vui lòng chọn giờ", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                String note = etNote.getText().toString().trim();
                createAppointment(note);
            })
            .setNegativeButton("Hủy", null)
            .show();
    }
    
    /**
     * Tạo lịch hẹn và gửi thông báo cho chủ trọ
     */
    private void createAppointment(String note) {
        showLoading(true);
        
        Appointment appointment = new Appointment();
        appointment.setRoomId(currentRoom.getId());
        appointment.setRoomTitle(currentRoom.getTitle());
        appointment.setRoomThumbnail(currentRoom.getImageUrls() != null && !currentRoom.getImageUrls().isEmpty() 
            ? currentRoom.getImageUrls().get(0) : null);
        appointment.setOwnerId(currentRoom.getOwnerId());
        appointment.setOwnerName(currentRoom.getOwnerName());
        appointment.setRequesterId(currentUserId);
        appointment.setRequesterName(currentUserName);
        appointment.setRequesterPhone(currentUserPhone);
        appointment.setAppointmentDate(selectedDate);
        appointment.setAppointmentTime(selectedTime);
        appointment.setNote(note);
        appointment.setStatus(Appointment.STATUS_PENDING);
        appointment.setCreatedAt(System.currentTimeMillis());
        
        firebaseManager.getFirestore()
            .collection("appointments")
            .add(appointment)
            .addOnSuccessListener(docRef -> {
                String appointmentId = docRef.getId();
                
                // Gửi thông báo cho chủ trọ
                sendNotificationToOwner(appointmentId);
                
                showLoading(false);
                Toast.makeText(this, "Đã gửi yêu cầu đặt lịch hẹn", Toast.LENGTH_SHORT).show();
                
                // Reset
                selectedDate = 0;
                selectedTime = "";
            })
            .addOnFailureListener(e -> {
                showLoading(false);
                Log.e(TAG, "Error creating appointment: " + e.getMessage());
                Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
    
    /**
     * Gửi thông báo cho chủ trọ
     */
    private void sendNotificationToOwner(String appointmentId) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        
        Notification notification = new Notification.Builder()
            .userId(currentRoom.getOwnerId())
            .senderId(currentUserId)
            .senderName(currentUserName)
            .type(Notification.TYPE_APPOINTMENT_REQUEST)
            .title("Yêu cầu đặt lịch hẹn mới")
            .message(currentUserName + " muốn xem phòng \"" + currentRoom.getTitle() + "\" vào ngày " 
                + dateFormat.format(new Date(selectedDate)) + " lúc " + selectedTime)
            .roomId(currentRoom.getId())
            .roomTitle(currentRoom.getTitle())
            .appointmentId(appointmentId)
            .build();
        
        firebaseManager.getFirestore()
            .collection("notifications")
            .add(notification)
            .addOnSuccessListener(docRef -> Log.d(TAG, "Notification sent to owner"))
            .addOnFailureListener(e -> Log.e(TAG, "Error sending notification: " + e.getMessage()));
    }

    /**
     * Thêm/Xóa khỏi yêu thích
     */
    private void toggleFavorite() {
        if (currentUserId == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (currentRoom == null) return;

        if (isFavorite) {
            // Xóa khỏi yêu thích
            firebaseManager.getFirestore()
                .collection("favorites")
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("roomId", currentRoom.getId())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        doc.getReference().delete();
                    }
                    isFavorite = false;
                    updateFavoriteIcon();
                    Toast.makeText(this, "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();
                });
        } else {
            // Thêm vào yêu thích
            Map<String, Object> favorite = new HashMap<>();
            favorite.put("userId", currentUserId);
            favorite.put("roomId", currentRoom.getId());
            favorite.put("createdAt", System.currentTimeMillis());
            
            firebaseManager.getFirestore()
                .collection("favorites")
                .add(favorite)
                .addOnSuccessListener(docRef -> {
                    isFavorite = true;
                    updateFavoriteIcon();
                    Toast.makeText(this, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
                });
        }
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}
