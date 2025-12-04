package com.example.nhom15_roomfinder.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.nhom15_roomfinder.R;
import com.example.nhom15_roomfinder.entity.ChatMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.MessageViewHolder> {

    private List<ChatMessage> messageList;
    private String currentUserId;
    private Context context;
    private OnImageClickListener imageClickListener;

    public interface OnImageClickListener {
        void onImageClick(String imageUrl);
    }

    public ChatMessageAdapter(List<ChatMessage> messageList, String currentUserId) {
        this.messageList = messageList;
        this.currentUserId = currentUserId;
    }

    public void setOnImageClickListener(OnImageClickListener listener) {
        this.imageClickListener = listener;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context)
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
        CardView cardSenderImage, cardReceiverImage;
        ImageView ivSenderImage, ivReceiverImage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutSender = itemView.findViewById(R.id.layoutSender);
            layoutReceiver = itemView.findViewById(R.id.layoutReceiver);
            tvSenderMessage = itemView.findViewById(R.id.tvSenderMessage);
            tvSenderTime = itemView.findViewById(R.id.tvSenderTime);
            tvReceiverMessage = itemView.findViewById(R.id.tvReceiverMessage);
            tvReceiverTime = itemView.findViewById(R.id.tvReceiverTime);
            cardSenderImage = itemView.findViewById(R.id.cardSenderImage);
            cardReceiverImage = itemView.findViewById(R.id.cardReceiverImage);
            ivSenderImage = itemView.findViewById(R.id.ivSenderImage);
            ivReceiverImage = itemView.findViewById(R.id.ivReceiverImage);
        }

        void bind(ChatMessage message) {
            String time = new SimpleDateFormat("HH:mm", Locale.getDefault())
                    .format(new Date(message.getTimestamp()));

            boolean hasImage = message.getImageUrl() != null && !message.getImageUrl().isEmpty();
            boolean hasText = message.getMessage() != null && !message.getMessage().isEmpty();

            if (message.getSenderId().equals(currentUserId)) {
                // Tin nhắn của mình -> hiện bên phải
                layoutSender.setVisibility(View.VISIBLE);
                layoutReceiver.setVisibility(View.GONE);
                
                // Xử lý ảnh
                if (hasImage) {
                    cardSenderImage.setVisibility(View.VISIBLE);
                    loadImage(message.getImageUrl(), ivSenderImage);
                    
                    // Click để xem ảnh full
                    ivSenderImage.setOnClickListener(v -> {
                        if (imageClickListener != null) {
                            imageClickListener.onImageClick(message.getImageUrl());
                        }
                    });
                } else {
                    cardSenderImage.setVisibility(View.GONE);
                }
                
                // Xử lý text
                if (hasText) {
                    tvSenderMessage.setVisibility(View.VISIBLE);
                    tvSenderMessage.setText(message.getMessage());
                } else {
                    tvSenderMessage.setVisibility(View.GONE);
                }
                
                tvSenderTime.setText(time);
            } else {
                // Tin nhắn của người khác -> hiện bên trái
                layoutSender.setVisibility(View.GONE);
                layoutReceiver.setVisibility(View.VISIBLE);
                
                // Xử lý ảnh
                if (hasImage) {
                    cardReceiverImage.setVisibility(View.VISIBLE);
                    loadImage(message.getImageUrl(), ivReceiverImage);
                    
                    // Click để xem ảnh full
                    ivReceiverImage.setOnClickListener(v -> {
                        if (imageClickListener != null) {
                            imageClickListener.onImageClick(message.getImageUrl());
                        }
                    });
                } else {
                    cardReceiverImage.setVisibility(View.GONE);
                }
                
                // Xử lý text
                if (hasText) {
                    tvReceiverMessage.setVisibility(View.VISIBLE);
                    tvReceiverMessage.setText(message.getMessage());
                } else {
                    tvReceiverMessage.setVisibility(View.GONE);
                }
                
                tvReceiverTime.setText(time);
            }
        }

        private void loadImage(String imageUrl, ImageView imageView) {
            RequestOptions options = new RequestOptions()
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_placeholder)
                    .transform(new RoundedCorners(16));

            Glide.with(context)
                    .load(imageUrl)
                    .apply(options)
                    .into(imageView);
        }
    }
}
