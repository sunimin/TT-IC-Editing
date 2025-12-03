package com.example.homework01.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

public class PhotoEditorView extends View {
    private Bitmap mBitmap;
    private Matrix mMatrix = new Matrix();
    private Matrix mSavedMatrix = new Matrix();
    private ScaleGestureDetector mScaleDetector;
    private GestureDetector mGestureDetector;
    private float mMinScale = 0.5f;
    private float mMaxScale = 2.0f;
    private PointF mLastTouchPoint = new PointF();
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mMode = NONE;
    private boolean mIsBitmapSet = false;

    public PhotoEditorView(Context context) {
        super(context);
        init(context);
    }

    public PhotoEditorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        mGestureDetector = new GestureDetector(context, new GestureListener());
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
        mIsBitmapSet = true;
        // 使用post方法确保在View布局完成后再进行适配
        post(new Runnable() {
            @Override
            public void run() {
                fitToScreen();
                invalidate();
            }
        });
    }

    // 适配屏幕
    private void fitToScreen() {
        if (mBitmap == null) return;

        float viewWidth = getWidth();
        float viewHeight = getHeight();
        float bitmapWidth = mBitmap.getWidth();
        float bitmapHeight = mBitmap.getHeight();
        
        // 确保View已经完成布局
        if (viewWidth == 0 || viewHeight == 0) return;

        float scaleX = viewWidth / bitmapWidth;
        float scaleY = viewHeight / bitmapHeight;
        float scale = Math.min(scaleX, scaleY);

        mMatrix.reset();
        mMatrix.postScale(scale, scale);
        mMatrix.postTranslate((viewWidth - bitmapWidth * scale) / 2, (viewHeight - bitmapHeight * scale) / 2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mBitmap != null) {
            canvas.drawBitmap(mBitmap, mMatrix, null);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // 当View尺寸改变时，重新适配图片
        if (mIsBitmapSet) {
            fitToScreen();
            invalidate();
        }
    }

    // 缩放监听
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            mMode = ZOOM;
            mSavedMatrix.set(mMatrix);
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();
            float[] values = new float[9];
            mSavedMatrix.getValues(values);
            float currentScale = values[Matrix.MSCALE_X];

            float newScale = currentScale * scaleFactor;
            if (newScale < mMinScale || newScale > mMaxScale) {
                return false;
            }

            mMatrix.set(mSavedMatrix);
            mMatrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());
            invalidate();
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            mMode = NONE;
        }
    }

    // 手势监听
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (mMode == DRAG) {
                mMatrix.postTranslate(-distanceX, -distanceY);
                invalidate();
            }
            return true;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mScaleDetector.onTouchEvent(event);
        mGestureDetector.onTouchEvent(event);

        PointF curr = new PointF(event.getX(), event.getY());

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mSavedMatrix.set(mMatrix);
                mLastTouchPoint.set(curr);
                mMode = DRAG;
                break;

            case MotionEvent.ACTION_MOVE:
                if (mMode == DRAG) {
                    mMatrix.set(mSavedMatrix);
                    mMatrix.postTranslate(curr.x - mLastTouchPoint.x, curr.y - mLastTouchPoint.y);
                    invalidate();
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mMode = NONE;
                break;
        }

        return true;
    }
}