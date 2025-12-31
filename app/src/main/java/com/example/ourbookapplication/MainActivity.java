package com.example.ourbookapplication;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.os.Vibrator;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.text.TextWatcher;  // 添加这个！
import android.widget.Button;  // 添加这个！
import com.amap.api.maps.model.LatLngBounds;  // 添加这个！
import com.amap.api.services.geocoder.GeocodeResult;  // 添加这个！

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.view.View; // 必须导入，setVisibility依赖此类的常量
import android.widget.FrameLayout; // 导入FrameLayout控件类

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
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.example.ourbookapplication.MapFragment.OnMapInteractionListener;
import com.amap.api.maps.model.LatLng;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Handler;

public class MainActivity extends AppCompatActivity implements OnMapInteractionListener {
    private static final int REQUEST_CODE_PUBLISH = 1001;

    private Handler handler = new Handler(Looper.getMainLooper());  // 正确初始化Handler
    private Runnable realTimeSearchRunnable;  // 声明Runnable变量

    private MapView mapView;
    private AMap aMap;

    private EditText etSearch;
    private ImageButton btnClearSearch;
    private TextView tvSearchHint;
    private List<Book> allBooks = new ArrayList<>();
    private boolean isDataLoaded = false; // 添加数据加载状态标志

    private List<Book> filteredBooks = new ArrayList<>();

    private ManualClusterManager clusterManager;

    private FindFragment findFragment;
    private boolean isFindFragmentShowing = false;
    private MapFragment mapFragment;
    private MyFragment myFragment;
    private Button btnFind, btnMap, btnMy;
    private RecyclerView searchResultsList;


    /*@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 先设置布局，避免空指针
        setContentView(R.layout.activity_main);

        // 隐藏标题栏
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // 检查登录状态（在onResume中更合适）
        if (!UserSession.getInstance().isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return; // 直接返回，不执行后续初始化
        }

        try {
            // 初始化基础UI
            setupBottomNavigation();
            initSearchView();
            initSearchResultsList();

            // 地图初始化（放在最后，因为它最耗时）
            initMapView(savedInstanceState);

            // 数据加载（异步进行）
            loadAndDisplayBooks();

            Log.d("MainActivity", "Activity初始化完成");

        } catch (Exception e) {
            Log.e("MainActivity", "初始化失败", e);
            Toast.makeText(this, "应用初始化失败，请重启", Toast.LENGTH_LONG).show();
        }
    } */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. 先设置布局
        setContentView(R.layout.activity_main);
        Log.d("MainActivity", "布局文件 activity_main 加载完成");

        // 2. 隐藏标题栏
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // 3. 检查登录状态（简化版本）
        /*if (!UserSession.getInstance().isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        } */
        Log.d("MainActivity", "临时跳过登录检查，直接进入主界面");

        // 4. 设置隐私合规
        MapsInitializer.updatePrivacyShow(this, true, true);
        MapsInitializer.updatePrivacyAgree(this, true);

        // 5. 仅初始化必要的视图和功能
        try {
            // 初始化基本视图
            initBasicViews();

            // 设置底部导航（这会初始化Fragment）
            setupBottomNavigation();

            // 初始化搜索功能
            initSearchView();
            initSearchResultsList();

            // 关键：只初始化地图，不立即加载数据
            initMapView(savedInstanceState);

            // 默认显示地图页面
            switchToMapView();

            // 延迟加载数据（避免阻塞UI）
            handler.postDelayed(() -> {
                loadAndDisplayBooks();
            }, 500);

            Log.d("MainActivity", "Activity初始化完成");

        } catch (Exception e) {
            Log.e("MainActivity", "初始化失败", e);
            Toast.makeText(this, "应用初始化失败，请重启", Toast.LENGTH_LONG).show();
        }
    }

    private void initBasicViews() {
        // 只绑定最必要的控件
        btnFind = findViewById(R.id.btn_find);
        btnMap = findViewById(R.id.btn_map);
        btnMy = findViewById(R.id.btn_my);
        etSearch = findViewById(R.id.et_search);
        btnClearSearch = findViewById(R.id.btn_clear_search);
        tvSearchHint = findViewById(R.id.tv_search_hint);
        searchResultsList = findViewById(R.id.search_results_list);
        mapView = findViewById(R.id.mapView);
    }

    // 修改 switchToMapView 方法
    private void switchToMapView() {
        // 显示地图
        if (mapView != null) {
            mapView.setVisibility(View.VISIBLE);
        }

        // 显示主界面的搜索框
        if (etSearch != null) {
            etSearch.setVisibility(View.VISIBLE);
        }
        if (btnClearSearch != null) {
            btnClearSearch.setVisibility(View.VISIBLE);
        }
        if (tvSearchHint != null) {
            tvSearchHint.setVisibility(View.VISIBLE);
        }

        // 隐藏Fragment容器
        View fragmentContainer = findViewById(R.id.fragment_container);
        if (fragmentContainer != null) {
            fragmentContainer.setVisibility(View.GONE);
        }

        // 清除Fragment回退栈
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        // 更新按钮状态
        setButtonsSelectedState(false, true, false);
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1001);
        }
    }

    private void switchToFragmentContainer() {
        // 显示Fragment容器，隐藏地图
        if (mapView != null) {
            mapView.setVisibility(View.GONE);
        }
        View fragmentContainer = findViewById(R.id.fragment_container);
        if (fragmentContainer != null) {
            fragmentContainer.setVisibility(View.VISIBLE);
        }
    }

    private void initViews() {
        // 绑定底部导航按钮（确保布局中id为btn_find/btn_map/btn_my）
        btnFind = findViewById(R.id.btn_find);
        btnMap = findViewById(R.id.btn_map);
        btnMy = findViewById(R.id.btn_my);
        // 绑定搜索相关控件
        etSearch = findViewById(R.id.et_search);
        btnClearSearch = findViewById(R.id.btn_clear_search);
        tvSearchHint = findViewById(R.id.tv_search_hint);
        searchResultsList = findViewById(R.id.search_results_list);

// 绑定测试数据按钮
        Button btnAddTest = findViewById(R.id.btn_add_test_data);
        Button btnClear = findViewById(R.id.btn_clear_data);

// 绑定状态文本
        TextView tvStatus = findViewById(R.id.tv_status);

// 绑定地图控件
        mapView = findViewById(R.id.mapView);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限授予，初始化定位
                initLocation();
            } else {
                Toast.makeText(this, "需要定位权限才能使用地图功能", Toast.LENGTH_SHORT).show();
            }
        }
    }



    /**
     * 初始化地图视图（修正：添加Bundle参数）
     */
    private void initMapView(Bundle savedInstanceState) {
        try {
            Log.d("MapView", "开始初始化地图");
            mapView = findViewById(R.id.mapView);
            if (mapView == null) {
                Log.e("MapView", "未找到MapView控件，检查布局文件");
                return;
            }

            mapView.onCreate(savedInstanceState);

            if (aMap == null) {
                aMap = mapView.getMap();
                if (aMap == null) {
                    Log.e("MapView", "AMap获取失败，可能是key配置问题");
                    return;
                }
                Log.d("MapView", "AMap初始化成功");

                // 设置地图初始位置（简化版）
                LatLng yunnanUniversity = new LatLng(24.8333, 102.8519);
                aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(yunnanUniversity, 15f));

                // 延迟初始化聚合管理器
                handler.postDelayed(() -> {
                    initClusterManager();
                }, 1000);
            }

        } catch (Exception e) {
            Log.e("MapView", "地图初始化异常", e);
            // 显示用户友好的提示
            runOnUiThread(() -> {
                Toast.makeText(this, "地图服务暂时不可用", Toast.LENGTH_SHORT).show();
            });
        }
    }

    /**
     * 初始化聚合管理器
     */
    private void initClusterManager() {
        try {
            if (aMap == null) {
                Log.e("Cluster", "AMap为空，无法初始化聚合管理器");
                return;
            }

            // 关键修复：确保正确创建clusterManager
            clusterManager = new ManualClusterManager(this, aMap);

            // 设置标记点击监听
            aMap.setOnMarkerClickListener(new AMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    if (clusterManager != null) {
                        return clusterManager.onMarkerClick(marker);
                    } else {
                        // 聚合管理器不可用，处理单个标记点击
                        return onSingleMarkerClick(marker);
                    }
                }
            });

            Log.d("Cluster", "聚合管理器初始化成功");

        } catch (Exception e) {
            Log.e("Cluster", "聚合管理器初始化失败", e);
            clusterManager = null; // 确保为null，避免后续错误
        }
    }

    /**
     * 单个标记点击处理（聚合功能不可用时使用）
     */
    private boolean onSingleMarkerClick(Marker marker) {
        Object object = marker.getObject();
        if (object instanceof Book) {
            Book book = (Book) object;
            Toast.makeText(this,
                    "点击了：" + book.getTitle() + "，价格：" + book.getPrice(),
                    Toast.LENGTH_SHORT).show();
            marker.showInfoWindow();
            return true;
        }
        return false;
    }

    // 在 MainActivity 中添加该方法（用于点击聚合点时缩放地图）
    // 修改后的 zoomToCluster 方法
    private void zoomToCluster(ClusterPoint cluster) {
        if (aMap == null || cluster.getBooks() == null || cluster.getBooks().isEmpty()) {
            return;
        }

        LatLng center = cluster.getCenter();
        int zoomLevel = cluster.getSize() > 10 ? 14 : 16;
        aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(center, zoomLevel), 500, null);
    }

    /**
     * 设置地图点击监听
     */
    private void setupMapClickListeners() {
        if (aMap == null) {
            Log.e("MapClick", "aMap为空");
            return;
        }

        aMap.setOnMapClickListener(new AMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                // 用户点击地图空白处
                showLocationConfirmationDialog(latLng);
            }
        });

        aMap.setOnMarkerClickListener(new AMap.OnMarkerClickListener() {
            private long lastClickTime = 0;
            private static final long DOUBLE_CLICK_TIME_DELTA = 300;
            private Marker lastClickedMarker = null;
            @Override
            public boolean onMarkerClick(Marker marker) {
                long clickTime = System.currentTimeMillis();
                Object obj = marker.getObject();
                boolean sameMarker = lastClickedMarker == marker;

                // 判断是否为双击（同一个标记，间隔小于300ms）
                if (sameMarker && (clickTime - lastClickTime) < DOUBLE_CLICK_TIME_DELTA) {
                    // 双击事件
                    if (obj instanceof Book) {
                        Book book = (Book) obj;
                        navigateToBookDetail(book);
                    } else if (obj instanceof ClusterPoint) {
                        ClusterPoint cluster = (ClusterPoint) obj;
                        if (!cluster.isCluster()) {
                            // 单个书籍的聚合点
                            Book book = cluster.getFirstBook();
                            navigateToBookDetail(book);
                        } else {
                            // 聚合点双击：放大到该区域
                            zoomToCluster(cluster);
                        }
                    }
                    lastClickTime = 0;
                    lastClickedMarker = null;
                    return true;
                }

                // 单击事件
                lastClickedMarker = marker;
                lastClickTime = clickTime;

                // 显示标记信息
                if (obj instanceof Book) {
                    Book book = (Book) obj;
                    marker.showInfoWindow();
                    Toast.makeText(MainActivity.this,
                            "《" + book.getTitle() + "》\n价格: ￥" + book.getPrice() + "\n位置: " + book.getLocation() +
                                    "\n\n单击显示信息，双击进入详情页",
                            Toast.LENGTH_SHORT).show();
                } else if (obj instanceof ClusterPoint) {
                    ClusterPoint cluster = (ClusterPoint) obj;
                    if (cluster.isCluster()) {
                        // 显示聚合点信息
                        String info = String.format("聚合了 %d 本书籍", cluster.getSize());
                        marker.setTitle("聚合点 (" + cluster.getSize() + "本书)");
                        marker.setSnippet(info + "\n单击查看详情，双击放大区域");
                        marker.showInfoWindow();
                        Toast.makeText(MainActivity.this, info + "\n双击可放大查看", Toast.LENGTH_SHORT).show();
                    } else {
                        // 单个书籍的聚合点
                        Book book = cluster.getFirstBook();
                        marker.showInfoWindow();
                    }
                }
                return true;
            }

            private void handleSingleClick(Marker marker, Object obj) {
                if (obj instanceof Book) {
                    Book book = (Book) obj;
                    marker.showInfoWindow();
                    Toast.makeText(MainActivity.this,
                            String.format("《%s》\n价格: ￥%.1f\n位置: %s",
                                    book.getTitle(), book.getPrice(), book.getLocation()),
                            Toast.LENGTH_SHORT).show();
                } else if (obj instanceof ClusterPoint) {
                    ClusterPoint cluster = (ClusterPoint) obj;
                    if (cluster.isCluster()) {
                        String info = String.format("聚合点 (%d本书)", cluster.getSize());
                        marker.setTitle(info);
                        marker.setSnippet("双击查看详情或放大区域");
                        marker.showInfoWindow();
                    } else {
                        Book book = cluster.getFirstBook();
                        if (book != null) {
                            marker.showInfoWindow();
                        }
                    }
                }
            }
        });

        aMap.setOnMarkerClickListener(new AMap.OnMarkerClickListener() {
            private long lastClickTime = 0;
            private static final long DOUBLE_CLICK_TIME_DELTA = 300; // 双击间隔300ms

            @Override
            public boolean onMarkerClick(Marker marker) {
                long clickTime = System.currentTimeMillis();
                Object obj = marker.getObject();
                if (!(obj instanceof Book)) {
                    // 非Book类型的marker，不处理双击/单击逻辑
                    return false;
                }
                Book book = (Book) obj;
                // 判断是否为双击
                if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
                    // 双击事件 - 跳转到详情页
                    //Book book = (Book) marker.getObject();
                    if (book != null) {
                        Intent intent = new Intent(MainActivity.this, BookDetailActivity.class);
                        intent.putExtra("book_id", book.getBookId());
                        startActivity(intent);
                    }
                    lastClickTime = 0; // 重置
                    return true;
                }
                // 单击事件 - 显示信息窗口
                else {
                    //Book book = (Book) marker.getObject();
                    if (book != null) {
                        Toast.makeText(MainActivity.this,
                                "点击了: " + book.getTitle() + ", 价格: ￥" + book.getPrice(),
                                Toast.LENGTH_SHORT).show();
                        marker.showInfoWindow();
                    }
                    lastClickTime = clickTime;
                    return true;
                }
            }
        });

        // 标记点击处理
        aMap.setOnMarkerClickListener(marker -> {
            try {
                Object obj = marker.getObject();

                if (obj instanceof Book) {
                    Book book = (Book) obj;
                    // 简单处理：直接跳转到详情页
                    navigateToBookDetail(book);
                    return true;
                } else if (obj instanceof ClusterPoint) {
                    ClusterPoint cluster = (ClusterPoint) obj;
                    if (!cluster.isCluster()) {
                        Book book = cluster.getFirstBook();
                        if (book != null) {
                            navigateToBookDetail(book);
                        }
                    } else {
                        // 聚合点：显示信息
                        marker.showInfoWindow();
                        Toast.makeText(this,
                                "聚合点：" + cluster.getSize() + "本书籍",
                                Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
            } catch (Exception e) {
                Log.e("MarkerClick", "标记点击异常", e);
            }
            return false;
        });

        Log.d("MapClick", "地图点击监听器设置完成");
    }

    /**
     * 设置按钮点击监听
     */
    private void setupButtonClickListeners() {
        // 添加测试数据按钮
        Button btnAddTest = findViewById(R.id.btn_add_test_data);
        if (btnAddTest != null) {
            btnAddTest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("添加测试数据")
                            .setMessage("这将清空现有数据并添加测试书籍，确定继续吗？")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    addDemoTestData();
                                }
                            })
                            .setNegativeButton("取消", null)
                            .show();
                }
            });
        }

        // 清空数据按钮
        Button btnClear = findViewById(R.id.btn_clear_data);
        if (btnClear != null) {
            btnClear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("清空数据")
                            .setMessage("确定要清空所有书籍数据吗？")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    clearAllBooks();
                                }
                            })
                            .setNegativeButton("取消", null)
                            .show();
                }
            });
        }
    }

    private void initSearchView() {
        try {
            etSearch = findViewById(R.id.et_search);
            btnClearSearch = findViewById(R.id.btn_clear_search);
            tvSearchHint = findViewById(R.id.tv_search_hint);

            // 关键修复1：确保搜索框可点击、可聚焦
            etSearch.setClickable(true);
            etSearch.setFocusable(true);
            etSearch.setFocusableInTouchMode(true); // 允许触摸获取焦点

            // 关键修复2：点击时主动请求焦点并弹出键盘
            etSearch.setOnClickListener(v -> {
                etSearch.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(etSearch, InputMethodManager.SHOW_IMPLICIT);
            });

            // 关键修复3：清除可能的焦点抢占
            etSearch.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    // 获得焦点时显示键盘
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(etSearch, InputMethodManager.SHOW_IMPLICIT);
                }
            });

            // 原有代码：实时搜索逻辑
            realTimeSearchRunnable = new Runnable() {
                @Override
                public void run() {
                    String query = etSearch.getText().toString().trim();
                    if (!query.isEmpty()) {
                        performSearch(query);
                    }
                }
            };

            // 文本变化监听（保持不变）
            // 修改搜索框的文本变化监听
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    handler.removeCallbacks(realTimeSearchRunnable);

                    String query = s.toString().trim();
                    if (query.isEmpty()) {
                        // 清空搜索时显示所有书籍
                        clearSearch();
                    } else {
                        // 延迟搜索，避免频繁触发
                        handler.postDelayed(() -> {
                            performSearch(query);
                        }, 500);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                    updateClearButtonVisibility(s.length() > 0);
                }
            });

            // 回车键搜索（保持不变）
            etSearch.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String query = etSearch.getText().toString().trim();
                    performSearch(query);
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
                    if (tvSearchHint != null) {
                        tvSearchHint.setText("搜索中: " + query);
                    }
                    return true;
                }
                return false;
            });

            // 清除按钮点击（保持不变）
            btnClearSearch.setOnClickListener(v -> clearSearch());

        } catch (Exception e) {
            Log.e("SearchView", "搜索初始化失败", e);
        }
    }

    /**
     * 执行搜索（增强中文支持）
     */
    /**
     * 执行搜索（修复数据访问问题）
     */
    /*private void performSearch(String query) {
        Log.d("Search", "=== 开始搜索 ===");
        Log.d("Search", "搜索查询: '" + query + "'");

        // 关键修复：检查数据状态
        if (!isDataLoaded) {
            Log.w("Search", "数据未加载，重新加载数据");
            loadAndDisplayBooks();
            Toast.makeText(this, "数据加载中，请稍后重试", Toast.LENGTH_SHORT).show();
            return;
        }

        if (allBooks == null || allBooks.isEmpty()) {
            Log.e("Search", "allBooks为空，无法搜索");
            showNoSearchDataMessage();
            return;
        }

        Log.d("Search", "搜索数据源: allBooks大小 = " + allBooks.size());

        // 预处理查询
        String processedQuery = preprocessQuery(query);
        Log.d("Search", "处理后的查询: '" + processedQuery + "'");

        // 执行搜索
        long startTime = System.currentTimeMillis();
        List<Book> results = enhancedSearch(processedQuery);
        long endTime = System.currentTimeMillis();

        Log.d("Search", "搜索完成: 找到 " + results.size() + " 个结果，耗时 " + (endTime - startTime) + "ms");
        showSearchResults(results, processedQuery);

        // 获取用户当前位置（简化版，实际应该用定位）
        LatLng userLocation = getCurrentUserLocation();

        // 按距离排序结果
        List<Book> sortedResults = sortBooksByDistance(results, userLocation);

        // 显示排序后的结果
        showSearchResults(sortedResults, query);
    } */

    // 在 MainActivity 类中添加拼音搜索支持

