package com.example.homework01;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.HorizontalScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.homework01.utils.BitmapUtils;
import com.example.homework01.utils.FileUtils;
import com.example.homework01.view.PhotoEditorView;
import com.example.homework01.view.TextOverlayView;

public class EditorActivity extends AppCompatActivity {
    private PhotoEditorView mEditorView;
    private TextOverlayView mTextOverlayView;
    private Bitmap mOriginalBitmap;
    private Bitmap mEditedBitmap;
    private LinearLayout mAdjustPanel;
    private HorizontalScrollView mFilterPanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // 初始化视图
        mEditorView = findViewById(R.id.editor_view);
        mTextOverlayView = findViewById(R.id.text_overlay_view);
        mAdjustPanel = findViewById(R.id.panel_adjust);
        mFilterPanel = findViewById(R.id.panel_filter);

        // 获取图片路径并加载
        String imagePath = getIntent().getStringExtra("image_path");
        
        // 尝试多种方式加载图片
        if (imagePath != null && !imagePath.isEmpty()) {
            // 使用采样加载，避免大图片导致内存溢出
            mOriginalBitmap = BitmapUtils.decodeSampledBitmapFromFile(imagePath, 1024, 1024);
            
            // 如果通过文件路径加载失败，尝试其他方式
            if (mOriginalBitmap == null) {
                Toast.makeText(this, "通过路径加载失败: " + imagePath, Toast.LENGTH_LONG).show();
            }
        }

