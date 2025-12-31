package com.example.ourbookapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;  // 添加这个导入
import androidx.appcompat.app.AppCompatActivity;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

// 确保你有DatabaseHelper的导入12411
import com.example.ourbookapplication.DatabaseHelper;

public class PublishActivity extends AppCompatActivity {
    private EditText etTitle, etPrice, etLocation, etDescription, etContact;
    private Button btnPublish, btnCancel;


    // 添加这些成员变量
    private double latitude = 0;
    private double longitude = 0;
    private String address = "";

    // 添加定位相关成员变量
    private AMapLocationClient locationClient = null;
    private AMapLocationClientOption locationOption = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_publish);

        Log.d("PublishActivity", "Activity创建完成");

        initViews();

        if (btnPublish == null || btnCancel == null) {
            Log.e("PublishActivity", "按钮初始化失败，btnPublish: " + btnPublish + ", btnCancel: " + btnCancel);
            Toast.makeText(this, "界面加载失败，请重启应用", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setupClickListeners();
        receiveIntentData();

        // 将地图选择器监听器设置移到正确的位置
        if (etLocation != null) {
            etLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(PublishActivity.this, MapPickerActivity.class);
                    startActivityForResult(intent, 100);
                }
            });
        }

        Log.d("PublishActivity", "Activity初始化完成");

        // 初始化定位
        initLocation();
    }

    // 初始化定位
    private void initLocation() {
        try {
            // 初始化定位客户端
            locationClient = new AMapLocationClient(getApplicationContext());
            locationOption = new AMapLocationClientOption();
            // 设置定位模式为高精度模式
            locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            // 设置是否返回地址信息
            locationOption.setNeedAddress(true);
            // 设置定位一次
            locationOption.setOnceLocation(true);
            // 设置定位参数
            locationClient.setLocationOption(locationOption);
            // 设置定位监听
            locationClient.setLocationListener(locationListener);
            // 开始定位
            locationClient.startLocation();
        } catch (Exception e) {
            Log.e("PublishActivity", "定位初始化失败: " + e.getMessage());
            setFallbackLocation();
        }
    }

    // 定位监听
    AMapLocationListener locationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            if (aMapLocation != null) {
                if (aMapLocation.getErrorCode() == 0) {
                    // 定位成功
                    latitude = aMapLocation.getLatitude();
                    longitude = aMapLocation.getLongitude();
                    address = aMapLocation.getAddress();

                    if (etLocation != null) {
                        etLocation.setText(address);
                    }
                    Log.d("PublishActivity", "定位成功: " + address);
                } else {
                    // 定位失败
                    Log.e("PublishActivity", "定位失败: " + aMapLocation.getErrorInfo());
                    setFallbackLocation();
                }
            }
        }
    };


    // 重写onActivityResult接收返回数据
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            double lat = data.getDoubleExtra("latitude", 0);
            double lng = data.getDoubleExtra("longitude", 0);
            String addr = data.getStringExtra("address");

            // 更新位置输入框
            if (etLocation != null && addr != null) {
                etLocation.setText(addr);
            }

            // 保存经纬度供后续发布使用
            this.latitude = lat;
            this.longitude = lng;
            this.address = addr != null ? addr : "";

            Log.d("PublishActivity", "从地图选择器接收数据: lat=" + lat + ", lng=" + lng + ", address=" + addr);
        }
    }

    /**
     * 初始化视图 - 添加详细的空值检查和日志
     */
    private void initViews() {
        try {
            // 初始化输入框
            etTitle = findViewById(R.id.et_title);
            etPrice = findViewById(R.id.et_price);
            etLocation = findViewById(R.id.et_location);
            etDescription = findViewById(R.id.et_description);
            etContact = findViewById(R.id.et_contact);

            Log.d("PublishActivity", "输入框初始化: " +
                    "etContact=" + (etContact != null) +
                    "etTitle=" + (etTitle != null) +
                    ", etPrice=" + (etPrice != null) +
                    ", etLocation=" + (etLocation != null) +
                    ", etDescription=" + (etDescription != null));

            btnPublish = findViewById(R.id.btn_publish);
            btnCancel = findViewById(R.id.btn_cancel);

            Log.d("PublishActivity", "按钮初始化: btnPublish=" + (btnPublish != null) + ", btnCancel=" + (btnCancel != null));

            if (etTitle == null) {
                Log.e("PublishActivity", "et_title 未找到");
            }
            if (etPrice == null) {
                Log.e("PublishActivity", "et_price 未找到");
            }
            if (btnPublish == null) {
                Log.e("PublishActivity", "btn_publish 未找到，检查布局文件ID");
            }
            if (btnCancel == null) {
                Log.e("PublishActivity", "btn_cancel 未找到，检查布局文件ID");
            }

        } catch (Exception e) {
            Log.e("PublishActivity", "视图初始化失败", e);
            Toast.makeText(this, "界面初始化失败", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 设置点击监听器 - 添加安全检查
     */
    private void setupClickListeners() {
        Log.d("PublishActivity", "开始设置点击监听器");

        if (btnPublish != null) {
            btnPublish.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("PublishActivity", "发布按钮被点击");
                    publishBook();
                }
            });
            Log.d("PublishActivity", "发布按钮监听器设置成功");
        } else {
            Log.e("PublishActivity", "发布按钮为null，无法设置监听器");
            setupFallbackPublish();
        }

        if (btnCancel != null) {
            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("PublishActivity", "取消按钮被点击");
                    finish();
                }
            });
            Log.d("PublishActivity", "取消按钮监听器设置成功");
        } else {
            Log.e("PublishActivity", "取消按钮为null");
        }
    }

    /**
     * 备用发布方案（如果按钮初始化失败）
     */
    private void setupFallbackPublish() {
        Log.w("PublishActivity", "使用备用发布方案");
    }

    /**
     * 接收Intent数据
     */
    private void receiveIntentData() {
        try {
            Intent intent = getIntent();
            boolean isTestData = intent.getBooleanExtra("is_test_data", false);
            if (isTestData) {
                if (etTitle != null) etTitle.setText("测试书籍" + System.currentTimeMillis());
                if (etPrice != null) etPrice.setText("20.0");
                if (etDescription != null) etDescription.setText("这是一本测试书籍");
            }

            if (intent != null) {
                this.latitude = intent.getDoubleExtra("latitude", 0);
                this.longitude = intent.getDoubleExtra("longitude", 0);
                this.address = intent.getStringExtra("address");

                Log.d("PublishActivity", "接收到的地址数据: " +
                        "lat=" + latitude + ", lng=" + longitude + ", address=" + address);

                if (address != null && !address.trim().isEmpty()) {
                    if (etLocation != null) {
                        etLocation.setText(address);
                        Log.d("PublishActivity", "设置具体地址: " + address);
                    }
                } else {
                    String coordinateAddress = String.format("云南大学附近位置 (纬度:%.6f, 经度:%.6f)",
                            latitude, longitude);
                    if (etLocation != null) {
                        etLocation.setText(coordinateAddress);
                        Log.d("PublishActivity", "设置坐标地址: " + coordinateAddress);
                    }
                }

                if (etLocation != null) {
                    String displayedText = etLocation.getText().toString();
                    Log.d("PublishActivity", "界面显示地址: " + displayedText);

                    if (displayedText.contains("纬度:") && displayedText.contains("经度:")) {
                        Log.w("PublishActivity", "警告：界面显示的是坐标而非具体地址");
                    } else {
                        Log.i("PublishActivity", "成功：界面显示具体地址信息");
                    }
                }

            } else {
                Log.e("PublishActivity", "Intent为null");
                setFallbackLocation();
            }
        } catch (Exception e) {
            Log.e("PublishActivity", "接收位置数据失败", e);
            setFallbackLocation();
        }
    }

    /**
     * 设置备用位置信息
     */
    private void setFallbackLocation() {
        if (etLocation != null) {
            etLocation.setText("位置信息加载中...");
        }
    }

    // 添加调试方法
    private void debugDatabaseTable() {
        try {
            DatabaseHelper dbHelper = new DatabaseHelper(this);
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            // 检查表是否存在
            Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='books'", null);
            if (cursor != null && cursor.moveToFirst()) {
                Log.d("PublishActivity", "books表存在");

                // 检查表结构
                Cursor tableInfo = db.rawQuery("PRAGMA table_info(books)", null);
                if (tableInfo != null) {
                    Log.d("PublishActivity", "=== books表结构 ===");
                    while (tableInfo.moveToNext()) {
                        String columnName = tableInfo.getString(tableInfo.getColumnIndexOrThrow("name"));
                        String columnType = tableInfo.getString(tableInfo.getColumnIndexOrThrow("type"));
                        Log.d("PublishActivity", "列: " + columnName + " 类型: " + columnType);
                    }
                    tableInfo.close();
                }
            } else {
                Log.e("PublishActivity", "books表不存在！");
            }

            if (cursor != null) cursor.close();
            db.close();
            dbHelper.close();
        } catch (Exception e) {
            Log.e("PublishActivity", "检查数据库失败: " + e.getMessage());
        }
    }

    /**
     * 发布书籍
     */
    // 在 PublishActivity.java 中修改 publishBook 方法
    private void publishBook() {
        try {
            Log.d("PublishActivity", "开始发布书籍");
            // 调试：先检查数据库表结构
            debugDatabaseTable();

            // 获取输入数据
            String title = etTitle != null ? etTitle.getText().toString().trim() : "";
            String priceStr = etPrice != null ? etPrice.getText().toString().trim() : "";
            String description = etDescription != null ? etDescription.getText().toString().trim() : "";
            String contact = etContact != null ? etContact.getText().toString().trim() : "";

            // 验证输入
            if (contact.isEmpty()) {
                Toast.makeText(this, "请填写联系方式", Toast.LENGTH_SHORT).show();
                if (etContact != null) etContact.requestFocus();
                return;
            }

            // 验证联系方式格式（简单验证）
            if (!isValidContact(contact)) {
                Toast.makeText(this, "请输入有效的联系方式（手机号或微信号）", Toast.LENGTH_SHORT).show();
                return;
            }

            if (title.isEmpty()) {
                Toast.makeText(this, "请输入书籍标题", Toast.LENGTH_SHORT).show();
                if (etTitle != null) etTitle.requestFocus();
                return;
            }

            if (priceStr.isEmpty()) {
                Toast.makeText(this, "请输入价格", Toast.LENGTH_SHORT).show();
                if (etPrice != null) etPrice.requestFocus();
                return;
            }

            // 解析价格
            double price;
            try {
                price = Double.parseDouble(priceStr);
                if (price <= 0) {
                    Toast.makeText(this, "价格必须大于0", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "价格格式不正确", Toast.LENGTH_SHORT).show();
                return;
            }

            // 使用成员变量中的位置信息
            double lat = this.latitude;
            double lng = this.longitude;
            String addr = this.address;

            // 验证位置信息
            if (latitude == 0 || longitude == 0) {
                Toast.makeText(this, "请获取位置信息", Toast.LENGTH_SHORT).show();
                return;
            }

            // 获取当前用户信息
            String sellerId = "unknown";
            if (UserSession.getInstance() != null && UserSession.getInstance().isLoggedIn()) {
                sellerId = UserSession.getInstance().getUsername();
            }

            // 创建书籍对象 - 使用正确的构造函数
            Book book = new Book();
            book.setTitle(title);
            book.setPrice(price);
            book.setLatitude(latitude);
            book.setLongitude(longitude);
            book.setLocationName(address != null && !address.trim().isEmpty() ? address : "位置未指定");
            book.setSellerId(sellerId);
            book.setDescription(description);
            book.setSellerContact(contact);

            Log.d("PublishActivity", "创建书籍: " + book.getTitle() + ", 价格: " + book.getPrice());

            // 保存到数据库
            try (DatabaseHelper dbHelper = new DatabaseHelper(this)) {
                long bookId = dbHelper.addBook(book);

                if (bookId != -1) {
                    Log.d("PublishActivity", "书籍保存成功，ID: " + bookId);
                    Toast.makeText(this, "发布成功！", Toast.LENGTH_SHORT).show();

                    // 关键修复：设置返回结果
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("new_book_id", bookId);
                    setResult(RESULT_OK, resultIntent);

                    finish();
                } else {
                    Log.e("PublishActivity", "书籍保存失败");
                    Toast.makeText(this, "发布失败，请重试", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.e("PublishActivity", "数据库操作异常", e);
                Toast.makeText(this, "数据库操作失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            Log.e("PublishActivity", "发布过程异常", e);
            Toast.makeText(this, "发布失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // 添加联系方式验证方法
    // 修改 PublishActivity.java 中的 isValidContact 方法
    /*private boolean isValidContact(String contact) {
        if (contact == null || contact.isEmpty()) {
            return false;
        }

        // 新需求：只要是不超过11位的数字即可
        if (contact.matches("^\\d+$")) { // 纯数字
            // 检查长度不超过11位
            if (contact.length() <= 11) {
                return true;
            } else {
                Toast.makeText(this, "联系方式不能超过11位数字", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        // 保留原有的其他格式验证（可选）
        // 微信号验证（6-20位字母、数字、下划线或减号）
        if (contact.matches("^[a-zA-Z][a-zA-Z0-9_-]{5,19}$")) {
            return true;
        }

        // QQ号验证（5-12位数字）
        if (contact.matches("^[1-9][0-9]{4,11}$")) {
            return true;
        }

        // 邮箱验证
        if (contact.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return true;
        }

        // 如果既不是纯数字，也不是其他格式，则提示
        Toast.makeText(this,
                "请输入有效联系方式：\n1. 不超过11位的数字\n2. 微信号\n3. QQ号\n4. 邮箱",
                Toast.LENGTH_LONG).show();
        return false;
    } */

    // 简化的联系方式验证（完全符合新需求）
    private boolean isValidContact(String contact) {
        if (contact == null || contact.isEmpty()) {
            Toast.makeText(this, "请输入联系方式", Toast.LENGTH_SHORT).show();
            return false;
        }

        // 检查是否为纯数字
        if (!contact.matches("^\\d+$")) {
            Toast.makeText(this, "联系方式必须为数字", Toast.LENGTH_SHORT).show();
            return false;
        }

        // 检查长度不超过11位
        if (contact.length() > 11) {
            Toast.makeText(this, "联系方式不能超过11位数字", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
    /**
     * 保存书籍到数据库
     */
    private void saveBookToDatabase(final Book book) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DatabaseHelper dbHelper = new DatabaseHelper(PublishActivity.this);
                try {
                    // 使用正确的方法名：addBook 而不是 insertBook
                    long result = dbHelper.addBook(book);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (result != -1) {
                                Log.d("PublishActivity", "书籍保存成功，ID: " + result);
                                Toast.makeText(PublishActivity.this, "发布成功！", Toast.LENGTH_LONG).show();

                                setResult(RESULT_OK);
                                finish();
                            } else {
                                Log.e("PublishActivity", "书籍保存失败");
                                Toast.makeText(PublishActivity.this, "发布失败，请重试", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                } catch (Exception e) {
                    Log.e("PublishActivity", "数据库保存失败", e);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(PublishActivity.this, "保存失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                } finally {
                    dbHelper.close();
                }
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 停止定位
        if (locationClient != null) {
            locationClient.stopLocation();
            locationClient.onDestroy();
        }
    }
}