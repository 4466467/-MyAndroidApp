package com.example.ourbookapplication;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChatMessage {
    public static final int TYPE_SENT = 0;
    public static final int TYPE_RECEIVED = 1;

    private String content;
    private int type;
    private String senderId;
    private Date timestamp;

    public ChatMessage(String content, int type, String senderId) {
        this.content = content;
        this.type = type;
        this.senderId = senderId;
        this.timestamp = new Date();
    }

    // Getters
    public String getContent() { return content; }
    public int getType() { return type; }
    public String getSenderId() { return senderId; }
    public Date getTimestamp() { return timestamp; }

    public String getFormattedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.CHINA);
        return sdf.format(timestamp);
    }
}