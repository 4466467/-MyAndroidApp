package com.example.ourbookapplication;

import com.amap.api.maps.model.LatLng;
import java.util.ArrayList;
import java.util.List;

/**
 * 聚合点类 - 表示一个聚合点或单个标记
 */
public class ClusterPoint {
    private LatLng center;           // 中心位置
    private List<Book> books;       // 包含的书籍
    private boolean isCluster;      // 是否是聚合点

    public ClusterPoint(LatLng center, List<Book> books) {
        this.center = center;
        this.books = books;
        this.isCluster = books.size() > 1;
    }

    public ClusterPoint(Book book) {
        this.center = new LatLng(book.getLatitude(), book.getLongitude());
        this.books = new ArrayList<>();
        this.books.add(book);
        this.isCluster = false;
    }

    // Getter方法
    //public LatLng getCenter() { return center; }
    //public List<Book> getBooks() { return books; }
   // public boolean isCluster() { return isCluster; }
    //public int getSize() { return books.size(); }
    //public Book getFirstBook() { return books.get(0); }
    public boolean isCluster() {
        return books.size() > 1;
    }

    public int getSize() {
        return books.size();
    }

    public LatLng getCenter() {
        return center;
    }

    public List<Book> getBooks() {
        return books;
    }

    public Book getFirstBook() {
        return books != null && !books.isEmpty() ? books.get(0) : null;
    }
}