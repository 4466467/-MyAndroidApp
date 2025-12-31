package com.example.ourbookapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import java.util.List;

public class ChatAdapter extends ArrayAdapter<ChatMessage> {
    private int sentLayout;
    private int receivedLayout;
    private String currentUserId;

    public ChatAdapter(@NonNull Context context, int sentLayout, int receivedLayout,
                       @NonNull List<ChatMessage> messages, String currentUserId) {
        super(context, 0, messages);
        this.sentLayout = sentLayout;
        this.receivedLayout = receivedLayout;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ChatMessage message = getItem(position);

        if (convertView == null) {
            // 根据消息类型选择布局
            if (message.getType() == ChatMessage.TYPE_SENT ||
                    message.getSenderId().equals(currentUserId)) {
                convertView = LayoutInflater.from(getContext()).inflate(sentLayout, parent, false);
            } else {
                convertView = LayoutInflater.from(getContext()).inflate(receivedLayout, parent, false);
            }
        }

        TextView tvMessage = convertView.findViewById(R.id.tv_message);
        TextView tvTime = convertView.findViewById(R.id.tv_time);
        LinearLayout messageBubble = convertView.findViewById(R.id.message_bubble);
        CardView cardView = convertView.findViewById(R.id.card_view);

        if (tvMessage != null) {
            tvMessage.setText(message.getContent());
        }

        if (tvTime != null) {
            tvTime.setText(message.getFormattedTime());
        }

        // 设置气泡样式
        if (messageBubble != null) {
            // 根据消息类型设置对齐方式
            if (message.getType() == ChatMessage.TYPE_SENT ||
                    message.getSenderId().equals(currentUserId)) {
                // 自己发送的消息 - 右侧
                ((LinearLayout.LayoutParams) messageBubble.getLayoutParams()).gravity = android.view.Gravity.END;
                if (cardView != null) {
                    cardView.setCardBackgroundColor(getContext().getResources().getColor(R.color.message_sent_bg));
                }
            } else {
                // 接收的消息 - 左侧
                ((LinearLayout.LayoutParams) messageBubble.getLayoutParams()).gravity = android.view.Gravity.START;
                if (cardView != null) {
                    cardView.setCardBackgroundColor(getContext().getResources().getColor(R.color.message_received_bg));
                }
            }
        }

        return convertView;
    }
}