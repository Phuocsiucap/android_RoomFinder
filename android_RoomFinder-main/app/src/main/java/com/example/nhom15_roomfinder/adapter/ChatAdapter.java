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
import com.example.nhom15_roomfinder.entity.Chat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Adapter cho danh sách chat
 */
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<Chat> chatList;
    private OnChatClickListener listener;

    public interface OnChatClickListener {
        void onChatClick(Chat chat);
    }

    public ChatAdapter(List<Chat> chatList, OnChatClickListener listener) {
        this.chatList = chatList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Chat chat = chatList.get(position);
        holder.bind(chat);
    }

    @Override
    public int getItemCount() {
        return chatList != null ? chatList.size() : 0;
    }

    public void updateData(List<Chat> newChatList) {
        this.chatList = newChatList;
        notifyDataSetChanged();
    }

    class ChatViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgAvatar;
        private TextView tvChatName;
        private TextView tvLastMessage;
        private TextView tvTime;
        private TextView tvUnreadCount;

        ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            tvChatName = itemView.findViewById(R.id.tvChatName);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvUnreadCount = itemView.findViewById(R.id.tvUnreadCount);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onChatClick(chatList.get(position));
                }
            });
        }

        void bind(Chat chat) {
            tvChatName.setText(chat.getRecipientName());
            tvLastMessage.setText(chat.getLastMessage());
            tvTime.setText(formatTime(chat.getLastMessageTime()));

            // Load avatar
            if (chat.getRecipientAvatar() != null && !chat.getRecipientAvatar().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(chat.getRecipientAvatar())
                        .placeholder(R.drawable.ic_profile_placeholder)
                        .circleCrop()
                        .into(imgAvatar);
            } else {
                imgAvatar.setImageResource(R.drawable.ic_profile_placeholder);
            }

            // Hiển thị số tin chưa đọc
            if (chat.getUnreadCount() > 0) {
                tvUnreadCount.setVisibility(View.VISIBLE);
                tvUnreadCount.setText(String.valueOf(chat.getUnreadCount()));
            } else {
                tvUnreadCount.setVisibility(View.GONE);
            }
        }

        private String formatTime(long timestamp) {
            long now = System.currentTimeMillis();
            long diff = now - timestamp;

            if (diff < TimeUnit.MINUTES.toMillis(1)) {
                return "Vừa xong";
            } else if (diff < TimeUnit.HOURS.toMillis(1)) {
                return (diff / TimeUnit.MINUTES.toMillis(1)) + " phút";
            } else if (diff < TimeUnit.DAYS.toMillis(1)) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                return sdf.format(new Date(timestamp));
            } else if (diff < TimeUnit.DAYS.toMillis(2)) {
                return "Hôm qua";
            } else if (diff < TimeUnit.DAYS.toMillis(7)) {
                return (diff / TimeUnit.DAYS.toMillis(1)) + " ngày";
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                return sdf.format(new Date(timestamp));
            }
        }
    }
}
