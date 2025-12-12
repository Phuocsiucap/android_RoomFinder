package com.example.nhom15_roomfinder.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nhom15_roomfinder.R;
import com.example.nhom15_roomfinder.entity.Room;

import java.util.List;

/**
 * Adapter mới cho danh sách phòng (kiểu layout khác, ví dụ trang tìm kiếm)
 */
public class RoomSearchAdapter extends RecyclerView.Adapter<RoomSearchAdapter.RoomViewHolder> {

    private Context context;
    private List<Room> roomList;
    private OnRoomClickListener listener;
    private double userLatitude = 0;
    private double userLongitude = 0;

    public interface OnRoomClickListener {
        void onRoomClick(Room room);

    }

    public RoomSearchAdapter(Context context, List<Room> roomList, OnRoomClickListener listener) {
        this.context = context;
        this.roomList = roomList;
        this.listener = listener;
    }
    public void setUserLocation(double lat, double lng) {
        this.userLatitude = lat;
        this.userLongitude = lng;
        notifyDataSetChanged();
    }
    // Thêm ở class RoomSearchAdapter, bên cạnh setUserLocation
    public Double getUserLat() {
        return userLatitude != 0 ? userLatitude : null;
    }

    public Double getUserLng() {
        return userLongitude != 0 ? userLongitude : null;
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Layout mới khác với trang chủ
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_room, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        Room room = roomList.get(position);
        holder.bind(room, position);
        TextView tvDistance = holder.itemView.findViewById(R.id.khoangcach);
        if (userLatitude != 0 && userLongitude != 0
                && room.getLatitude() != 0 && room.getLongitude() != 0) {
            double distance = calculateDistanceKm(userLatitude, userLongitude,
                    room.getLatitude(), room.getLongitude());
            tvDistance.setText(String.format("%.1f km", distance));
        } else {
            tvDistance.setText("Chưa xác định");
        }
    }
    private double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // bán kính Trái Đất (km)
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // trả về km
    }


    @Override
    public int getItemCount() {
        return roomList != null ? roomList.size() : 0;
    }

    public void updateData(List<Room> newRoomList) {
        this.roomList = newRoomList;
        notifyDataSetChanged();
    }

    class RoomViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgRoom;

        private TextView tvRoomTitle;
        private TextView tvRoomPrice;
        private TextView tvRoomLocation;

        RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            imgRoom = itemView.findViewById(R.id.imgRoom);

            tvRoomTitle = itemView.findViewById(R.id.tvTitle);
            tvRoomPrice = itemView.findViewById(R.id.tvPrice);
            tvRoomLocation = itemView.findViewById(R.id.tvLocation);


            // Click vào item
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onRoomClick(roomList.get(position));
                }
            });

        }

        void bind(Room room, int position) {
            tvRoomTitle.setText(room.getTitle());
            tvRoomPrice.setText(room.getPriceDisplay());


            String location = room.getDistrict() != null ? room.getDistrict() : "";
            if (room.getCity() != null && !room.getCity().isEmpty()) {
                location += (location.isEmpty() ? "" : ", ") + room.getCity();
            }
            tvRoomLocation.setText(location.isEmpty() ? room.getAddress() : location);

            // Load ảnh
            if (room.getThumbnailUrl() != null && !room.getThumbnailUrl().isEmpty()) {
                Glide.with(context)
                        .load(room.getThumbnailUrl())
                        .placeholder(R.drawable.ic_home)
                        .centerCrop()
                        .into(imgRoom);
            } else {
                imgRoom.setImageResource(R.drawable.ic_home);
            }



        }
    }
}
