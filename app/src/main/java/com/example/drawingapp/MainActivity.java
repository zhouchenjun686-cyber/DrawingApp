package com.example.drawingapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MainActivity extends Activity {

    // 数据存储（内存版，关掉App会重置）
    private List<String> friendList = new ArrayList<>();
    private Map<String, List<String>> chatHistory = new HashMap<>();

    // 界面容器
    private LinearLayout mainContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 初始化根容器
        mainContainer = new LinearLayout(this);
        mainContainer.setOrientation(LinearLayout.VERTICAL);
        setContentView(mainContainer);

        // 预设一个好友
        friendList.add("小明");
        chatHistory.put("小明", new ArrayList<>());

        // 显示好友列表页
        showFriendListPage();
    }

    // ================== 页面1：好友列表页 ==================
    private void showFriendListPage() {
        mainContainer.removeAllViews();
        mainContainer.setPadding(30, 30, 30, 30);

        // 标题
        TextView title = new TextView(this);
        title.setText("我的好友");
        title.setTextSize(24f);
        title.setGravity(Gravity.CENTER);
        mainContainer.addView(title);

        // 添加好友按钮
        Button addBtn = new Button(this);
        addBtn.setText("+ 添加好友");
        addBtn.setOnClickListener(v -> showAddFriendDialog());
        mainContainer.addView(addBtn);

        // 好友列表
        ScrollView scrollView = new ScrollView(this);
        LinearLayout friendLayout = new LinearLayout(this);
        friendLayout.setOrientation(LinearLayout.VERTICAL);
        friendLayout.setPadding(0, 20, 0, 0);

        for (String friend : friendList) {
            Button friendBtn = new Button(this);
            friendBtn.setText(friend);
            friendBtn.setTextSize(18f);
            friendBtn.setOnClickListener(v -> showChatPage(friend));
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 10, 0, 10);
            friendLayout.addView(friendBtn, params);
        }

        scrollView.addView(friendLayout);
        mainContainer.addView(scrollView);
    }

    // 弹出添加好友的输入框
    private void showAddFriendDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("添加好友");

        final EditText input = new EditText(this);
        input.setHint("输入好友名字");
        builder.setView(input);

        builder.setPositiveButton("添加", (dialog, which) -> {
            String name = input.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "名字不能为空", Toast.LENGTH_SHORT).show();
            } else if (friendList.contains(name)) {
                Toast.makeText(this, "该好友已存在", Toast.LENGTH_SHORT).show();
            } else {
                friendList.add(name);
                chatHistory.put(name, new ArrayList<>());
                Toast.makeText(this, "添加成功", Toast.LENGTH_SHORT).show();
                showFriendListPage(); // 刷新列表
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    // ================== 页面2：聊天详情页 ==================
    private void showChatPage(String friendName) {
        mainContainer.removeAllViews();
        mainContainer.setPadding(20, 20, 20, 20);

        // 顶部栏（返回 + 好友名）
        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL);

        Button backBtn = new Button(this);
        backBtn.setText("返回");
        backBtn.setOnClickListener(v -> showFriendListPage());

        TextView nameTv = new TextView(this);
        nameTv.setText(friendName);
        nameTv.setTextSize(20f);
        nameTv.setPadding(20, 0, 0, 0);

        topBar.addView(backBtn);
        topBar.addView(nameTv);
        mainContainer.addView(topBar);

        // 聊天记录区
        ScrollView scrollView = new ScrollView(this);
        LinearLayout chatLayout = new LinearLayout(this);
        chatLayout.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(chatLayout);
        
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f);
        mainContainer.addView(scrollView, scrollParams);

        // 渲染历史记录
        List<String> history = chatHistory.get(friendName);
        if (history != null) {
            for (String msg : history) {
                addMessageToView(chatLayout, msg);
            }
        }

        // 底部输入区
        LinearLayout bottomBar = new LinearLayout(this);
        bottomBar.setOrientation(LinearLayout.HORIZONTAL);

        EditText inputBox = new EditText(this);
        inputBox.setHint("输入消息...");
        inputBox.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));

        Button sendBtn = new Button(this);
        sendBtn.setText("发送");
        sendBtn.setOnClickListener(v -> {
            String text = inputBox.getText().toString().trim();
            if (text.isEmpty()) return;

            // 1. 添加我的消息
            String myMsg = "我: " + text;
            history.add(myMsg);
            addMessageToView(chatLayout, myMsg);
            inputBox.setText("");
            scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));

            // 2. 模拟好友回复
            new Handler().postDelayed(() -> {
                String[] replies = {"收到！", "哈哈哈", "真的吗？", "我一会儿找你", "太棒了！"};
                String replyText = replies[new Random().nextInt(replies.length)];
                String friendMsg = friendName + ": " + replyText;
                history.add(friendMsg);
                addMessageToView(chatLayout, friendMsg);
                scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
            }, 1000);
        });

        bottomBar.addView(inputBox);
        bottomBar.addView(sendBtn);
        mainContainer.addView(bottomBar);
    }

    // 辅助方法：把消息加到气泡里
    private void addMessageToView(LinearLayout chatLayout, String msg) {
        TextView tv = new TextView(this);
        tv.setText(msg);
        tv.setTextSize(18f);
        tv.setPadding(10, 15, 10, 15);
        
        if (msg.startsWith("我:")) {
            tv.setGravity(Gravity.END);
        } else {
            tv.setGravity(Gravity.START);
        }
        chatLayout.addView(tv);
    }
            }
