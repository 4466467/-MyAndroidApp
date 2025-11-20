package com.example.ourbookapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapsInitializer;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
// 新增聚合库相关的import
//import com.amap.api.maps.cluster.ClusterItem;
//import com.amap.api.maps.cluster.ClusterManager;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private MapView mapView;
    private AMap aMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 设置隐私合规
        MapsInitializer.updatePrivacyShow(this, true, true);
        MapsInitializer.updatePrivacyAgree(this, true);

        setContentView(R.layout.activity_main);

        // 初始化地图
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        if (aMap == null) {
            aMap = mapView.getMap();


            // 设置地图初始位置
            LatLng yunnanUniversity = new LatLng(24.8333, 102.8519);
            aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(yunnanUniversity, 15f));

            // 设置标记点击监听器 - 正确位置
            aMap.setOnMarkerClickListener(marker -> {
                Book clickedBook = (Book) marker.getObject();
                if (clickedBook != null) {
                    Toast.makeText(MainActivity.this,
                            "点击了：" + clickedBook.getTitle() + "，价格：" + clickedBook.getPrice(),
                            Toast.LENGTH_SHORT).show();
                    marker.showInfoWindow();
                }
                return true;
            });

            // 加载并显示书籍标记
            loadAndDisplayBooks();


            // 初始化定位和附近搜索
            initLocation();


        }





        // 测试网络连接 - 使用Lambda简化
        new Thread(() -> {
            try {
                URL url = new URL("https://restapi.amap.com");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(5000);
                connection.connect();
                Log.d("NetworkTest", "Response Code: " + connection.getResponseCode());
                connection.disconnect();
            } catch (Exception e) {
                Log.e("NetworkTest", "Network error: " + e.getMessage());
            }
        }).start();

        // 临时测试：添加一个固定标记点
        LatLng testPoint = new LatLng(24.834653, 102.851120); // 云南大学坐标
        aMap.addMarker(new MarkerOptions()
                .position(testPoint)
                .title("测试标记")
                .snippet("这是一个测试标记点")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

// 移动到测试点
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(testPoint, 16f));

        // 检查是否需要添加测试数据
        checkAndInitializeDatabase();
    }

    private void checkAndInitializeDatabase() {
        new Thread(() -> {
            try {
                DatabaseHelper dbHelper = new DatabaseHelper(this);
                List<Book> books = dbHelper.getAllBooks();

                runOnUiThread(() -> {
                    if (books.isEmpty()) {
                        Toast.makeText(this, "数据库为空，正在添加测试数据...", Toast.LENGTH_SHORT).show();
                        addTestData(); // 添加测试数据
                    } else {
                        Log.d("Database", "数据库中已有 " + books.size() + " 本书籍");
                    }
                });
            } catch (Exception e) {
                Log.e("Database", "检查数据库失败", e);
            }
        }).start();
    }



    private void addTestData() {
        new Thread(() -> {
            DatabaseHelper dbHelper = null;
            try {
                dbHelper = new DatabaseHelper(MainActivity.this); // 使用 MainActivity.this

                // 修正：使用正确的构造函数参数传递方式
                Book testBook1 = new Book("高等数学", 25.0, 24.834653, 102.851120, "云南大学梓苑食堂", "test_seller_1");
                Book testBook2 = new Book("Java编程思想", 40.0, 24.836000, 102.849800, "图书馆门口", "test_seller_2");
                Book testBook3 = new Book("英语四级词汇", 15.0, 24.832500, 102.853200, "教学楼A区", "test_seller_3");

                // 需要在 DatabaseHelper 中实现 addBook 方法
                dbHelper.addBook(testBook1);
                dbHelper.addBook(testBook2);
                dbHelper.addBook(testBook3);

                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "已添加3本测试书籍", Toast.LENGTH_SHORT).show();
                    // 重新加载并显示书籍
                    loadAndDisplayBooks();
                });

            } catch (Exception e) {
                Log.e("Database", "添加测试数据失败", e);
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this, "添加失败: " + e.getMessage(), Toast.LENGTH_LONG).show());
            } finally {
                if (dbHelper != null) {
                    dbHelper.close(); // 确保关闭数据库连接
                }
            }
        }).start();
    }


    // 修复1：使用try-with-resources管理数据库资源
    // ==================== 替换或修改您现有的方法 ====================
    private void loadAndDisplayBooks() {
        new Thread(() -> {
            try {
                DatabaseHelper dbHelper = new DatabaseHelper(MainActivity.this);
                List<Book> bookList = dbHelper.getAllBooks();

                Log.d("MapDebug", "从数据库加载了 " + bookList.size() + " 本书籍");

                runOnUiThread(() -> {
                    // 清除旧标记（可选）
                    // aMap.clear();

                    for (Book book : bookList) {
                        LatLng bookLocation = new LatLng(book.getLatitude(), book.getLongitude());

                        Log.d("MapDebug", "添加标记: " + book.getTitle() + " 位置: " + bookLocation);

                        Marker marker = aMap.addMarker(new MarkerOptions()
                                .position(bookLocation)
                                .title(book.getTitle())
                                .snippet("价格: ￥" + book.getPrice())
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                        marker.setObject(book);
                    }

                    if (bookList.isEmpty()) {
                        Toast.makeText(MainActivity.this, "没有找到书籍数据", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Log.e("MapDebug", "加载书籍数据失败", e);
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "加载数据失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
// ==================== 到这里结束 ====================


    // 修复4：实现matchBooksWithPois方法
    private void matchBooksWithPois(ArrayList<PoiItem> pois) {
        if (pois == null || pois.isEmpty()) {
            Log.d("PoiSearch", "未找到附近POI");
            return;
        }

        // 简单的匹配逻辑：这里可以根据您的业务需求实现
        // 例如：将POI名称与书籍位置名称匹配
        for (PoiItem poi : pois) {
            Log.d("PoiMatch", "找到POI: " + poi.getTitle() + " at " + poi.getLatLonPoint());
            // 实际业务中，这里应该与您的书籍数据进行匹配
        }

        // 临时解决方案：直接在地图上显示这些POI
        runOnUiThread(() -> {
            for (PoiItem poi : pois) {
                LatLonPoint point = poi.getLatLonPoint();
                LatLng location = new LatLng(point.getLatitude(), point.getLongitude());

                aMap.addMarker(new MarkerOptions()
                        .position(location)
                        .title(poi.getTitle())
                        .snippet("类型: " + poi.getTypeDes()));
            }
        });
    }

    // 修复5：更新定位初始化方法，处理异常
    private void initLocation() {
        try {
            AMapLocationClient locationClient = new AMapLocationClient(this);
            AMapLocationClientOption option = new AMapLocationClientOption();
            option.setOnceLocation(true);
            option.setNeedAddress(true);

            locationClient.setLocationListener(aMapLocation -> {
                if (aMapLocation != null && aMapLocation.getErrorCode() == 0) {
                    double userLat = aMapLocation.getLatitude();
                    double userLon = aMapLocation.getLongitude();

                    // 搜索附近书籍
                    searchNearbyBooks(userLat, userLon, 1000);

                    LatLng userLocation = new LatLng(userLat, userLon);
                    aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f));
                }
            });
            locationClient.setLocationOption(option);
            locationClient.startLocation();
        } catch (Exception e) {
            Log.e("Location", "定位初始化失败", e);
        }
    }

    // 修复6：更新POI搜索方法，使用新API（如果可用）
    private void searchNearbyBooks(double userLat, double userLon, int radius) {
        try {
            // 注意：PoiSearch已弃用，建议查看高德最新SDK使用新API
            PoiSearch.Query query = new PoiSearch.Query("", "", "");
            query.setPageSize(20);
            query.setPageNum(1);

            PoiSearch poiSearch = new PoiSearch(this, query);
            LatLonPoint centerPoint = new LatLonPoint(userLat, userLon);
            PoiSearch.SearchBound searchBound = new PoiSearch.SearchBound(centerPoint, radius);
            poiSearch.setBound(searchBound);

            poiSearch.setOnPoiSearchListener(new PoiSearch.OnPoiSearchListener() {
                @Override
                public void onPoiSearched(PoiResult result, int rCode) {
                    if (rCode == 1000) {
                        ArrayList<PoiItem> pois = result.getPois();
                        matchBooksWithPois(pois);
                    }
                }

                @Override
                public void onPoiItemSearched(PoiItem item, int rCode) {
                    // 单个POI搜索回调
                }
            });

            poiSearch.searchPOIAsyn();
        } catch (Exception e) {
            Log.e("PoiSearch", "POI搜索失败", e);
        }
    }

    // 在MainActivity中添加
    //private ClusterManager<BookClusterItem> mClusterManager; // 聚合管理器


    private void setupClusterManager() {
        // 1. 创建聚合管理器
       // mClusterManager = new ClusterManager<>(this, aMap);

        // 2. 设置地图的点击事件由聚合管理器处理
       // aMap.setOnCameraChangeListener(mClusterManager);
       // aMap.setOnMarkerClickListener(mClusterManager);

        // 3. 设置聚合渲染器（自定义聚合点的外观）
        //mClusterManager.setRenderer(new BookClusterRenderer(this, aMap, mClusterManager));
    }

    // 修改加载书籍的方法，使用聚合管理器添加标记
    private void addBooksToMap(List<Book> bookList) {
        // 清除旧的聚合项目
        //mClusterManager.clearItems();
/*
        for (Book book : bookList) {
            // 为每本书创建一个ClusterItem（聚合项目）
            LatLng bookLocation = new LatLng(book.getLatitude(), book.getLongitude());
            BookClusterItem item = new BookClusterItem(bookLocation, book.getTitle(), "￥" + book.getPrice(), book);
            mClusterManager.addItem(item);
        }*/

        aMap.clear(); // 清除旧标记

        for (Book book : bookList) {
            LatLng bookLocation = new LatLng(book.getLatitude(), book.getLongitude());
            Marker marker = aMap.addMarker(new MarkerOptions()
                    .position(bookLocation)
                    .title(book.getTitle())
                    .snippet("价格：￥" + book.getPrice()));

            marker.setObject(book);
        }

        // 告诉聚合管理器进行聚类
        //mClusterManager.cluster();
    }

    // 自定义聚合项目类
    /*public class BookClusterItem implements ClusterItem {
        private final LatLng position;
        private final String title;
        private final String snippet;
        private final Book book;

        public BookClusterItem(LatLng position, String title, String snippet, Book book) {
            this.position = position;
            this.title = title;
            this.snippet = snippet;
            this.book = book;
        }

        @Override
        public LatLng getPosition() {
            return position;
        }

        public String getTitle() { return title; }
        public String getSnippet() { return snippet; }
        public Book getBook() { return book; }
    }*/

    // 自定义聚合渲染器（控制聚合点的外观）
    /*public class BookClusterRenderer extends DefaultClusterRenderer<BookClusterItem> {
        public BookClusterRenderer(Context context, AMap map, ClusterManager<BookClusterItem> clusterManager) {
            super(context, map, clusterManager);
        }

        @Override
        protected void onBeforeClusterItemRendered(BookClusterItem item, MarkerOptions markerOptions) {
            // 设置单个标记点的样式
            markerOptions.title(item.getTitle())
                    .snippet(item.getSnippet())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        }
    }*/

    // 生命周期方法保持不变
    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapView != null) mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) mapView.onSaveInstanceState(outState);
    }
}