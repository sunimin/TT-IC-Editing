package com.example.homework01.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FileUtils {
    // 保存Bitmap到相册
    public static boolean saveBitmapToGallery(Context context, Bitmap bitmap) {
        String fileName = "EDITOR_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".jpg";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "PhotoEditor");

        if (!storageDir.exists()) {
            if (!storageDir.mkdirs()) {
                return false;
            }
        }

        File imageFile = new File(storageDir, fileName);
        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();

            // 通知相册刷新
            MediaStore.Images.Media.insertImage(context.getContentResolver(),
                    imageFile.getAbsolutePath(), imageFile.getName(), null);

            // 发送广播更新图库
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(imageFile)));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 保存临时Bitmap文件
    public static File saveTempBitmap(Context context, Bitmap bitmap) {
        String fileName = "TEMP_" + System.currentTimeMillis() + ".jpg";
        File tempDir = new File(context.getCacheDir(), "temp");

        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }

        File tempFile = new File(tempDir, fileName);
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            return tempFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 分享到抖音
    public static void shareToDouyin(Context context, Bitmap bitmap) {
        File tempFile = saveTempBitmap(context, bitmap);
        if (tempFile == null) {
            Toast.makeText(context, "分享失败：文件创建失败", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        Uri photoURI = FileProvider.getUriForFile(context,
                context.getPackageName() + ".fileprovider",
                tempFile);
        shareIntent.putExtra(Intent.EXTRA_STREAM, photoURI);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        shareIntent.setPackage("com.ss.android.ugc.aweme"); // 抖音包名

        try {
            context.startActivity(shareIntent);
        } catch (Exception e) {
            Toast.makeText(context, "未安装抖音或分享失败", Toast.LENGTH_SHORT).show();
        }
    }
}