// 修改 performSearch 方法，增加拼音搜索
    private void performSearch(String query) {
        Log.d("Search", "=== 开始搜索 ===");
        Log.d("Search", "搜索查询: '" + query + "'");

        if (allBooks == null || allBooks.isEmpty()) {
            Log.w("Search", "书籍数据为空，尝试重新加载");
            loadAndDisplayBooks();
            Toast.makeText(this, "数据加载中，请稍后重试", Toast.LENGTH_SHORT).show();
            return;
        }

        // 清空之前的搜索结果
        searchResults.clear();

        String processedQuery = query.trim().toLowerCase();

        // 执行搜索
        for (Book book : allBooks) {
            if (book == null) continue;

            boolean matched = false;

            // 检查标题是否匹配
            if (book.getTitle() != null &&
                    book.getTitle().toLowerCase().contains(processedQuery)) {
                matched = true;
            }

            // 检查位置是否匹配
            if (!matched && book.getLocation() != null &&
                    book.getLocation().toLowerCase().contains(processedQuery)) {
                matched = true;
            }

            // 检查拼音匹配
            if (!matched && PinyinUtils.enhancedMatch(book.getTitle(), query)) {
                matched = true;
            }

            if (matched) {
                searchResults.add(book);
                Log.d("Search", "匹配成功: " + book.getTitle());
            }
        }

        Log.d("Search", "搜索完成，找到 " + searchResults.size() + " 个结果");

        // 显示搜索结果
        showSearchResultsList(searchResults);

        // 在地图上显示结果标记
        showSearchResultsOnMap(searchResults, query);
    }

    private void showSearchResultsOnMap(List<Book> results, String query) {
        if (aMap == null) {
            Log.e("Search", "地图未初始化");
            return;
        }

        // 清除旧标记
        aMap.clear();

        if (results.isEmpty()) {
            Toast.makeText(this, "未找到包含 \"" + query + "\" 的书籍", Toast.LENGTH_LONG).show();
            return;
        }

        // 在地图上添加搜索结果标记
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        for (Book book : results) {
            try {
                LatLng location = new LatLng(book.getLatitude(), book.getLongitude());

                // 添加标记
                Marker marker = aMap.addMarker(new MarkerOptions()
                        .position(location)
                        .title(book.getTitle())
                        .snippet("价格: ￥" + book.getPrice() + "\n位置: " + book.getLocation())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

                marker.setObject(book);

                // 添加到边界计算
                builder.include(location);

                Log.d("SearchMap", "添加搜索结果标记: " + book.getTitle());

            } catch (Exception e) {
                Log.e("SearchMap", "添加标记失败: " + book.getTitle(), e);
            }
        }

        // 调整地图视野以显示所有结果
        try {
            if (results.size() == 1) {
                // 单个结果，放大显示
                Book book = results.get(0);
                LatLng location = new LatLng(book.getLatitude(), book.getLongitude());
                aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 16f));
            } else {
                // 多个结果，显示边界
                LatLngBounds bounds = builder.build();
                aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
            }
        } catch (Exception e) {
            Log.e("SearchMap", "调整地图视野失败", e);
        }

        // 设置标记点击监听
        aMap.setOnMarkerClickListener(marker -> {
            Object object = marker.getObject();
            if (object instanceof Book) {
                Book clickedBook = (Book) object;
                // 跳转到书籍详情页
                navigateToBookDetail(clickedBook);
                return true;
            }
            return false;
        });

        Toast.makeText(this, "找到 " + results.size() + " 个相关书籍", Toast.LENGTH_SHORT).show();
    }

    // 添加拼音搜索示例数据（可选）
    private void addPinyinTestData() {
        Book[] testBooks = {
                new Book("高等数学", 25.0, 24.826929, 102.850335, "楠苑食堂", "seller_001"),
                new Book("高级数学", 28.0, 24.827000, 102.851000, "图书馆", "seller_009"),
                new Book("高中数学", 20.0, 24.828000, 102.852000, "教学区", "seller_010"),
                new Book("Java编程思想", 40.0, 24.824424, 102.8488, "图书馆", "seller_002"),
                new Book("Java入门", 30.0, 24.825000, 102.849000, "信息学院", "seller_011"),
                new Book("英语四级词汇", 15.0, 24.823841, 102.854084, "楸苑", "seller_003"),
                new Book("大学英语", 20.0, 24.828009, 102.845139, "外国语学院", "seller_004"),
                new Book("英语听力", 18.0, 24.829000, 102.846000, "外语楼", "seller_012"),
                new Book("C程序设计", 35.0, 24.828387, 102.859714, "体育场", "seller_005"),
                new Book("数据结构", 45.0, 24.826575, 102.854406, "软件学院", "seller_006"),
                new Book("线性代数", 18.0, 24.824288, 102.851175, "数统学院", "seller_007"),
                new Book("概率论", 22.0, 24.824878, 102.85089, "信息学院", "seller_008"),
                new Book("离散数学", 26.0, 24.827500, 102.853000, "计算机学院", "seller_013"),
                new Book("微积分", 24.0, 24.829500, 102.855000, "理学院", "seller_014")
        };
    }


    private void showNoSearchDataMessage() {
        Toast.makeText(this, "暂无搜索数据", Toast.LENGTH_SHORT).show();
    }

    private String preprocessQuery(String query) {
        return query != null ? query.trim().toLowerCase() : "";
    }

    /**
     * 创建完整的拼音映射表
     */
    private Map<Character, String> createPinyinMap() {
        Map<Character, String> map = new HashMap<>();

        // 常用中文字符拼音映射
        String[][] pinyinData = {
                // 数学相关
                {"高", "g"}, {"等", "d"}, {"数", "s"}, {"学", "x"}, {"线", "x"}, {"性", "x"},
                {"代", "d"}, {"概", "g"}, {"率", "l"}, {"论", "l"}, {"微", "w"}, {"积", "j"},
                {"分", "f"}, {"几", "j"}, {"何", "h"}, {"逻", "l"}, {"辑", "j"}, {"统", "t"},
                {"计", "j"}, {"离", "l"}, {"散", "s"}, {"数", "s"}, {"分", "f"}, {"析", "x"},

                // 编程相关
                {"编", "b"}, {"程", "c"}, {"程", "c"}, {"序", "x"}, {"设", "s"}, {"计", "j"},
                {"算", "s"}, {"法", "f"}, {"数", "s"}, {"据", "j"}, {"结", "j"}, {"构", "g"},
                {"操", "c"}, {"作", "z"}, {"系", "x"}, {"统", "t"}, {"软", "r"}, {"件", "j"},
                {"硬", "y"}, {"件", "j"}, {"网", "w"}, {"络", "l"}, {"安", "a"}, {"全", "q"},

                // 语言相关
                {"英", "y"}, {"语", "y"}, {"大", "d"}, {"学", "x"}, {"四", "s"}, {"级", "j"},
                {"六", "l"}, {"词", "c"}, {"汇", "h"}, {"阅", "y"}, {"读", "d"}, {"写", "x"},
                {"作", "z"}, {"听", "t"}, {"力", "l"}, {"口", "k"}, {"语", "y"}, {"文", "w"},
                {"法", "f"}, {"翻", "f"}, {"译", "y"}, {"会", "h"}, {"话", "h"}, {"日", "r"},
                {"语", "y"}, {"法", "f"}, {"语", "y"}, {"德", "d"}, {"语", "y"}, {"西", "x"},

                // 其他常用字
                {"原", "y"}, {"理", "l"}, {"应", "y"}, {"用", "y"}, {"实", "s"}, {"验", "y"},
                {"教", "j"}, {"材", "c"}, {"指", "z"}, {"导", "d"}, {"手", "s"}, {"册", "c"},
                {"基", "j"}, {"础", "c"}, {"进", "j"}, {"阶", "j"}, {"高", "g"}, {"级", "j"},
                {"经", "j"}, {"典", "d"}, {"现", "x"}, {"代", "d"}, {"传", "c"}, {"统", "t"}
        };

        for (String[] item : pinyinData) {
            String chinese = item[0];
            String pinyin = item[1];
            char character = chinese.charAt(0);
            map.put(character, pinyin);
        }

        return map;
    }

    /**
     * 增强版搜索算法
     */
    private List<Book> enhancedSearch(String query) {
        List<Book> results = new ArrayList<>();

        if (query == null || query.trim().isEmpty()) {
            Log.d("Search", "查询为空，返回所有书籍");
            return new ArrayList<>(allBooks); // 返回副本
        }

        String lowerQuery = query.toLowerCase().trim();
        Log.d("Search", "开始搜索，关键词: '" + lowerQuery + "', 总书籍数: " + allBooks.size());

        int matchCount = 0;
        for (Book book : allBooks) {
            if (book == null) continue;

            boolean matched = checkBookMatchWithDebug(book, lowerQuery);
            if (matched) {
                results.add(book);
                matchCount++;
                Log.d("SearchMatch", "匹配成功[" + matchCount + "]: " + book.getTitle());
            }
        }

        Log.d("Search", "搜索完成，共匹配 " + matchCount + " 本书籍");
        return results;
    }

    /**
     * 带调试信息的书籍匹配检查
     */
    private boolean checkBookMatchWithDebug(Book book, String query) {
        if (book.getTitle() == null) {
            Log.d("MatchDebug", "书籍标题为空，跳过");
            return false;
        }

        String title = book.getTitle();
        String lowerTitle = title.toLowerCase();

        Log.d("MatchDebug", "检查匹配: 《" + title + "》 vs '" + query + "'");

        // 1. 直接包含匹配
        if (lowerTitle.contains(query)) {
            Log.d("Match", "直接包含匹配: " + title);
            return true;
        }

        // 2. 开头匹配
        if (lowerTitle.startsWith(query)) {
            Log.d("Match", "开头匹配: " + title);
            return true;
        }

        // 3. 拼音匹配（针对中文）
        if (chinesePinyinMatch(title, query)) {
            Log.d("Match", "拼音匹配: " + title);
            return true;
        }

        // 4. 首字母匹配
        if (initialMatch(title, query)) {
            Log.d("Match", "首字母匹配: " + title);
            return true;
        }

        Log.d("MatchDebug", "未匹配: " + title);
        return false;
    }

    /**
     * 中文拼音匹配
     */
    private boolean chinesePinyinMatch(String chineseText, String query) {
        try {
            // 简单的拼音首字母匹配
            String pinyinInitials = getPinyinInitials(chineseText);
            String queryInitials = getPinyinInitials(query);

            Log.d("PinyinMatch", "文本《" + chineseText + "》拼音: " + pinyinInitials);
            Log.d("PinyinMatch", "查询'" + query + "'拼音: " + queryInitials);

            if (pinyinInitials.contains(queryInitials)) {
                return true;
            }

            // 全拼音匹配（简单实现）
            String fullPinyin = getSimplePinyin(chineseText);
            return fullPinyin.contains(query.toLowerCase());

        } catch (Exception e) {
            Log.e("PinyinMatch", "拼音匹配异常", e);
            return false;
        }
    }

    /**
     * 简单拼音转换
     */
    private String getSimplePinyin(String chinese) {
        // 这里是简化实现，实际项目应使用专业拼音库
        Map<Character, String> pinyinMap = createPinyinMap();
        StringBuilder pinyin = new StringBuilder();

        for (char c : chinese.toCharArray()) {
            if (pinyinMap.containsKey(c)) {
                pinyin.append(pinyinMap.get(c));
            } else if (Character.isLetter(c)) {
                pinyin.append(Character.toLowerCase(c));
            }
        }

        return pinyin.toString();
    }

    // 添加成员变量

    private SearchResultsAdapter searchAdapter;
    private List<Book> searchResults = new ArrayList<>();

    /**
     * 初始化搜索结果列表
     */
    private void initSearchResultsList() {
        searchResultsList = findViewById(R.id.search_results_list);

        // 设置布局管理器
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        searchResultsList.setLayoutManager(layoutManager);

        // 关键修复：传入用户位置
        LatLng userLocation = getCurrentUserLocation();

        // 设置适配器 - 使用 SearchResultsAdapter
        searchAdapter = new SearchResultsAdapter(searchResults, new SearchResultsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Book book) {
                // 跳转到详情页
                navigateToBookDetail(book);
            }
        });

        searchResultsList.setAdapter(searchAdapter);
        Log.d("SearchList", "搜索结果列表初始化完成，用户位置: " + userLocation);
    }
    /**
     * 获取用户当前位置
     */
    private LatLng getCurrentUserLocation() {
        // 方法1：使用地图中心作为参考点
        if (aMap != null) {
            LatLng center = aMap.getCameraPosition().target;
            Log.d("Location", "使用地图中心作为用户位置: " + center);
            return center;
        }

        // 方法2：使用固定位置（云南大学中心）
        LatLng defaultLocation = new LatLng(24.8333, 102.8519);
        Log.d("Location", "使用默认位置: " + defaultLocation);
        return defaultLocation;
    }

    /**
     * 显示搜索进行中状态
     */
    private void showSearchingState(String query) {
        if (tvSearchHint != null) {
            tvSearchHint.setText("搜索中: " + query + "...");
        }

        // 可以添加加载动画
        Toast.makeText(this, "正在搜索: " + query, Toast.LENGTH_SHORT).show();
    }

    /**
     * 显示搜索完成状态
     */
    private void showSearchCompleteState(int resultCount, String query) {
        if (tvSearchHint != null) {
            if (resultCount > 0) {
                tvSearchHint.setText("找到 " + resultCount + " 个结果");
            } else {
                tvSearchHint.setText("未找到相关书籍");
            }
        }

        // 振动反馈（可选）
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(50); // 振动50毫秒
        }
    }

    /**
     * 显示搜索结果列表
     */
    private void showSearchResultsList(List<Book> results) {
        if (searchResultsList == null) {
            Log.e("SearchList", "搜索结果列表未初始化");
            return;
        }

        // 更新数据源
        searchResults.clear();
        searchResults.addAll(results);

        // 检查适配器是否存在，不存在则创建
        if (searchAdapter == null) {
            searchAdapter = new SearchResultsAdapter(searchResults, new SearchResultsAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(Book book) {
                    // 点击列表项时跳转到详情页
                    navigateToBookDetail(book);
                }
            });
            searchResultsList.setAdapter(searchAdapter);
        }

        // 更新适配器
        if (searchAdapter != null) {
            searchAdapter.updateData(searchResults);
            searchAdapter.notifyDataSetChanged();
        }

        // 显示或隐藏列表
        if (results.isEmpty()) {
            searchResultsList.setVisibility(View.GONE);
            tvSearchHint.setText("未找到相关书籍");
        } else {
            searchResultsList.setVisibility(View.VISIBLE);
            tvSearchHint.setText("找到 " + results.size() + " 个结果");

            // 滚动到顶部
            searchResultsList.smoothScrollToPosition(0);

            Log.d("SearchList", "显示搜索结果列表: " + results.size() + " 项");
        }
    }

    /**
     * 定位到特定书籍
     */
    private void focusOnBook(Book book) {
        if (book == null || aMap == null) return;

        LatLng location = new LatLng(book.getLatitude(), book.getLongitude());
        aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 16f));

        // 显示信息窗口
        for (Marker marker : aMap.getMapScreenMarkers()) {
            Object obj = marker.getObject();
            if (obj instanceof Book && ((Book) obj).getTitle().equals(book.getTitle())) {
                marker.showInfoWindow();
                break;
            }
        }

        Toast.makeText(this, "已定位到: " + book.getTitle(), Toast.LENGTH_SHORT).show();
    }

    /**
     * 显示搜索结果（修复缺失的方法）
     */
    /**
     * 显示搜索结果（修复地图标记问题）
     */
    private void showSearchResults(List<Book> results, String query) {
        if (aMap == null) {
            Log.e("Search", "地图未初始化");
            return;
        }

        // 关键修复1：清除旧标记
        aMap.clear();

        Log.d("SearchResults", "开始显示搜索结果: " + results.size() + " 本书");

        if (results.isEmpty()) {
            showNoResultsMessage(query);
            updateStatusText(0, allBooks.size(), query);
        } else {
            // 关键修复2：在地图上添加搜索结果标记
            addSearchResultMarkers(results);

            // 关键修复3：自动调整地图视野
            adjustMapToShowSearchResults(results);

            // 关键修复4：显示搜索结果列表
            showSearchResultsList(results);

            updateStatusText(results.size(), allBooks.size(), query);

            Log.d("SearchResults", "成功显示 " + results.size() + " 个搜索结果");
        }
    }

    /**
     * 调整地图视野以显示所有搜索结果
     */
    private void adjustMapToShowSearchResults(List<Book> results) {
        if (results == null || results.isEmpty()) {
            Log.w("MapAdjust", "无结果可调整视野");
            return;
        }

        try {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            int validPoints = 0;

            for (Book book : results) {
                if (book != null) {
                    LatLng location = new LatLng(book.getLatitude(), book.getLongitude());
                    builder.include(location);
                    validPoints++;
                    Log.d("MapAdjust", "添加位置: " + book.getTitle() + " - " + location);
                }
            }

            if (validPoints > 0) {
                // 根据结果数量调整缩放级别
                int padding = 100; // 边距
                int zoomLevel = (int)calculateZoomLevel(validPoints);

                if (validPoints == 1) {
                    // 单个结果，直接定位
                    Book book = results.get(0);
                    LatLng location = new LatLng(book.getLatitude(), book.getLongitude());
                    aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, zoomLevel));
                    Log.d("MapAdjust", "单个结果定位: 缩放级别 " + zoomLevel);
                } else {
                    // 多个结果，显示边界
                    LatLngBounds bounds = builder.build();
                    aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
                    Log.d("MapAdjust", "多个结果调整视野: " + validPoints + " 个点");
                }
            }

        } catch (Exception e) {
            Log.e("MapAdjust", "调整地图视野失败", e);
            // 备用方案：使用默认位置
            LatLng defaultCenter = new LatLng(24.8333, 102.8519);
            aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(defaultCenter, 15f));
        }
    }

    /**
     * 显示无结果提示
     */
    private void showNoResultsMessage(String query) {
        // 显示Toast提示
        Toast.makeText(this, "未找到包含 \"" + query + "\" 的书籍", Toast.LENGTH_LONG).show();

        // 在地图中心显示提示标记
        LatLng center = aMap.getCameraPosition().target;
        Marker noResultMarker = aMap.addMarker(new MarkerOptions()
                .position(center)
                .title("未找到结果")
                .snippet("搜索关键词: " + query)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));

        noResultMarker.showInfoWindow();

        // 隐藏结果列表
        if (searchResultsList != null) {
            searchResultsList.setVisibility(View.GONE);
        }

        Log.d("Search", "显示无结果提示: " + query);
    }

    /**
     * 按距离排序搜索结果显示
     */
    private List<Book> sortBooksByDistance(List<Book> books, LatLng userLocation) {
        if (books == null || books.isEmpty() || userLocation == null) {
            return books;
        }

        // 创建带距离的临时对象
        List<BookWithDistance> booksWithDistance = new ArrayList<>();

        for (Book book : books) {
            double distance = calculateDistance(
                    userLocation.latitude, userLocation.longitude,
                    book.getLatitude(), book.getLongitude()
            );
            booksWithDistance.add(new BookWithDistance(book, distance));
        }

        // 按距离升序排序（近→远）
        Collections.sort(booksWithDistance, new Comparator<BookWithDistance>() {
            @Override
            public int compare(BookWithDistance b1, BookWithDistance b2) {
                return Double.compare(b1.distance, b2.distance);
            }
        });

        // 提取排序后的书籍
        List<Book> sortedBooks = new ArrayList<>();
        for (BookWithDistance bwd : booksWithDistance) {
            sortedBooks.add(bwd.book);
        }

        return sortedBooks;
    }

    /**
     * 计算两点间距离（米）
     */
    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371000; // 地球半径（米）
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return earthRadius * c;
    }

    /**
     * 带距离的书籍包装类
     */
    private class BookWithDistance {
        Book book;
        double distance; // 距离（米）

        BookWithDistance(Book book, double distance) {
            this.book = book;
            this.distance = distance;
        }
    }

    /**
     * 显示所有书籍（搜索为空时）
     */
    private void showAllBooks() {
        if (aMap == null || allBooks == null) return;

        aMap.clear();

        // 使用普通颜色显示所有书籍
        for (Book book : allBooks) {
            if (book != null) {
                LatLng location = new LatLng(book.getLatitude(), book.getLongitude());
                Marker marker = aMap.addMarker(new MarkerOptions()
                        .position(location)
                        .title(book.getTitle())
                        .snippet("价格: ￥" + book.getPrice())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))); // 普通红色
                marker.setObject(book);
            }
        }

        // 隐藏结果列表
        if (searchResultsList != null) {
            searchResultsList.setVisibility(View.GONE);
        }

        updateStatusText(allBooks.size(), allBooks.size(), null);
        Log.d("Search", "显示所有书籍: " + allBooks.size() + " 本");
    }

    /**
     * 根据结果数量计算合适的缩放级别
     */
    private float calculateZoomLevel(int resultCount) {
        if (resultCount <= 1) return 16.0f;     // 单个结果，放大显示
        if (resultCount <= 3) return 15.0f;      // 少量结果，中等缩放
        if (resultCount <= 6) return 14.0f;     // 中等数量，稍大范围
        return 13.0f;                           // 大量结果，更大范围
    }

    /**
     * 添加搜索结果标记到地图（突出显示）
     */
    private void addSearchResultMarkers(List<Book> results) {
        if (results == null || results.isEmpty()) {
            Log.w("Markers", "无结果可显示");
            return;
        }

        int markerCount = 0;
        for (Book book : results) {
            try {
                if (book == null) continue;

                LatLng location = new LatLng(book.getLatitude(), book.getLongitude());

                // 关键修复：使用不同颜色突出显示搜索结果
                MarkerOptions options = new MarkerOptions()
                        .position(location)
                        .title(book.getTitle())
                        .snippet("价格: ￥" + book.getPrice() + " | 搜索匹配")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)) // 蓝色突出
                        .zIndex(10.0f); // 提高层级，确保在最前面

                Marker marker = aMap.addMarker(options);
                marker.setObject(book);

                // 可选：自动显示第一个结果的信息窗口
                if (markerCount == 0) {
                    marker.showInfoWindow();
                }

                markerCount++;
                Log.d("Marker", "添加搜索结果标记: " + book.getTitle());

            } catch (Exception e) {
                Log.e("Marker", "添加标记失败: " + (book != null ? book.getTitle() : "未知"), e);
            }
        }

        Log.d("Markers", "共添加 " + markerCount + " 个搜索结果标记");
    }

    private void addBookMarker(Book book) {
        if (book == null || aMap == null) return;

        LatLng location = new LatLng(book.getLatitude(), book.getLongitude());
        Marker marker = aMap.addMarker(new MarkerOptions()
                .position(location)
                .title(book.getTitle())
                .snippet("价格: ￥" + book.getPrice())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        marker.setObject(book);
    }

    /**
     * 处理初始匹配逻辑（修复缺失的方法）
     */
    private boolean initialMatch(String text, String query) {
        if (text == null || query == null) return false;

        // 基础匹配：检查文本是否以查询词开头（忽略大小写）
        boolean matches = text.toLowerCase().startsWith(query.toLowerCase());
        Log.d("Match", "初始匹配: " + text + " <- " + query + " = " + matches);
        return matches;
    }

    /**
     * 预处理中文查询词
     */
    private String preprocessChineseQuery(String query) {
        if (query == null) return "";

        // 1. 去除首尾空格
        String processed = query.trim();

        // 2. 统一转换为小写（针对英文部分）
        processed = processed.toLowerCase();

        // 3. 移除多余空格（中文不需要空格分词）
        processed = processed.replaceAll("\\s+", "");

        Log.d("QueryProcess", "查询预处理: '" + query + "' -> '" + processed + "'");
        return processed;
    }

    /**
     * 增强版中文搜索
     */
    private List<Book> enhancedChineseSearch(String query) {
        List<Book> results = new ArrayList<>();

        if (allBooks == null || allBooks.isEmpty()) {
            Log.w("ChineseSearch", "书籍数据为空");
            return results;
        }

        Log.d("ChineseSearch", "开始搜索，总书籍数: " + allBooks.size());
        int matchCount = 0;

        for (Book book : allBooks) {
            if (book == null || book.getTitle() == null) continue;

            boolean matched = checkChineseBookMatch(book, query);
            if (matched) {
                results.add(book);
                matchCount++;
                Log.d("ChineseMatch", "匹配成功[" + matchCount + "]: " + book.getTitle());
            }
        }

        Log.d("ChineseSearch", "搜索完成，共匹配 " + matchCount + " 本书籍");
        return results;
    }

    /**
     * 中文书籍专用匹配方法
     */
    private boolean checkChineseBookMatch(Book book, String query) {
        String title = book.getTitle();
        if (title == null) return false;

        Log.d("ChineseBookMatch", "匹配检查: 《" + title + "》 vs '" + query + "'");

        // 1. 直接包含匹配（最高优先级）
        if (title.toLowerCase().contains(query)) {
            Log.d("ChineseMatch", "直接包含匹配: " + title);
            return true;
        }

        // 2. 中文精确匹配（考虑全角/半角）
        if (chineseContains(title, query)) {
            Log.d("ChineseMatch", "中文精确匹配: " + title);
            return true;
        }

        // 3. 拼音匹配
        if (advancedPinyinMatch(title, query)) {
            Log.d("ChineseMatch", "拼音匹配: " + title);
            return true;
        }

        // 4. 首字母匹配
        if (chineseInitialMatch(title, query)) {
            Log.d("ChineseMatch", "首字母匹配: " + title);
            return true;
        }

        // 5. 模糊匹配（容错匹配）
        if (fuzzyChineseMatch(title, query)) {
            Log.d("ChineseMatch", "模糊匹配: " + title);
            return true;
        }

        return false;
    }

    /**
     * 中文首字母匹配
     */
    private boolean chineseInitialMatch(String text, String query) {
        if (query.length() <= 1) return false; // 单字符搜索使用其他策略

        String textInitials = getChineseInitials(text);
        String queryInitials = getChineseInitials(query);

        Log.d("InitialMatch", "文本首字母: " + textInitials + " vs 查询首字母: " + queryInitials);

        return textInitials.contains(queryInitials);
    }


    /**
     * 中文包含匹配（处理全角/半角）
     */
    private boolean chineseContains(String text, String query) {
        // 全角转半角
        String fullWidthText = toFullWidth(text);
        String fullWidthQuery = toFullWidth(query);

        // 半角转全角
        String halfWidthText = toHalfWidth(text);
        String halfWidthQuery = toHalfWidth(query);

        return text.contains(query) ||
                fullWidthText.contains(fullWidthQuery) ||
                halfWidthText.contains(halfWidthQuery) ||
                text.toLowerCase().contains(query.toLowerCase());
    }

    /**
     * 全角转半角
     */
    private String toHalfWidth(String str) {
        if (str == null) return "";
        char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '　') {
                chars[i] = ' ';
            } else if (chars[i] >= '！' && chars[i] <= '～') {
                chars[i] = (char) (chars[i] - 65248);
            }
        }
        return new String(chars);
    }

    /**
     * 半角转全角
     */
    private String toFullWidth(String str) {
        if (str == null) return "";
        char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == ' ') {
                chars[i] = '　';
            } else if (chars[i] >= '!' && chars[i] <= '~') {
                chars[i] = (char) (chars[i] + 65248);
            }
        }
        return new String(chars);
    }

    /**
     * 增强拼音匹配
     */
    private boolean advancedPinyinMatch(String text, String query) {
        try {
            // 简单的拼音首字母匹配（实际项目可以使用第三方拼音库）
            String textPinyin = getSimplePinyin(text);
            String queryPinyin = getSimplePinyin(query);

            Log.d("PinyinDebug", "文本《" + text + "》拼音: " + textPinyin);
            Log.d("PinyinDebug", "查询'" + query + "'拼音: " + queryPinyin);

            return textPinyin.contains(queryPinyin);
        } catch (Exception e) {
            Log.e("PinyinMatch", "拼音匹配异常", e);
            return false;
        }
    }


    /**
     * 增强版拼音映射表
     */
    private Map<Character, String> createEnhancedPinyinMap() {
        Map<Character, String> map = new HashMap<>();

        // 常用中文字符拼音映射
        String[][] pinyinData = {
                {"高等数学", "gdsx"}, {"数学", "sx"}, {"高等", "gd"},
                {"Java编程", "javabc"}, {"编程", "bc"}, {"Java", "java"},
                {"英语", "yy"}, {"四级", "sj"}, {"词汇", "ch"}, {"英语四级", "yysj"},
                {"大学英语", "dxyy"}, {"大学", "dx"},
                {"C程序设计", "ccxsj"}, {"程序", "cx"}, {"设计", "sj"},
                {"数据结构", "sjjg"}, {"数据", "sj"}, {"结构", "jg"},
                {"线性代数", "xxds"}, {"线性", "xx"}, {"代数", "ds"},
                {"概率论", "gll"}, {"概率", "gl"}, {"论", "l"}
        };

        // 为每个字符创建映射
        for (String[] item : pinyinData) {
            String chinese = item[0];
            String pinyin = item[1];

            for (char c : chinese.toCharArray()) {
                // 只为每个字符存储首字母
                if (!map.containsKey(c) && pinyin.length() > 0) {
                    map.put(c, String.valueOf(pinyin.charAt(0)));
                }
            }
        }

        return map;
    }



    /**
     * 增强版中文搜索匹配
     */
    private boolean checkBookMatch(Book book, String query) {
        if (book.getTitle() == null) return false;

        String bookTitle = book.getTitle();
        String lowerQuery = query.toLowerCase().trim();

        Log.d("ChineseSearch", "中文搜索: '" + bookTitle + "' vs '" + query + "'");

        // 1. 首先检查是否包含中文字符
        boolean hasChinese = containsChinese(query);
        Log.d("ChineseSearch", "查询包含中文: " + hasChinese);

        // 2. 基础匹配（大小写不敏感）
        if (bookTitle.toLowerCase().contains(lowerQuery)) {
            Log.d("ChineseMatch", "基础匹配成功: " + bookTitle);
            return true;
        }

        // 3. 中文特殊匹配策略
        if (hasChinese) {
            // 中文专用匹配逻辑
            if (chineseMatch(bookTitle, query)) {
                Log.d("ChineseMatch", "中文匹配成功: " + bookTitle);
                return true;
            }
        }

        // 4. 拼音匹配（支持中文转拼音搜索）
        if (pinyinMatch(bookTitle, query)) {
            Log.d("ChineseMatch", "拼音匹配成功: " + bookTitle);
            return true;
        }

        // 5. 首字母匹配
        if (initialMatch(bookTitle, query)) {
            Log.d("ChineseMatch", "首字母匹配成功: " + bookTitle);
            return true;
        }

        return false;
    }

    /**
     * 检查字符串是否包含中文字符
     */
    private boolean containsChinese(String str) {
        if (str == null) return false;
        return str.matches(".*[\\u4e00-\\u9fa5]+.*");
    }

    /**
     * 中文专用匹配逻辑
     */
    private boolean chineseMatch(String text, String query) {
        // 1. 精确包含匹配
        if (text.contains(query)) {
            return true;
        }

        // 2. 分词匹配（简单版本）
        String[] textWords = text.split(""); // 简单按字符分割
        String[] queryWords = query.split("");

        // 检查查询词是否按顺序出现在文本中
        int queryIndex = 0;
        for (String textWord : textWords) {
            if (queryIndex < queryWords.length &&
                    textWord.equals(queryWords[queryIndex])) {
                queryIndex++;
            }
        }

        if (queryIndex == queryWords.length) {
            return true; // 所有查询字符都按顺序找到了
        }

        return false;
    }

    /**
     * 拼音匹配（支持中文转拼音搜索）
     */
    private boolean pinyinMatch(String text, String query) {
        try {
            // 简单拼音匹配：将中文转换为拼音首字母
            String textPinyin = getPinyinInitials(text);
            String queryPinyin = getPinyinInitials(query);

            Log.d("PinyinMatch", "文本拼音: " + textPinyin + " vs 查询拼音: " + queryPinyin);

            return textPinyin.contains(queryPinyin);
        } catch (Exception e) {
            Log.e("PinyinMatch", "拼音匹配失败", e);
            return false;
        }
    }

    /**
     * 获取中文字符串的拼音首字母（简化版）
     */
    private String getPinyinInitials(String chinese) {
        if (chinese == null) return "";

        // 简单的中文拼音映射表（常用字）
        Map<Character, String> pinyinMap = createPinyinMap();
        StringBuilder initials = new StringBuilder();

        for (char c : chinese.toCharArray()) {
            if (pinyinMap.containsKey(c)) {
                initials.append(pinyinMap.get(c));
            } else if (Character.isLetter(c)) {
                initials.append(Character.toLowerCase(c));
            }
        }

        return initials.toString();
    }

    /**
     * 获取中文拼音首字母
     */
    private String getChineseInitials(String chinese) {
        if (chinese == null) return "";

        Map<Character, Character> initialMap = createInitialMap();
        StringBuilder initials = new StringBuilder();

        for (char c : chinese.toCharArray()) {
            if (initialMap.containsKey(c)) {
                initials.append(initialMap.get(c));
            } else if (Character.isLetter(c)) {
                initials.append(c);
            }
        }

        return initials.toString();
    }

    /**
     * 创建拼音首字母映射表
     */
    private Map<Character, Character> createInitialMap() {
        Map<Character, Character> map = new HashMap<>();
        // 简单示例，实际项目需要完整映射
        map.put('书', 'S');
        map.put('籍', 'J');
        map.put('编', 'B');
        map.put('程', 'C');
        map.put('数', 'S');
        map.put('学', 'X');
        map.put('英', 'Y');
        map.put('语', 'Y');
        map.put('大', 'D');
        map.put('学', 'X');
        // 继续添加...
        return map;
    }

    /**
     * 全角转半角
     */
    private String fullToHalf(String text) {
        if (text == null) return "";

        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == 12288) { // 全角空格
                chars[i] = ' ';
            } else if (chars[i] > 65280 && chars[i] < 65375) {
                chars[i] = (char) (chars[i] - 65248);
            }
        }
        return new String(chars);
    }

    /**
     * 移除标点符号
     */
    private String removePunctuation(String text) {
        if (text == null) return "";
        return text.replaceAll("[\\pP\\p{Punct}]", "");
    }

    /**
     * 优化版搜索逻辑，专门处理中文
     */
    private void filterBooks(String query) {
        List<Book> results = new ArrayList<>();

        if (query == null || query.trim().isEmpty()) {
            showAllBooks();
            return;
        }

        if (allBooks == null || allBooks.isEmpty()) {
            showNoResultsMessage(query);
            return;
        }

        Log.d("ChineseSearch", "=== 开始中文搜索 ===");
        Log.d("ChineseSearch", "搜索关键词: '" + query + "', 总书籍数: " + allBooks.size());

        int matchCount = 0;
        for (Book book : allBooks) {
            if (book == null) continue;

            boolean matched = false;

            // 针对中文优化匹配逻辑
            if (containsChineseCharacters(query)) {
                // 中文搜索：使用增强的中文匹配算法
                matched = checkBookMatch(book, query);
            } else {
                // 英文/拼音搜索：使用原有逻辑
                matched = checkBookMatchBasic(book, query);
            }

            if (matched) {
                results.add(book);
                matchCount++;
                Log.d("ChineseSearch", "匹配成功[" + matchCount + "]: " + book.getTitle());
            }
        }

        Log.d("ChineseSearch", "中文搜索完成，找到 " + results.size() + " 个结果");
        showSearchResults(results, query);
    }

    /**
     * 检查字符串是否包含中文字符
     */
    private boolean containsChineseCharacters(String text) {
        if (text == null) return false;

        for (char c : text.toCharArray()) {
            if (isChineseCharacter(c)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查字符是否为中文字符
     */
    private boolean isChineseCharacter(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        return ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS;
    }

    /**
     * 基础匹配逻辑（英文、数字等）
     */
    private boolean checkBookMatchBasic(Book book, String query) {
        if (book.getTitle() == null) return false;

        String lowerQuery = query.toLowerCase();
        String title = book.getTitle().toLowerCase();

        // 基础匹配：包含匹配、开头匹配等
        return title.contains(lowerQuery) ||
                title.startsWith(lowerQuery) ||
                fuzzyMatchBasic(title, lowerQuery);
    }

    /**
     * 基础模糊匹配
     */
    private boolean fuzzyMatchBasic(String text, String query) {
        if (text == null || query == null) return false;

        // 移除空格和特殊字符后匹配
        String cleanText = text.replaceAll("[^a-z0-9\\u4e00-\\u9fa5]", ""); // 保留中文、英文、数字
        String cleanQuery = query.replaceAll("[^a-z0-9\\u4e00-\\u9fa5]", "");

        return cleanText.contains(cleanQuery);
    }

    /**
     * 模糊中文匹配（容错处理）
     */
    private boolean fuzzyChineseMatch(String text, String query) {
        if (text == null || query == null) return false;

        // 1. 移除所有非中文字符后匹配
        String cleanText = text.replaceAll("[^\\u4e00-\\u9fa5]", "");
        String cleanQuery = query.replaceAll("[^\\u4e00-\\u9fa5]", "");

        if (cleanText.contains(cleanQuery)) {
            return true;
        }

        // 2. 相似度匹配（简单版本）
        if (text.length() >= query.length()) {
            int matchCount = 0;
            for (char qChar : query.toCharArray()) {
                if (text.indexOf(qChar) >= 0) {
                    matchCount++;
                }
            }

            // 如果超过70%的字符匹配，则认为匹配成功
            double matchRatio = (double) matchCount / query.length();
            if (matchRatio >= 0.7) {
                Log.d("FuzzyMatch", "模糊匹配成功，匹配度: " + matchRatio);
                return true;
            }
        }

        return false;
    }

    /**
     * 更新状态文本
     */
    private void updateStatusText(int displayed, int total, String query) {
        TextView tvStatus = findViewById(R.id.tv_status);

        if (query != null && !query.isEmpty()) {
            tvStatus.setText(String.format("搜索 \"%s\": 显示 %d/%d 个结果", query, displayed, total));
        } else {
            tvStatus.setText(String.format("显示 %d/%d 个标记点", displayed, total));
        }
    }

    /**
     * 清除搜索
     */
    private void clearSearch() {
        etSearch.setText("");
        showAllBooks();
        updateClearButtonVisibility(false);

        // 隐藏键盘
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
    }

    /**
     * 更新清除按钮显示状态
     */
    private void updateClearButtonVisibility(boolean visible) {
        btnClearSearch.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    /**
     * 调整地图视野以显示书籍
     */
    private void adjustMapToShowBooks(List<Book> books) {
        if (books.isEmpty()) return;

        try {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();

            for (Book book : books) {
                LatLng location = new LatLng(book.getLatitude(), book.getLongitude());
                builder.include(location);
            }

            // 添加一些边距
            int padding = 100;
            aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), padding));

        } catch (Exception e) {
            Log.e("Search", "调整地图视野失败", e);
        }
    }

    /**
     * 安全地在地图上显示书籍（聚合功能不可用时使用）
     */
    private void showBooksOnMap(List<Book> books) {
        if (aMap == null) {
            Log.e("Map", "AMap为空，无法显示书籍");
            return;
        }

        try {
            // 清除旧标记
            aMap.clear();

            int markerCount = 0;
            for (Book book : books) {
                try {
                    LatLng location = new LatLng(book.getLatitude(), book.getLongitude());

                    Marker marker = aMap.addMarker(new MarkerOptions()
                            .position(location)
                            .title(book.getTitle())
                            .snippet("价格：￥" + book.getPrice())
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                    marker.setObject(book);
                    markerCount++;

                } catch (Exception e) {
                    Log.e("Map", "添加标记失败: " + book.getTitle(), e);
                }
            }

            Log.d("Map", "成功添加了 " + markerCount + "/" + books.size() + " 个标记");

        } catch (Exception e) {
            Log.e("Map", "显示书籍失败", e);
            Toast.makeText(this, "地图显示失败", Toast.LENGTH_SHORT).show();
        }
    }



    /**
     * 显示位置确认对话框，让用户确认选择的位置
     */
    private void showLocationConfirmationDialog(LatLng selectedLocation) {
        // 生成备用地址（立即可用）
        final String[] addressHolder = new String[1];
        addressHolder[0] = generateFallbackAddress(selectedLocation);

        // 创建确认对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("确认位置");

        builder.setMessage("您选择了这个位置作为交易地点\n\n" +
                "纬度: " + String.format("%.6f", selectedLocation.latitude) + "\n" +
                "经度: " + String.format("%.6f", selectedLocation.longitude) + "\n\n" +
                "正在解析具体地址...");

        // 显示对话框前添加临时标记
        final Marker tempMarker = aMap.addMarker(new MarkerOptions()
                .position(selectedLocation)
                .title("选择的位置")
                .snippet("正在解析地址...")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

        // 关键修复：立即设置可用的发布按钮
        builder.setPositiveButton("确认，发布书籍", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 立即发布，不等待任何网络请求
                openPublishActivity(selectedLocation, addressHolder[0]);
                if (tempMarker != null) {
                    tempMarker.remove();
                }
            }
        });

        builder.setNegativeButton("重新选择", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (tempMarker != null) {
                    tempMarker.remove();
                }
                dialog.dismiss();
            }
        });

        builder.setNeutralButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (tempMarker != null) {
                    tempMarker.remove();
                }
                dialog.dismiss();
            }
        });

        final AlertDialog dialog = builder.create();
        dialog.show();

        // 设置对话框关闭监听器
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (tempMarker != null) {
                    tempMarker.remove();
                }
            }
        });

        // 逆地理编码改为后台异步执行，不阻塞用户操作
        // 逆地理编码回调
        performReverseGeocode(selectedLocation, new GeocodeCallback() {
            @Override
            public void onAddressResolved(String address) {
                // 关键修复：更新当前地址为解析后的详细地址
                if (address != null && !address.trim().isEmpty()) {
                    addressHolder[0] = formatAddressForDisplay(address, selectedLocation);
                    Log.d("AddressFlow", "逆地理编码成功，更新地址为: " + addressHolder[0]);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateDialogWithSuccess(dialog, selectedLocation, addressHolder[0], tempMarker);
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                Log.w("AddressFlow", "逆地理编码失败，使用备用地址: " + addressHolder[0]);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateDialogWithError(dialog, selectedLocation, errorMessage, tempMarker);
                    }
                });
            }
        });

        // 设置发布按钮点击事件 - 使用最新的地址
        builder.setPositiveButton("确认，发布书籍", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d("AddressFlow", "用户确认发布，最终地址: " + addressHolder[0]);
                openPublishActivity(selectedLocation, addressHolder[0]);
                if (tempMarker != null) {
                    tempMarker.remove();
                }
            }
        });
    }
    // 逆地理编码回调接口
    interface GeocodeCallback {
        void onAddressResolved(String address);
        void onError(String errorMessage);
    }

    private String generateFallbackAddress(LatLng location) {
        double lat = location.latitude;
        double lng = location.longitude;

        // 更智能的位置描述
        StringBuilder address = new StringBuilder("云南大学");

        // 云南大学呈贡校区范围内的智能位置描述
        if (lat >= 24.8300 && lat <= 24.8400 && lng >= 102.8470 && lng <= 102.8530) {
            if (lat > 24.8360) {
                address.append("楠苑区域");
            } else if (lat > 24.8340) {
                address.append("教学区");
            } else if (lat > 24.8320) {
                address.append("梓苑区域");
            } else {
                address.append("校区");
            }

            // 添加建筑物类型判断（基于经度）
            if (lng > 102.8500) {
                address.append("(东侧)");
            } else if (lng > 102.8480) {
                address.append("(中部)");
            } else {
                address.append("(西侧)");
            }
        } else {
            address.append("附近位置");
        }

        address.append(" (纬度:").append(String.format("%.6f", lat))
                .append(", 经度:").append(String.format("%.6f", lng)).append(")");

        return address.toString();
    }

    // 执行逆地理编码
    private void performReverseGeocode(LatLng latLng, final GeocodeCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    GeocodeSearch geocodeSearch = new GeocodeSearch(MainActivity.this);

                    // 设置超时（5秒）
                    final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
                    final String[] result = new String[1];
                    final int[] resultCode = new int[1];

                    geocodeSearch.setOnGeocodeSearchListener(new GeocodeSearch.OnGeocodeSearchListener() {
                        @Override
                        public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int rCode) {
                            resultCode[0] = rCode;
                            if (rCode == 1000 && regeocodeResult != null && regeocodeResult.getRegeocodeAddress() != null) {
                                result[0] = regeocodeResult.getRegeocodeAddress().getFormatAddress();
                            } else {
                                result[0] = "逆地理编码失败，错误码: " + rCode;
                            }
                            latch.countDown();
                        }

                        @Override
                        public void onGeocodeSearched(GeocodeResult geocodeResult, int rCode) {
                            latch.countDown();
                        }
                    });

                    LatLonPoint point = new LatLonPoint(latLng.latitude, latLng.longitude);
                    RegeocodeQuery query = new RegeocodeQuery(point, 200, GeocodeSearch.AMAP);
                    geocodeSearch.getFromLocationAsyn(query);

                    // 等待结果，最多5秒
                    boolean completed = latch.await(5000, java.util.concurrent.TimeUnit.MILLISECONDS);

                    if (completed) {
                        if (resultCode[0] == 1000) {
                            callback.onAddressResolved(result[0]);
                        } else {
                            callback.onError(result[0]);
                        }
                    } else {
                        callback.onError("网络请求超时");
                    }

                } catch (Exception e) {
                    callback.onError("服务异常: " + e.getMessage());
                }
            }
        }).start();
    }

    /**
     * 成功解析地址时的更新（不断开用户操作）
     */
    private void updateDialogWithSuccess(AlertDialog oldDialog, LatLng location, String address, Marker tempMarker) {
        if (tempMarker != null) {
            tempMarker.setSnippet(address);
            tempMarker.showInfoWindow();
        }

        // 如果对话框还存在，静默更新内容（不打断用户）
        if (oldDialog != null && oldDialog.isShowing()) {
            // 可以静默更新标题和消息，但不重新创建对话框
            try {
                // 通过反射或其他方式更新对话框内容，或者简单显示Toast提示
                Toast.makeText(this, "地址解析成功: " + address, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                // 静默失败，不影响用户操作
            }
        }
    }

    /**
     * 网络错误时的更新（不断开用户操作）
     */
    private void updateDialogWithNetworkError(AlertDialog oldDialog, LatLng location, String errorMessage, Marker tempMarker) {
        if (tempMarker != null) {
            tempMarker.setSnippet("离线模式");
        }

        // 静默提示，不打断用户
        if (oldDialog != null && oldDialog.isShowing()) {
            Toast.makeText(this, "网络连接失败，使用离线位置信息", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 更新对话框显示具体地址（逆地理编码成功时调用）
     */
    private void updateDialogWithAddress(AlertDialog dialog, LatLng location, String address, Marker tempMarker) {
        // 更新标记点信息
        if (tempMarker != null) {
            tempMarker.setSnippet(address);
            tempMarker.showInfoWindow();
        }

        // 创建新对话框显示具体地址（可选，提升用户体验）
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("位置确认 ✅");
        builder.setMessage("📍 交易位置详情：\n\n" +
                "🗺️ 具体地址：\n" + address + "\n\n" +
                "📡 坐标信息：\n" +
                "纬度: " + String.format("%.6f", location.latitude) + "\n" +
                "经度: " + String.format("%.6f", location.longitude) + "\n\n" +
                "💡 地址解析成功！");

        setupDialogButtons(builder, location, address, tempMarker, dialog);

        // 关闭旧对话框，显示新对话框
        dialog.dismiss();
        builder.create().show();
    }

    /**
     * 更新对话框显示备用地址（逆地理编码失败时调用）
     */
    private void updateDialogWithFallback(AlertDialog dialog, LatLng location, String errorMessage, Marker tempMarker) {
        String fallbackAddress = generateFallbackAddress(location);

        // 更新标记点信息
        if (tempMarker != null) {
            tempMarker.setSnippet("离线位置");
        }

        // 创建新对话框显示备用地址
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("位置确认 ⚠️");
        builder.setMessage("📍 交易位置详情：\n\n" +
                "🗺️ 具体地址：\n" + fallbackAddress + "\n\n" +
                "📡 坐标信息：\n" +
                "纬度: " + String.format("%.6f", location.latitude) + "\n" +
                "经度: " + String.format("%.6f", location.longitude) + "\n\n" +
                "💡 提示：网络连接失败，使用离线位置信息。您仍可继续发布书籍。");

        setupDialogButtons(builder, location, fallbackAddress, tempMarker, dialog);

        // 关闭旧对话框，显示新对话框
        dialog.dismiss();
        builder.create().show();
    }

    /**
     * 设置对话框按钮（共用逻辑）
     */
    private void setupDialogButtons(AlertDialog.Builder builder, LatLng location, String address,
                                    Marker tempMarker, AlertDialog oldDialog) {
        builder.setPositiveButton("确认，发布书籍", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                openPublishActivity(location, address);
                if (tempMarker != null) {
                    tempMarker.remove();
                }
            }
        });

        builder.setNegativeButton("重新选择", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (tempMarker != null) {
                    tempMarker.remove();
                }
                dialog.dismiss();
            }
        });

        builder.setNeutralButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (tempMarker != null) {
                    tempMarker.remove();
                }
                dialog.dismiss();
            }
        });

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (tempMarker != null) {
                    tempMarker.remove();
                }
            }
        });
    }

    // 更新对话框显示具体地址

    // 处理解析错误
    // 更新对话框显示具体地址
