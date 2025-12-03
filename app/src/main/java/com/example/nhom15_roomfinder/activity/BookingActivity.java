package com.example.nhom15_roomfinder.activity; // đổi lại package cho đúng app của bạn

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nhom15_roomfinder.R;

import java.util.Calendar;

public class BookingActivity extends AppCompatActivity {

    private EditText etDate, etTime, etNote;
    private Button btnSubmit;
    private LinearLayout successMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking); // đúng tên layout bạn gửi

        initViews();
        setClickListeners();
    }

    private void initViews() {
        etDate = findViewById(R.id.etDate);
        etTime = findViewById(R.id.etTime);
        etNote = findViewById(R.id.etNote);
        btnSubmit = findViewById(R.id.btnSubmit);
        successMessage = findViewById(R.id.successMessage); // LinearLayout
    }

    private void setClickListeners() {
        // Chọn ngày
        etDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        BookingActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                // month + 1 vì tháng bắt đầu từ 0
                                String date = String.format("%02d/%02d/%04d", month + 1, dayOfMonth, year);
                                etDate.setText(date);
                            }
                        },
                        year, month, day
                );
                datePickerDialog.show();
            }
        });

        // Chọn giờ
        etTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
            }
        });

        // Submit
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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

                // Tùy bạn có bắt buộc ghi chú hay không
                // if (note.isEmpty()) { ... }

                successMessage.setVisibility(View.VISIBLE);

                Toast.makeText(BookingActivity.this,
                        "Yêu cầu đặt phòng đã được gửi",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
