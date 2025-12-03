package com.example.homework01;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.homework01.utils.PermissionUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.core.content.FileProvider;

import androidx.core.content.FileProvider;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_GALLERY = 101;
    private static final int REQUEST_CAMERA = 102;
    private String mCameraImagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 相册选取
        findViewById(R.id.btn_gallery).setOnClickListener(v -> {
            if (PermissionUtils.checkGalleryPermission(this)) {
                openGallery();
            } else {
                PermissionUtils.requestGalleryPermission(this);
            }
        });

        // 相机拍摄
        findViewById(R.id.btn_camera).setOnClickListener(v -> {
            if (PermissionUtils.checkCameraPermission(this)) {
                openCamera();
            } else {
                PermissionUtils.requestCameraPermission(this);
            }
        });

        // 图片拼接
        findViewById(R.id.btn_stitch).setOnClickListener(v -> {
            // 跳转到图片拼接界面
            Intent stitchIntent = new Intent(this, StitchActivity.class);
            startActivity(stitchIntent);
        });

        // 夜间模式
        findViewById(R.id.btn_night_mode).setOnClickListener(v -> {
            // 切换夜间模式逻辑
            getDelegate().setLocalNightMode(
                    getDelegate().getLocalNightMode() == androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
                            ? androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
                            : androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
            );
            recreate();
        });
    }

    // 打开相册
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // 检查是否有应用可以处理这个Intent
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_GALLERY);
        } else {
            Toast.makeText(this, "无法打开相册", Toast.LENGTH_SHORT).show();
        }
    }

    // 打开相机
    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            File imageFile = createImageFile();
            if (imageFile != null) {
                mCameraImagePath = imageFile.getAbsolutePath();
                Uri imageUri = FileProvider.getUriForFile(this, 
                        "com.example.homework01.fileprovider", imageFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                startActivityForResult(intent, REQUEST_CAMERA);
            } else {
                Toast.makeText(this, "无法创建图片文件", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "设备无相机或相机不可用", Toast.LENGTH_SHORT).show();
        }
    }

    // 创建相机图片文件
    private File createImageFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            return File.createTempFile(imageFileName, ".jpg", storageDir);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Intent editorIntent = new Intent(this, EditorActivity.class);
            if (requestCode == REQUEST_GALLERY && data != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    // 将URI转换为真实路径
                    String realPath = getRealPathFromURI(uri);
                    if (realPath != null) {
                        editorIntent.putExtra("image_path", realPath);
                    } else {
                        // 如果无法获取真实路径，则复制文件到缓存目录
                        String cachedPath = copyFileToCache(uri);
                        if (cachedPath != null) {
                            editorIntent.putExtra("image_path", cachedPath);
                        } else {
                            Toast.makeText(this, "无法读取图片文件", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                } else {
                    Toast.makeText(this, "无法获取图片", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else if (requestCode == REQUEST_CAMERA) {
                editorIntent.putExtra("image_path", mCameraImagePath);
            }
            startActivity(editorIntent);
        } else if (resultCode == RESULT_CANCELED) {
            if (requestCode == REQUEST_CAMERA) {
                // 删除创建的空文件
                if (mCameraImagePath != null) {
                    File file = new File(mCameraImagePath);
                    if (file.exists()) {
                        file.delete();
                    }
                }
            }
            Toast.makeText(this, "操作已取消", Toast.LENGTH_SHORT).show();
        }
    }

    // 通过URI获取真实路径
    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);
            cursor.close();
            return path;
        }
        return null;
    }

    // 将URI指向的文件复制到缓存目录
    private String copyFileToCache(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            File cacheDir = getCacheDir();
            String fileName = "temp_image_" + System.currentTimeMillis() + ".jpg";
            File outputFile = new File(cacheDir, fileName);

            FileOutputStream outputStream = new FileOutputStream(outputFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();
            inputStream.close();

            return outputFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionUtils.REQUEST_GALLERY) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "需要相册权限才能选择图片", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == PermissionUtils.REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "需要相机权限才能拍摄", Toast.LENGTH_SHORT).show();
            }
        }
    }
}