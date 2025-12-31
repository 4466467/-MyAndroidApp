package com.example.ourbookapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class MessagesActivity extends AppCompatActivity {
    private ListView lvMessages;
    private TextView tvEmpty;
    private MessageAdapter messageAdapter;
    private List<Message> messageList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        // 设置标题栏
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("消息通知");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initViews();
        loadMessages();
    }

    private void initViews() {
        lvMessages = findViewById(R.id.lv_messages);
        tvEmpty = findViewById(R.id.tv_empty);

        messageAdapter = new MessageAdapter(this, messageList);
        lvMessages.setAdapter(messageAdapter);  // 现在应该可以了，因为MessageAdapter是BaseAdapter

        // 设置点击事件
        lvMessages.setOnItemClickListener((parent, view, position, id) -> {
            Message message = messageList.get(position);
            openChatActivity(message);
        });
    }

    private void loadMessages() {
        // 模拟消息数据
        messageList.add(new Message(
                "seller_001",
                "张三",
                "关于《高等数学》这本书",
                "您好，这本书还在吗？",
                "10:30",
                1
        ));

        messageList.add(new Message(
                "seller_002",
                "李四",
                "《Java编程思想》交易咨询",
                "价格可以便宜点吗？",
                "昨天",
                3
        ));

        messageList.add(new Message(
                "seller_003",
                "王五",
                "《英语四级词汇》面交确认",
                "下午2点楠苑食堂见",
                "前天",
                0
        ));

        messageAdapter.notifyDataSetChanged();  // 现在可以调用了

        // 显示/隐藏空状态
        if (messageList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            lvMessages.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            lvMessages.setVisibility(View.VISIBLE);
        }
    }

    private void openChatActivity(Message message) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("seller_id", message.getSellerId());
        intent.putExtra("seller_name", message.getSellerName());
        intent.putExtra("book_title", message.getBookTitle());
        startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}