package com.example.homework01.view;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

public class CropImageView extends androidx.appcompat.widget.AppCompatImageView {
    private RectF mCropRect;
    private Paint mBorderPaint;
    private Paint mAreaPaint;
    private float mLastX, mLastY;
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int MOVE_LEFT = 2;
    private static final int MOVE_TOP = 3;
    private static final int MOVE_RIGHT = 4;
    private static final int MOVE_BOTTOM = 5;
    private static final int MOVE_INSIDE = 6;
    private int mMode = NONE;
    private float mBorderWidth = 30f;
    private OnCropRectChangedListener mListener;
    
    public interface OnCropRectChangedListener {
        void onCropRectChanged(RectF cropRect);
    }
    
    public CropImageView(android.content.Context context) {
        super(context);
        init();
    }
    
    public CropImageView(android.content.Context context, android.util.AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
        mBorderPaint = new Paint();
        mBorderPaint.setColor(Color.WHITE);
        mBorderPaint.setStrokeWidth(3f);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        
        mAreaPaint = new Paint();
        mAreaPaint.setColor(Color.parseColor("#80FFFFFF")); // 半透明白色
        mAreaPaint.setStyle(Paint.Style.FILL);
        
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return handleTouchEvent(event);
            }
        });
    }
    
    public void setCropRect(RectF cropRect) {
        mCropRect = cropRect;
        invalidate();
    }
    
    public void setOnCropRectChangedListener(OnCropRectChangedListener listener) {
        mListener = listener;
    }
    
    private boolean handleTouchEvent(MotionEvent event) {
        if (mCropRect == null) return false;
        
        float x = event.getX();
        float y = event.getY();
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastX = x;
                mLastY = y;
                mMode = getTouchMode(x, y);
                return true;
                
            case MotionEvent.ACTION_MOVE:
                float dx = x - mLastX;
                float dy = y - mLastY;
                
                switch (mMode) {
                    case MOVE_INSIDE:
                        moveCropRect(dx, dy);
                        break;
                    case MOVE_LEFT:
                        resizeCropRectLeft(dx);
                        break;
                    case MOVE_TOP:
                        resizeCropRectTop(dy);
                        break;
                    case MOVE_RIGHT:
                        resizeCropRectRight(dx);
                        break;
                    case MOVE_BOTTOM:
                        resizeCropRectBottom(dy);
                        break;
                }
                
                mLastX = x;
                mLastY = y;
                
                // 通知裁剪区域已改变
                if (mListener != null) {
                    mListener.onCropRectChanged(mCropRect);
                }
                
                invalidate();
                return true;
                
            case MotionEvent.ACTION_UP:
                mMode = NONE;
                return true;
        }
        
        return false;
    }
    
    private int getTouchMode(float x, float y) {
        if (mCropRect == null) return NONE;
        
        // 检查是否在裁剪框内部
        if (x > mCropRect.left + mBorderWidth && 
            x < mCropRect.right - mBorderWidth && 
            y > mCropRect.top + mBorderWidth && 
            y < mCropRect.bottom - mBorderWidth) {
            return MOVE_INSIDE;
        }
        
        // 检查是否在边界上
        if (Math.abs(x - mCropRect.left) < mBorderWidth && y > mCropRect.top && y < mCropRect.bottom) {
            return MOVE_LEFT;
        }
        
        if (Math.abs(x - mCropRect.right) < mBorderWidth && y > mCropRect.top && y < mCropRect.bottom) {
            return MOVE_RIGHT;
        }
        
        if (Math.abs(y - mCropRect.top) < mBorderWidth && x > mCropRect.left && x < mCropRect.right) {
            return MOVE_TOP;
        }
        
        if (Math.abs(y - mCropRect.bottom) < mBorderWidth && x > mCropRect.left && x < mCropRect.right) {
            return MOVE_BOTTOM;
        }
        
        return NONE;
    }
    
    private void moveCropRect(float dx, float dy) {
        if (mCropRect == null) return;
        
        float newLeft = mCropRect.left + dx;
        float newTop = mCropRect.top + dy;
        float newRight = mCropRect.right + dx;
        float newBottom = mCropRect.bottom + dy;
        
        // 边界检查
        if (newLeft < 0) {
            newLeft = 0;
            newRight = mCropRect.width();
        }
        
        if (newTop < 0) {
            newTop = 0;
            newBottom = mCropRect.height();
        }
        
        if (newRight > getWidth()) {
            newRight = getWidth();
            newLeft = getWidth() - mCropRect.width();
        }
        
        if (newBottom > getHeight()) {
            newBottom = getHeight();
            newTop = getHeight() - mCropRect.height();
        }
        
        mCropRect.set(newLeft, newTop, newRight, newBottom);
    }
    
    private void resizeCropRectLeft(float dx) {
        if (mCropRect == null) return;
        
        float newLeft = mCropRect.left + dx;
        // 限制最小宽度
        if (mCropRect.right - newLeft < 50) {
            newLeft = mCropRect.right - 50;
        }
        // 边界检查
        if (newLeft < 0) {
            newLeft = 0;
        }
        if (newLeft > mCropRect.right - 50) {
            newLeft = mCropRect.right - 50;
        }
        mCropRect.left = newLeft;
    }
    
    private void resizeCropRectTop(float dy) {
        if (mCropRect == null) return;
        
        float newTop = mCropRect.top + dy;
        // 限制最小高度
        if (mCropRect.bottom - newTop < 50) {
            newTop = mCropRect.bottom - 50;
        }
        // 边界检查
        if (newTop < 0) {
            newTop = 0;
        }
        if (newTop > mCropRect.bottom - 50) {
            newTop = mCropRect.bottom - 50;
        }
        mCropRect.top = newTop;
    }
    
    private void resizeCropRectRight(float dx) {
        if (mCropRect == null) return;
        
        float newRight = mCropRect.right + dx;
        // 限制最小宽度
        if (newRight - mCropRect.left < 50) {
            newRight = mCropRect.left + 50;
        }
        // 边界检查
        if (newRight > getWidth()) {
            newRight = getWidth();
        }
        if (newRight < mCropRect.left + 50) {
            newRight = mCropRect.left + 50;
        }
        mCropRect.right = newRight;
    }
    
    private void resizeCropRectBottom(float dy) {
        if (mCropRect == null) return;
        
        float newBottom = mCropRect.bottom + dy;
        // 限制最小高度
        if (newBottom - mCropRect.top < 50) {
            newBottom = mCropRect.top + 50;
        }
        // 边界检查
        if (newBottom > getHeight()) {
            newBottom = getHeight();
        }
        if (newBottom < mCropRect.top + 50) {
            newBottom = mCropRect.top + 50;
        }
        mCropRect.bottom = newBottom;
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (mCropRect != null) {
            // 绘制裁剪区域以外的半透明遮罩
            float width = getWidth();
            float height = getHeight();
            
            // 上方区域
            canvas.drawRect(0, 0, width, mCropRect.top, mAreaPaint);
            // 下方区域
            canvas.drawRect(0, mCropRect.bottom, width, height, mAreaPaint);
            // 左方区域
            canvas.drawRect(0, mCropRect.top, mCropRect.left, mCropRect.bottom, mAreaPaint);
            // 右方区域
            canvas.drawRect(mCropRect.right, mCropRect.top, width, mCropRect.bottom, mAreaPaint);
            
            // 绘制裁剪框边框
            canvas.drawRect(mCropRect, mBorderPaint);
            
            // 绘制裁剪框的角标
            float cornerLength = 30f;
            float cornerWidth = 8f;
            
            Paint cornerPaint = new Paint();
            cornerPaint.setColor(Color.WHITE);
            cornerPaint.setStrokeWidth(cornerWidth);
            cornerPaint.setStyle(Paint.Style.STROKE);
            
            // 左上角
            canvas.drawLine(mCropRect.left, mCropRect.top, mCropRect.left + cornerLength, mCropRect.top, cornerPaint);
            canvas.drawLine(mCropRect.left, mCropRect.top, mCropRect.left, mCropRect.top + cornerLength, cornerPaint);
            
            // 右上角
            canvas.drawLine(mCropRect.right - cornerLength, mCropRect.top, mCropRect.right, mCropRect.top, cornerPaint);
            canvas.drawLine(mCropRect.right, mCropRect.top, mCropRect.right, mCropRect.top + cornerLength, cornerPaint);
            
            // 左下角
            canvas.drawLine(mCropRect.left, mCropRect.bottom - cornerLength, mCropRect.left, mCropRect.bottom, cornerPaint);
            canvas.drawLine(mCropRect.left, mCropRect.bottom, mCropRect.left + cornerLength, mCropRect.bottom, cornerPaint);
            
            // 右下角
            canvas.drawLine(mCropRect.right - cornerLength, mCropRect.bottom, mCropRect.right, mCropRect.bottom, cornerPaint);
            canvas.drawLine(mCropRect.right, mCropRect.bottom - cornerLength, mCropRect.right, mCropRect.bottom, cornerPaint);
        }
    }
}