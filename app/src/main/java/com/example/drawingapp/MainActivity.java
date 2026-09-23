package com.example.drawingapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MainActivity extends Activity {

    // 1. 加载 C++ 动态库（名字必须和 CMake 里的一致）
    static {
        System.loadLibrary("share_helper");
    }
    // 2. 声明本地方法，这个方法由 C++ 实现
    public native String getShareText(String prefix);

    private LinearLayout mainContainer;
    private List<String> friendList = new ArrayList<>();
    private Map<String, List<String>> chatHistory = new HashMap<>();
    private DrawingView drawingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainContainer = new LinearLayout(this);
        mainContainer.setOrientation(LinearLayout.VERTICAL);
        mainContainer.setPadding(30, 30, 30, 30);
        setContentView(mainContainer);

        friendList.add("小明");
        chatHistory.put("小明", new ArrayList<>());
        showHomePage();
    }

    // ================== 主菜单 ==================
    private void showHomePage() {
        mainContainer.removeAllViews();
        TextView title = new TextView(this);
        title.setText("全能混合编程助手");
        title.setTextSize(26f);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 50, 0, 50);
        mainContainer.addView(title);

        Button drawBtn = new Button(this);
        drawBtn.setText("进入画板");
        drawBtn.setOnClickListener(v -> showDrawingPage());
        mainContainer.addView(drawBtn);

        Button friendBtn = new Button(this);
        friendBtn.setText("我的好友");
        friendBtn.setOnClickListener(v -> showFriendListPage());
        mainContainer.addView(friendBtn);

        TextView versionTv = new TextView(this);
        versionTv.setText("版本: v1.2 (Java+C++)");
        versionTv.setGravity(Gravity.CENTER);
        versionTv.setPadding(0, 50, 0, 0);
        mainContainer.addView(versionTv);
    }

    // ================== 页面1：画板 ==================
    private void showDrawingPage() {
        mainContainer.removeAllViews();
        LinearLayout topBar = new LinearLayout(this);
        
        Button backBtn = new Button(this); backBtn.setText("返回");
        backBtn.setOnClickListener(v -> showHomePage());
        Button clearBtn = new Button(this); clearBtn.setText("清空");
        Button saveBtn = new Button(this); saveBtn.setText("保存");
        Button loadBtn = new Button(this); loadBtn.setText("读取");
        Button shareBtn = new Button(this); shareBtn.setText("分享");

        topBar.addView(backBtn); topBar.addView(clearBtn);
        topBar.addView(saveBtn); topBar.addView(loadBtn); topBar.addView(shareBtn);
        mainContainer.addView(topBar);

        drawingView = new DrawingView(this);
        mainContainer.addView(drawingView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f));

        clearBtn.setOnClickListener(v -> drawingView.clearCanvas());
        saveBtn.setOnClickListener(v -> drawingView.saveToFile());
        loadBtn.setOnClickListener(v -> drawingView.loadFromFile());
        
        // 分享画板（这里演示分享文字，由 C++ 拼接）
        shareBtn.setOnClickListener(v -> {
            String cppText = getShareText("我刚刚画了一幅画！");
            shareContent(cppText);
        });
    }

    // 画板内部类
    class DrawingView extends View {
        private Paint paint; private Path path;
        private float startX, startY; private Bitmap backgroundBitmap;
        public DrawingView(Context context) {
            super(context); paint = new Paint();
            paint.setColor(Color.BLACK); paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(8f); paint.setAntiAlias(true);
            paint.setStrokeCap(Paint.Cap.ROUND); path = new Path();
        }
        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (backgroundBitmap != null) canvas.drawBitmap(backgroundBitmap, 0, 0, null);
            canvas.drawPath(path, paint);
        }
        @Override public boolean onTouchEvent(MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startX = event.getX(); startY = event.getY();
                    path.moveTo(startX, startY); return true;
                case MotionEvent.ACTION_MOVE:
                    float cx = event.getX(); float cy = event.getY();
                    path.quadTo(startX, startY, (startX + cx) / 2, (startY + cy) / 2);
                    startX = cx; startY = cy; invalidate(); return true;
            } return super.onTouchEvent(event);
        }
        public void clearCanvas() { path.reset(); backgroundBitmap = null; invalidate(); }
        public void saveToFile() {
            Bitmap bmp = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(bmp);
            if (backgroundBitmap != null) c.drawBitmap(backgroundBitmap, 0, 0, null);
            c.drawPath(path, paint);
            try {
                FileOutputStream fos = getContext().openFileOutput("my_drawing.png", Context.MODE_PRIVATE);
                bmp.compress(Bitmap.CompressFormat.PNG, 100, fos); fos.close();
                Toast.makeText(getContext(), "保存成功！", Toast.LENGTH_SHORT).show();
            } catch (Exception e) { Toast.makeText(getContext(), "保存失败", Toast.LENGTH_SHORT).show(); }
        }
        public void loadFromFile() {
            try {
                FileInputStream fis = getContext().openFileInput("my_drawing.png");
                backgroundBitmap = BitmapFactory.decodeStream(fis); fis.close();
                path.reset(); invalidate(); Toast.makeText(getContext(), "读取成功！", Toast.LENGTH_SHORT).show();
            } catch (Exception e) { Toast.makeText(getContext(), "没有找到已保存的画作", Toast.LENGTH_SHORT).show(); }
        }
    }

    // ================== 页面2：好友列表 ==================
    private void showFriendListPage() {
        mainContainer.removeAllViews();
        LinearLayout topBar = new LinearLayout(this);
        Button backBtn = new Button(this); backBtn.setText("返回");
        backBtn.setOnClickListener(v -> showHomePage());
        TextView title = new TextView(this); title.setText(" 我的好友"); title.setTextSize(20f);
        topBar.addView(backBtn); topBar.addView(title); mainContainer.addView(topBar);

        Button addBtn = new Button(this); addBtn.setText("+ 添加好友");
        addBtn.setOnClickListener(v -> showAddFriendDialog());
        mainContainer.addView(addBtn);

        ScrollView scrollView = new ScrollView(this);
        LinearLayout friendLayout = new LinearLayout(this);
        friendLayout.setOrientation(LinearLayout.VERTICAL);

        for (String friend : friendList) {
            Button friendBtn = new Button(this); friendBtn.setText(friend);
            friendBtn.setOnClickListener(v -> showChatPage(friend));
            friendLayout.addView(friendBtn);
        }
        scrollView.addView(friendLayout);
        mainContainer.addView(scrollView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f));
    }

    private void showAddFriendDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("添加好友");
        final EditText input = new EditText(this); input.setHint("输入好友名字");
        builder.setView(input);
        builder.setPositiveButton("添加", (dialog, which) -> {
            String name = input.getText().toString().trim();
            if (!name.isEmpty() && !friendList.contains(name)) {
                friendList.add(name); chatHistory.put(name, new ArrayList<>()); showFriendListPage();
            }
        });
        builder.setNegativeButton("取消", null); builder.show();
    }

    // ================== 页面3：聊天详情 ==================
    private void showChatPage(String friendName) {
        mainContainer.removeAllViews();
        LinearLayout topBar = new LinearLayout(this);
        Button backBtn = new Button(this); backBtn.setText("返回");
        backBtn.setOnClickListener(v -> showFriendListPage());
        TextView nameTv = new TextView(this); nameTv.setText(" " + friendName);
        Button shareBtn = new Button(this); shareBtn.setText("分享聊天");
        
        topBar.addView(backBtn); topBar.addView(nameTv); topBar.addView(shareBtn);
        mainContainer.addView(topBar);

        ScrollView scrollView = new ScrollView(this);
        LinearLayout chatLayout = new LinearLayout(this);
        chatLayout.setOrientation(LinearLayout.VERTICAL); scrollView.addView(chatLayout);
        mainContainer.addView(scrollView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f));

        List<String> history = chatHistory.get(friendName);
        if (history != null) { for (String msg : history) addMessageToView(chatLayout, msg); }

        // 分享聊天记录（由 C++ 拼接）
        shareBtn.setOnClickListener(v -> {
            String cppText = getShareText("我正在和 " + friendName + " 聊天！");
            shareContent(cppText);
        });

        LinearLayout bottomBar = new LinearLayout(this);
        EditText inputBox = new EditText(this); inputBox.setHint("输入消息...");
        inputBox.setLayoutParams(new Line