        if (mOriginalBitmap == null) {
            Toast.makeText(this, "图片加载失败", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        mEditedBitmap = mOriginalBitmap.copy(mOriginalBitmap.getConfig(), true);
        mEditorView.setBitmap(mEditedBitmap);

        // 返回按钮
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // 保存按钮
        findViewById(R.id.btn_save).setOnClickListener(v -> {
            // 合并文字和图片
            Bitmap finalBitmap = mergeTextWithBitmap();
            if (FileUtils.saveBitmapToGallery(this, finalBitmap)) {
                Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show();
            }
        });

        // 分享按钮
        findViewById(R.id.btn_share).setOnClickListener(v -> {
            // 合并文字和图片
            Bitmap finalBitmap = mergeTextWithBitmap();
            // 分享到抖音逻辑
            FileUtils.shareToDouyin(this, finalBitmap);
        });

        // 标签切换
        findViewById(R.id.tab_crop).setOnClickListener(v -> {
            Intent cropIntent = new Intent(this, CropActivity.class);
            cropIntent.putExtra("image_path", imagePath);
            startActivityForResult(cropIntent, 103);
        });

        findViewById(R.id.tab_rotate).setOnClickListener(v -> showRotateDialog());

        findViewById(R.id.tab_adjust).setOnClickListener(v -> {
            mAdjustPanel.setVisibility(View.VISIBLE);
            mFilterPanel.setVisibility(View.GONE);
        });

        findViewById(R.id.tab_filter).setOnClickListener(v -> {
            mFilterPanel.setVisibility(View.VISIBLE);
            mAdjustPanel.setVisibility(View.GONE);
        });

        findViewById(R.id.tab_text).setOnClickListener(v -> showTextEditorDialog());

        // 亮度调节
        SeekBar brightnessSeek = findViewById(R.id.seek_brightness);
        // 设置亮度调节范围为-100到100，默认值为0
        brightnessSeek.setMax(200);
        brightnessSeek.setProgress(100);
        brightnessSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int brightness = progress - 100;
                float contrast = (((SeekBar)findViewById(R.id.seek_contrast)).getProgress() - 100) / 100f;

                mEditedBitmap = BitmapUtils.adjustBrightnessContrast(mOriginalBitmap, brightness, contrast);
                mEditorView.setBitmap(mEditedBitmap);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // 对比度调节
        SeekBar contrastSeek = findViewById(R.id.seek_contrast);
        // 设置对比度调节范围为-50到150，默认值为0
        contrastSeek.setMax(200);
        contrastSeek.setProgress(100);
        contrastSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int brightness = ((SeekBar)findViewById(R.id.seek_brightness)).getProgress() - 100;
                // 对比度范围从-50到150，转换为-0.5到1.5
                float contrast = (progress - 100) / 100f;
                mEditedBitmap = BitmapUtils.adjustBrightnessContrast(mOriginalBitmap, brightness, contrast);
                mEditorView.setBitmap(mEditedBitmap);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // 滤镜按钮
        findViewById(R.id.filter_origin).setOnClickListener(v -> {
            mEditedBitmap = mOriginalBitmap.copy(mOriginalBitmap.getConfig(), true);
            mEditorView.setBitmap(mEditedBitmap);
        });

        findViewById(R.id.filter_black_white).setOnClickListener(v -> {
            mEditedBitmap = BitmapUtils.applyBlackWhiteFilter(mOriginalBitmap);
            mEditorView.setBitmap(mEditedBitmap);
        });

        findViewById(R.id.filter_vintage).setOnClickListener(v -> {
            mEditedBitmap = BitmapUtils.applyVintageFilter(mOriginalBitmap);
            mEditorView.setBitmap(mEditedBitmap);
        });

        findViewById(R.id.filter_warm).setOnClickListener(v -> {
            mEditedBitmap = BitmapUtils.applyWarmFilter(mOriginalBitmap);
            mEditorView.setBitmap(mEditedBitmap);
        });
        
        // 添加更多滤镜
        findViewById(R.id.filter_cold).setOnClickListener(v -> {
            mEditedBitmap = BitmapUtils.applyColdFilter(mOriginalBitmap);
            mEditorView.setBitmap(mEditedBitmap);
        });
        
        findViewById(R.id.filter_fresh).setOnClickListener(v -> {
            mEditedBitmap = BitmapUtils.applyFreshFilter(mOriginalBitmap);
            mEditorView.setBitmap(mEditedBitmap);
        });
    }

    // 旋转功能弹窗
    private void showRotateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("旋转与翻转");
        String[] options = {"顺时针旋转90°", "逆时针旋转90°", "旋转180°", "水平翻转", "垂直翻转"};
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: // 顺时针旋转90°
                    mEditedBitmap = BitmapUtils.rotateBitmap(mEditedBitmap, 90);
                    break;
                case 1: // 逆时针旋转90°
                    mEditedBitmap = BitmapUtils.rotateBitmap(mEditedBitmap, -90);
                    break;
                case 2: // 旋转180°
                    mEditedBitmap = BitmapUtils.rotateBitmap(mEditedBitmap, 180);
                    break;
                case 3: // 水平翻转
                    mEditedBitmap = BitmapUtils.flipBitmap(mEditedBitmap, true);
                    break;
                case 4: // 垂直翻转
                    mEditedBitmap = BitmapUtils.flipBitmap(mEditedBitmap, false);
                    break;
            }
            mEditorView.setBitmap(mEditedBitmap);
            // 更新原始图像为当前编辑后的图像，以便后续操作基于最新图像
            mOriginalBitmap = mEditedBitmap.copy(mEditedBitmap.getConfig(), true);
        });
        builder.show();
    }

    // 文字编辑弹窗
    private void showTextEditorDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_text_editor, null);
        EditText etText = dialogView.findViewById(R.id.et_text_content);
        Spinner spinnerFont = dialogView.findViewById(R.id.spinner_font);
        SeekBar seekFontSize = dialogView.findViewById(R.id.seek_font_size);
        TextView tvFontSize = dialogView.findViewById(R.id.tv_font_size);
        View colorBlack = dialogView.findViewById(R.id.color_black);
        View colorWhite = dialogView.findViewById(R.id.color_white);
        View colorRed = dialogView.findViewById(R.id.color_red);
        View colorBlue = dialogView.findViewById(R.id.color_blue);

        // 设置默认字体大小显示
        tvFontSize.setText("24");

        seekFontSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int fontSize = progress + 12;
                tvFontSize.setText(String.valueOf(fontSize));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // 文字颜色选择
        final int[] selectedColor = {Color.WHITE};
        
        colorBlack.setOnClickListener(v -> selectedColor[0] = Color.BLACK);
        colorWhite.setOnClickListener(v -> selectedColor[0] = Color.WHITE);
        colorRed.setOnClickListener(v -> selectedColor[0] = Color.RED);
        colorBlue.setOnClickListener(v -> selectedColor[0] = Color.BLUE);

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("确定", (dialog, which) -> {
                    // 添加文字到图片逻辑
                    String text = etText.getText().toString();
                    if (text.isEmpty()) {
                        Toast.makeText(this, "请输入文字内容", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    int fontSize = seekFontSize.getProgress() + 12;
                    
                    // 在图片上添加文字（居中位置）
                    float x = mEditedBitmap.getWidth() / 2f;
                    float y = mEditedBitmap.getHeight() / 2f;
                    mTextOverlayView.addTextElement(text, x, y, selectedColor[0], fontSize);
                    
                    Toast.makeText(this, "请在图片上拖动、缩放或旋转文字", Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    // 合并文字和图片
    private Bitmap mergeTextWithBitmap() {
        // 创建一个新的Bitmap用于绘制
        Bitmap newBitmap = Bitmap.createBitmap(mEditedBitmap.getWidth(), mEditedBitmap.getHeight(), mEditedBitmap.getConfig());
        Canvas canvas = new Canvas(newBitmap);
        
        // 先绘制原始图片
        canvas.drawBitmap(mEditedBitmap, 0, 0, null);
        
        // 绘制所有文字元素
        for (TextOverlayView.TextElement element : mTextOverlayView.getTextElements()) {
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setColor(element.getColor());
            paint.setTextSize(element.getSize() * element.getScale());
            
            // 保存画布状态
            canvas.save();
            
            // 移动到文字中心点并旋转
            canvas.rotate(element.getRotation(), element.getX(), element.getY());
            
            // 绘制文字
            canvas.drawText(element.getText(), element.getX(), element.getY(), paint);
            
            // 恢复画布状态
            canvas.restore();
        }
        
        return newBitmap;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 103 && resultCode == RESULT_OK) {
            String croppedPath = data.getStringExtra("cropped_path");
            mOriginalBitmap = BitmapUtils.decodeSampledBitmapFromFile(croppedPath, 1024, 1024);
            if (mOriginalBitmap != null) {
                mEditedBitmap = mOriginalBitmap.copy(mOriginalBitmap.getConfig(), true);
                mEditorView.setBitmap(mEditedBitmap);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 释放Bitmap内存
        if (mOriginalBitmap != null && !mOriginalBitmap.isRecycled()) {
            mOriginalBitmap.recycle();
        }
        if (mEditedBitmap != null && !mEditedBitmap.isRecycled()) {
            mEditedBitmap.recycle();
        }
    }
}