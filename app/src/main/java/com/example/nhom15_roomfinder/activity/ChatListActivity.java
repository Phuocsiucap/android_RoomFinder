package com.example.nhom15_roomfinder.activity; // ĐỔI cho đúng package của bạn

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom15_roomfinder.R;

import java.util.ArrayList;
import java.util.List;

public class ChatListActivity extends AppCompatActivity {

    private RecyclerView rvChatList;
    private LinearLayout navHome, navSearch, navChat, navFavorite, navProfile;

    private ChatListAdapter chatListAdapter;
    private List<ChatItem> chatItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list); // đúng tên layout bạn gửi

        initViews();
        setupRecyclerView();
        setupBottomNav();
    }

    private void initViews() {
        rvChatList   = findViewById(R.id.rvChatList);
        navHome      = findViewById(R.id.navHome);
        navSearch    = findViewById(R.id.navSearch);
        navChat      = findViewById(R.id.navChat);
        navFavorite  = findViewById(R.id.navFavorite);
        navProfile   = findViewById(R.id.navProfile);
    }

    private void setupRecyclerView() {
        chatItems = new ArrayList<>();

        // Demo dữ liệu – sau này bạn thay bằng dữ liệu thật
        chatItems.add(new ChatItem(
                "Chủ trọ Nguyễn Văn A",
                "Phòng còn trống, em ghé lúc nào được?",
                "10:45",
                2
        ));
        chatItems.add(new ChatItem(
                "Chủ trọ Trần Thị B",
                "Ok em, mai 9h anh chờ.",
                "Hôm qua",
                0
        ));
        chatItems.add(new ChatItem(
                "Căn hộ dịch vụ Quận 1",
                "Em nhớ mang CMND khi đến xem phòng nhé.",
                "Thứ 2",
                1
        ));

        chatListAdapter = new ChatListAdapter(chatItems, item -> {
            // TODO: mở ChatDetailActivity nếu bạn muốn
            // Intent i = new Intent(ChatListActivity.this, ChatDetailActivity.class);
            // i.putExtra("chatTitle", item.getTitle());
            // startActivity(i);

            Toast.makeText(
                    ChatListActivity.this,
                    "Mở chat với: " + item.getTitle(),
                    Toast.LENGTH_SHORT
            ).show();
        });

        rvChatList.setLayoutManager(new LinearLayoutManager(this));
        rvChatList.setAdapter(chatListAdapter);
    }

    private void setupBottomNav() {
        navHome.setOnClickListener(v ->
                Toast.makeText(this, "Đi tới Home (TODO)", Toast.LENGTH_SHORT).show()
        );

        navSearch.setOnClickListener(v ->
                Toast.makeText(this, "Đi tới Search (TODO)", Toast.LENGTH_SHORT).show()
        );

        navChat.setOnClickListener(v ->
                Toast.makeText(this, "Bạn đang ở Chat", Toast.LENGTH_SHORT).show()
        );

        navFavorite.setOnClickListener(v ->
                Toast.makeText(this, "Đi tới Yêu thích (TODO)", Toast.LENGTH_SHORT).show()
        );

        navProfile.setOnClickListener(v ->
                Toast.makeText(this, "Đi tới Profile (TODO)", Toast.LENGTH_SHORT).show()
        );
    }
}
