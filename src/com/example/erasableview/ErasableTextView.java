
package com.example.erasableview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * @ClassName: TextRubbler
 * @Description: 可擦除的textview, 类似于刮刮乐的效果. 设计思路 : 在文字的上面绘制一层遮罩，当用户在该区域涂抹时，
 *               会在该遮罩层上绘制一条曲线path, 此时该path下的文字就会显示出来，与mPaint设置的setXfermode有关.
 * @author Honghui He
 */
public class ErasableTextView extends TextView {

    /**
     * 
     */
    private Canvas mCanvas = null;
    /**
     * 
     */
    private Paint mPaint = null;
    /**
     * 整个用于遮罩的bitmap, 将文字遮住.当用户涂抹该view时，绘制一条透明的path到上面，这样path下的文字就显示出来.
     */
    private Bitmap mErasableBitmap = null;

    /**
     * 绘制可擦除范围内的路径，用户手指绘制的擦除路径
     */
    private Path mFingerPath;
    /**
     * 
     */
    private float mX;
    private float mY;

    /**
     * 是否绘制颜色背景
     */
    private boolean canErase = false;

    /**
     * 触摸公差
     */
    private float mTouchTolerance;

    /**
     * 遮罩层的宽度
     */
    private int mErasableWidth = 0;
    /**
     * 遮罩层的高度
     */
    private int mErasableHeight = 0;
    /**
     * 擦除时的画笔大小
     */
    private int mStokeWidth = 8;
    /**
     * 默认可擦除颜色的背景为gray
     */
    private int mErasableColor = Color.GRAY;

    private final String TAG = this.getClass().getSimpleName();

    /**
     * @Title: ErasableTextView
     * @Description: ErasableTextView Constructor
     * @param context
     */
    public ErasableTextView(Context context) {
        super(context);

    }

