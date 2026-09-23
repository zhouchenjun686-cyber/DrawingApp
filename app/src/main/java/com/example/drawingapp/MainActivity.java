package com.example.drawingapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;

public class MainActivity extends Activity {

    private DrawingView drawingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);

        Button clearBtn = new Button(this);
        clearBtn.setText("清空");

        Button saveBtn = new Button(this);
        saveBtn.setText("保存");

        Button loadBtn = new Button(this);
        loadBtn.setText("读取");

        topBar.addView(clearBtn);
        topBar.addView(saveBtn);
        topBar.addView(loadBtn);

        drawingView = new DrawingView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1.0f
        );

        rootLayout.addView(topBar);
        rootLayout.addView(drawingView, params);
        setContentView(rootLayout);

        clearBtn.setOnClickListener(v -> drawingView.clearCanvas());
        saveBtn.setOnClickListener(v -> drawingView.saveToFile());
        loadBtn.setOnClickListener(v -> drawingView.loadFromFile());
    }

    class DrawingView extends View {
        private Paint paint;
        private Path path;
        private float startX, startY;
        private Bitmap backgroundBitmap;

        public DrawingView(Context context) {
            super(context);
            init();
        }

        private void init() {
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
          }
