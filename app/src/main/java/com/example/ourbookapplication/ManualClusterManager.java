package com.example.ourbookapplication;

import android.content.Context;
import android.util.Log;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.model.*;
import java.util.*;

/**
 * 手动聚合管理器 - 核心聚合逻辑
 */
public class ManualClusterManager {
    private Context context;
    private AMap aMap;
    private List<Marker> currentMarkers = new ArrayList<>();
    private List<Book> allBooks = new ArrayList<>();
    private float currentZoom = 15.0f;
    private DatabaseHelper dbHelper;

    public ManualClusterManager(Context context, AMap aMap) {
        this.context = context;
        this.aMap = aMap;
        setupZoomListener();
        this.dbHelper = new DatabaseHelper(context);
        //loadBooks(); // 加载所有书籍
        //initMarkerClick(); // 初始化标记/聚合点点击
    }


    /**
     * 设置缩放监听
     */
    private void setupZoomListener() {
        aMap.setOnCameraChangeListener(new AMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                // 什么都不做
            }

            @Override
            public void onCameraChangeFinish(CameraPosition cameraPosition) {
                currentZoom = cameraPosition.zoom;
                updateClusters();
            }
        });
    }

    /**
     * 添加书籍数据
     */
    public void addBooks(List<Book> books) {
        allBooks.addAll(books);
        updateClusters();
    }

    /**
     * 更新聚合状态
     */
    public void updateClusters() {
        clearMarkers();

        if (currentZoom < 14.0f) {
            // 缩放级别小，显示聚合点
            showClusteredMarkers();
        } else {
            // 缩放级别大，显示详细标记
            showDetailedMarkers();
        }
    }

    /**
     * 显示聚合标记
     */
    private void showClusteredMarkers() {
        List<ClusterPoint> clusters = clusterBooks(allBooks);

        for (ClusterPoint cluster : clusters) {
            Marker marker = createClusterMarker(cluster);
            currentMarkers.add(marker);
        }

        Log.d("Cluster", "显示 " + clusters.size() + " 个聚合点");
    }

    /**
     * 显示详细标记
     */
    private void showDetailedMarkers() {
        for (Book book : allBooks) {
            Marker marker = createBookMarker(book);
            currentMarkers.add(marker);
        }

        Log.d("Cluster", "显示 " + allBooks.size() + " 个详细标记");
    }

    /**
     * 简单聚合算法
     */
    private List<ClusterPoint> clusterBooks(List<Book> books) {
        List<ClusterPoint> clusters = new ArrayList<>();
        Set<Book> processed = new HashSet<>();
        double clusterDistance = 0.001; // 约100米

        for (Book book : books) {
            if (processed.contains(book)) continue;

            LatLng bookLocation = new LatLng(book.getLatitude(), book.getLongitude());
            List<Book> clusterBooks = new ArrayList<>();
            clusterBooks.add(book);
            processed.add(book);

            // 查找附近的书籍
            for (Book other : books) {
                if (processed.contains(other)) continue;

                LatLng otherLocation = new LatLng(other.getLatitude(), other.getLongitude());
                double distance = calculateDistance(bookLocation, otherLocation);

                if (distance <= clusterDistance) {
                    clusterBooks.add(other);
                    processed.add(other);
                }
            }

            // 创建聚合点
            if (clusterBooks.size() == 1) {
                clusters.add(new ClusterPoint(clusterBooks.get(0)));
            } else {
                // 计算中心点
                double avgLat = 0, avgLng = 0;
                for (Book b : clusterBooks) {
                    avgLat += b.getLatitude();
                    avgLng += b.getLongitude();
                }
                avgLat /= clusterBooks.size();
                avgLng /= clusterBooks.size();

                clusters.add(new ClusterPoint(new LatLng(avgLat, avgLng), clusterBooks));
            }
        }

        return clusters;
    }

    /**
     * 计算两点间距离
     */
    private double calculateDistance(LatLng p1, LatLng p2) {
        double latDiff = p1.latitude - p2.latitude;
        double lngDiff = p1.longitude - p2.longitude;
        return Math.sqrt(latDiff * latDiff + lngDiff * lngDiff);
    }

    /**
     * 创建聚合标记
     */
    private Marker createClusterMarker(ClusterPoint cluster) {
        MarkerOptions options = new MarkerOptions().position(cluster.getCenter());

        if (cluster.isCluster()) {
            // 聚合点
            options.title(cluster.getSize() + " 本书籍")
                    .snippet("点击查看详情");

            // 根据数量设置颜色
            if (cluster.getSize() > 10) {
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            } else if (cluster.getSize() > 5) {
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            } else {
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
            }
        } else {
            // 单个标记
            Book book = cluster.getFirstBook();
            options.title(book.getTitle())
                    .snippet("价格：￥" + book.getPrice())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        }

        Marker marker = aMap.addMarker(options);
        marker.setObject(cluster);
        return marker;
    }

    /**
     * 创建单个书籍标记
     */
    private Marker createBookMarker(Book book) {
        LatLng location = new LatLng(book.getLatitude(), book.getLongitude());
        Marker marker = aMap.addMarker(new MarkerOptions()
                .position(location)
                .title(book.getTitle())
                .snippet("价格：￥" + book.getPrice())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        marker.setObject(book);
        return marker;
    }

    /**
     * 清除所有标记
     */
    private void clearMarkers() {
        for (Marker marker : currentMarkers) {
            marker.remove();
        }
        currentMarkers.clear();
    }

    /**
     * 处理标记点击
     */
    // 在ManualClusterManager的onMarkerClick方法中修改：
    public boolean onMarkerClick(Marker marker) {
        Object obj = marker.getObject();

        if (obj instanceof ClusterPoint) {
            ClusterPoint cluster = (ClusterPoint) obj;
            if (cluster.isCluster()) {
                // 聚合点，判断是否需要缩放或跳转
                if (cluster.getSize() == 1) {
                    // 单个书籍，跳转到详情页
                    Book book = cluster.getFirstBook();
                    if (book != null && context instanceof MainActivity) {
                        ((MainActivity) context).navigateToBookDetail(book);
                        return true;
                    }
                } else {
                    // 多个标记，缩放到聚合点区域
                    zoomToCluster(cluster);
                }
                return true;
            } else {
                // 单个点，跳转到详情页
                Book book = cluster.getFirstBook();
                if (book != null && context instanceof MainActivity) {
                    ((MainActivity) context).navigateToBookDetail(book);
                    return true;
                }
            }
        } else if (obj instanceof Book) {
            // 书籍标记，跳转到详情页
            Book book = (Book) obj;
            if (context instanceof MainActivity) {
                ((MainActivity) context).navigateToBookDetail(book);
                return true;
            }
        }

        return false;
    }

    /**
     * 缩放到聚合点区域
     */
    private void zoomToCluster(ClusterPoint cluster) {
        if (cluster.getSize() == 1) {
            // 单个标记，直接定位
            Book book = cluster.getFirstBook();
            LatLng location = new LatLng(book.getLatitude(), book.getLongitude());
            aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 16f));
        } else {
            // 多个标记，计算边界
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (Book book : cluster.getBooks()) {
                builder.include(new LatLng(book.getLatitude(), book.getLongitude()));
            }
            aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));
        }
    }

    /**
     * 清除所有数据
     */
    public void clear() {
        clearMarkers();
        allBooks.clear();
    }
}