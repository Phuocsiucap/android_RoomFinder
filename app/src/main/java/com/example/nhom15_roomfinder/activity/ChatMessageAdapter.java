package com.example.nhom15_roomfinder.activity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom15_roomfinder.R;
import com.example.nhom15_roomfinder.entity.ChatMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.MessageViewHolder> {

    private List<ChatMessage> messageList;
    private String currentUserId;

    public ChatMessageAdapter(List<ChatMessage> messageList, String currentUserId) {
        this.messageList = messageList;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessage message = messageList.get(position);
        holder.bind(message);
    }

    @Override
    public int getItemCount() {
        return messageList != null ? messageList.size() : 0;
    }

    class MessageViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutSender, layoutReceiver;
        TextView tvSenderMessage, tvSenderTime;
        TextView tvReceiverMessage, tvReceiverTime;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutSender = itemView.findViewById(R.id.layoutSender);
            layoutReceiver = itemView.findViewById(R.id.layoutReceiver);
            tvSenderMessage = itemView.findViewById(R.id.tvSenderMessage);
            tvSenderTime = itemView.findViewById(R.id.tvSenderTime);
            tvReceiverMessage = itemView.findViewById(R.id.tvReceiverMessage);
            tvReceiverTime = itemView.findViewById(R.id.tvReceiverTime);
        }

        void bind(ChatMessage message) {
            String time = new SimpleDateFormat("HH:mm", Locale.getDefault())
                    .format(new Date(message.getTimestamp()));

            if (message.getSenderId().equals(currentUserId)) {
                // Tin nhắn của mình -> hiện bên phải
                layoutSender.setVisibility(View.VISIBLE);
                layoutReceiver.setVisibility(View.GONE);
                tvSenderMessage.setText(message.getMessage());
                tvSenderTime.setText(time);
            } else {
                // Tin nhắn của người khác -> hiện bên trái
                layoutSender.setVisibility(View.GONE);
                layoutReceiver.setVisibility(View.VISIBLE);
                tvReceiverMessage.setText(message.getMessage());
                tvReceiverTime.setText(time);
            }
        }
    }
}
