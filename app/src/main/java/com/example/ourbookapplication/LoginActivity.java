package com.example.ourbookapplication;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteConstraintException;
import android.content.ContentValues;
import android.database.Cursor;
import android.view.inputmethod.InputMethodManager;
import android.content.Intent;
import android.widget.EditText;
import android.widget.Toast;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import android.util.Log;
import android.content.SharedPreferences;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText etUsername;
    private TextInputEditText etPassword;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏标题栏
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.login_activity);

        // 初始化数据库（添加异常捕获，防止数据库初始化失败导致闪退）
        try {
            dbHelper = new DatabaseHelper(this);
        } catch (Exception e) {
            showToast("数据库初始化失败，请重启应用");
            e.printStackTrace(); // 打印日志便于调试
        }

        initViews();
        setupBackPressed();
    }

    private void initViews() {
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        Button btnLogin = findViewById(R.id.btn_login);
        Button btnRegister = findViewById(R.id.btn_register);

        // 登录按钮点击（添加异常捕获）
        btnLogin.setOnClickListener(v -> {
            try {
                login();
            } catch (Exception e) {
                showToast("登录失败，请重试");
                e.printStackTrace();
            }
        });

        // 注册按钮点击（添加异常捕获）
        btnRegister.setOnClickListener(v -> {
            try {
                register();
            } catch (Exception e) {
                showToast("注册失败，请重试");
                e.printStackTrace();
            }
        });
    }

    // 登录逻辑（完善验证，防止空指针）
    private void login() {
        // 检查数据库是否初始化成功
        if (dbHelper == null) {
            showToast("数据库未准备好，请重启应用");
            return;
        }

        // 获取输入（防止空指针：getText()可能为null）
        String username = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        // 关闭软键盘
        hideKeyboard();

        // 输入验证（逐字段检查，给出明确提示）
        if (username.isEmpty()) {
            showToast("请输入用户名");
            etUsername.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            showToast("请输入密码");
            etPassword.requestFocus();
            return;
        }

        // 增加日志输出
        Log.d("Login", "尝试登录: " + username);

        // 检查用户是否注册
        if (!dbHelper.isUsernameExists(username)) {
            Log.d("Login", "用户名不存在: " + username);
            showToast("用户名未注册，请先注册");
            return;
        }

        // 验证密码并登录
        if (dbHelper.loginUser(username, password)) {
            UserSession.getInstance().setLoggedIn(true);
            UserSession.getInstance().setUsername(username);
            showToast("登录成功，欢迎回来");
            startActivity(new Intent(this, MainActivity.class));
            finish(); // 防止返回登录页
        } else {
            Log.d("Login", "密码错误: " + username);
            showToast("密码错误，请重新输入");
        }
    }

    // 注册逻辑（完善验证，防止空指针）
    private void register() {
        // 检查数据库是否初始化成功
        if (dbHelper == null) {
            showToast("数据库未准备好，请重启应用");
            return;
        }

        // 获取输入（防止空指针）
        String username = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        // 关闭软键盘
        hideKeyboard();

        // 输入验证（更严格的检查）
        if (username.isEmpty()) {
            showToast("请输入用户名");
            etUsername.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            showToast("请输入密码");
            etPassword.requestFocus();
            return;
        }
        if (username.length() < 3) {
            showToast("用户名长度不能少于3位");
            etUsername.requestFocus();
            return;
        }
        if (password.length() < 6) {
            showToast("密码长度不能少于6位");
            etPassword.requestFocus();
            return;
        }

        // 检查用户名是否已存在
        if (dbHelper.isUsernameExists(username)) {
            showToast("用户名已存在，请更换");
            return;
        }

        // 执行注册
        if (dbHelper.registerUser(username, password)) {
            Log.d("Register", "注册成功: " + username);
            showToast("注册成功，请登录");
            etPassword.setText("");
            etPassword.requestFocus();
        } else {
            Log.d("Register", "注册失败: " + username);
            showToast("注册失败，用户名可能已存在");
        }
    }

    // 工具方法：显示提示
    private void showToast(String message) {
        // 确保在主线程显示Toast
        runOnUiThread(() -> Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show());
    }

    // 工具方法：隐藏软键盘
    private void hideKeyboard() {
        try {
            View view = getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        } catch (Exception e) {
            // 忽略键盘隐藏失败的异常
        }
    }

    // 处理返回键
    private void setupBackPressed() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 未登录时退出应用，已登录时返回上一页
                if (UserSession.getInstance().isLoggedIn()) {
                    finish();
                } else {
                    finishAffinity(); // 关闭所有Activity
                }
            }
        });
    }

    // 释放资源
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 关闭数据库连接（防止内存泄漏）
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}

