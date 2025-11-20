package com.example.ourbookapplication;

import android.app.Application;
import com.amap.api.maps.MapsInitializer;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // 高德地图隐私合规设置（必须在初始化地图前调用）
        MapsInitializer.updatePrivacyShow(this, true, true);
        MapsInitializer.updatePrivacyAgree(this, true);
    }
}