package com.example.nhom15_roomfinder.firebase;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom15_roomfinder.R;
import com.example.nhom15_roomfinder.entity.Room;

import java.util.List;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder> {

    private Context context;
    private List<Room> roomList;

    public RoomAdapter(Context context, List<Room> roomList) {
        this.context = context;
        this.roomList = roomList;
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_room, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        Room room = roomList.get(position);

        // --- CÁC DÒNG SỬA LỖI ĐỂ PHÙ HỢP VỚI ENTITY MỚI ---

        holder.tvTitle.setText(room.getTitle());

        // 1. Sửa lỗi giá tiền: Dùng getPriceDisplay() vì nó đã được định dạng sẵn thành chuỗi String.
        holder.tvPrice.setText(room.getPriceDisplay());

        // 2. Sửa lỗi địa chỉ: Kết hợp các trường địa chỉ mới thành một chuỗi hoàn chỉnh.
        String fullAddress = room.getAddress() + ", " + room.getDistrict() + ", " + room.getCity();
        holder.tvLocation.setText(fullAddress);

        // 3. Sửa lỗi khoảng cách: Dùng TextView này để hiển thị diện tích (area).
        // Dùng String.format để tạo chuỗi, ví dụ: "40.0 m²"
        String areaText = String.format("%.1f m²", room.getArea());
        holder.tvDistance.setText(areaText);

        // 4. Giữ nguyên logic tải ảnh từ drawable của bạn.

        if (room.getImageUrl() != null && !room.getImageUrl().isEmpty()) {
            int imageResId = context.getResources().getIdentifier(
                    room.getImageUrl(), "drawable", context.getPackageName());

            // Kiểm tra xem có tìm thấy ảnh không, nếu không thì hiển thị ảnh mặc định để tránh crash
            if (imageResId != 0) {
                holder.imgRoom.setImageResource(imageResId);
            } else {
                holder.imgRoom.setImageResource(R.drawable.ic_logo); // Thay bằng ảnh mặc định của bạn
                Log.w("RoomAdapter", "Không tìm thấy ảnh trong drawable: " + room.getImageUrl());
            }
        } else {
            // Nếu imageUrl trống, hiển thị ảnh mặc định
            holder.imgRoom.setImageResource(R.drawable.ic_logo); // Thay bằng ảnh mặc định của bạn
        }
    }

    @Override
    public int getItemCount() {
        return roomList.size();
    }

    public static class RoomViewHolder extends RecyclerView.ViewHolder {
        ImageView imgRoom;
        TextView tvTitle, tvPrice, tvLocation, tvDistance;

        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            imgRoom = itemView.findViewById(R.id.imgRoom);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            // Bây giờ tvDistance được dùng để hiển thị diện tích
            tvDistance = itemView.findViewById(R.id.tvDistance);
        }
    }
}
