// 修改 UserSession.java
package com.example.ourbookapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class UserSession {
    private static UserSession instance;
    private SharedPreferences sharedPreferences;
    private Context appContext;

    private static final String PREFS_NAME = "UserSessionPrefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PHONE = "phone";

    // 修改构造函数
    private UserSession(Context context) {
        this.appContext = context.getApplicationContext(); // 使用Application Context
        sharedPreferences = this.appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Log.d("UserSession", "UserSession初始化完成");
    }

    // 在 UserSession.java 中添加这两个方法
    public void setLoggedIn(boolean isLoggedIn) {
        if (sharedPreferences == null) {
            Log.e("UserSession", "sharedPreferences为空");
            return;
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_LOGGED_IN, isLoggedIn);
        editor.apply();
        Log.d("UserSession", "设置登录状态: " + isLoggedIn);
    }

    public void setUsername(String username) {
        if (sharedPreferences == null) {
            Log.e("UserSession", "sharedPreferences为空");
            return;
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USERNAME, username);
        editor.apply();
        Log.d("UserSession", "设置用户名: " + username);
    }

    // 添加同步锁确保线程安全
    public static synchronized UserSession getInstance() {
        if (instance == null) {
            // 尝试获取全局Application Context
            Context context = MyApplication.getAppContext();
            if (context == null) {
                Log.e("UserSession", "无法获取Application Context");
                return null;
            }
            instance = new UserSession(context);
        }
        return instance;
    }

    // 初始化方法
    public static synchronized void initialize(Context context) {
        if (instance == null) {
            instance = new UserSession(context.getApplicationContext());
            Log.d("UserSession", "UserSession已初始化");
        }
    }

    public boolean isLoggedIn() {
        if (sharedPreferences == null) {
            Log.e("UserSession", "sharedPreferences为空");
            return false;
        }
        boolean loggedIn = sharedPreferences.getBoolean(KEY_LOGGED_IN, false);
        Log.d("UserSession", "检查登录状态: " + loggedIn);
        return loggedIn;
    }

    public String getUsername() {
        if (sharedPreferences == null) return "";
        return sharedPreferences.getString(KEY_USERNAME, "");
    }

    // 增强的登录方法
    public void login(String username, String userId) {
        if (sharedPreferences == null) {
            Log.e("UserSession", "sharedPreferences为空，无法登录");
            return;
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_LOGGED_IN, true);
        editor.putString(KEY_USERNAME, username);
        if (userId != null) {
            editor.putString(KEY_USER_ID, userId);
        }
        editor.apply();
        Log.d("UserSession", "用户登录成功: " + username);
    }

    // 增强的退出方法
    public void logout() {
        if (sharedPreferences == null) {
            Log.e("UserSession", "sharedPreferences为空，无法退出");
            return;
        }

        try {
            // 先获取当前用户名用于日志
            String username = getUsername();

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            boolean success = editor.commit(); // 使用commit确保同步完成

            if (success) {
                Log.d("UserSession", "用户退出成功: " + username);

                // 可选：通知所有页面用户已退出
                // EventBus.getDefault().post(new LogoutEvent());
            } else {
                Log.e("UserSession", "用户退出失败: SharedPreferences提交失败");
            }

        } catch (Exception e) {
            Log.e("UserSession", "用户退出异常", e);
        }
    }

    // 添加其他用户信息管理方法
    public void setUserInfo(String username, String email, String phone) {
        if (sharedPreferences == null) return;

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PHONE, phone);
        editor.apply();
    }

    public String getUserId() {
        if (sharedPreferences == null) return "";
        return sharedPreferences.getString(KEY_USER_ID, "");
    }
}