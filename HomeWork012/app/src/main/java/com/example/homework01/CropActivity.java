package com.example.homework01;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Toast;

import com.example.homework01.utils.BitmapUtils;
import com.example.homework01.utils.FileUtils;
import com.example.homework01.view.CropImageView;

import java.io.File;

public class CropActivity extends AppCompatActivity {
    private CropImageView mIvPreview;
    private Bitmap mOriginalBitmap;
    private String mCurrentRatio = "free";
    private android.graphics.RectF mCropRect = new android.graphics.RectF();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);

        mIvPreview = findViewById(R.id.iv_crop_preview);
        String imagePath = getIntent().getStringExtra("image_path");
        
        if (imagePath == null || imagePath.isEmpty()) {
            Toast.makeText(this, "图片路径无效", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // 使用采样加载，避免大图片导致内存溢出
        mOriginalBitmap = BitmapUtils.decodeSampledBitmapFromFile(imagePath, 1024, 1024);
        if (mOriginalBitmap == null) {
            Toast.makeText(this, "无法加载图片", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        mIvPreview.setImageBitmap(mOriginalBitmap);
        mIvPreview.setCropRect(mCropRect);
        
        // 设置裁剪区域变化监听器
        mIvPreview.setOnCropRectChangedListener(new CropImageView.OnCropRectChangedListener() {
            @Override
            public void onCropRectChanged(android.graphics.RectF cropRect) {
                mCropRect = cropRect;
            }
        });

        // 初始化裁剪框
        initCropRect();

        // 取消按钮
        findViewById(R.id.btn_cancel_crop).setOnClickListener(v -> finish());

        // 完成按钮
        findViewById(R.id.btn_confirm_crop).setOnClickListener(v -> {
            if (mOriginalBitmap == null || mCropRect == null) {
                Toast.makeText(this, "无法裁剪图片", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // 裁剪图片
            android.graphics.Rect cropRect = new android.graphics.Rect(
                    (int) mCropRect.left,
                    (int) mCropRect.top,
                    (int) mCropRect.right,
                    (int) mCropRect.bottom
            );
            
            Bitmap croppedBitmap = BitmapUtils.cropBitmap(mOriginalBitmap, cropRect);

            // 保存裁剪后的图片
            File croppedFile = FileUtils.saveTempBitmap(this, croppedBitmap);
            if (croppedFile != null) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("cropped_path", croppedFile.getAbsolutePath());
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(this, "裁剪失败", Toast.LENGTH_SHORT).show();
            }
        });

        // 比例选择
        findViewById(R.id.crop_free).setOnClickListener(v -> {
            mCurrentRatio = "free";
            initCropRect();
            mIvPreview.setCropRect(mCropRect);
        });
        
        findViewById(R.id.crop_1_1).setOnClickListener(v -> {
            mCurrentRatio = "1:1";
            initCropRect();
            mIvPreview.setCropRect(mCropRect);
        });
        
        findViewById(R.id.crop_4_3).setOnClickListener(v -> {
            mCurrentRatio = "4:3";
            initCropRect();
            mIvPreview.setCropRect(mCropRect);
        });
        
        findViewById(R.id.crop_16_9).setOnClickListener(v -> {
            mCurrentRatio = "16:9";
            initCropRect();
            mIvPreview.setCropRect(mCropRect);
        });
        
        findViewById(R.id.crop_3_4).setOnClickListener(v -> {
            mCurrentRatio = "3:4";
            initCropRect();
            mIvPreview.setCropRect(mCropRect);
        });
        
        findViewById(R.id.crop_9_16).setOnClickListener(v -> {
            mCurrentRatio = "9:16";
            initCropRect();
            mIvPreview.setCropRect(mCropRect);
        });
    }

    // 初始化裁剪框
    private void initCropRect() {
        if (mOriginalBitmap == null) return;
        
        int bitmapWidth = mOriginalBitmap.getWidth();
        int bitmapHeight = mOriginalBitmap.getHeight();
        
        if ("1:1".equals(mCurrentRatio)) {
            int size = Math.min(bitmapWidth, bitmapHeight);
            int left = (bitmapWidth - size) / 2;
            int top = (bitmapHeight - size) / 2;
            mCropRect.set(left, top, left + size, top + size);
        } else if ("4:3".equals(mCurrentRatio)) {
            int width = bitmapWidth;
            int height = (int) (width * 3.0 / 4);
            if (height > bitmapHeight) {
                height = bitmapHeight;
                width = (int) (height * 4.0 / 3);
            }
            int left = (bitmapWidth - width) / 2;
            int top = (bitmapHeight - height) / 2;
            mCropRect.set(left, top, left + width, top + height);
        } else if ("16:9".equals(mCurrentRatio)) {
            int width = bitmapWidth;
            int height = (int) (width * 9.0 / 16);
            if (height > bitmapHeight) {
                height = bitmapHeight;
                width = (int) (height * 16.0 / 9);
            }
            int left = (bitmapWidth - width) / 2;
            int top = (bitmapHeight - height) / 2;
            mCropRect.set(left, top, left + width, top + height);
        } else if ("3:4".equals(mCurrentRatio)) {
            int height = bitmapHeight;
            int width = (int) (height * 3.0 / 4);
            if (width > bitmapWidth) {
                width = bitmapWidth;
                height = (int) (width * 4.0 / 3);
            }
            int left = (bitmapWidth - width) / 2;
            int top = (bitmapHeight - height) / 2;
            mCropRect.set(left, top, left + width, top + height);
        } else if ("9:16".equals(mCurrentRatio)) {
            int height = bitmapHeight;
            int width = (int) (height * 9.0 / 16);
            if (width > bitmapWidth) {
                width = bitmapWidth;
                height = (int) (width * 16.0 / 9);
            }
            int left = (bitmapWidth - width) / 2;
            int top = (bitmapHeight - height) / 2;
            mCropRect.set(left, top, left + width, top + height);
        } else { // free
            mCropRect.set(
                    bitmapWidth * 0.1f,
                    bitmapHeight * 0.1f,
                    bitmapWidth * 0.9f,
                    bitmapHeight * 0.9f
            );
        }
        if (mIvPreview != null) {
            mIvPreview.setCropRect(mCropRect);
        }
    }
}