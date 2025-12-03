package com.example.homework01.utils;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;

public class BitmapUtils {
    // 采样加载图片，避免内存溢出
    public static Bitmap decodeSampledBitmapFromFile(String filePath, int reqWidth, int reqHeight) {
        // 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        // 计算inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // 使用获取到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    // 计算图片采样率
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // 源图片的高度和宽度
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            // 计算出实际宽高和目标宽高的比率
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // 在保证解析出的bitmap宽高分别大于目标尺寸宽高的前提下，取可能的inSampleSize的最大值
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    // 裁剪图片
    public static Bitmap cropBitmap(Bitmap bitmap, Rect cropRect) {
        // 确保裁剪区域不超出图片边界
        int left = Math.max(0, cropRect.left);
        int top = Math.max(0, cropRect.top);
        int right = Math.min(bitmap.getWidth(), cropRect.right);
        int bottom = Math.min(bitmap.getHeight(), cropRect.bottom);
        
        // 确保裁剪区域有效
        if (left >= right || top >= bottom) {
            return bitmap;
        }
        
        return Bitmap.createBitmap(bitmap, left, top,
                right - left, bottom - top);
    }

    // 旋转图片
    public static Bitmap rotateBitmap(Bitmap bitmap, int degrees) {
        if (degrees == 0) {
            return bitmap;
        }
        
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    // 翻转图片
    public static Bitmap flipBitmap(Bitmap bitmap, boolean isHorizontal) {
        Matrix matrix = new Matrix();
        matrix.postScale(isHorizontal ? -1 : 1, isHorizontal ? 1 : -1);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    // 调节亮度和对比度
    public static Bitmap adjustBrightnessContrast(Bitmap bitmap, int brightness, float contrast) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap result = Bitmap.createBitmap(width, height, bitmap.getConfig());

        // 亮度范围从-100到100，转换为-255到255
        int brightnessValue = (int) (brightness / 100.0 * 255);
        // 对比度范围从-50到150，转换为0.5到2.5
        float contrastFactor = (100 + contrast * 100) / 100.0f;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = bitmap.getPixel(x, y);
                int r = Color.red(pixel);
                int g = Color.green(pixel);
                int b = Color.blue(pixel);

                // 调节亮度
                r = Math.max(0, Math.min(255, r + brightnessValue));
                g = Math.max(0, Math.min(255, g + brightnessValue));
                b = Math.max(0, Math.min(255, b + brightnessValue));

                // 调节对比度
                r = (int) (((r / 255.0f - 0.5f) * contrastFactor + 0.5f) * 255);
                g = (int) (((g / 255.0f - 0.5f) * contrastFactor + 0.5f) * 255);
                b = (int) (((b / 255.0f - 0.5f) * contrastFactor + 0.5f) * 255);

                r = Math.max(0, Math.min(255, r));
                g = Math.max(0, Math.min(255, g));
                b = Math.max(0, Math.min(255, b));

                result.setPixel(x, y, Color.rgb(r, g, b));
            }
        }
        return result;
    }

    // 黑白滤镜
    public static Bitmap applyBlackWhiteFilter(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap result = Bitmap.createBitmap(width, height, bitmap.getConfig());

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = bitmap.getPixel(x, y);
                int r = Color.red(pixel);
                int g = Color.green(pixel);
                int b = Color.blue(pixel);
                int gray = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                result.setPixel(x, y, Color.rgb(gray, gray, gray));
            }
        }
        return result;
    }

    // 复古滤镜
    public static Bitmap applyVintageFilter(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap result = Bitmap.createBitmap(width, height, bitmap.getConfig());

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = bitmap.getPixel(x, y);
                int r = Color.red(pixel);
                int g = Color.green(pixel);
                int b = Color.blue(pixel);

                int newR = (int) (0.393 * r + 0.769 * g + 0.189 * b);
                int newG = (int) (0.349 * r + 0.686 * g + 0.168 * b);
                int newB = (int) (0.272 * r + 0.534 * g + 0.131 * b);

                newR = Math.min(255, newR);
                newG = Math.min(255, newG);
                newB = Math.min(255, newB);

                result.setPixel(x, y, Color.rgb(newR, newG, newB));
            }
        }
        return result;
    }

    // 暖色调滤镜
    public static Bitmap applyWarmFilter(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap result = Bitmap.createBitmap(width, height, bitmap.getConfig());

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = bitmap.getPixel(x, y);
                int r = Color.red(pixel);
                int g = Color.green(pixel);
                int b = Color.blue(pixel);

                r = Math.min(255, r + 30);
                b = Math.max(0, b - 30);

                result.setPixel(x, y, Color.rgb(r, g, b));
            }
        }
        return result;
    }

    // 冷色调滤镜
    public static Bitmap applyColdFilter(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap result = Bitmap.createBitmap(width, height, bitmap.getConfig());

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = bitmap.getPixel(x, y);
                int r = Color.red(pixel);
                int g = Color.green(pixel);
                int b = Color.blue(pixel);

                r = Math.max(0, r - 30);
                b = Math.min(255, b + 30);

                result.setPixel(x, y, Color.rgb(r, g, b));
            }
        }
        return result;
    }
    
    // 清新滤镜
    public static Bitmap applyFreshFilter(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap result = Bitmap.createBitmap(width, height, bitmap.getConfig());

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = bitmap.getPixel(x, y);
                int r = Color.red(pixel);
                int g = Color.green(pixel);
                int b = Color.blue(pixel);

                g = Math.min(255, g + 20);
                b = Math.min(255, b + 10);

                result.setPixel(x, y, Color.rgb(r, g, b));
            }
        }
        return result;
    }
}