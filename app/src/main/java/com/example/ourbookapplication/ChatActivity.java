package com.example.ourbookapplication;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;
import android.util.Log;

public class ChatActivity extends AppCompatActivity {
    private ListView chatListView;
    private EditText etMessage;
    private Button btnSend;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList = new ArrayList<>();
    private String sellerId; // 卖家ID
    private String currentUserId = UserSession.getInstance().getUsername(); // 当前用户ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_chat);

        // 获取卖家ID
        sellerId = getIntent().getStringExtra("seller_id");
        if (sellerId == null) {
            sellerId = "seller_001"; // 默认卖家
        }

        // 初始化控件
        initViews();

        // 初始化适配器 - 传入当前用户ID
        chatAdapter = new ChatAdapter(
                this,
                R.layout.item_message_sent,    // 发送消息布局
                R.layout.item_message_received, // 接收消息布局
                messageList,
                currentUserId // 传入当前用户ID
        );
        chatListView.setAdapter(chatAdapter);

        // 发送按钮点击事件
        btnSend.setOnClickListener(v -> sendMessage());

        // 模拟历史消息
        loadChatHistory();

        // 设置列表背景色
        chatListView.setBackgroundColor(Color.parseColor("#F0F0F0"));
    }

    private void initViews() {
        chatListView = findViewById(R.id.chat_list_view);
        etMessage = findViewById(R.id.et_message);
        btnSend = findViewById(R.id.btn_send);
    }

    // 发送消息
    private void sendMessage() {
        String content = etMessage.getText().toString().trim();
        if (!content.isEmpty()) {
            // 添加发送的消息 - 当前用户发送
            ChatMessage sentMessage = new ChatMessage(content, ChatMessage.TYPE_SENT, currentUserId);
            messageList.add(sentMessage);
            chatAdapter.notifyDataSetChanged(); // 刷新列表
            chatListView.setSelection(messageList.size() - 1); // 滚动到底部

            // 清空输入框
            etMessage.setText("");

            // 模拟卖家回复（实际应用中应从服务器获取）
            simulateSellerReply(content);

            // 保存消息到数据库（实际应用）
            saveMessageToDatabase(sentMessage);
        }
    }

    // 模拟卖家回复
    private void simulateSellerReply(String userMessage) {
        // 延迟1秒回复
        chatListView.postDelayed(() -> {
            String reply = generateReply(userMessage);
            ChatMessage receivedMessage = new ChatMessage(reply, ChatMessage.TYPE_RECEIVED, sellerId);
            messageList.add(receivedMessage);
            chatAdapter.notifyDataSetChanged();
            chatListView.setSelection(messageList.size() - 1);

            // 保存回复消息到数据库
            saveMessageToDatabase(receivedMessage);
        }, 1000);
    }

    // 根据用户消息生成回复
    private String generateReply(String userMessage) {
        if (userMessage.contains("你好") || userMessage.contains("您好")) {
            return "你好！有什么可以帮助您的吗？";
        } else if (userMessage.contains("价格") || userMessage.contains("多少钱")) {
            return "这本书价格是30元，可以小刀。";
        } else if (userMessage.contains("位置") || userMessage.contains("哪里")) {
            return "我在云南大学楠苑，可以面交。";
        } else if (userMessage.contains("时间") || userMessage.contains("什么时候")) {
            return "我下午2点到5点都有空。";
        } else {
            return "收到您的消息了，我会尽快回复！";
        }
    }

    // 加载聊天历史
    private void loadChatHistory() {
        // 模拟加载历史消息
        messageList.add(new ChatMessage("您好！有什么可以帮助您的吗？", ChatMessage.TYPE_RECEIVED, sellerId));
        messageList.add(new ChatMessage("我想买您的《高等数学》这本书", ChatMessage.TYPE_SENT, currentUserId));
        messageList.add(new ChatMessage("好的，这本书还在。价格是25元，您觉得可以吗？", ChatMessage.TYPE_RECEIVED, sellerId));
        messageList.add(new ChatMessage("可以便宜点吗？20元可以吗？", ChatMessage.TYPE_SENT, currentUserId));
        messageList.add(new ChatMessage("好的，20元可以。我们在哪里见面？", ChatMessage.TYPE_RECEIVED, sellerId));

        chatAdapter.notifyDataSetChanged();
        chatListView.setSelection(messageList.size() - 1);
    }

    // 保存消息到数据库（模拟）
    private void saveMessageToDatabase(ChatMessage message) {
        // 实际应用中这里应该保存到数据库
        Log.d("Chat", "保存消息: " + message.getContent() + ", 类型: " + message.getType());
    }
}