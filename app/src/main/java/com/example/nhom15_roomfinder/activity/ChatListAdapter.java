package com.example.nhom15_roomfinder.activity; // ĐỔI cho đúng package

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatViewHolder> {

    public interface OnChatClickListener {
        void onChatClick(ChatItem item);
    }

    private List<ChatItem> chatItems;
    private OnChatClickListener listener;

    public ChatListAdapter(List<ChatItem> chatItems, OnChatClickListener listener) {
        this.chatItems = chatItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Dùng layout có sẵn của Android cho nhanh
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatItem item = chatItems.get(position);
        holder.title.setText(item.getTitle());

        String subtitle = item.getLastMessage() + " · " + item.getTime();
        if (item.getUnreadCount() > 0) {
            subtitle += " · " + item.getUnreadCount() + " tin chưa đọc";
        }
        holder.subtitle.setText(subtitle);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onChatClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatItems.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView subtitle;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            title    = itemView.findViewById(android.R.id.text1);
            subtitle = itemView.findViewById(android.R.id.text2);
        }
    }
}
