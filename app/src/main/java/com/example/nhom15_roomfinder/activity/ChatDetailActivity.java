package com.example.nhom15_roomfinder.activity; // đổi cho đúng package của bạn

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom15_roomfinder.R;

import java.util.ArrayList;
import java.util.List;

public class ChatDetailActivity extends AppCompatActivity {

    private ImageButton btnBack, btnSend;
    private EditText etMessage;
    private RecyclerView rvMessages;

    private ChatMessageAdapter adapter;
    private List<String> messageList;   // ví dụ đơn giản, để bạn tự đổi sang model object sau này

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);  // đúng layout của bạn

        initViews();
        setupRecyclerView();
        setListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnSend = findViewById(R.id.btnSend);
        etMessage = findViewById(R.id.etMessage);
        rvMessages = findViewById(R.id.rvMessages);
    }

    private void setupRecyclerView() {
        messageList = new ArrayList<>();
        adapter = new ChatMessageAdapter(messageList);

        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        rvMessages.setAdapter(adapter);
    }

    private void setListeners() {

        btnBack.setOnClickListener(v -> finish());

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String msg = etMessage.getText().toString().trim();

                if (msg.isEmpty()) {
                    Toast.makeText(ChatDetailActivity.this,
                            "Vui lòng nhập tin nhắn", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Thêm vào list hiển thị
                messageList.add(msg);
                adapter.notifyItemInserted(messageList.size() - 1);

                // Scroll xuống cuối
                rvMessages.scrollToPosition(messageList.size() - 1);

                // Clear input
                etMessage.setText("");
            }
        });
    }
}