// 处理解析错误
    private void updateDialogWithError(AlertDialog dialog, LatLng location, String errorMessage, Marker tempMarker) {
        // 更新标记点信息
        if (tempMarker != null) {
            tempMarker.setSnippet("地址解析失败");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("确认位置");
        builder.setMessage("位置确认（地址解析失败）\n\n" +
                "纬度: " + String.format("%.6f", location.latitude) + "\n" +
                "经度: " + String.format("%.6f", location.longitude) + "\n\n" +
                "错误: " + errorMessage + "\n\n" +
                "您仍可继续发布书籍");

        // 补全按钮设置，与updateDialogWithAddress保持一致的交互逻辑
        setupDialogButtons(builder, location, null, tempMarker, dialog);

        dialog.dismiss();
        builder.create().show();
    }

    // 修改发布方法，传递地址信息

    private void openPublishActivity(LatLng location, String address) {
        try {
            Log.d("AddressFlow", "传递地址到发布页面: " + address);

            Intent intent = new Intent(MainActivity.this, PublishActivity.class);
            intent.putExtra("latitude", location.latitude);
            intent.putExtra("longitude", location.longitude);

            // 关键修复：确保传递解析后的具体地址
            if (address != null && !address.trim().isEmpty()) {
                // 如果地址包含"云南大学"，使用解析后的详细地址
                if (address.contains("云南大学")) {
                    intent.putExtra("address", address);
                    Log.d("AddressFlow", "使用解析地址: " + address);
                } else {
                    // 否则使用格式化的地址
                    String formattedAddress = formatAddressForDisplay(address, location);
                    intent.putExtra("address", formattedAddress);
                    Log.d("AddressFlow", "使用格式化地址: " + formattedAddress);
                }
            } else {
                // 备用地址
                String fallbackAddress = generateDetailedAddress(location);
                intent.putExtra("address", fallbackAddress);
                Log.d("AddressFlow", "使用备用地址: " + fallbackAddress);
            }

            startActivityForResult(intent, REQUEST_CODE_PUBLISH);

        } catch (Exception e) {
            Log.e("AddressFlow", "打开发布页面失败", e);
            Toast.makeText(this, "打开发布页面失败", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 格式化地址显示（优化显示效果）
     */
    private String formatAddressForDisplay(String originalAddress, LatLng location) {
        // 移除重复的"云南省昆明市"等前缀，使显示更简洁
        if (originalAddress.contains("云南省昆明市")) {
            // 提取更简洁的地址描述
            String simplified = originalAddress.replace("云南省昆明市", "");

            // 如果是云南大学相关地址，进一步优化显示
            if (simplified.contains("云南大学呈贡校区")) {
                // 提取具体的建筑/学院信息
                if (simplified.contains("材料科学与工程学院")) {
                    return "云南大学呈贡校区材料科学与工程学院";
                } else if (simplified.contains("信息学院")) {
                    return "云南大学呈贡校区信息学院";
                } else if (simplified.contains("楠苑综合楼")) {
                    return "云南大学楠苑综合楼";
                } else if (simplified.contains("知味堂")) {
                    return "云南大学知味堂";
                }
            }

            return simplified;
        }

        return originalAddress;
    }

    /**
     * 生成详细的备用地址
     */
    private String generateDetailedAddress(LatLng location) {
        double lat = location.latitude;
        double lng = location.longitude;

        // 基于坐标生成更精确的位置描述
        if (lat >= 24.826 && lat <= 24.829 && lng >= 102.850 && lng <= 102.853) {
            // 材料科学与工程学院区域
            if (lat > 24.827 && lat < 24.828 && lng > 102.851 && lng < 102.852) {
                return "云南大学呈贡校区材料科学与工程学院";
            }
            // 信息学院区域
            else if (lat > 24.8265 && lat < 24.8275 && lng > 102.8505 && lng < 102.8515) {
                return "云南大学呈贡校区信息学院";
            }
        }

        // 默认详细描述
        return String.format("云南大学呈贡校区 (纬度:%.6f, 经度:%.6f)", lat, lng);
    }

    /**
     * 检查并初始化数据库
     */
    private void checkAndInitializeDatabase() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DatabaseHelper dbHelper = new DatabaseHelper(MainActivity.this);
                    List<Book> books = dbHelper.getAllBooks();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (books.isEmpty()) {
                                Toast.makeText(MainActivity.this, "数据库为空，添加测试数据...", Toast.LENGTH_SHORT).show();
                                addDemoTestData();
                            } else {
                                Log.d("Database", "数据库中已有 " + books.size() + " 本书籍");
                                loadAndDisplayBooks();
                                Toast.makeText(MainActivity.this,
                                        "加载了 " + books.size() + " 本二手书",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    dbHelper.close();
                } catch (Exception e) {
                    Log.e("Database", "检查数据库失败", e);
                }
            }
        }).start();
    }


    /**
     * 匹配书籍与POI
     */

    /**
     * 专门用于添加测试数据的方法（用于演示和测试）
     */
    private void addDemoTestData() {
        new Thread(() -> {
            DatabaseHelper dbHelper = new DatabaseHelper(MainActivity.this);
            try {
                // 清空现有数据（仅用于演示）
                dbHelper.clearAllBooks();

                // 添加多样化的测试数据
                Book[] testBooks = {
                        new Book("高等数学", 25.0, 24.826929, 102.850335, "楠苑食堂", "seller_001"),
                        new Book("Java编程思想", 40.0, 24.824424, 102.8488, "图书馆", "seller_002"),
                        new Book("英语四级词汇", 15.0, 24.823841, 102.854084, "楸苑", "seller_003"),
                        new Book("大学英语", 20.0, 24.828009, 102.845139, "外国语学院", "seller_004"),
                        new Book("C程序设计", 35.0, 24.828387, 102.859714, "体育场", "seller_005"),
                        new Book("数据结构", 45.0, 24.826575, 102.854406, "软件学院", "seller_006"),
                        new Book("线性代数", 18.0, 24.824288, 102.851175, "数统学院", "seller_007"),
                        new Book("概率论", 22.0, 24.824878, 102.85089, "信息学院", "seller_008")
                };

                for (Book book : testBooks) {
                    dbHelper.addBook(book);
                }

                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this,
                            "已添加" + testBooks.length + "本测试书籍",
                            Toast.LENGTH_LONG).show();
                    loadAndDisplayBooks();
                });

            } catch (Exception e) {
                Log.e("TestData", "添加测试数据失败", e);
            } finally {
                dbHelper.close();
            }
        }).start();
    }

    private void clearAllBooks() {
        new Thread(() -> {
            DatabaseHelper dbHelper = new DatabaseHelper(MainActivity.this);
            try {
                dbHelper.clearAllBooks();
                runOnUiThread(() -> {
                    Toast.makeText(this, "已清空所有数据", Toast.LENGTH_SHORT).show();
                    loadAndDisplayBooks(); // 刷新显示
                });
            } catch (Exception e) {
                Log.e("Database", "清空数据失败", e);
            } finally {
                dbHelper.close();
            }
        }).start();
    }


    private void addTestData() {
        new Thread(() -> {
            DatabaseHelper dbHelper = new DatabaseHelper(MainActivity.this);
            try {
                // 彻底清空现有数据
                dbHelper.clearAllBooks(); // 确保使用清空所有数据的方法

                // 使用更分散的坐标，确保5个点都能看到
                Book[] campusBooks = {
                        new Book("高等数学", 25.0, 24.826929, 102.850335, "楠苑食堂", "seller_001"),
                        // 图书馆区域（校区中心）
                        new Book("Java编程思想", 40.0, 24.8375, 102.8485, "云大图书馆", "seller_002"),
                        // 教学楼区域（校区东南）
                        new Book("英语四级词汇", 15.0, 24.8358, 102.8502, "文汇楼", "seller_003"),
                        // 体育场区域（校区西南）
                        new Book("大学英语", 20.0, 2.8362, 102.8465, "体育场", "seller_004"),
                        // 校医院区域（校区西北）
                        new Book("C程序设计", 35.0, 24.8385, 102.8460, "校医院", "seller_005"),
                        // 信息学院区域（校区东北）
                        new Book("数据结构", 45.0, 24.8398, 102.8490, "信息学院", "seller_006"),
                        // 梓苑区域（校区中部）
                        new Book("线性代数", 18.0, 24.8370, 102.8495, "梓苑食堂", "seller_007"),
                        // 学生会堂区域
                        new Book("概率论", 22.0, 24.8360, 102.8480, "学生会堂", "seller_008")
                };

                for (Book book : campusBooks) {
                    dbHelper.addBook(book);
                    Log.d("TestData", "添加书籍: " + book.getTitle() + " 坐标: " + book.getLatitude() + "," + book.getLongitude());
                }

                // 验证添加结果
                List<Book> addedBooks = dbHelper.getAllBooks();
                Log.d("TestData", "数据库实际书籍数量: " + addedBooks.size());

                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this,
                            "已添加" + addedBooks.size() + "/5本测试书籍",
                            Toast.LENGTH_LONG).show();

                    // 重新加载并显示
                    loadAndDisplayBooks();
                });

            } catch (Exception e) {
                Log.e("TestData", "添加测试数据失败", e);
            } finally {
                dbHelper.close();
            }
        }).start();
    }


    // 修复1：使用try-with-resources管理数据库资源
    // ==================== 替换或修改您现有的方法 ====================
    private void loadAndDisplayBooks() {
        new Thread(() -> {
            try (DatabaseHelper dbHelper = new DatabaseHelper(MainActivity.this)) {
                List<Book> books = dbHelper.getAllBooks();

                runOnUiThread(() -> {
                    if (books.isEmpty()) {
                        Log.w("DataLoad", "数据库为空，添加测试数据");
                        addDemoTestData();
                    } else {
                        // 关键修复：确保数据正确同步
                        synchronized (allBooks) {
                            allBooks.clear();
                            allBooks.addAll(books);
                            isDataLoaded = true;
                        }

                        Log.d("DataLoad", "数据加载完成: allBooks大小 = " + allBooks.size());
                        Log.d("DataLoad", "实际书籍列表: " + books.size());

                        // 使用聚合管理器显示
                        if (clusterManager != null) {
                            clusterManager.addBooks(books);
                            Log.d("Cluster", "聚合管理器添加了 " + books.size() + " 本书籍");
                        } else {
                            // 聚合管理器不可用，直接显示
                            showBooksOnMap(books);
                        }

                        Toast.makeText(MainActivity.this,
                                "加载了 " + books.size() + " 本书籍",
                                Toast.LENGTH_SHORT).show();

                        // 记录数据状态
                        logDataStatus();
                    }
                });

            } catch (Exception e) {
                Log.e("DataLoad", "加载书籍失败", e);
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this, "数据加载失败", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    /**
     * 记录数据状态（调试用）
     */
    private void logDataStatus() {
        Log.d("DataStatus", "=== 数据状态报告 ===");
        Log.d("DataStatus", "allBooks是否为空: " + (allBooks == null));
        Log.d("DataStatus", "allBooks大小: " + (allBooks != null ? allBooks.size() : 0));
        Log.d("DataStatus", "数据加载状态: " + isDataLoaded);

        if (allBooks != null && !allBooks.isEmpty()) {
            for (int i = 0; i < Math.min(allBooks.size(), 3); i++) {
                Book book = allBooks.get(i);
                if (book != null) {
                    Log.d("DataStatus", "书籍 " + i + ": " + book.getTitle());
                }
            }
        }
    }
    /**
     * 自动调整地图视野以显示所有标记点
     */
    private void adjustMapToShowAllMarkers(double minLat, double maxLat, double minLng, double maxLng) {
        try {
            // 计算中心点
            double centerLat = (minLat + maxLat) / 2;
            double centerLng = (minLng + maxLng) / 2;
            LatLng center = new LatLng(centerLat, centerLng);

            // 计算合适的缩放级别
            double latDelta = maxLat - minLat;
            double lngDelta = maxLng - minLng;
            double maxDelta = Math.max(latDelta, lngDelta);

            // 根据标记点分布调整缩放级别
            float zoomLevel = 15f; // 默认缩放级别

            if (maxDelta < 0.001) { // 标记点非常集中
                zoomLevel = 17f;
            } else if (maxDelta < 0.005) { // 标记点比较集中
                zoomLevel = 15f;
            } else if (maxDelta < 0.01) { // 标记点有一定分布
                zoomLevel = 14f;
            } else { // 标记点分布较广
                zoomLevel = 13f;
            }

            // 移动地图视角
            aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, zoomLevel));

            Log.d("MapDebug", "调整地图视野: 中心(" + centerLat + "," + centerLng + "), 缩放:" + zoomLevel);

        } catch (Exception e) {
            Log.e("MapDebug", "调整地图视野失败", e);
            // 失败时使用默认视角
            LatLng defaultCenter = new LatLng(24.8333, 102.8519);
            aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultCenter, 15f));
        }
    }


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
    // 在类中定义成员变量
    private AMapLocationClient locationClient;

    // 修复5：更新定位初始化方法，处理异常
    private void initLocation() {
        try {
            // 移除局部变量声明，使用成员变量
            locationClient = new AMapLocationClient(this);
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
            // 可添加用户提示：如Toast.makeText(this, "定位服务初始化失败", Toast.LENGTH_SHORT).show();
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
    /**
     * 打开发布页面
     */
    private void openPublishActivity(LatLng location) {
        Intent intent = new Intent(MainActivity.this, PublishActivity.class);
        intent.putExtra("latitude", location.latitude);
        intent.putExtra("longitude", location.longitude);
        startActivityForResult(intent, REQUEST_CODE_PUBLISH);
    }

    // 处理发布结果
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PUBLISH && resultCode == RESULT_OK) {
            // 发布成功，刷新地图
            Toast.makeText(this, "新书发布成功！", Toast.LENGTH_SHORT).show();
            loadAndDisplayBooks();
        }
    }


    /**
     * 添加普通标记（聚合功能不可用时使用）
     */
    private void addBooksAsNormalMarkers(List<Book> bookList) {
        aMap.clear();

        for (Book book : bookList) {
            LatLng bookLocation = new LatLng(book.getLatitude(), book.getLongitude());
            Marker marker = aMap.addMarker(new MarkerOptions()
                    .position(bookLocation)
                    .title(book.getTitle())
                    .snippet("价格：￥" + book.getPrice())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

            marker.setObject(book);
        }

        Log.d("Cluster", "使用普通标记添加了 " + bookList.size() + " 个标记");
    }


    // 修改底部导航设置
    private void setupBottomNavigation() {
        btnFind = findViewById(R.id.btn_find);
        btnMap = findViewById(R.id.btn_map);
        btnMy = findViewById(R.id.btn_my);

        // 初始状态：地图按钮选中
        setButtonsSelectedState(false, true, false);

        btnFind.setOnClickListener(v -> {
            if (!isFindFragmentShowing) {
                switchToFindFragment();
            }
        });

        btnMap.setOnClickListener(v -> {
            switchToMapView();
            isFindFragmentShowing = false;
        });

        btnMy.setOnClickListener(v -> {
            switchToMyFragment();
            isFindFragmentShowing = false;
        });
    }

    private void setButtonsSelectedState(boolean findSelected, boolean mapSelected, boolean mySelected) {
        // 设置选中状态
        btnFind.setSelected(findSelected);
        btnMap.setSelected(mapSelected);
        btnMy.setSelected(mySelected);

        // 设置文字颜色（确保colors.xml中有对应颜色）
        btnFind.setTextColor(findSelected ? getResources().getColor(R.color.colorPrimary) : getResources().getColor(R.color.textDefault));
        btnMap.setTextColor(mapSelected ? getResources().getColor(R.color.colorPrimary) : getResources().getColor(R.color.textDefault));
        btnMy.setTextColor(mySelected ? getResources().getColor(R.color.colorPrimary) : getResources().getColor(R.color.textDefault));
    }


    // 在 MainActivity 中添加该方法（用于切换导航时高亮当前按钮）
    /*private void updateBottomNavState(View selectedView) {
        // 重置所有按钮状态（示例：假设未选中为灰色，选中为蓝色）
        setButtonColor(btnFind, R.color.unselected);
        setButtonColor(btnMap, R.color.unselected);
        setButtonColor(btnMy, R.color.unselected);

        // 选中状态
        setButtonColor(selectedView, R.color.selected);
    }

    // 封装设置按钮颜色的方法
    private void setButtonColor(View view, int colorResId) {
        if (view instanceof Button) {
            Button button = (Button) view;
            // 获取按钮背景 Drawable
            Drawable background = button.getBackground();
            if (background != null) {
                // 设置颜色滤镜
                background.setColorFilter(ContextCompat.getColor(this, colorResId), PorterDuff.Mode.SRC_IN);
            } else {
                // 若没有背景，直接设置文字颜色
                button.setTextColor(ContextCompat.getColor(this, colorResId));
            }
        }
    } */



    private void updateBottomNavState(View selectedView) {
        // 重置文字颜色
        btnFind.setTextColor(ContextCompat.getColor(this, R.color.unselected));
        btnMap.setTextColor(ContextCompat.getColor(this, R.color.unselected));
        btnMy.setTextColor(ContextCompat.getColor(this, R.color.unselected));

        // 选中状态文字颜色
        if (selectedView instanceof Button) {
            ((Button) selectedView).setTextColor(ContextCompat.getColor(this, R.color.selected));
        }
    }

    private void switchFragment(Fragment fragment) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit(); // 关键：提交事务
    }


    private void switchToFindFragment() {
        // 隐藏地图和搜索栏，显示FindFragment
        if (mapView != null) {
            mapView.setVisibility(View.GONE);
        }

        // 隐藏主界面的搜索框（如果有）
        if (etSearch != null) {
            etSearch.setVisibility(View.GONE);
        }
        if (btnClearSearch != null) {
            btnClearSearch.setVisibility(View.GONE);
        }
        if (tvSearchHint != null) {
            tvSearchHint.setVisibility(View.GONE);
        }

        // 显示Fragment容器
        View fragmentContainer = findViewById(R.id.fragment_container);
        if (fragmentContainer != null) {
            fragmentContainer.setVisibility(View.VISIBLE);
        }

        // 加载FindFragment
        if (findFragment == null) {
            findFragment = new FindFragment();
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, findFragment)
                .addToBackStack(null)
                .commit();

        isFindFragmentShowing = true;

        // 更新按钮状态
        setButtonsSelectedState(true, false, false);
    }

    public List<Book> getAllBooks() {
        return allBooks;
    }

    // 修改onBackPressed方法支持Fragment返回
    @Override
    public void onBackPressed() {
        View fragmentContainer = findViewById(R.id.fragment_container);
        if (fragmentContainer != null && fragmentContainer.getVisibility() == View.VISIBLE) {
            // 如果Fragment可见，返回到地图页面
            switchToMapView();
            isFindFragmentShowing = false;
        } else {
            super.onBackPressed();
        }
    }

    private void switchToMapFragment() {
        // 显示地图，隐藏Fragment容器
        if (mapView != null) {
            mapView.setVisibility(View.VISIBLE);
        }
        View fragmentContainer = findViewById(R.id.fragment_container);
        if (fragmentContainer != null) {
            fragmentContainer.setVisibility(View.GONE);
        }

        // 隐藏搜索栏
        hideSearchBar();

        // 更新按钮状态
        setButtonsSelectedState(false, true, false);
    }

    // 在 MainActivity.java 中修改 switchToMyFragment 方法
    private void switchToMyFragment() {
        try {
            // 隐藏地图
            if (mapView != null) {
                mapView.setVisibility(View.GONE);
            }

            // 显示Fragment容器
            FrameLayout fragmentContainer = findViewById(R.id.fragment_container);
            if (fragmentContainer != null) {
                fragmentContainer.setVisibility(View.VISIBLE);
            }

            // 隐藏搜索结果列表
            if (searchResultsList != null) {
                searchResultsList.setVisibility(View.GONE);
            }

            // 创建并显示MyFragment
            FragmentManager manager = getSupportFragmentManager();

            // 清除回退栈
            manager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

            FragmentTransaction transaction = manager.beginTransaction();

            // 使用replace而不是add，避免重叠
            MyFragment myFragment = new MyFragment();
            transaction.replace(R.id.fragment_container, myFragment, "MyFragment");
            transaction.commitAllowingStateLoss(); // 使用commitAllowingStateLoss避免状态丢失

            // 更新按钮状态
            setButtonsSelectedState(false, false, true);

            Log.d("Navigation", "切换到我的页面成功");

        } catch (Exception e) {
            Log.e("Navigation", "切换到我的页面失败", e);
            Toast.makeText(this, "切换页面失败，请重试", Toast.LENGTH_SHORT).show();
            // 失败时回到地图页面
            switchToMapView();
        }
    }

    // 添加显示搜索栏的方法
    private void showSearchBar() {
        View searchContainer = findViewById(R.id.top_container); // 假设搜索栏在top_container中
        if (searchContainer != null) {
            searchContainer.setVisibility(View.VISIBLE);
        }
    }

    // 添加隐藏搜索栏的方法
    private void hideSearchBar() {
        View searchContainer = findViewById(R.id.top_container);
        if (searchContainer != null) {
            searchContainer.setVisibility(View.GONE);
        }
    }

    // 修改按钮选中状态设置
    /*private void setButtonsSelectedState(boolean findSelected, boolean mapSelected, boolean mySelected) {
        btnFind.setSelected(findSelected);
        btnMap.setSelected(mapSelected);
        btnMy.setSelected(mySelected);

        // 可以在这里设置按钮选中时的样式变化
        btnFind.setTextColor(findSelected ? getResources().getColor(R.color.selected_color) : getResources().getColor(R.color.unselected_color));
        btnMap.setTextColor(mapSelected ? getResources().getColor(R.color.selected_color) : getResources().getColor(R.color.unselected_color));
        btnMy.setTextColor(mySelected ? getResources().getColor(R.color.selected_color) : getResources().getColor(R.color.unselected_color));
    } */

    // 在MainActivity中添加
    public void navigateToBookDetail(Book book) {
        try {
            if (book == null) {
                Log.e("MainActivity", "书籍信息为空，无法跳转");
                Toast.makeText(this, "书籍信息错误", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d("BookDetail", "准备跳转，书籍信息: " +
                    book.getTitle() + ", ID: " + book.getBookId());

            // 检查 BookDetailActivity 是否在 AndroidManifest.xml 中注册
            // 这是常见的崩溃原因！
            Intent intent = new Intent(this, BookDetailActivity.class);

            // 传递必要参数
            intent.putExtra("book", book);

            // 方法2：同时传递ID作为备用
            intent.putExtra("book_id", book.getBookId());

            // 添加启动标志
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            Log.d("BookDetail", "开始跳转到 BookDetailActivity");
            startActivity(intent);

        } catch (Exception e) {
            Log.e("BookDetail", "跳转失败，错误信息: " + e.getMessage(), e);
            //Toast.makeText(this, "无法打开详情页: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
            try {
                Intent intent = new Intent(this, BookDetailActivity.class);
                intent.putExtra("book_id", book.getBookId());
                startActivity(intent);
            } catch (Exception ex) {
                Toast.makeText(this, "无法打开详情页，请检查BookDetailActivity配置", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void hideAllFragments(FragmentTransaction transaction) {
        if (findFragment != null) transaction.hide(findFragment);
        if (mapFragment != null) transaction.hide(mapFragment);
        if (myFragment != null) transaction.hide(myFragment);
    }

    /*private void switchToMyFragment() {
        if (CurrentUser.getInstance().isLoggedIn()) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, myFragment)
                    .commit();
            btnFind.setSelected(false);
            btnMap.setSelected(false);
            btnMy.setSelected(true);
        } else {
            // 如果未登录，跳转到登录页
            goToLogin();
        }
    } */


    public void goToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
    }

    public void showBookDetail(int bookId) {
        try {
            // 在allBooks中查找对应的书籍
            Book targetBook = null;
            for (Book book : allBooks) {
                if (book != null && book.getBookId() == bookId) {
                    targetBook = book;
                    break;
                }
            }

            if (targetBook != null) {
                navigateToBookDetail(targetBook);
            } else {
                // 如果本地没找到，创建基础Book对象
                Intent intent = new Intent(this, BookDetailActivity.class);
                intent.putExtra("book_id", bookId);
                startActivity(intent);
            }
        } catch (Exception e) {
            Log.e("BookDetail", "通过ID跳转失败", e);
            Toast.makeText(this, "无法打开详情页", Toast.LENGTH_SHORT).show();
        }
    }

    // 生命周期方法保持不变
    /*@Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
    } */
    // 在 MainActivity.java 的 onResume 方法中添加更健壮的登录检查
    @Override
    protected void onResume() {
        super.onResume();

        // 检查登录状态 - 更健壮的检查
        try {
            UserSession userSession = UserSession.getInstance();
            if (userSession == null || !userSession.isLoggedIn()) {
                Log.w("MainActivity", "未登录或UserSession未初始化");
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return;
            }
        } catch (Exception e) {
            Log.e("MainActivity", "登录检查异常", e);
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // 恢复地图点击发布功能
        if (aMap != null) {
            setupMapClickListeners();
        }

        // 每次返回时刷新数据
        loadAndDisplayBooks();

        // 恢复地图状态
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapView != null) {
            mapView.onDestroy();
        }
        // 清理聚合管理器
        if (clusterManager != null) {
            clusterManager.clear();
        }
        // 新增：销毁定位客户端（需先在类中定义成员变量保存locationClient）
        if (locationClient != null) {
            locationClient.stopLocation(); // 停止定位
            locationClient.onDestroy();   // 销毁客户端
        }

        findFragment = null;
        mapFragment = null;
        myFragment = null;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) mapView.onSaveInstanceState(outState);
    }

    // 必须实现接口的两个方法
    // MainActivity.java 中修正后的方法
    @Override
    public void onMapClick(LatLng latLng) { // 参数名必须为 latLng（与接口一致）
        // 实现地图点击逻辑
        Log.d("MainActivity", "地图被点击，坐标：" + latLng.latitude + "," + latLng.longitude);
    }

    @Override
    public void onMarkerClick(Book book) { // 参数名必须为 book（与接口一致）
        // 实现标记点击逻辑
        if (book != null) {
            Toast.makeText(this, "点击了书籍：" + book.getTitle(), Toast.LENGTH_SHORT).show();
        }
    }


}

