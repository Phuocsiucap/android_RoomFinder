package com.example.nhom15_roomfinder;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.nhom15_roomfinder.adapter.UserAdapter;
import com.example.nhom15_roomfinder.firebase.FirebaseCallback;
import com.example.nhom15_roomfinder.firebase.FirebaseManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class UserListActivity extends AppCompatActivity implements UserAdapter.OnUserActionListener {

    private static final String TAG = "UserListActivity";

    private RecyclerView rvUsers;
    private EditText edtSearchUser;
    private Spinner spinnerUserStatus;
    private SwipeRefreshLayout swipeRefreshLayout;
    private UserAdapter userAdapter;
    private List<Map<String, Object>> allUsersList;
    private List<Map<String, Object>> filteredUsersList;
    private FirebaseManager firebaseManager;
    private String currentStatusFilter = "all"; // all, active, blocked

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_list);
        
        firebaseManager = FirebaseManager.getInstance();
        allUsersList = new ArrayList<>();
        filteredUsersList = new ArrayList<>();
        
        initViews();
        setupRecyclerView();
        setupSearch();
        setupSwipeRefresh();
        loadUsers();
    }

    private void initViews() {
        rvUsers = findViewById(R.id.rvUsers);
        edtSearchUser = findViewById(R.id.edtSearchUser);
        spinnerUserStatus = findViewById(R.id.spinnerUserStatus);
        swipeRefreshLayout = findViewById(R.id.swipeRefresh);

        setupStatusFilter();
    }

    private void setupSwipeRefresh() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(() -> {
                loadUsers();
            });
        }
    }

    private void setupRecyclerView() {
        userAdapter = new UserAdapter(filteredUsersList, this);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        rvUsers.setAdapter(userAdapter);
    }

    private void setupSearch() {
        edtSearchUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterUsers(String query) {
        filteredUsersList.clear();
        
        if (query.isEmpty()) {
            filteredUsersList.addAll(allUsersList);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Map<String, Object> user : allUsersList) {
                String name = (String) user.get("name");
                String email = (String) user.get("email");
                
                boolean textMatch = (name != null && name.toLowerCase().contains(lowerQuery)) ||
                                    (email != null && email.toLowerCase().contains(lowerQuery));

                if (textMatch) {
                    filteredUsersList.add(user);
                }
            }
        }

        // Áp dụng filter theo trạng thái khóa/mở
        List<Map<String, Object>> statusFiltered = new ArrayList<>();
        for (Map<String, Object> user : filteredUsersList) {
            Object blockedObj = user.get("isBlocked");
            boolean isBlocked = blockedObj instanceof Boolean && (Boolean) blockedObj;

            boolean match = false;
            switch (currentStatusFilter) {
                case "all":
                    match = true;
                    break;
                case "active":
                    match = !isBlocked;
                    break;
                case "blocked":
                    match = isBlocked;
                    break;
            }

            if (match) {
                statusFiltered.add(user);
            }
        }

        filteredUsersList.clear();
        filteredUsersList.addAll(statusFiltered);
        
        // Sort by creation date (newest first)
        Collections.sort(filteredUsersList, (user1, user2) -> {
            Object time1 = user1.get("createdAt");
            Object time2 = user2.get("createdAt");
            if (time1 instanceof Number && time2 instanceof Number) {
                return Long.compare(((Number) time2).longValue(), ((Number) time1).longValue());
            }
            return 0;
        });
        
        userAdapter.updateList(filteredUsersList);
    }

    private void setupStatusFilter() {
        if (spinnerUserStatus == null) return;

        String[] statusOptions = {"Tất cả", "Đang hoạt động", "Đã khóa"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_item, statusOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUserStatus.setAdapter(adapter);

        spinnerUserStatus.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                switch (position) {
                    case 0: currentStatusFilter = "all"; break;
                    case 1: currentStatusFilter = "active"; break;
                    case 2: currentStatusFilter = "blocked"; break;
                }
                filterUsers(edtSearchUser.getText().toString());
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private void loadUsers() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(true);
        }

        firebaseManager.getCollection("users",
            querySnapshot -> {
                allUsersList.clear();
                for (com.google.firebase.firestore.QueryDocumentSnapshot document : querySnapshot) {
                    Map<String, Object> userData = document.getData();
                    userData.put("userId", document.getId());
                    allUsersList.add(userData);
                }
                filterUsers(edtSearchUser.getText().toString());
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            },
            e -> {
                Log.e(TAG, "Error loading users: " + e.getMessage());
                Toast.makeText(this, "Lỗi tải danh sách người dùng: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.admin_users_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_refresh) {
            loadUsers();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDeleteClick(String userId) {
        // Không cho admin tự khóa chính mình
        String currentUserId = firebaseManager.getUserId();
        if (userId != null && userId.equals(currentUserId)) {
            Toast.makeText(this, "Không thể khóa tài khoản của chính bạn", 
                Toast.LENGTH_SHORT).show();
            return;
        }

        // Tìm user hiện tại để biết trạng thái isBlocked
        boolean isCurrentlyBlocked = false;
        for (Map<String, Object> user : allUsersList) {
            String id = (String) user.get("userId");
            if (userId != null && userId.equals(id)) {
                Object blockedObj = user.get("isBlocked");
                isCurrentlyBlocked = blockedObj instanceof Boolean && (Boolean) blockedObj;
                break;
            }
        }

        boolean newStatus = !isCurrentlyBlocked;
        String title = newStatus ? "Khóa người dùng" : "Mở khóa người dùng";
        String message = newStatus 
                ? "Bạn có chắc chắn muốn KHÓA người dùng này? Người dùng sẽ không thể đăng nhập."
                : "Bạn có chắc chắn muốn MỞ KHÓA người dùng này?";
        String positive = newStatus ? "Khóa" : "Mở khóa";

        new AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positive, (dialog, which) -> {
                Map<String, Object> updates = new HashMap<>();
                updates.put("isBlocked", newStatus);

                firebaseManager.updateDocument("users", userId, updates,
                    aVoid -> {
                        String toastMsg = newStatus ? "Đã khóa người dùng" : "Đã mở khóa người dùng";
                        Toast.makeText(UserListActivity.this, toastMsg, Toast.LENGTH_SHORT).show();
                        loadUsers();
                    },
                    e -> {
                        Log.e(TAG, "Error updating user block status: " + e.getMessage());
                        Toast.makeText(UserListActivity.this, 
                            "Lỗi cập nhật trạng thái người dùng: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    });
            })
            .setNegativeButton("Hủy", null)
            .show();
    }
}