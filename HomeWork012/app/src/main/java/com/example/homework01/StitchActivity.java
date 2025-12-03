package com.example.homework01;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.homework01.utils.BitmapUtils;
import com.example.homework01.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StitchActivity extends AppCompatActivity {
    private static final int REQUEST_SELECT_IMAGES = 201;
    private static final int MAX_IMAGES = 4;
    
    private GridLayout mImageGrid;
    private RadioGroup mStitchModeGroup;
    private List<String> mSelectedImages = new ArrayList<>();
    private List<ImageView> mImageViews = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stitch);
        
        initViews();
        setupClickListeners();
    }
    
    private void initViews() {
        mImageGrid = findViewById(R.id.image_grid);
        mStitchModeGroup = findViewById(R.id.stitch_mode_group);
        RadioButton horizontalBtn = findViewById(R.id.rb_horizontal);
        horizontalBtn.setChecked(true); // 默认选中水平拼接
        
        // 初始化图片显示框
        for (int i = 0; i < MAX_IMAGES; i++) {
            ImageView imageView = new ImageView(this);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setBackgroundResource(R.drawable.btn_selector); // 使用现有背景
            
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = 200;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(8, 8, 8, 8);
            imageView.setLayoutParams(params);
            
            mImageGrid.addView(imageView);
            mImageViews.add(imageView);
        }
    }
    
    private void setupClickListeners() {
        // 添加图片按钮
        findViewById(R.id.btn_add_images).setOnClickListener(v -> selectImages());
        
        // 拼接按钮
        findViewById(R.id.btn_stitch).setOnClickListener(v -> stitchImages());
        
        // 返回按钮
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }
    
    private void selectImages() {
        if (mSelectedImages.size() >= MAX_IMAGES) {
            Toast.makeText(this, "最多只能选择" + MAX_IMAGES + "张图片", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 检查权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1001);
            return;
        }
        
        // 启动图片选择器
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "选择图片"), REQUEST_SELECT_IMAGES);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_SELECT_IMAGES && resultCode == RESULT_OK && data != null) {
            if (data.getClipData() != null) {
                // 多张图片选择
                int count = data.getClipData().getItemCount();
                int remainingSlots = MAX_IMAGES - mSelectedImages.size();
                int imagesToAdd = Math.min(count, remainingSlots);
                
                for (int i = 0; i < imagesToAdd; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    mSelectedImages.add(imageUri.toString());
                }
            } else if (data.getData() != null) {
                // 单张图片选择
                if (mSelectedImages.size() < MAX_IMAGES) {
                    Uri imageUri = data.getData();
                    mSelectedImages.add(imageUri.toString());
                }
            }
            
            updateImageGrid();
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImages();
            } else {
                Toast.makeText(this, "需要存储权限才能选择图片", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void updateImageGrid() {
        // 清空所有图片
        for (ImageView imageView : mImageViews) {
            imageView.setImageDrawable(null);
            imageView.setBackgroundResource(R.drawable.btn_selector);
        }
        
        // 显示已选择的图片
        for (int i = 0; i < mSelectedImages.size(); i++) {
            if (i < mImageViews.size()) {
                String imagePath = mSelectedImages.get(i);
                if (imagePath.startsWith("content://")) {
                    // URI格式
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(imagePath));
                        // 缩略图显示
                        Bitmap thumb = Bitmap.createScaledBitmap(bitmap, 200, 200, true);
                        mImageViews.get(i).setImageBitmap(thumb);
                        mImageViews.get(i).setBackground(null);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (OutOfMemoryError e) {
                        Toast.makeText(this, "内存不足，无法加载图片", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // 文件路径格式
                    try {
                        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                        if (bitmap != null) {
                            Bitmap thumb = Bitmap.createScaledBitmap(bitmap, 200, 200, true);
                            mImageViews.get(i).setImageBitmap(thumb);
                            mImageViews.get(i).setBackground(null);
                        }
                    } catch (OutOfMemoryError e) {
                        Toast.makeText(this, "内存不足，无法加载图片", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }
    
    private void stitchImages() {
        if (mSelectedImages.size() < 2) {
            Toast.makeText(this, "至少需要选择2张图片进行拼接", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 获取拼接模式
        int selectedModeId = mStitchModeGroup.getCheckedRadioButtonId();
        int stitchMode;
        if (selectedModeId == R.id.rb_horizontal) {
            stitchMode = 0; // 水平拼接
        } else if (selectedModeId == R.id.rb_vertical) {
            stitchMode = 1; // 垂直拼接
        } else {
            stitchMode = 2; // 网格拼接 (2x2)
        }
        
        // 加载所有图片
        List<Bitmap> bitmaps = new ArrayList<>();
        for (String imagePath : mSelectedImages) {
            try {
                Bitmap bitmap = null;
                if (imagePath.startsWith("content://")) {
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(imagePath));
                    } catch (IOException e) {
                        e.printStackTrace();
                        continue;
                    }
                } else {
                    bitmap = BitmapFactory.decodeFile(imagePath);
                }
                
                if (bitmap != null) {
                    // 使用采样加载减小内存占用
                    // 如果是文件路径，使用采样加载
                    if (!imagePath.startsWith("content://")) {
                        bitmap = BitmapUtils.decodeSampledBitmapFromFile(imagePath, 512, 512);
                    } else {
                        // 对于URI，先缩放到合适大小
                        bitmap = Bitmap.createScaledBitmap(bitmap, 512, 512, true);
                    }
                    bitmaps.add(bitmap);
                }
            } catch (OutOfMemoryError e) {
                // 释放已加载的图片
                for (Bitmap b : bitmaps) {
                    if (b != null && !b.isRecycled()) {
                        b.recycle();
                    }
                }
                bitmaps.clear();
                Toast.makeText(this, "内存不足，无法完成拼接", Toast.LENGTH_SHORT).show();
                System.gc(); // 提示垃圾回收
                return;
            }
        }
        
        if (bitmaps.isEmpty()) {
            Toast.makeText(this, "无法加载任何图片", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 执行拼接
        Bitmap stitchedBitmap = null;
        try {
            stitchedBitmap = performStitch(bitmaps, stitchMode);
        } catch (OutOfMemoryError e) {
            // 释放所有位图
            for (Bitmap bitmap : bitmaps) {
                if (bitmap != null && !bitmap.isRecycled()) {
                    bitmap.recycle();
                }
            }
            bitmaps.clear();
            Toast.makeText(this, "内存不足，无法完成拼接", Toast.LENGTH_SHORT).show();
            System.gc(); // 提示垃圾回收
            return;
        }
        
        // 释放原始图片内存
        for (Bitmap bitmap : bitmaps) {
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
        
        if (stitchedBitmap != null) {
            // 保存拼接后的图片
            File stitchedFile = FileUtils.saveTempBitmap(this, stitchedBitmap);
            
            if (stitchedFile != null) {
                Toast.makeText(this, "图片拼接完成", Toast.LENGTH_SHORT).show();
                
                // 跳转到编辑界面
                Intent editorIntent = new Intent(this, EditorActivity.class);
                editorIntent.putExtra("image_path", stitchedFile.getAbsolutePath());
                startActivity(editorIntent);
            } else {
                Toast.makeText(this, "保存拼接图片失败", Toast.LENGTH_SHORT).show();
            }
            
            // 释放拼接后的图片内存
            if (stitchedBitmap != null && !stitchedBitmap.isRecycled()) {
                stitchedBitmap.recycle();
            }
        } else {
            Toast.makeText(this, "图片拼接失败", Toast.LENGTH_SHORT).show();
        }
    }
    
    private Bitmap performStitch(List<Bitmap> bitmaps, int mode) throws OutOfMemoryError {
        if (bitmaps.isEmpty()) return null;
        
        try {
            switch (mode) {
                case 0: // 水平拼接
                    return stitchHorizontally(bitmaps);
                case 1: // 垂直拼接
                    return stitchVertically(bitmaps);
                case 2: // 网格拼接 (2x2)
                    return stitchGrid(bitmaps);
                default:
                    return null;
            }
        } catch (OutOfMemoryError e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private Bitmap stitchHorizontally(List<Bitmap> bitmaps) throws OutOfMemoryError {
        // 计算总宽度和最大高度
        int totalWidth = 0;
        int maxHeight = 0;
        
        for (Bitmap bitmap : bitmaps) {
            totalWidth += bitmap.getWidth();
            maxHeight = Math.max(maxHeight, bitmap.getHeight());
        }
        
        // 限制最大尺寸以防止内存溢出
        if (totalWidth > 4096 || maxHeight > 4096) {
            float scale = Math.min(4096f / totalWidth, 4096f / maxHeight);
            totalWidth = (int)(totalWidth * scale);
            maxHeight = (int)(maxHeight * scale);
        }
        
        // 进一步限制尺寸以确保不会导致内存问题
        if (totalWidth * maxHeight > 2048 * 2048) {
            float scale = (float) Math.sqrt((2048.0 * 2048.0) / (totalWidth * maxHeight));
            totalWidth = (int)(totalWidth * scale);
            maxHeight = (int)(maxHeight * scale);
        }
        
        // 创建结果图片
        Bitmap result = Bitmap.createBitmap(totalWidth, maxHeight, Bitmap.Config.RGB_565); // 使用RGB_565减少内存占用
        Canvas canvas = new Canvas(result);
        
        // 绘制每张图片
        int currentX = 0;
        for (Bitmap bitmap : bitmaps) {
            if (bitmap.isRecycled()) continue;
            // 居中绘制
            int y = (maxHeight - bitmap.getHeight()) / 2;
            canvas.drawBitmap(bitmap, currentX, y, null);
            currentX += bitmap.getWidth();
        }
        
        return result;
    }
    
    private Bitmap stitchVertically(List<Bitmap> bitmaps) throws OutOfMemoryError {
        // 计算总高度和最大宽度
        int totalHeight = 0;
        int maxWidth = 0;
        
        for (Bitmap bitmap : bitmaps) {
            totalHeight += bitmap.getHeight();
            maxWidth = Math.max(maxWidth, bitmap.getWidth());
        }
        
        // 限制最大尺寸以防止内存溢出
        if (maxWidth > 4096 || totalHeight > 4096) {
            float scale = Math.min(4096f / maxWidth, 4096f / totalHeight);
            maxWidth = (int)(maxWidth * scale);
            totalHeight = (int)(totalHeight * scale);
        }
        
        // 进一步限制尺寸以确保不会导致内存问题
        if (maxWidth * totalHeight > 2048 * 2048) {
            float scale = (float) Math.sqrt((2048.0 * 2048.0) / (maxWidth * totalHeight));
            maxWidth = (int)(maxWidth * scale);
            totalHeight = (int)(totalHeight * scale);
        }
        
        // 创建结果图片
        Bitmap result = Bitmap.createBitmap(maxWidth, totalHeight, Bitmap.Config.RGB_565); // 使用RGB_565减少内存占用
        Canvas canvas = new Canvas(result);
        
        // 绘制每张图片
        int currentY = 0;
        for (Bitmap bitmap : bitmaps) {
            if (bitmap.isRecycled()) continue;
            // 居中绘制
            int x = (maxWidth - bitmap.getWidth()) / 2;
            canvas.drawBitmap(bitmap, x, currentY, null);
            currentY += bitmap.getHeight();
        }
        
        return result;
    }
    
    private Bitmap stitchGrid(List<Bitmap> bitmaps) throws OutOfMemoryError {
        // 网格拼接 (2x2)
        int size = Math.min(bitmaps.size(), 4);
        int rows = (size > 2) ? 2 : 1;
        int cols = (size == 1) ? 1 : (size <= 2) ? 2 : 2;
        
        // 计算单元格大小
        int cellWidth = 0;
        int cellHeight = 0;
        
        for (Bitmap bitmap : bitmaps) {
            if (bitmap.isRecycled()) continue;
            cellWidth = Math.max(cellWidth, bitmap.getWidth());
            cellHeight = Math.max(cellHeight, bitmap.getHeight());
        }
        
        // 调整单元格大小以适应屏幕并防止内存溢出
        cellWidth = Math.min(cellWidth, 512);
        cellHeight = Math.min(cellHeight, 512);
        
        // 创建结果图片
        int resultWidth = cellWidth * cols;
        int resultHeight = cellHeight * rows;
        
        // 限制最大尺寸
        if (resultWidth > 2048) {
            float scale = 2048f / resultWidth;
            resultWidth = (int)(resultWidth * scale);
            resultHeight = (int)(resultHeight * scale);
        }
        
        if (resultHeight > 2048) {
            float scale = 2048f / resultHeight;
            resultWidth = (int)(resultWidth * scale);
            resultHeight = (int)(resultHeight * scale);
        }
        
        // 额外检查防止内存溢出
        if (resultWidth * resultHeight > 2048 * 2048) {
            float scale = (float) Math.sqrt((2048.0 * 2048.0) / (resultWidth * resultHeight));
            resultWidth = (int)(resultWidth * scale);
            resultHeight = (int)(resultHeight * scale);
        }
        
        Bitmap result = Bitmap.createBitmap(resultWidth, resultHeight, Bitmap.Config.RGB_565); // 使用RGB_565减少内存占用
        Canvas canvas = new Canvas(result);
        
        // 绘制每张图片
        for (int i = 0; i < size; i++) {
            Bitmap bitmap = bitmaps.get(i);
            if (bitmap.isRecycled()) continue;
            int row = i / cols;
            int col = i % cols;
            
            // 缩放图片以适应单元格
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, resultWidth/cols, resultHeight/rows, true);
            
            // 绘制到指定位置
            int x = col * (resultWidth/cols);
            int y = row * (resultHeight/rows);
            canvas.drawBitmap(scaledBitmap, x, y, null);
            
            // 释放缩放后的图片内存
            if (scaledBitmap != bitmap && !scaledBitmap.isRecycled()) {
                scaledBitmap.recycle();
            }
        }
        
        return result;
    }
}