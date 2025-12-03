# 图片编辑器应用

一个功能丰富的Android图片编辑应用，包含图像滤镜、裁剪、文字叠加和图片拼接功能。

## 功能特性

- **图片选择**：从相册选择图片或使用相机拍摄新照片
- **图片编辑**：为图片应用各种滤镜和效果
- **图片裁剪**：使用可自定义的裁剪区域裁剪图片
- **文字叠加**：在图片上任意位置添加和定位文字
- **图片拼接**：将多张图片水平、垂直或网格布局组合
- **夜间模式**：在浅色和深色主题之间切换

## 截图展示

主界面             |  编辑界面          |  裁剪界面
:---------------------:|:----------------------:|:--------------------:
![image-20251203185026246](D:\AndroidWork\TT-IC-Editing\HomeWork012\picture\image-20251203185026246.png)  | ![image-20251203185125763](D:\AndroidWork\TT-IC-Editing\HomeWork012\picture\image-20251203185125763.png) | ![image-20251203185153753](D:\AndroidWork\TT-IC-Editing\HomeWork012\picture\image-20251203185153753.png) 

拼接界面          |  滤镜选项         |  文字叠加
:---------------------:|:----------------------:|:--------------------:
![image-20251203185245011](D:\AndroidWork\TT-IC-Editing\HomeWork012\picture\image-20251203185245011.png) | ![image-20251203185307455](D:\AndroidWork\TT-IC-Editing\HomeWork012\picture\image-20251203185307455.png) | ![image-20251203185354922](D:\AndroidWork\TT-IC-Editing\HomeWork012\picture\image-20251203185354922.png) 

## 环境要求

- Android SDK API 24 或更高版本
- Android Studio 及 Gradle 插件
- Java 开发工具包 (JDK) 11 或更高版本

## 项目构建

### 使用 Android Studio

1. 打开 Android Studio
2. 选择 "Open an existing Android Studio project"
3. 导航到项目目录并选择
4. 等待 Gradle 同步完成
5. 在模拟器或连接的设备上运行应用

### 使用命令行

1. 导航到项目根目录：
   ```
   cd HomeWork012
   ```

2. Windows 系统：
   ```
   gradlew.bat assembleDebug
   ```

3. macOS/Linux 系统：
   ```
   ./gradlew assembleDebug
   ```

4. 安装生成的 APK：
   ```
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

## 项目结构

```
HomeWork012/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/homework01/
│   │   │   │   ├── MainActivity.java       # 入口点和主界面
│   │   │   │   ├── EditorActivity.java     # 主图片编辑界面
│   │   │   │   ├── CropActivity.java       # 图片裁剪功能
│   │   │   │   ├── StitchActivity.java     # 图片拼接功能
│   │   │   │   ├── utils/                  # 工具类
│   │   │   │   └── view/                   # 自定义视图
│   │   │   └── res/                        # 资源文件（布局、图片等）
│   │   └── build.gradle                    # 模块级构建配置
├── build.gradle                            # 项目级构建配置
└── settings.gradle                         # 项目设置
```

## 核心组件

### 活动界面

- **MainActivity**：应用程序入口点，包含从相册选择图片、相机拍摄、图片拼接和切换夜间模式的选项
- **EditorActivity**：主图片编辑界面，用户可以应用滤镜、裁剪图片、添加文字以及保存/分享编辑后的图片
- **CropActivity**：专用的图片裁剪界面，带有可视化的裁剪区域选择功能
- **StitchActivity**：图片拼接界面，支持多种布局（水平、垂直、网格）组合多张图片

### 工具类

- **BitmapUtils**：位图操作助手类，包括采样、旋转、翻转和应用滤镜等功能
- **FileUtils**：用于将图片保存到相册和分享到其他应用的工具
- **PermissionUtils**：处理相机和存储访问的Android运行时权限

### 自定义视图

- **CropImageView**：扩展的ImageView，带有裁剪区域的可视化和操作功能
- **PhotoEditorView**：用于显示具有编辑功能的图片的自定义视图
- **TextOverlayView**：用于在图片上添加和定位文字叠加的视图

## 权限说明

应用需要以下权限：

- `CAMERA`：用于拍摄照片
- `READ_EXTERNAL_STORAGE`：用于访问相册中的照片
- `WRITE_EXTERNAL_STORAGE`：用于保存编辑后的照片
- `READ_MEDIA_IMAGES`：用于在Android 13上访问图片

## 使用的库

- AndroidX AppCompat
- AndroidX Material Components
- AndroidX ConstraintLayout
- AndroidX Activity

## 支持的Android版本

- 最低SDK：API 24（Android 7.0 Nougat）
- 目标SDK：API 36（Android 15）
