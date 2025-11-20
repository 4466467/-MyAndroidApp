package com.example.ourbookapplication; // 你的包名

public class Book {
    // 1. 定义核心字段（成员变量）
    private int bookId; // 书籍唯一标识
    private String title; // 书名
    private double price; // 价格
    private double latitude; // 新增：位置纬度（核心）
    private double longitude; // 新增：位置经度（核心）
    private String locationName; // 新增：可读的位置描述，如“梓苑食堂门口”
    private String sellerId; // 卖家ID

    // 2. 构造方法（用于创建Book对象）
    public Book(String title, double price, double latitude, double longitude, String locationName, String sellerId) {
        this.title = title;
        this.price = price;
        this.latitude = latitude;
        this.longitude = longitude;
        this.locationName = locationName;
        this.sellerId = sellerId;
    }

    // 3. 为每个字段生成 Getter 和 Setter 方法
    // 在Android Studio中，你可以将光标放在字段声明行，按 `Alt + Insert` 快速生成。

    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public double getLatitude() { return latitude; } // 重要：地图SDK将使用这个值
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; } // 重要：地图SDK将使用这个值
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }

    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }
}