package com.example.ourbookapplication;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import android.content.Context;

public class BookDetailActivity extends AppCompatActivity {
    private TextView tvBookTitle, tvBookPrice, tvBookLocation, tvBookDescription, tvSellerName, tvSellerRating;
    private ImageView ivSellerAvatar;
    private Button btnChat, btnBack;
    private DatabaseHelper dbHelper;
    private Book currentBook;
    private TextView tvSellerContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            // 隐藏标题栏
            if (getSupportActionBar() != null) {
                getSupportActionBar().hide();
            }

            // 设置状态栏颜色
            getWindow().setStatusBarColor(Color.parseColor("#4CAF50"));

            setContentView(R.layout.activity_book_detail);

            Log.d("BookDetailActivity", "Activity创建成功");

            initViews();

            // 尝试从Intent获取Book对象
            Intent intent = getIntent();
            if (intent != null) {
                // 方法1：获取Parcelable Book对象（如果传递了）
                if (intent.hasExtra("book")) {
                    currentBook = intent.getParcelableExtra("book");
                    Log.d("BookDetailActivity", "获取到Book对象: " + (currentBook != null ? currentBook.getTitle() : "null"));
                }

                // 方法2：如果方法1失败，尝试通过ID加载
                if (currentBook == null) {
                    int bookId = intent.getIntExtra("book_id", -1);
                    if (bookId != -1) {
                        Log.d("BookDetailActivity", "通过ID加载书籍: " + bookId);
                        loadBookDetails(bookId);
                    } else {
                        // 方法3：从其他参数构建
                        String title = intent.getStringExtra("book_title");
                        double price = intent.getDoubleExtra("book_price", 0.0);
                        String location = intent.getStringExtra("book_location");

                        if (title != null) {
                            currentBook = new Book(title, price, 0, 0, location);
                            currentBook.setBookId(intent.getIntExtra("book_id", currentBook.getBookId()));
                        } else {
                            Toast.makeText(this, "未收到书籍信息", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                }

                // 显示书籍信息
                if (currentBook != null) {
                    displayBookInfo(currentBook);
                }
            }

            setupChatButton();
            setupBackButton();

        } catch (Exception e) {
            Log.e("BookDetailActivity", "创建失败", e);
            Toast.makeText(this, "页面加载失败", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        try {
            tvBookTitle = findViewById(R.id.tvBookTitle);
            tvBookPrice = findViewById(R.id.tvBookPrice);
            tvBookLocation = findViewById(R.id.tvBookLocation);
            tvBookDescription = findViewById(R.id.tvBookDescription);
            tvSellerName = findViewById(R.id.tv_seller_name);
            tvSellerRating = findViewById(R.id.tv_seller_rating);
            ivSellerAvatar = findViewById(R.id.iv_seller_avatar);
            btnChat = findViewById(R.id.btnChat);
            btnBack = findViewById(R.id.btnBack);
            tvSellerContact = findViewById(R.id.tv_seller_contact);

            // 注意：布局文件中的CardView没有设置id，所以无法直接通过findViewById获取
            // 如果需要设置CardView样式，可以在布局文件中添加id，或通过其他方式获取

            // 设置返回按钮点击事件
            if (btnBack != null) {
                btnBack.setOnClickListener(v -> finish());
            }

            Log.d("BookDetailActivity", "视图初始化完成");
        } catch (Exception e) {
            Log.e("BookDetailActivity", "视图初始化失败", e);
        }
    }

    private void displayBookInfo(Book book) {
        if (book == null) return;

        try {
            if (tvBookTitle != null) {
                tvBookTitle.setText(book.getTitle());
                tvBookTitle.setTextSize(20);
                tvBookTitle.setTextColor(Color.parseColor("#333333"));
            }
            if (tvBookPrice != null) {
                tvBookPrice.setText(String.format("￥%.2f", book.getPrice()));
                tvBookPrice.setTextSize(24);
                tvBookPrice.setTextColor(Color.parseColor("#FF5722")); // 橙色价格
                tvBookPrice.setTypeface(null, android.graphics.Typeface.BOLD);
            }
            if (tvBookLocation != null) {
                tvBookLocation.setText("📍 " + book.getLocation());
                tvBookLocation.setTextSize(16);
                tvBookLocation.setTextColor(Color.parseColor("#666666"));
            }
            if (tvBookDescription != null) {
                String desc = book.getDescription();
                if (desc != null && !desc.isEmpty()) {
                    tvBookDescription.setText(desc);
                    tvBookDescription.setTextSize(14);
                    tvBookDescription.setTextColor(Color.parseColor("#444444"));
                    tvBookDescription.setLineSpacing(1.2f, 1.2f);
                } else {
                    tvBookDescription.setText("暂无描述");
                    tvBookDescription.setTextColor(Color.parseColor("#999999"));
                }
            }
            // 显示卖家信息 - 使用分开的TextView
            if (tvSellerName != null && book.getSellerId() != null) {
                tvSellerName.setText("卖家: " + book.getSellerId());
                tvSellerName.setTextSize(16);
                tvSellerName.setTextColor(Color.parseColor("#333333"));
            }
            if (tvSellerRating != null) {
                // 这里可以根据书籍或卖家的其他信息设置信誉评分
                tvSellerRating.setText("信誉良好");
                tvSellerRating.setTextSize(12);
                tvSellerRating.setTextColor(Color.parseColor("#666666"));
            }
            if (tvSellerContact != null) {
                String contact = book.getSellerContact();
                if (contact != null && !contact.isEmpty()) {
                    // 格式化显示联系方式
                    String formattedContact = formatContact(contact);
                    tvSellerContact.setText("📞 联系方式: " + formattedContact);
                    tvSellerContact.setTextSize(14);
                    tvSellerContact.setTextColor(Color.parseColor("#2196F3")); // 蓝色
                } else {
                    tvSellerContact.setText("📞 联系方式: 未提供");
                    tvSellerContact.setTextColor(Color.parseColor("#999999"));
                }
            }

            Log.d("BookDetailActivity", "显示书籍信息: " + book.getTitle());
        } catch (Exception e) {
            Log.e("BookDetailActivity", "显示书籍信息失败", e);
        }
    }

    private void loadBookDetails(int bookId) {
        Log.d("BookDetailActivity", "从数据库加载书籍ID: " + bookId);

        // 检查dbHelper是否初始化
        if (dbHelper == null) {
            dbHelper = new DatabaseHelper(this);
        }

        new Thread(() -> {
            try {
                // 需要在DatabaseHelper中添加getBookById方法
                currentBook = dbHelper.getBookById(bookId);

                runOnUiThread(() -> {
                    if (currentBook != null) {
                        displayBookInfo(currentBook);
                    } else {
                        Toast.makeText(BookDetailActivity.this,
                                "未找到书籍信息 (ID: " + bookId + ")",
                                Toast.LENGTH_SHORT).show();

                        // 显示一个占位符
                        if (tvBookTitle != null) {
                            tvBookTitle.setText("书籍ID: " + bookId);
                        }
                    }
                });

            } catch (Exception e) {
                Log.e("BookDetailActivity", "加载书籍详情失败", e);
                runOnUiThread(() -> {
                    Toast.makeText(BookDetailActivity.this,
                            "加载失败: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void setupChatButton() {
        if (btnChat != null) {
            btnChat.setOnClickListener(v -> {
                try {
                    if (currentBook != null) {
                        // 显示卖家联系方式对话框
                        showContactDialog(currentBook);
                    } else {
                        Toast.makeText(BookDetailActivity.this, "书籍信息错误", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e("BookDetailActivity", "打开联系方式失败", e);
                }
            });

            // 美化聊天按钮 - 检查背景颜色是否已设置
            // 如果布局中已经设置了背景，可以注释掉下面这行
            // btnChat.setBackgroundColor(Color.parseColor("#4CAF50"));
            btnChat.setText("查看联系方式");
            btnChat.setTextColor(Color.WHITE);
            btnChat.setAllCaps(false);
            btnChat.setTextSize(16);
        }
    }

    // 添加显示联系方式对话框的方法
    private void showContactDialog(Book book) {
        String contact = book.getSellerContact();
        String sellerId = book.getSellerId();
        String formattedContact = formatContact(contact);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("卖家联系方式");

        String message = "📖 书籍: 《" + book.getTitle() + "》\n\n" +
                "👤 卖家: " + sellerId + "\n\n" +
                "📞 联系方式: " + formattedContact + "\n\n" +
                "💡 提示: 请自行联系卖家进行交易";

        builder.setMessage(message);

        builder.setPositiveButton("复制联系方式", (dialog, which) -> {
            // 复制到剪贴板
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("卖家联系方式", contact);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "已复制到剪贴板", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("关闭", (dialog, which) -> dialog.dismiss());

        builder.setNeutralButton("发起聊天", (dialog, which) -> {
            // 原有聊天功能
            Intent intent = new Intent(BookDetailActivity.this, ChatActivity.class);
            intent.putExtra("seller_id", sellerId);
            intent.putExtra("book_id", book.getBookId());
            intent.putExtra("book_title", book.getTitle());
            startActivity(intent);
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // 格式化联系方式
    private String formatContact(String contact) {
        if (contact == null || contact.isEmpty()) {
            return "未提供";
        }

        // 手机号格式化
        if (contact.matches("^1[3-9]\\d{9}$")) {
            return contact.substring(0, 3) + "****" + contact.substring(7);
        }

        // 其他联系方式不隐藏
        return contact;
    }

    private void setupBackButton() {
        // 已经在initViews中设置了
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}