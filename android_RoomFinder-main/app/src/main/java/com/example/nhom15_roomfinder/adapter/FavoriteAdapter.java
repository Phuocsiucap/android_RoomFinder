package com.example.nhom15_roomfinder.adapter;

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
 * Adapter cho danh sách phòng yêu thích
 */
public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder> {

    private List<Room> roomList;
    private OnFavoriteClickListener listener;

    public interface OnFavoriteClickListener {
        void onRoomClick(Room room);
        void onRemoveFavorite(Room room, int position);
    }

    public FavoriteAdapter(List<Room> roomList, OnFavoriteClickListener listener) {
        this.roomList = roomList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_favorite_room, parent, false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
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

    public void removeItem(int position) {
        if (position >= 0 && position < roomList.size()) {
            roomList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, roomList.size());
        }
    }

    class FavoriteViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgRoom;
        private TextView tvRoomTitle;
        private TextView tvRoomPrice;
        private TextView tvRoomLocation;
        private ImageView imgFavorite;

        FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            imgRoom = itemView.findViewById(R.id.imgRoom);
            tvRoomTitle = itemView.findViewById(R.id.tvRoomTitle);
            tvRoomPrice = itemView.findViewById(R.id.tvRoomPrice);
            tvRoomLocation = itemView.findViewById(R.id.tvRoomLocation);
            imgFavorite = itemView.findViewById(R.id.imgFavorite);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onRoomClick(roomList.get(position));
                }
            });

            imgFavorite.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onRemoveFavorite(roomList.get(position), position);
                }
            });
        }

        void bind(Room room, int position) {
            tvRoomTitle.setText(room.getTitle());
            tvRoomPrice.setText("Price: " + room.getPriceDisplay());
            tvRoomLocation.setText(room.getFullAddress());

            // Load image
            if (room.getThumbnailUrl() != null && !room.getThumbnailUrl().isEmpty()) {
                Glide.with(itemView.getContext())
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