    /**
     * @Title: ErasableTextView
     * @Description: ErasableTextView Constructor
     * @param context
     * @param attrs
     * @param defStyle
     */
    public ErasableTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * @Title: ErasableTextView
     * @Description: ErasableTextView Constructor
     * @param context
     * @param attrs
     */
    public ErasableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * (非 Javadoc)
     * 
     * @Title: onDraw
     * @Description: 首先绘制文本，再绘制遮罩层
     * @param canvas
     * @see android.widget.TextView#onDraw(android.graphics.Canvas)
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 如果是可擦除状态则绘制背景色
        if (canErase && mCanvas != null) {
            // 绘制用户滑动的路径到擦除层, 即用户已经涂掉的区域
            mCanvas.drawPath(mFingerPath, mPaint);
            // 绘制整个擦除层
            canvas.drawBitmap(mErasableBitmap, 0, 0, null);
        }
    }

    /**
     * @Title: setErasable
     * @Description: 设置是否为可擦除textview
     * @param enable
     * @throws
     */
    public void setErasable(boolean enable) {
        canErase = enable;

        // 初始化画笔
        initPaint();

        // 创建路径
        mFingerPath = new Path();

        // 检测宽度和高度
        checkErasableRect();

        // 初始化画布
        initCanvas();
    }

    /**
     * @Title: initPaint
     * @Description: 初始化画笔
     * @throws
     */
    private void initPaint() {
        mPaint = new Paint();
        // 设置透明度为0
        // 设置为透明的画笔，因此当用户用手涂时会绘制透明的路径，然后被遮住的文字就会显示出来
        // 与setXfermode向协作即可实现擦除效果.
        mPaint.setAlpha(0);
        //
        mPaint.setColor(Color.BLACK);
        // 可以通过修改Paint的Xfermode来影响在Canvas已有的图像上面绘制新的颜色模式, 这里设置为显示新绘制的颜色
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));

        // 设置反锯齿
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.STROKE);
        // 设置圆角
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        // 设置画笔的笔尖风格
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        // 设置画笔stoke宽度
        mPaint.setStrokeWidth(mStokeWidth);
    }

    /**
     * @Title: initCanvas
     * @Description: 初始化画布
     * @throws
     */
    private void initCanvas() {
        try {
            // 以自身的宽和高创建bitmap对象, widht, height < 0则抛出异常
            mErasableBitmap = Bitmap.createBitmap(mErasableWidth,
                    mErasableHeight, Config.ARGB_8888);

            // 以mBitmap创建画布
            mCanvas = new Canvas(mErasableBitmap);
            // 背景色
            mCanvas.drawColor(mErasableColor);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "请在调用setErasable之前\n\n设置可擦除的宽度和高度, 调用方法为setErasableWidth, setErasableHeight");
            e.printStackTrace();
        }

    }

    /**
     * @Title: checkErasableRect
     * @Description: 检测开发者在xml设置的宽度和高度.
     *               如果不是设置确切的高度值，而是设置match_parent等,则width和height都为-1
     * @throws
     */
    private void checkErasableRect() {
        int width = getLayoutParams().width;
        int height = getLayoutParams().height;
        if (width > 0 && height > 0) {
            mErasableWidth = width;
            mErasableHeight = height;
        }
    }

    /**
     * 获取 canErase
     * 
     * @return 返回 canErase
     */
    public boolean isErasable() {
        return canErase;
    }

    /**
     * @Title: setStokeWidth
     * @Description:
     * @throws
     */
    public void setStokeWidth(int stokeWidth) {
        mStokeWidth = stokeWidth;
    }

    public int getStokeWidth() {
        return mStokeWidth;
    }

    /**
     * @Title: setErasableColor
     * @Description: 设置可擦除的背景色
     * @param color
     * @throws
     */
    public void setErasableColor(int color) {
        mErasableColor = color;
    }

    /**
     * @Title: getErasableColor
     * @Description: 获取可擦除的背景色
     * @return
     * @throws
     */
    public int getErasableColor() {
        return mErasableColor;
    }

    /**
     * @Title: setTouchTolerance
     * @Description: 设置滑动多大距离为有效滑动
     * @param tolerance
     * @throws
     */
    public void setTouchTolerance(float tolerance) {
        mTouchTolerance = tolerance;
    }

    /**
     * @Title: getTouchTolerance
     * @Description:
     * @return
     * @throws
     */
    public float getTouchTolerance() {
        return mTouchTolerance;
    }

    /**
     * 获取 mErasableWidth
     * 
     * @return 返回 mErasableWidth
     */
    public int getErasableWidth() {
        return mErasableWidth;
    }

    /**
     * 设置 mErasableWidth
     * 
     * @param 对mErasableWidth进行赋值
     */
    public void setErasableWidth(int width) {
        this.mErasableWidth = width;
    }

    /**
     * 获取 mErasableHeight
     * 
     * @return 返回 mErasableHeight
     */
    public int getErasableHeight() {
        return mErasableHeight;
    }

    /**
     * 设置 mErasableHeight
     * 
     * @param 对mErasableHeight进行赋值
     */
    public void setErasableHeight(int height) {
        this.mErasableHeight = height;
    }

    /**
     * (非 Javadoc)
     * 
     * @Title: onTouchEvent
     * @Description: 触摸事件
     * @param event
     * @return
     * @see android.widget.TextView#onTouchEvent(android.view.MotionEvent)
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!canErase) {
            return true;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: // 手指按下
                touchDown(event.getX(), event.getY());
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE: // 移动
                touchMove(event.getX(), event.getY());
                invalidate();
                break;
            case MotionEvent.ACTION_UP: // 抬起
                touchUp(event.getX(), event.getY());
                invalidate();
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * @Title: touchDown
     * @Description:
     * @param x
     * @param y
     * @throws
     */
    private void touchDown(float x, float y) {
        mFingerPath.reset();
        mFingerPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    /**
     * @Title: touchMove
     * @Description:
     * @param x
     * @param y
     * @throws
     */
    private void touchMove(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= mTouchTolerance || dy >= mTouchTolerance) {
            // quadTo这个方法将一条线段变成一个曲线
            mFingerPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }

    }

    /**
     * @Title: touchUp
     * @Description:
     * @param x
     * @param y
     * @throws
     */
    private void touchUp(float x, float y) {
        mFingerPath.lineTo(x, y);
        mCanvas.drawPath(mFingerPath, mPaint);
        mFingerPath.reset();
    }

}
