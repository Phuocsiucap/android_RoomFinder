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
 * Adapter cho danh sách phòng (hiển thị ngang)
 */
public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder> {

    private Context context;
    private List<Room> roomList;
    private OnRoomClickListener listener;

    public interface OnRoomClickListener {
        void onRoomClick(Room room);
        void onFavoriteClick(Room room, int position);
    }

    public RoomAdapter(Context context, List<Room> roomList, OnRoomClickListener listener) {
        this.context = context;
        this.roomList = roomList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_room_horizontal, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        Room room = roomList.get(position);
        holder.bind(room, position);
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
        private ImageView imgFavorite;
        private TextView tvRoomTitle;
        private TextView tvRoomPrice;
        private TextView tvRoomLocation;

        RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            imgRoom = itemView.findViewById(R.id.imgRoom);
            imgFavorite = itemView.findViewById(R.id.imgFavorite);
            tvRoomTitle = itemView.findViewById(R.id.tvRoomTitle);
            tvRoomPrice = itemView.findViewById(R.id.tvRoomPrice);
            tvRoomLocation = itemView.findViewById(R.id.tvRoomLocation);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onRoomClick(roomList.get(position));
                }
            });

            imgFavorite.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onFavoriteClick(roomList.get(position), position);
                }
            });
        }

        void bind(Room room, int position) {
            tvRoomTitle.setText(room.getTitle());
            tvRoomPrice.setText(room.getPriceDisplay());
            
            // Hiển thị địa chỉ ngắn gọn
            String location = room.getDistrict() != null ? room.getDistrict() : "";
            if (room.getCity() != null && !room.getCity().isEmpty()) {
                location += (location.isEmpty() ? "" : ", ") + room.getCity();
            }
            tvRoomLocation.setText(location.isEmpty() ? room.getAddress() : location);

            // Load thumbnail image
            if (room.getThumbnailUrl() != null && !room.getThumbnailUrl().isEmpty()) {
                Glide.with(context)
                        .load(room.getThumbnailUrl())
                        .placeholder(R.drawable.ic_home)
                        .centerCrop()
                        .into(imgRoom);
            } else {
                imgRoom.setImageResource(R.drawable.ic_home);
            }

            // Update favorite icon
            imgFavorite.setImageResource(room.isFavorite() ? 
                R.drawable.ic_favorite_filled : R.drawable.ic_favorite);
        }
    }
}
