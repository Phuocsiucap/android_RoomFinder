package com.example.nhom15_roomfinder.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nhom15_roomfinder.R;
import com.example.nhom15_roomfinder.firebase.FirebaseManager;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class BookingActivity extends AppCompatActivity {

    private EditText etDate, etTime, etNote;
    private TextView tvRoomTitle;
    private Button btnSubmit;
    private LinearLayout successMessage;
    
    private FirebaseManager firebaseManager;
    private String currentUserId;
    private String roomId;
    private String roomTitle;
    private String ownerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        firebaseManager = FirebaseManager.getInstance();
        currentUserId = firebaseManager.getUserId();

        // Lấy dữ liệu từ Intent
        if (getIntent() != null) {
            roomId = getIntent().getStringExtra("roomId");
            roomTitle = getIntent().getStringExtra("roomTitle");
            ownerId = getIntent().getStringExtra("ownerId");
        }

        if (roomId == null || ownerId == null) {
            Toast.makeText(this, "Lỗi dữ liệu phòng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setClickListeners();
    }

    private void initViews() {
        etDate = findViewById(R.id.etDate);
        etTime = findViewById(R.id.etTime);
        etNote = findViewById(R.id.etNote);
        btnSubmit = findViewById(R.id.btnSubmit);
        successMessage = findViewById(R.id.successMessage);
        
        // Có thể thêm TextView hiển thị tên phòng nếu layout có
        // tvRoomTitle = findViewById(R.id.tvRoomTitle);
        // if (tvRoomTitle != null) tvRoomTitle.setText(roomTitle);
    }

    private void setClickListeners() {
        // Chọn ngày
        etDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    BookingActivity.this,
                    (view, year1, month1, dayOfMonth) -> {
                        // month + 1 vì tháng bắt đầu từ 0
                        String date = String.format("%02d/%02d/%04d", dayOfMonth, month1 + 1, year1);
                        etDate.setText(date);
                    },
                    year, month, day
            );
            // Không cho chọn ngày quá khứ
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            datePickerDialog.show();
        });

        // Chọn giờ
        etTime.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    BookingActivity.this,
                    (view, hourOfDay, minute1) -> {
                        String time = String.format("%02d:%02d", hourOfDay, minute1);
                        etTime.setText(time);
                    },
                    hour, minute, true
            );
            timePickerDialog.show();
        });

        // Submit
        btnSubmit.setOnClickListener(v -> submitBooking());
    }

    private void submitBooking() {
        String date = etDate.getText().toString().trim();
        String time = etTime.getText().toString().trim();
        String note = etNote.getText().toString().trim();

        if (date.isEmpty()) {
            etDate.setError("Vui lòng chọn ngày");
            return;
        }

        if (time.isEmpty()) {
            etTime.setError("Vui lòng chọn giờ");
            return;
        }

        // Tạo object booking để lưu vào Firestore
        Map<String, Object> booking = new HashMap<>();
        booking.put("roomId", roomId);
        booking.put("roomTitle", roomTitle);
        booking.put("ownerId", ownerId);
        booking.put("renterId", currentUserId);
        booking.put("date", date);
        booking.put("time", time);
        booking.put("note", note);
        booking.put("status", "PENDING"); // Trạng thái chờ duyệt
        booking.put("createdAt", System.currentTimeMillis());

        btnSubmit.setEnabled(false); // Disable button để tránh double click

        firebaseManager.getFirestore()
                .collection("bookings")
                .add(booking)
                .addOnSuccessListener(documentReference -> {
                    successMessage.setVisibility(View.VISIBLE);
                    btnSubmit.setVisibility(View.GONE); // Ẩn nút submit
                    Toast.makeText(BookingActivity.this, "Đặt lịch thành công!", Toast.LENGTH_SHORT).show();
                    
                    // Tự động đóng sau 2 giây
                    new android.os.Handler().postDelayed(this::finish, 2000);
                })
                .addOnFailureListener(e -> {
                    btnSubmit.setEnabled(true);
                    Toast.makeText(BookingActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
