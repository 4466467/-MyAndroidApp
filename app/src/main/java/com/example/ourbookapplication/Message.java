package com.example.ourbookapplication;

public class Message {
    private String sellerId;
    private String sellerName;
    private String bookTitle;
    private String lastMessage;
    private String time;
    private int unreadCount;

    public Message(String sellerId, String sellerName, String bookTitle,
                   String lastMessage, String time, int unreadCount) {
        this.sellerId = sellerId;
        this.sellerName = sellerName;
        this.bookTitle = bookTitle;
        this.lastMessage = lastMessage;
        this.time = time;
        this.unreadCount = unreadCount;
    }

    // Getters
    public String getSellerId() { return sellerId; }
    public String getSellerName() { return sellerName; }
    public String getBookTitle() { return bookTitle; }
    public String getLastMessage() { return lastMessage; }
    public String getTime() { return time; }
    public int getUnreadCount() { return unreadCount; }

    // Setters
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }
}