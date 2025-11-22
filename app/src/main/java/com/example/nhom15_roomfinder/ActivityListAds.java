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

import com.example.nhom15_roomfinder.adapter.AdAdapter;
import com.example.nhom15_roomfinder.firebase.FirebaseCallback;
import com.example.nhom15_roomfinder.firebase.FirebaseManager;
import com.example.nhom15_roomfinder.firebase.RoomFirebaseHelper;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ActivityListAds extends AppCompatActivity implements AdAdapter.OnAdActionListener {

    private RecyclerView rvAds;
    private EditText edtSearch;
    private AdAdapter adAdapter;
    private List<Map<String, Object>> allAdsList;
    private FirebaseManager firebaseManager;
    private RoomFirebaseHelper roomFirebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_list_ads);
        
        firebaseManager = FirebaseManager.getInstance();
        roomFirebaseHelper = new RoomFirebaseHelper();
        allAdsList = new ArrayList<>();
        
        initViews();
        setupRecyclerView();
        setupSearch();
        loadAds();
    }

    private void initViews() {
        rvAds = findViewById(R.id.rvAds);
        edtSearch = findViewById(R.id.edtSearch);
    }

    private void setupRecyclerView() {
        adAdapter = new AdAdapter(allAdsList, this);
        rvAds.setLayoutManager(new LinearLayoutManager(this));
        rvAds.setAdapter(adAdapter);
    }

    private void setupSearch() {
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterAds(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterAds(String query) {
        if (query.isEmpty()) {
            adAdapter.updateList(allAdsList);
            return;
        }

        List<Map<String, Object>> filteredList = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        
        for (Map<String, Object> ad : allAdsList) {
            String title = (String) ad.get("title");
            String location = (String) ad.get("location");
            
            if ((title != null && title.toLowerCase().contains(lowerQuery)) ||
                (location != null && location.toLowerCase().contains(lowerQuery))) {
                filteredList.add(ad);
            }
        }
        
        adAdapter.updateList(filteredList);
    }

    private void loadAds() {
        roomFirebaseHelper.getAllRooms(new FirebaseCallback<List<Map<String, Object>>>() {
            @Override
            public void onSuccess(List<Map<String, Object>> data) {
                allAdsList = data;
                adAdapter.updateList(allAdsList);
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(ActivityListAds.this, "Lỗi tải danh sách tin: " + error, 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onEditClick(Map<String, Object> ad) {
        // TODO: Implement edit functionality
        Toast.makeText(this, "Chức năng sửa đang được phát triển", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteClick(String adId) {
        new AlertDialog.Builder(this)
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc chắn muốn xóa tin đăng này?")
            .setPositiveButton("Xóa", (dialog, which) -> {
                roomFirebaseHelper.deleteRoom(adId, new FirebaseCallback<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        Toast.makeText(ActivityListAds.this, "Xóa tin đăng thành công", 
                            Toast.LENGTH_SHORT).show();
                        loadAds();
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(ActivityListAds.this, "Lỗi xóa tin: " + error, 
                            Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .setNegativeButton("Hủy", null)
            .show();
    }
}