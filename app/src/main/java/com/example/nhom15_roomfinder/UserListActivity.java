package com.example.nhom15_roomfinder;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import com.example.nhom15_roomfinder.adapter.UserAdapter;
import com.example.nhom15_roomfinder.firebase.FirebaseCallback;
import com.example.nhom15_roomfinder.firebase.FirebaseManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserListActivity extends AppCompatActivity implements UserAdapter.OnUserActionListener {

    private RecyclerView rvUsers;
    private EditText edtSearchUser;
    private UserAdapter userAdapter;
    private List<Map<String, Object>> allUsersList;
    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_list);
        
        firebaseManager = FirebaseManager.getInstance();
        allUsersList = new ArrayList<>();
        
        initViews();
        setupRecyclerView();
        setupSearch();
        loadUsers();
    }

    private void initViews() {
        rvUsers = findViewById(R.id.rvUsers);
        edtSearchUser = findViewById(R.id.edtSearchUser);
    }

    private void setupRecyclerView() {
        userAdapter = new UserAdapter(allUsersList, this);
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
        if (query.isEmpty()) {
            userAdapter.updateList(allUsersList);
            return;
        }

        List<Map<String, Object>> filteredList = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        
        for (Map<String, Object> user : allUsersList) {
            String name = (String) user.get("name");
            String email = (String) user.get("email");
            
            if ((name != null && name.toLowerCase().contains(lowerQuery)) ||
                (email != null && email.toLowerCase().contains(lowerQuery))) {
                filteredList.add(user);
            }
        }
        
        userAdapter.updateList(filteredList);
    }

    private void loadUsers() {
        firebaseManager.getCollection("users",
            querySnapshot -> {
                allUsersList.clear();
                for (com.google.firebase.firestore.QueryDocumentSnapshot document : querySnapshot) {
                    Map<String, Object> userData = document.getData();
                    userData.put("userId", document.getId());
                    allUsersList.add(userData);
                }
                userAdapter.updateList(allUsersList);
            },
            e -> {
                Toast.makeText(this, "Lỗi tải danh sách người dùng: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
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
                        Toast.makeText(UserListActivity.this, "Lỗi xóa người dùng: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    });
            })
            .setNegativeButton("Hủy", null)
            .show();
    }
}