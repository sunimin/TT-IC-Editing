package com.example.homework01.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义View，用于在图片上添加和编辑文字
 */
public class TextOverlayView extends View {
    private List<TextElement> mTextElements = new ArrayList<>();
    private Paint mTextPaint;
    private Paint mBorderPaint;
    private TextElement mSelectedElement;
    private float mLastX, mLastY;
    private OnTextOperationListener mListener;

    // 操作模式
    private static final int MODE_NONE = 0;
    private static final int MODE_DRAG = 1;
    private static final int MODE_SCALE = 2;
    private static final int MODE_ROTATE = 3;
    private int mCurrentMode = MODE_NONE;

    public interface OnTextOperationListener {
        void onTextSelected(TextElement element);
        void onTextDeselected();
    }

    public TextOverlayView(Context context) {
        super(context);
        init();
    }

    public TextOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(60);
        mTextPaint.setColor(Color.WHITE);

        mBorderPaint = new Paint();
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setColor(Color.BLUE);
        mBorderPaint.setStrokeWidth(3);
    }

    /**
     * 添加文字元素
     */
    public void addTextElement(String text, float x, float y) {
        TextElement element = new TextElement(text, x, y);
        mTextElements.add(element);
        invalidate();
    }

    /**
     * 添加文字元素（带样式）
     */
    public void addTextElement(String text, float x, float y, int color, float size) {
        TextElement element = new TextElement(text, x, y, color, size);
        mTextElements.add(element);
        invalidate();
    }

    /**
     * 移除选中的文字元素
     */
    public void removeSelectedElement() {
        if (mSelectedElement != null) {
            mTextElements.remove(mSelectedElement);
            mSelectedElement = null;
            if (mListener != null) {
                mListener.onTextDeselected();
            }
            invalidate();
        }
    }

    /**
     * 获取选中的文字元素
     */
    public TextElement getSelectedElement() {
        return mSelectedElement;
    }

    /**
     * 设置文字操作监听器
     */
    public void setOnTextOperationListener(OnTextOperationListener listener) {
        mListener = listener;
    }

    /**
     * 获取所有文字元素
     */
    public List<TextElement> getTextElements() {
        return mTextElements;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 绘制所有文字元素
        for (TextElement element : mTextElements) {
            element.draw(canvas, mTextPaint, mSelectedElement == element, mBorderPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastX = x;
                mLastY = y;
                // 检查是否点击到某个文字元素
                mSelectedElement = findTextElementAt(x, y);
                if (mSelectedElement != null) {
                    mCurrentMode = getTouchMode(x, y);
                    if (mListener != null) {
                        mListener.onTextSelected(mSelectedElement);
                    }
                } else {
                    if (mListener != null) {
                        mListener.onTextDeselected();
                    }
                }
                invalidate();
                return true;

            case MotionEvent.ACTION_MOVE:
                if (mSelectedElement != null) {
                    float dx = x - mLastX;
                    float dy = y - mLastY;

                    switch (mCurrentMode) {
                        case MODE_DRAG:
                            mSelectedElement.move(dx, dy);
                            break;
                        case MODE_SCALE:
                            mSelectedElement.scale(1 + dy * 0.01f); // 根据垂直移动调整大小
                            break;
                        case MODE_ROTATE:
                            // 计算旋转角度
                            float centerX = mSelectedElement.getX();
                            float centerY = mSelectedElement.getY();
                            double angle1 = Math.atan2(mLastY - centerY, mLastX - centerX);
                            double angle2 = Math.atan2(y - centerY, x - centerX);
                            float rotationDelta = (float) Math.toDegrees(angle2 - angle1);
                            mSelectedElement.rotate(rotationDelta);
                            break;
                    }

                    mLastX = x;
                    mLastY = y;
                    invalidate();
                }
                return true;

            case MotionEvent.ACTION_UP:
                mCurrentMode = MODE_NONE;
                return true;
        }

        return super.onTouchEvent(event);
    }

    /**
     * 查找点击位置的文字元素
     */
    private TextElement findTextElementAt(float x, float y) {
        // 从后往前遍历，优先选择后面添加的元素（在上层）
        for (int i = mTextElements.size() - 1; i >= 0; i--) {
            TextElement element = mTextElements.get(i);
            if (element.contains(x, y)) {
                return element;
            }
        }
        return null;
    }

    /**
     * 获取触摸模式
     */
    private int getTouchMode(float x, float y) {
        if (mSelectedElement != null) {
            float distance = (float) Math.sqrt(Math.pow(x - mSelectedElement.getX(), 2) + 
                                             Math.pow(y - mSelectedElement.getY(), 2));
            // 如果点击在文字元素附近，则为旋转模式
            if (distance > mSelectedElement.getBounds().width() / 2) {
                return MODE_ROTATE;
            }
            // 如果点击在右下角，则为缩放模式
            else if (x > mSelectedElement.getBounds().right - 50 && 
                     y > mSelectedElement.getBounds().bottom - 50) {
                return MODE_SCALE;
            }
            // 默认为拖拽模式
            else {
                return MODE_DRAG;
            }
        }
        return MODE_NONE;
    }

    /**
     * 文字元素类
     */
    public static class TextElement {
        private String mText;
        private float mX, mY;
        private int mColor = Color.WHITE;
        private float mSize = 60;
        private float mRotation = 0;
        private float mScale = 1.0f;
        private Rect mBounds = new Rect();

        public TextElement(String text, float x, float y) {
            mText = text;
            mX = x;
            mY = y;
        }

        public TextElement(String text, float x, float y, int color, float size) {
            mText = text;
            mX = x;
            mY = y;
            mColor = color;
            mSize = size;
        }

        public void draw(Canvas canvas, Paint paint, boolean isSelected, Paint borderPaint) {
            paint.setColor(mColor);
            paint.setTextSize(mSize * mScale);
            
            // 保存画布状态
            canvas.save();
            
            // 移动到文字中心点并旋转
            canvas.rotate(mRotation, mX, mY);
            
            // 绘制文字
            canvas.drawText(mText, mX, mY, paint);
            
            // 计算文字边界
            paint.getTextBounds(mText, 0, mText.length(), mBounds);
            mBounds.offset((int) (mX - mBounds.width() / 2), (int) (mY - mBounds.height() / 2));
            
            // 如果选中，绘制边框
            if (isSelected) {
                // 绘制文字边界
                canvas.drawRect(mBounds, borderPaint);
                
                // 绘制缩放控制点（右下角）
                canvas.drawCircle(mBounds.right, mBounds.bottom, 20, borderPaint);
                
                // 绘制旋转控制点（上方）
                canvas.drawCircle(mX, mBounds.top - 50, 20, borderPaint);
            }
            
            // 恢复画布状态
            canvas.restore();
        }

        public boolean contains(float x, float y) {
            return mBounds.contains((int)x, (int)y);
        }

        public void move(float dx, float dy) {
            mX += dx;
            mY += dy;
            // 更新边界
            mBounds.offset((int)dx, (int)dy);
        }

        public void scale(float factor) {
            mScale *= factor;
            // 限制缩放范围
            if (mScale < 0.5f) mScale = 0.5f;
            if (mScale > 3.0f) mScale = 3.0f;
        }

        public void rotate(float degrees) {
            mRotation += degrees;
            // 规范化角度到0-360度
            mRotation = mRotation % 360;
            if (mRotation < 0) mRotation += 360;
        }

        // Getters and Setters
        public String getText() { return mText; }
        public void setText(String text) { mText = text; }
        
        public float getX() { return mX; }
        public void setX(float x) { mX = x; }
        
        public float getY() { return mY; }
        public void setY(float y) { mY = y; }
        
        public int getColor() { return mColor; }
        public void setColor(int color) { mColor = color; }
        
        public float getSize() { return mSize; }
        public void setSize(float size) { mSize = size; }
        
        public float getRotation() { return mRotation; }
        public void setRotation(float rotation) { mRotation = rotation; }
        
        public float getScale() { return mScale; }
        public void setScale(float scale) { mScale = scale; }
        
        public Rect getBounds() { return mBounds; }
    }
}