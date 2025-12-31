package com.example.ourbookapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.ArrayList;
import android.util.Log;
import androidx.appcompat.app.AlertDialog;

public class MyFragment extends Fragment {
    private TextView tvUsername;
    private TextView tvUserStatus;
    private CardView cardMyBooks;
    private CardView cardLogout;
    private RecyclerView rvMyBooks;
    private BookAdapter bookAdapter;
    private DatabaseHelper dbHelper;
    private TextView tvEmpty;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("MyFragment", "onCreate 被调用");
        dbHelper = new DatabaseHelper(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d("MyFragment", "onCreateView 被调用");
        View view = inflater.inflate(R.layout.fragment_my, container, false);

        initViews(view);
        loadUserInfo();
        setupClickListeners();

        return view;
    }

    private void initViews(View view) {
        Log.d("MyFragment", "开始初始化视图");

        try {
            tvUsername = view.findViewById(R.id.tv_username);
            tvUserStatus = view.findViewById(R.id.tv_user_status);
            cardMyBooks = view.findViewById(R.id.card_my_books);
            cardLogout = view.findViewById(R.id.card_logout);
            rvMyBooks = view.findViewById(R.id.rv_my_books);
            tvEmpty = view.findViewById(R.id.tv_empty);

            // 检查所有视图是否成功初始化
            Log.d("MyFragment", "视图初始化检查:");
            Log.d("MyFragment", "tvUsername: " + (tvUsername != null));
            Log.d("MyFragment", "tvUserStatus: " + (tvUserStatus != null));
            Log.d("MyFragment", "cardMyBooks: " + (cardMyBooks != null));
            Log.d("MyFragment", "cardLogout: " + (cardLogout != null));
            Log.d("MyFragment", "rvMyBooks: " + (rvMyBooks != null));
            Log.d("MyFragment", "tvEmpty: " + (tvEmpty != null));

            // 初始化RecyclerView
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
            rvMyBooks.setLayoutManager(layoutManager);

            bookAdapter = new BookAdapter(new ArrayList<>(), new BookAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(Book book) {
                    // 点击我的书籍跳转到详情页
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).navigateToBookDetail(book);
                    }
                }
            });
            rvMyBooks.setAdapter(bookAdapter);

            // 初始隐藏书籍列表
            rvMyBooks.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.GONE);

            Log.d("MyFragment", "视图初始化完成");

        } catch (Exception e) {
            Log.e("MyFragment", "视图初始化失败", e);
            Toast.makeText(getContext(), "界面初始化失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupClickListeners() {
        Log.d("MyFragment", "开始设置点击监听器");

        if (cardMyBooks == null) {
            Log.e("MyFragment", "cardMyBooks 为 null，无法设置监听器");
        } else {
            // 我发布的书籍 - 修复点击逻辑
            cardMyBooks.setOnClickListener(v -> {
                Log.d("MyFragment", "cardMyBooks 被点击");

                // 先检查登录状态
                if (!UserSession.getInstance().isLoggedIn()) {
                    Toast.makeText(getContext(), "请先登录", Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean isVisible = rvMyBooks.getVisibility() == View.VISIBLE;
                if (!isVisible) {
                    // 显示加载中
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("加载中...");
                    rvMyBooks.setVisibility(View.VISIBLE);
                    loadMyBooks();
                } else {
                    // 隐藏列表
                    rvMyBooks.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.GONE);
                }
            });
            Log.d("MyFragment", "cardMyBooks 监听器设置成功");
        }

        if (cardLogout == null) {
            Log.e("MyFragment", "cardLogout 为 null，无法设置监听器");
            // 创建备用按钮用于测试
            createFallbackLogoutButton();
        } else {
            // 退出登录 - 简化版本
            cardLogout.setOnClickListener(v -> {
                Log.d("MyFragment", "cardLogout 被点击 - 显示对话框");

                // 先显示一个Toast确认按钮被点击
                Toast.makeText(getContext(), "正在处理退出登录...", Toast.LENGTH_SHORT).show();

                // 检查Fragment状态
                if (!isAdded() || getActivity() == null) {
                    Log.e("MyFragment", "Fragment未附加或Activity为null");
                    Toast.makeText(getContext(), "请稍后再试", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 直接显示对话框
                showLogoutDialog();
            });
            Log.d("MyFragment", "cardLogout 监听器设置成功");
        }

        Log.d("MyFragment", "点击监听器设置完成");
    }

    // 创建备用退出按钮（用于调试）
    private void createFallbackLogoutButton() {
        Log.w("MyFragment", "创建备用退出按钮");
        try {
            if (getView() != null && getContext() != null) {
                CardView fallbackCard = new CardView(getContext());
                fallbackCard.setCardBackgroundColor(0xFFF44336); // 红色

                TextView textView = new TextView(getContext());
                textView.setText("退出登录 (备用)");
                textView.setTextColor(0xFFFFFFFF); // 白色
                textView.setTextSize(16);
                textView.setPadding(32, 32, 32, 32);

                fallbackCard.addView(textView);
                fallbackCard.setOnClickListener(v -> {
                    Log.d("MyFragment", "备用退出按钮被点击");
                    Toast.makeText(getContext(), "使用备用方式退出", Toast.LENGTH_SHORT).show();
                    performLogout();
                });

                // 添加到布局中
                if (getView() instanceof ViewGroup) {
                    ((ViewGroup) getView()).addView(fallbackCard);
                }
            }
        } catch (Exception e) {
            Log.e("MyFragment", "创建备用按钮失败", e);
        }
    }

    // 显示退出登录确认对话框
    private void showLogoutDialog() {
        Log.d("MyFragment", "显示退出登录对话框");

        try {
            Context context = requireContext();

            new AlertDialog.Builder(context)
                    .setTitle("退出登录")
                    .setMessage("确定要退出登录吗？")
                    .setPositiveButton("确定", (dialog, which) -> {
                        Log.d("MyFragment", "用户点击了确定退出");
                        Toast.makeText(context, "正在退出...", Toast.LENGTH_SHORT).show();
                        performLogout();
                    })
                    .setNegativeButton("取消", (dialog, which) -> {
                        Log.d("MyFragment", "用户取消了退出");
                        dialog.dismiss();
                        Toast.makeText(context, "已取消", Toast.LENGTH_SHORT).show();
                    })
                    .setOnCancelListener(dialog -> {
                        Log.d("MyFragment", "对话框被取消");
                    })
                    .show();

            Log.d("MyFragment", "对话框已显示");

        } catch (Exception e) {
            Log.e("MyFragment", "显示对话框失败", e);
            // 备选方案：直接执行退出
            Toast.makeText(getContext(), "退出登录中...", Toast.LENGTH_SHORT).show();
            performLogout();
        }
    }

    // 执行退出登录
    private void performLogout() {
        Log.d("MyFragment", "开始执行退出登录");

        try {
            // 1. 清除用户会话
            UserSession userSession = UserSession.getInstance();
            if (userSession != null) {
                userSession.logout();
                Log.d("MyFragment", "UserSession已清除");
            } else {
                Log.e("MyFragment", "UserSession为空");
            }

            // 2. 清除SharedPreferences
            try {
                Context context = requireContext();
                SharedPreferences preferences = context.getSharedPreferences("UserSessionPrefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.clear();
                editor.apply();
                Log.d("MyFragment", "SharedPreferences已清除");
            } catch (Exception e) {
                Log.e("MyFragment", "清除SharedPreferences失败", e);
            }

            // 3. 显示提示
            Toast.makeText(requireContext(), "已退出登录", Toast.LENGTH_SHORT).show();

            // 4. 延迟一小会儿再跳转，让Toast显示出来
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                try {
                    // 5. 跳转到登录页
                    Intent intent = new Intent(requireContext(), LoginActivity.class);
                    // 清除所有Activity栈
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                    Log.d("MyFragment", "跳转到登录页完成");

                    // 6. 结束当前Activity（可选）
                    Activity activity = getActivity();
                    if (activity != null && !activity.isFinishing()) {
                        activity.finish();
                        Log.d("MyFragment", "当前Activity已结束");
                    }

                } catch (Exception e) {
                    Log.e("MyFragment", "跳转异常", e);
                    // 最后的尝试：使用最简单的跳转
                    try {
                        Intent intent = new Intent(getContext(), LoginActivity.class);
                        startActivity(intent);
                        if (getActivity() != null) {
                            getActivity().finish();
                        }
                    } catch (Exception ex) {
                        Log.e("MyFragment", "简单跳转也失败", ex);
                    }
                }
            }, 500); // 延迟500毫秒

        } catch (Exception e) {
            Log.e("MyFragment", "退出登录过程异常", e);

            // 异常处理：尝试简单退出
            performSimpleLogout();
        }
    }

    // 简单退出登录方法（备用）
    private void performSimpleLogout() {
        Log.d("MyFragment", "使用简单退出方法");

        try {
            // 1. 清除会话
            UserSession.getInstance().logout();

            // 2. 显示提示
            Toast.makeText(getContext(), "退出登录成功", Toast.LENGTH_SHORT).show();

            // 3. 跳转到登录页
            Activity activity = getActivity();
            if (activity != null) {
                Intent intent = new Intent(activity, LoginActivity.class);
                activity.startActivity(intent);
                activity.finish();
            }

        } catch (Exception e) {
            Log.e("MyFragment", "简单退出方法也失败", e);
            Toast.makeText(getContext(), "退出失败，请重启应用", Toast.LENGTH_LONG).show();
        }
    }

    private void loadUserInfo() {
        Log.d("MyFragment", "开始加载用户信息");

        try {
            if (!isAdded() || getContext() == null) {
                Log.e("MyFragment", "Fragment未附加，无法加载用户信息");
                return;
            }

            String currentUser = UserSession.getInstance().getUsername();
            boolean isLoggedIn = UserSession.getInstance().isLoggedIn();

            Log.d("MyFragment", "当前用户: " + currentUser + ", 登录状态: " + isLoggedIn);

            if (isLoggedIn && currentUser != null && !currentUser.isEmpty()) {
                tvUsername.setText("欢迎，" + currentUser);
                tvUserStatus.setText("账号状态：正常");
                if (cardMyBooks != null) {
                    cardMyBooks.setVisibility(View.VISIBLE);
                }
            } else {
                tvUsername.setText("未登录用户");
                tvUserStatus.setText("账号状态：未登录");
                if (cardMyBooks != null) {
                    cardMyBooks.setVisibility(View.GONE);
                }
            }
        } catch (Exception e) {
            Log.e("MyFragment", "加载用户信息失败", e);
            if (tvUsername != null) {
                tvUsername.setText("用户信息加载失败");
            }
            if (tvUserStatus != null) {
                tvUserStatus.setText("账号状态：未知");
            }
        }
    }

    private void loadMyBooks() {
        Log.d("MyFragment", "开始加载我的书籍");

        if (getContext() == null) {
            Log.e("MyFragment", "Context为null，无法加载书籍");
            return;
        }

        // 检查登录状态
        if (!UserSession.getInstance().isLoggedIn()) {
            Log.e("MyFragment", "用户未登录，无法加载书籍");
            showEmptyMessage("请先登录查看发布的书籍");
            return;
        }

        String userId = UserSession.getInstance().getUsername();
        if (userId == null || userId.isEmpty()) {
            Log.e("MyFragment", "用户ID为空");
            showEmptyMessage("用户信息错误");
            return;
        }

        Log.d("MyFragment", "正在加载用户 [" + userId + "] 的书籍");

        // 显示加载中
        showLoading();

        new Thread(() -> {
            try {
                Log.d("MyFragment", "线程开始执行数据库查询");

                // 使用新的数据库连接
                DatabaseHelper localDbHelper = new DatabaseHelper(getContext());

                // 调试：先检查数据库是否正常
                // 在数据库查询前添加
                localDbHelper.debugTableStructure();
                List<Book> allBooks = localDbHelper.getAllBooks();
                Log.d("MyFragment", "数据库中共有 " + allBooks.size() + " 本书");

                // 查询指定用户的书籍12411
                List<Book> myBooks = localDbHelper.getBooksBySellerId(userId);
                localDbHelper.close();

                Log.d("MyFragment", "数据库查询完成，找到 " + myBooks.size() + " 本书");

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // 更新数据
                        if (bookAdapter != null) {
                            bookAdapter.setBooks(myBooks);
                            bookAdapter.notifyDataSetChanged();
                            Log.d("MyFragment", "适配器已更新，项目数: " + bookAdapter.getItemCount());
                        } else {
                            Log.e("MyFragment", "bookAdapter为null");
                        }

                        // 显示结果
                        if (myBooks.isEmpty()) {
                            showEmptyMessage("您还没有发布过书籍");
                            Log.d("MyFragment", "用户没有发布过书籍");
                        } else {
                            hideEmptyMessage();
                            Log.d("MyFragment", "成功加载 " + myBooks.size() + " 本书籍到界面");

                            // 显示第一条书籍的详细信息用于调试
                            if (myBooks.size() > 0) {
                                Book firstBook = myBooks.get(0);
                                Log.d("MyFragment", "第一本书信息 - ID: " + firstBook.getBookId() +
                                        ", 标题: " + firstBook.getTitle() +
                                        ", 卖家: " + firstBook.getSellerId());
                            }
                        }
                    });
                }
            } catch (Exception e) {
                Log.e("MyFragment", "加载我的书籍异常", e);
                e.printStackTrace();

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showEmptyMessage("加载失败：" + e.getMessage());
                        Log.e("MyFragment", "加载失败详情", e);
                    });
                }
            }
        }).start();
    }

    private void showLoading() {
        if (tvEmpty != null) {
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("加载中...");
        }
        if (rvMyBooks != null) {
            rvMyBooks.setVisibility(View.VISIBLE);
        }
    }

    private void showEmptyMessage(String message) {
        if (tvEmpty != null) {
            tvEmpty.setText(message);
            tvEmpty.setVisibility(View.VISIBLE);
        }
        if (rvMyBooks != null) {
            rvMyBooks.setVisibility(View.GONE);
        }
    }

    private void hideEmptyMessage() {
        if (tvEmpty != null) {
            tvEmpty.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("MyFragment", "onResume 被调用");
        loadUserInfo();
        // 如果列表正在显示，刷新数据
        if (rvMyBooks != null && rvMyBooks.getVisibility() == View.VISIBLE) {
            loadMyBooks();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("MyFragment", "onPause 被调用");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("MyFragment", "onDestroyView 被调用");
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("MyFragment", "onDestroy 被调用");
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.d("MyFragment", "onAttach 被调用");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d("MyFragment", "onDetach 被调用");
    }
}