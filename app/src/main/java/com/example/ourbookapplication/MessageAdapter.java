package com.example.ourbookapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.List;

public class MessageAdapter extends BaseAdapter {
    private Context context;
    private List<Message> messageList;

    public MessageAdapter(Context context, List<Message> messageList) {
        this.context = context;
        this.messageList = messageList;
    }

    @Override
    public int getCount() {
        return messageList.size();
    }

    @Override
    public Message getItem(int position) {
        return messageList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_message_list, parent, false);
            holder = new ViewHolder();
            holder.tvSellerName = convertView.findViewById(R.id.tv_seller_name);
            holder.tvBookTitle = convertView.findViewById(R.id.tv_book_title);
            holder.tvLastMessage = convertView.findViewById(R.id.tv_last_message);
            holder.tvTime = convertView.findViewById(R.id.tv_time);
            holder.tvUnreadCount = convertView.findViewById(R.id.tv_unread_count);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Message message = getItem(position);

        holder.tvSellerName.setText(message.getSellerName());
        holder.tvBookTitle.setText(message.getBookTitle());
        holder.tvLastMessage.setText(message.getLastMessage());
        holder.tvTime.setText(message.getTime());

        if (message.getUnreadCount() > 0) {
            holder.tvUnreadCount.setVisibility(View.VISIBLE);
            holder.tvUnreadCount.setText(String.valueOf(message.getUnreadCount()));
        } else {
            holder.tvUnreadCount.setVisibility(View.GONE);
        }

        return convertView;
    }

    public void setData(List<Message> messages) {
        this.messageList = messages;
        notifyDataSetChanged();
    }

    public void addMessage(Message message) {
        this.messageList.add(message);
        notifyDataSetChanged();
    }

    public void clear() {
        this.messageList.clear();
        notifyDataSetChanged();
    }

    static class ViewHolder {
        TextView tvSellerName;
        TextView tvBookTitle;
        TextView tvLastMessage;
        TextView tvTime;
        TextView tvUnreadCount;
    }
}