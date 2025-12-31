package com.example.ourbookapplication;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import androidx.appcompat.app.AlertDialog;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.model.LatLng;

import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.AMapException;

public class MapPickerActivity extends AppCompatActivity implements GeocodeSearch.OnGeocodeSearchListener {

    private MapView mapView;
    private AMap aMap;
    private GeocodeSearch geocodeSearch;
    private LatLonPoint selectedPoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_map_picker);

        // 初始化地图
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        if (aMap == null) {
            aMap = mapView.getMap();

            // 设置云南大学呈贡校区为初始位置
            LatLng yunnanUniversity = new LatLng(24.834653, 102.851120);
            aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(yunnanUniversity, 15f));

            // 初始化地理编码搜索
            try {
                geocodeSearch = new GeocodeSearch(this);
                geocodeSearch.setOnGeocodeSearchListener(this);
            } catch (AMapException e) {
                Log.e("MapPicker", "地理编码初始化失败", e);
            }

            // 设置地图点击事件
            aMap.setOnMapClickListener(latLng -> {
                selectedPoint = new LatLonPoint(latLng.latitude, latLng.longitude);
                performReverseGeocode(selectedPoint);
            });
        }
    }

    // 修复1：执行逆地理编码查询 - 修正语法错误
    private void performReverseGeocode(LatLonPoint point) {
        if (geocodeSearch != null && point != null) {
            // 修正：移除参数名，直接传递值
            RegeocodeQuery query = new RegeocodeQuery(point, 200, GeocodeSearch.AMAP);

            // 修正：移除不必要的try-catch，因为这是异步方法不会抛出异常
            geocodeSearch.getFromLocationAsyn(query);
        }
    }

    // 修复2：逆地理编码成功回调 - 修正逻辑错误
    @Override
    public void onRegeocodeSearched(RegeocodeResult result, int resultCode) {
        if (resultCode == 1000) { // 1000代表成功
            // 修正：检查result而不是resultCode（resultCode是int，不会为null）
            if (result != null && result.getRegeocodeAddress() != null) {
                String address = result.getRegeocodeAddress().getFormatAddress();
                Log.d("MapPicker", "解析到的地址: " + address);

                // 显示地址确认
                showAddressConfirmationDialog(address);
            }
        } else {
            Log.e("MapPicker", "逆地理编码失败，错误码: " + resultCode);
        }
    }

    // 地理编码回调（空实现）
    @Override
    public void onGeocodeSearched(GeocodeResult result, int resultCode) {
        // 空实现，因为我们只使用逆地理编码
    }

    // 显示地址确认对话框
    // 在showAddressConfirmationDialog方法中添加返回逻辑
    private void showAddressConfirmationDialog(String address) {
        new AlertDialog.Builder(this)
                .setTitle("确认位置")
                .setMessage("是否选择该位置：" + address)
                .setPositiveButton("确认", (dialog, which) -> {
                    // 返回位置数据给PublishActivity
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("latitude", selectedPoint.getLatitude());
                    resultIntent.putExtra("longitude", selectedPoint.getLongitude());
                    resultIntent.putExtra("address", address);
                    setResult(RESULT_OK, resultIntent);
                    finish(); // 关闭当前页面
                })
                .setNegativeButton("取消", null)
                .show();
    }

    // 地图生命周期方法
    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy(); // 修正：应该是onDestroy而不是onResume
        if (mapView != null) {
            mapView.onDestroy();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) {
            mapView.onSaveInstanceState(outState);
        }
    }
}