package com.example.drawingapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
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

    // 根容器
    private LinearLayout mainContainer;
    
    // 好友数据
    private List<String> friendList = new ArrayList<>();
    private Map<String, List<String>> chatHistory = new HashMap<>();
    private DrawingView drawingView; // 画板视图

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mainContainer = new LinearLayout(this);
        mainContainer.setOrientation(LinearLayout.VERTICAL);
        mainContainer.setPadding(30, 30, 30, 30);
        setContentView(mainContainer);

        // 预设一个好友
        friendList.add("小明");
        chatHistory.put("小明", new ArrayList<>());

        // 显示主菜单
        showHomePage();
    }

    // ================== 主菜单 ==================
    private void showHomePage() {
        mainContainer.removeAllViews();

        TextView title = new TextView(this);
        title.setText("欢迎使用全能助手");
        title.setTextSize(28f);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 50, 0, 50);
        mainContainer.addView(title);

        Button drawBtn = new Button(this);
        drawBtn.setText("进入画板");
        drawBtn.setTextSize(20f);
        drawBtn.setOnClickListener(v -> showDrawingPage());
        mainContainer.addView(drawBtn);

        Button friendBtn = new Button(this);
        friendBtn.setText("我的好友");
        friendBtn.setTextSize(20f);
        friendBtn.setOnClickListener(v -> showFriendListPage());
        mainContainer.addView(friendBtn);
        
        // 显示版本号
        TextView versionTv = new TextView(this);
        versionTv.setText("版本: v1.1");
        versionTv.setGravity(Gravity.CENTER);
        versionTv.setTextSize(14f);
        versionTv.setPadding(0, 50, 0, 0);
        mainContainer.addView(versionTv);
    }

    // ================== 页面1：画板 ==================
    private void showDrawingPage() {
        mainContainer.removeAllViews();

        // 顶部工具栏
        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);

        Button backBtn = new Button(this);
        backBtn.setText("返回");
        backBtn.setOnClickListener(v -> showHomePage());

        Button clearBtn = new Button(this);
        clearBtn.setText("清空");

        Button saveBtn = new Button(this);
        saveBtn.setText("保存");

        Button loadBtn = new Button(this);
        loadBtn.setText("读取");

        topBar.addView(backBtn);
        topBar.addView(clearBtn);
        topBar.addView(saveBtn);
        topBar.addView(loadBtn);
        mainContainer.addView(topBar);

        // 画板视图
        drawingView = new DrawingView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f);
        mainContainer.addView(drawingView, params);

        // 绑定事件
        clearBtn.setOnClickListener(v -> drawingView.clearCanvas());
        saveBtn.setOnClickListener(v -> drawingView.saveToFile());
        loadBtn.setOnClickListener(v -> drawingView.loadFromFile());
    }

    // 画板内部类
    class DrawingView extends View {
        private Paint paint;
        private Path path;
        private float startX, startY;
        private Bitmap backgroundBitmap;

        public DrawingView(Context context) {
            super(context);
            paint = new Paint();
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(8f);
            paint.setAntiAlias(true);
            paint.setStrokeCap(Paint.Cap.ROUND);
            path = new Path();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (backgroundBitmap != null) {
                canvas.drawBitmap(backgroundBitmap, 0, 0, null);
            }
            canvas.drawPath(path, paint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startX = event.getX();
                    startY = event.getY();
                    path.moveTo(startX, startY);
                    return true;
                case MotionEvent.ACTION_MOVE:
                    float currentX = event.getX();
                    float currentY = event.getY();
                    path.quadTo(startX, startY, (startX + currentX) / 2, (startY + currentY) / 2);
                    startX = currentX;
                    startY = currentY;
                    invalidate();
                    return true;
            }
            return super.onTouchEvent(event);
        }

        public void clearCanvas() {
            path.reset();
            backgroundBitmap = null;
            invalidate();
            Toast.makeText(getContext(), "画板已清空", Toast.LENGTH_SHORT).show();
        }

        public void saveToFile() {
            Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            if (backgroundBitmap != null) {
                canvas.drawBitmap(backgroundBitmap, 0, 0, null);
            }
            canvas.drawPath(path, paint);
            try {
                FileOutputStream fos = getContext().openFileOutput("my_drawing.png", Context.MODE_PRIVATE);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.close();
                Toast.makeText(getContext(), "保存成功！", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "保存失败", Toast.LENGTH_SHORT).show();
            }
        }

        public void loadFromFile() {
            try {
                FileInputStream fis = getContext().openFileInput("my_drawing.png");
                backgroundBitmap = BitmapFactory.decodeStream(fis);
                fis.close();
                path.reset();
                invalidate();
                Toast.makeText(getContext(), "读取成功！", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "没有找到已保存的画作", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ================== 页面2：好友列表 ==================
    private void showFriendListPage() {
        mainContainer.removeAllViews();

        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        Button backBtn = new Button(this);
        backBtn.setText("返回");
        backBtn.setOnClickListener(v -> showHomePage());
        TextView title = new TextView(this);
        title.setText(" 我的好友");
        title.setTextSize(20f);
        topBar.addView(backBtn);
        topBar.addView(title);
        mainContainer.addView(topBar);

        Button addBtn = new Button(this);
        addBtn.setText("+ 添加好友");
        addBtn.setOnClickListener(v -> showAddFriendDialog());
        mainContainer.addView(addBtn);

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
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 10, 0, 10);
            friendLayout.addView(friendBtn, params);
        }
        scrollView.addView(friendLayout);
        mainContainer.addView(scrollView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f));
    }

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
                showFriendListPage();
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    // ================== 页面3：聊天详情 ==================
    private void showChatPage(String friendName) {
        mainContainer.removeAllViews();

        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        Button backBtn = new Button(this);
        backBtn.setText("返回");
        backBtn.setOnClickListener(v -> showFriendListPage());
        TextView nameTv = new TextView(this);
        nameTv.setText(" " + friendName);
        nameTv.setTextSize(20f);
        topBar.addView(backBtn);
        topBar.addView(nameTv);
        mainContainer.addView(topBar);

        ScrollView scrollView = new ScrollView(this);
        LinearLayout chatLayout = new LinearLayout(this);
        chatLayout.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(chatLayout);
        mainContainer.addView(scrollView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f));

        List<String> history = chatHistory.get(friendName);
        if (history != null) {
            for (String msg : history) {
                addMessageToView(chatLayout, msg);
            }
        }

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
            String myMsg = "我: " + text;
            history.add(myMsg);
            addMessageToView(chatLayout, myMsg);
            inputBox.setText("");
            scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
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

    private void addMessageToView(LinearLayout chatLayout, String msg) {
        TextView tv = new TextView(this);
        tv.setText(msg);
        tv.setTextSize(18f);
        tv.setPadding(10, 15, 10, 15);
        tv.setGravity(msg.startsWith("我:") ? Gravity.END : Gravity.START);
        chatLayout.addView(tv);
    }
}
