package com.example.ourbookapplication;

import android.os.Bundle;
import android.util.Log; // 添加Log导入
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.geocoder.GeocodeAddress;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeResult;

public class PublishActivity extends AppCompatActivity {
    private GeocodeSearch geocodeSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish); // 错误1：现在这个布局文件已存在

        // 初始化地理编码搜索对象
        try {
            geocodeSearch = new GeocodeSearch(this);
            geocodeSearch.setOnGeocodeSearchListener(new MyGeocodeListener());
        } catch (AMapException e) {
            // 错误4：替换printStackTrace为更健壮的日志记录
            Log.e("PublishActivity", "地理编码初始化失败", e);
            Toast.makeText(this, "地理编码初始化失败", Toast.LENGTH_SHORT).show();
        }

        // 错误2：现在btn_confirm_location已存在
        Button btnConfirmLocation = findViewById(R.id.btn_confirm_location);
        // 错误3：现在et_location已存在
        EditText etLocation = findViewById(R.id.et_location);

        // 错误5&6：简化Lambda表达式
        btnConfirmLocation.setOnClickListener(v -> {
            String address = etLocation.getText().toString().trim();

            if (!address.isEmpty()) {
                GeocodeQuery query = new GeocodeQuery(address, "昆明市");
                geocodeSearch.getFromLocationNameAsyn(query);
            } else {
                Toast.makeText(PublishActivity.this, "请输入地址", Toast.LENGTH_SHORT).show();
            }
        });
    }

    class MyGeocodeListener implements GeocodeSearch.OnGeocodeSearchListener {
        @Override
        public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
            // 逆地理编码回调（不需要但必须实现）
        }

        @Override
        public void onGeocodeSearched(GeocodeResult result, int rCode) {
            if (rCode == 1000) {
                if (result != null && result.getGeocodeAddressList() != null &&
                        !result.getGeocodeAddressList().isEmpty()) {

                    GeocodeAddress address = result.getGeocodeAddressList().get(0);
                    double latitude = address.getLatLonPoint().getLatitude();
                    double longitude = address.getLatLonPoint().getLongitude();
                    String formattedAddress = address.getFormatAddress();

                    runOnUiThread(() -> {
                        Toast.makeText(PublishActivity.this,
                                "解析成功: " + formattedAddress + "\n经纬度: " + latitude + "," + longitude,
                                Toast.LENGTH_LONG).show();
                    });

                    // 错误7,8,9：移除未使用的参数，直接调用保存方法
                    saveBookWithLocation(latitude, longitude, formattedAddress);
                }
            } else {
                runOnUiThread(() -> {
                    Toast.makeText(PublishActivity.this, "地址解析失败，请重试", Toast.LENGTH_SHORT).show();
                });
            }
        }
    }

    // 修改方法签名，移除未使用的参数警告
    private void saveBookWithLocation(double latitude, double longitude, String address) {
        // 实际实现保存逻辑
        // 例如：DatabaseHelper dbHelper = new DatabaseHelper(this);
        // dbHelper.insertBook(latitude, longitude, address);

        Log.d("PublishActivity", "保存位置: " + latitude + ", " + longitude + " - " + address);
    }
}