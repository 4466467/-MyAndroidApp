package com.example.ourbookapplication;

import android.os.Parcel;
import android.os.Parcelable;

// 添加 Parcelable 接口实现
public class Book implements Parcelable {
    // 1. 定义核心字段（成员变量）
    private int id;
    private int bookId; // 书籍唯一标识
    private String title; // 书名
    private String author;
    private String coverImage; // 新增
    private double price; // 价格
    private double latitude; // 新增：位置纬度（核心）
    private double longitude; // 新增：位置经度（核心）
    private String locationName; // 新增：可读的位置描述，如"梓苑食堂门口"
    private String sellerId; // 卖家ID
    private String location;  // 确保有这个字段
    private String description; // 添加描述字段
    private String sellerContact;

    public Book() {
        // 空构造函数
    }

    // 2. 构造方法（用于创建Book对象）
    public Book(String title, double price, double latitude, double longitude, String locationName, String sellerId) {
        //this.bookId = generateUniqueId();  // 生成唯一ID
        this.title = title;
        this.price = price;
        this.latitude = latitude;
        this.longitude = longitude;
        this.locationName = locationName;
        //this.location = locationName;
        this.sellerId = sellerId;
        this.description = ""; // 初始化描述
        this.sellerContact = "";
    }

    // 添加带描述的构造方法
    public Book(String title, double price, double latitude, double longitude, String location, String sellerId, String description) {
        this(title, price, latitude, longitude, location, sellerId);
        this.description = description;
    }

    // 简单构造方法
    public Book(String title, double price, double latitude, double longitude, String location) {
        this.title = title;
        this.price = price;
        this.latitude = latitude;
        this.longitude = longitude;
        this.location = (location != null && !location.trim().isEmpty()) ?
                location : generateLocationFromCoordinates(latitude, longitude);
        this.bookId = generateUniqueId();
        this.sellerId = "unknown";
        this.description = "";
        this.locationName = this.location;
    }

    // ============ Parcelable 实现 ============
    // Parcelable 构造方法
    protected Book(Parcel in) {
        bookId = in.readInt();
        title = in.readString();
        price = in.readDouble();
        latitude = in.readDouble();
        longitude = in.readDouble();
        locationName = in.readString();
        sellerId = in.readString();
        location = in.readString();
        description = in.readString();
        sellerContact = in.readString();
    }

    // 必须使用 public static final
    public static final Parcelable.Creator<Book> CREATOR = new Parcelable.Creator<Book>() {
        @Override
        public Book createFromParcel(Parcel in) {
            return new Book(in);
        }

        @Override
        public Book[] newArray(int size) {
            return new Book[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(bookId);
        dest.writeString(title);
        dest.writeDouble(price);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(locationName);
        dest.writeString(sellerId);
        dest.writeString(location);
        dest.writeString(description);
        dest.writeString(sellerContact);
    }

    @Override
    public int describeContents() {
        return 0;
    }
    // ============ Parcelable 结束 ============

    private int generateUniqueId() {
        // 简单的ID生成方法
        return (int) (System.currentTimeMillis() & 0xfffffff);
    }

    /**
     * 根据坐标生成位置描述
     */
    private String generateLocationFromCoordinates(double lat, double lng) {
        // 云南大学呈贡校区范围内的智能位置识别
        if (lat >= 24.82 && lat <= 24.84 && lng >= 102.84 && lng <= 102.86) {
            if (lat > 24.835) {
                return "云南大学楠苑区域";
            } else if (lat > 24.833) {
                return "云南大学教学区";
            } else if (lat > 24.831) {
                return "云南大学梓苑区域";
            } else {
                return "云南大学校区";
            }
        }
        return String.format("位置(%.4f,%.4f)", lat, lng);
    }

    // 3. 为每个字段生成 Getter 和 Setter 方法
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }

    public String getTitle() { return title != null ? title : "未知书名"; }
    public void setTitle(String title) { this.title = title; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getLocationName() { return locationName != null ? locationName : "未知位置"; }
    public void setLocationName(String locationName) { this.locationName = locationName; }

    public String getSellerId() { return sellerId != null ? sellerId : "unknown"; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }

    public String getLocation() {
        if (location != null && !location.isEmpty()) {
            return location;
        }
        if (locationName != null && !locationName.isEmpty()) {
            return locationName;
        }
        return generateLocationFromCoordinates(latitude, longitude);
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description != null ? description : "";
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSellerContact() {
        return sellerContact != null ? sellerContact : "";
    }

    public void setSellerContact(String sellerContact) {
        this.sellerContact = sellerContact;
    }

    @Override
    public String toString() {
        return "Book{" +
                "id=" + bookId +
                ", title='" + title + '\'' +
                ", price=" + price +
                ", location='" + getLocation() + '\'' +
                '}';
    }
}