package com.example.ourbookapplication;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.RegeocodeResult;
//import com.amap.api.services.geocoder.RegeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;

/*public class MapPickerActivity extends AppCompatActivity implements RegeocodeSearch.OnGeocodeSearchListener {

    private MapView mapView;
    private AMap aMap;
    private RegeocodeSearch regeocodeSearch;
    private LatLonPoint selectedPoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_picker);

        // 初始化地图
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        if (aMap == null) {
            aMap = mapView.getMap();

            // 设置云南大学呈贡校区为初始位置
            LatLng yunnanUniversity = new LatLng(24.834653, 102.851120);
            aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(yunnanUniversity, 15f));

            // 初始化逆地理编码 - 使用try-catch包装
            try {
                regeocodeSearch = new RegeocodeSearch(this);
                regeocodeSearch.setOnGeocodeSearchListener(this);
            } catch (AMapException e) {
                Log.e("MapPicker", "逆地理编码初始化失败", e);
                // 处理初始化失败的情况
                return;
            }

            // 设置地图点击事件 - 使用Lambda表达式简化（修复警告）
            aMap.setOnMapClickListener(latLng -> {
                // 用户点击地图时执行逆地理编码
                selectedPoint = new LatLonPoint(latLng.latitude, latLng.longitude);
                performReverseGeocode(selectedPoint);
            });
        }
    }

    // 执行逆地理编码查询
    private void performReverseGeocode(LatLonPoint point) {
        if (regeocodeSearch != null && point != null) {
            // 创建查询对象 - 修正参数顺序
            RegeocodeQuery query = new RegeocodeQuery(point, 200, RegeocodeSearch.AMAP);
            try {
                regeocodeSearch.getFromLocationAsyn(query);
            } catch (AMapException e) {
                Log.e("MapPicker", "逆地理编码查询失败", e);
            }
        }
    }

    // 逆地理编码成功回调
    @Override
    public void onRegeocodeSearched(RegeocodeResult result, int resultCode) {
        if (resultCode == 1000) { // AMapException.CODE_AMAP_SUCCESS
            if (result != null && result.getRegeocodeAddress() != null) {
                String address = result.getRegeocodeAddress().getFormatAddress();
                Log.d("MapPicker", "解析到的地址: " + address);

                // 显示地址确认对话框
                showAddressConfirmationDialog(address);
            }
        } else {
            Log.e("MapPicker", "逆地理编码失败，错误码: " + resultCode);
            // 可以根据不同的错误码给出用户提示
        }
    }

    // 地理编码回调（本例中不需要但接口要求实现）
    @Override
    public void onGeocodeSearched(GeocodeResult result, int resultCode) {
        // 空实现即可，因为我们只使用逆地理编码
    }

    // 显示地址确认对话框
    private void showAddressConfirmationDialog(String address) {
        // 这里实现地址确认逻辑
        // 可以使用AlertDialog或启动新的Activity来确认地址

        // 示例：简单的Toast提示
        runOnUiThread(() -> {
            android.widget.Toast.makeText(this,
                    "选中位置: " + address,
                    android.widget.Toast.LENGTH_LONG).show();

            // 可以选择将地址返回给调用者
            // Intent resultIntent = new Intent();
            // resultIntent.putExtra("selected_address", address);
            // resultIntent.putExtra("latitude", selectedPoint.getLatitude());
            // resultIntent.putExtra("longitude", selectedPoint.getLongitude());
            // setResult(RESULT_OK, resultIntent);
            // finish();
        });
    }

    // 地图生命周期方法
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onResume();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}*/