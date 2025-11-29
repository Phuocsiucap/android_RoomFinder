package com.example.nhom15_roomfinder;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
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

public class UserListActivity extends AppCompatActivity implements UserAdapter.OnUserActionListener {

    private static final String TAG = "UserListActivity";

    private RecyclerView rvUsers;
    private EditText edtSearchUser;
    private SwipeRefreshLayout swipeRefreshLayout;
    private UserAdapter userAdapter;
    private List<Map<String, Object>> allUsersList;
    private List<Map<String, Object>> filteredUsersList;
    private FirebaseManager firebaseManager;

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
        swipeRefreshLayout = findViewById(R.id.swipeRefresh);
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
                
                if ((name != null && name.toLowerCase().contains(lowerQuery)) ||
                    (email != null && email.toLowerCase().contains(lowerQuery))) {
                    filteredUsersList.add(user);
                }
            }
        }
        
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
        // Prevent deleting current user
        String currentUserId = firebaseManager.getUserId();
        if (userId != null && userId.equals(currentUserId)) {
            Toast.makeText(this, "Không thể xóa tài khoản của chính bạn", 
                Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc chắn muốn xóa người dùng này? Hành động này không thể hoàn tác.")
            .setPositiveButton("Xóa", (dialog, which) -> {
                // Delete user from Firestore
                firebaseManager.deleteDocument("users", userId,
                    aVoid -> {
                        Toast.makeText(UserListActivity.this, "Xóa người dùng thành công", 
                            Toast.LENGTH_SHORT).show();
                        loadUsers();
                    },
                    e -> {
                        Log.e(TAG, "Error deleting user: " + e.getMessage());
                        Toast.makeText(UserListActivity.this, "Lỗi xóa người dùng: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    });
            })
            .setNegativeButton("Hủy", null)
            .show();
    }
}