package com.example.nhom15_roomfinder.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom15_roomfinder.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<Map<String, Object>> usersList;
    private OnUserActionListener listener;

    public interface OnUserActionListener {
        void onDeleteClick(String userId);
    }

    public UserAdapter(List<Map<String, Object>> usersList, OnUserActionListener listener) {
        this.usersList = usersList != null ? usersList : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_admin, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        Map<String, Object> user = usersList.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    public void updateList(List<Map<String, Object>> newList) {
        this.usersList = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        private TextView txtUserInitial, txtUserName, txtUserEmail, txtUserCreated;
        private Button btnDeleteUser;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            txtUserInitial = itemView.findViewById(R.id.txtUserInitial);
            txtUserName = itemView.findViewById(R.id.txtUserName);
            txtUserEmail = itemView.findViewById(R.id.txtUserEmail);
            txtUserCreated = itemView.findViewById(R.id.txtUserCreated);
            btnDeleteUser = itemView.findViewById(R.id.btnDeleteUser);
        }

        public void bind(Map<String, Object> user) {
            String name = (String) user.get("name");
            String email = (String) user.get("email");
            String userId = (String) user.get("userId");
            Object createdAtObj = user.get("createdAt");

            // Set initial
            if (name != null && !name.isEmpty()) {
                txtUserInitial.setText(String.valueOf(name.charAt(0)).toUpperCase());
            } else if (email != null && !email.isEmpty()) {
                txtUserInitial.setText(String.valueOf(email.charAt(0)).toUpperCase());
            } else {
                txtUserInitial.setText("U");
            }

            txtUserName.setText(name != null ? name : "Không có tên");
            txtUserEmail.setText(email != null ? email : "Không có email");

            // Format created date
            if (createdAtObj != null) {
                try {
                    long timestamp;
                    if (createdAtObj instanceof Number) {
                        timestamp = ((Number) createdAtObj).longValue();
                    } else {
                        timestamp = Long.parseLong(createdAtObj.toString());
                    }
                    
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    String dateStr = sdf.format(new Date(timestamp));
                    txtUserCreated.setText("Ngày tạo: " + dateStr);
                } catch (Exception e) {
                    txtUserCreated.setText("Ngày tạo: --");
                }
            } else {
                txtUserCreated.setText("Ngày tạo: --");
            }

            btnDeleteUser.setOnClickListener(v -> {
                if (userId != null && listener != null) {
                    listener.onDeleteClick(userId);
                }
            });
        }
    }
}

