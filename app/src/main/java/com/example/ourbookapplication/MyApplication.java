package com.example.ourbookapplication;

import android.app.Application;
import com.amap.api.maps.MapsInitializer;
import android.util.Log;
import android.content.Context;

public class MyApplication extends Application {
    private static final String TAG = "MyApplication";
    private static Context appContext;

    @Override
    public void onCreate() {
        super.onCreate();

        appContext = getApplicationContext();

        // 1. 初始化UserSession
        UserSession.initialize(this);

        // 2. 初始化高德地图隐私合规设置（必须在所有地图初始化之前）
        initAMapPrivacy();

        Log.d(TAG, "MyApplication初始化完成");
    }

    private void initAMapPrivacy() {
        try {
            // 高德地图隐私合规设置（必须在初始化地图前调用）
            MapsInitializer.updatePrivacyShow(this, true, true);
            MapsInitializer.updatePrivacyAgree(this, true);
            Log.d(TAG, "高德地图隐私合规设置完成");
        } catch (Exception e) {
            Log.e(TAG, "高德地图隐私合规设置失败", e);
        }
    }

    public static Context getAppContext() {
        return appContext;
    }
}

        // 关键修复：设置高德地图SDK初始化（必须在线程中进行）
        /*new Thread(() -> {
            try {
                // 高德地图隐私合规设置（必须在初始化地图前调用）
                MapsInitializer.updatePrivacyShow(this, true, true);
                MapsInitializer.updatePrivacyAgree(this, true);

                // 初始化高德地图
                MapsInitializer.initialize(this);

                Log.d(TAG, "高德地图初始化成功");
            } catch (Exception e) {
                Log.e(TAG, "高德地图初始化失败", e);
            }
        }).start(); */



        // 高德地图隐私合规设置（必须在初始化地图前调用）
        //MapsInitializer.updatePrivacyShow(this, true, true);
        //MapsInitializer.updatePrivacyAgree(this, true);



