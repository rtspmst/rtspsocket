package cn.jzvd.seekbar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.SeekBar;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;

import cn.jzvd.R;

public class SignSeekBar extends SeekBar {

    @Retention(RetentionPolicy.SOURCE)
    public @interface TextPosition {
    }

    private float mMin; // min
    private float mMax; // max
    private float mProgress; // real time value
    private int mTrackSize; // 右轨高度（在拇指右侧）
    private int mSecondTrackSize; //左轨道高度（拇指左侧）
    private int mThumbRadius; //拇指半径
    private int mThumbRadiusOnDragging; // 拖动时的拇指半径
    private int mTrackColor; //标记颜色
    private int mSecondTrackColor; //左轨的颜色 走过的颜色
    private int mThumbColor; //拇指的颜色
    private int mSectionCount = 1000; //整体进度的份额（最大值-最小值）
    @TextPosition
    private boolean isTouchToSeek; // 触摸轨道上的任何地方以快速寻找
    private boolean isSeekBySection; // 按部分搜索，进度可能不是线性的
    private long mAnimDuration; // 动画持续时间

    private float mDelta; // max - min
    private float mSectionValue; // (mDelta / mSectionCount)
    private float mThumbCenterX; //拇指中心的 X 坐标
    private float mTrackLength; //整个轨迹的像素长度
    private float mSectionOffset; // 一节的像素长度
    private boolean isThumbOnDragging; // 是拇指拖还是不拖
    private boolean triggerSeekBySection;

    private OnSeekBarChangeListener1 mProgressListener; //进度变化的听众
    private float mLeft; // 轨道左侧和视图左侧之间的空间
    private float mRight; // 轨道右侧和视图左侧之间的空间
    private Paint mPaint;

    private float mPreSecValue; // 上一节值
    private SignConfigBuilder mConfigBuilder; // 配置属性
    private float mThumbBgAlpha; //  拇指阴影的 alpha
    private float mThumbRatio; // 拇指阴影比例
    private boolean isShowThumbShadow;

    public SignSeekBar(Context context) {
        this(context, null);
    }

    public SignSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SignSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SignSeekBar, defStyleAttr, 0);
        mMin = a.getFloat(R.styleable.SignSeekBar_ssb_min, 0.0f);
        mMax = a.getFloat(R.styleable.SignSeekBar_ssb_max, 1000.0f);
        mProgress = a.getFloat(R.styleable.SignSeekBar_ssb_progress, mMin);
        mTrackSize = a.getDimensionPixelSize(R.styleable.SignSeekBar_ssb_track_size, SignUtils.dp2px(2));
        mSecondTrackSize = a.getDimensionPixelSize(R.styleable.SignSeekBar_ssb_second_track_size, mTrackSize + SignUtils.dp2px(2));
        mThumbRadius = a.getDimensionPixelSize(R.styleable.SignSeekBar_ssb_thumb_radius, mSecondTrackSize + SignUtils.dp2px(2));
        mThumbRadiusOnDragging = a.getDimensionPixelSize(R.styleable.SignSeekBar_ssb_thumb_radius, mSecondTrackSize);

        isSeekBySection = a.getBoolean(R.styleable.SignSeekBar_ssb_seek_by_section, false);
        int duration = a.getInteger(R.styleable.SignSeekBar_ssb_anim_duration, -1);
        mAnimDuration = duration < 0 ? 200 : duration;
        isTouchToSeek = a.getBoolean(R.styleable.SignSeekBar_ssb_touch_to_seek, false);
        mThumbBgAlpha = a.getFloat(R.styleable.SignSeekBar_ssb_thumb_bg_alpha, 0.2f);
        mThumbRatio = a.getFloat(R.styleable.SignSeekBar_ssb_thumb_ratio, 0.7f);
        isShowThumbShadow = a.getBoolean(R.styleable.SignSeekBar_ssb_show_thumb_shadow, false);

        mTrackColor = a.getColor(R.styleable.SignSeekBar_ssb_track_color, context.getResources().getColor(R.color.colorB));
        mSecondTrackColor = a.getColor(R.styleable.SignSeekBar_ssb_second_track_color, context.getResources().getColor(R.color.colorA));
        mThumbColor = a.getColor(R.styleable.SignSeekBar_ssb_thumb_color, context.getResources().getColor(R.color.colorA));

        a.recycle();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setTextAlign(Paint.Align.CENTER);

        initConfigByPriority();
    }


    private void initConfigByPriority() {
        if (mMin == mMax) {
            mMin = 0.0f;
            mMax = 1000.0f;
        }
        if (mMin > mMax) {
            float tmp = mMax;
            mMax = mMin;
            mMin = tmp;
        }
        if (mProgress < mMin) {
            mProgress = mMin;
        }
        if (mProgress > mMax) {
            mProgress = mMax;
        }
        if (mSecondTrackSize < mTrackSize) {
            mSecondTrackSize = mTrackSize + SignUtils.dp2px(2);
        }
        if (mThumbRadius <= mSecondTrackSize) {
            mThumbRadius = mSecondTrackSize + SignUtils.dp2px(2);
        }
        if (mThumbRadiusOnDragging <= mSecondTrackSize) {
            mThumbRadiusOnDragging = mSecondTrackSize * 2;
        }

        mDelta = mMax - mMin;

        mSectionValue = mDelta / mSectionCount;

        if (isSeekBySection) {
            mPreSecValue = mMin;
            if (mProgress != mMin) {
                mPreSecValue = mSectionValue;
            }
            isTouchToSeek = false;
        }
        setProgress1(mProgress);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int height = mThumbRadiusOnDragging * 2; // 默认高度为拖动时thumb圆的直径
        setMeasuredDimension(resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec), height);
        mLeft = getPaddingLeft() + mThumbRadiusOnDragging;
        mRight = getMeasuredWidth() - getPaddingRight() - mThumbRadiusOnDragging;
        mTrackLength = mRight - mLeft;
        mSectionOffset = mTrackLength * 1f / mSectionCount;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float xLeft = mLeft;
        float xRight = mRight;

        drawMark(canvas, xLeft);

        if (!isThumbOnDragging) {
            mThumbCenterX = mTrackLength / mDelta * (mProgress - mMin) + xLeft;
        }

        // draw track
        mPaint.setColor(mSecondTrackColor);
        mPaint.setStrokeWidth(mSecondTrackSize);
        canvas.drawLine(xLeft, getHeight() / 2, mThumbCenterX, getHeight() / 2, mPaint);

        // draw second track
        mPaint.setColor(mTrackColor);
        mPaint.setStrokeWidth(mTrackSize);
        canvas.drawLine(mThumbCenterX, getHeight() / 2, xRight, getHeight() / 2, mPaint);

        // draw thumb
        mPaint.setColor(mThumbColor);
        //draw thumb shadow
        if (isShowThumbShadow) {
            canvas.drawCircle(mThumbCenterX, getHeight() / 2, isThumbOnDragging ? mThumbRadiusOnDragging * mThumbRatio : mThumbRadius * mThumbRatio, mPaint);
            mPaint.setColor(getColorWithAlpha(mThumbColor, mThumbBgAlpha));
        }

        canvas.drawCircle(mThumbCenterX, getHeight() / 2, isThumbOnDragging ? mThumbRadiusOnDragging : mThumbRadius, mPaint);
    }

    ArrayList<Long> arrayList;

    public void setMarkDate(ArrayList<Long> array) {

        arrayList = new ArrayList<>();

        if (array != null && array.size() > 0) {

            for (int i = 0; i < array.size(); i++) {

                if (array.get(i) != 0) {
                    arrayList.add(array.get(i));
                }

            }
        }

        Log.e("TAG", "进度条:占有的千分比集合 ============" + Arrays.toString(arrayList.toArray()));

    }


    //draw mark  画标记
    private void drawMark(Canvas canvas, float xLeft) {

        float r = (mThumbRadiusOnDragging - SignUtils.dp2px(2)) / 2f;
        float junction = mTrackLength / mDelta * Math.abs(mProgress - mMin) + mLeft; // 交汇点

        float x_;

        int temporary = 0;

        for (int i = 0; i < arrayList.size(); i++) {

            for (int j = temporary; j < mSectionCount; j++) {

                if (j == arrayList.get(i)) {

                    temporary = j;

                    x_ = xLeft + j * mSectionOffset;
                    mPaint.setColor(x_ <= junction ? mSecondTrackColor : mTrackColor);
                    //画标记
                    canvas.drawCircle(x_, getHeight() / 2, r, mPaint);

                    break;
                }

            }

        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        post(new Runnable() {
            @Override
            public void run() {
                requestLayout();
            }
        });
    }

    float dx;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:

                if (mProgressListener != null) {
                    mProgressListener.MY_ONTOUCHEVENT_ACTION_DOWN(true);
                }

                getParent().requestDisallowInterceptTouchEvent(true);

                isThumbOnDragging = isThumbTouched(event);
                if (isThumbOnDragging) {
                    if (isSeekBySection && !triggerSeekBySection) {
                        triggerSeekBySection = true;
                    }
                    invalidate();
                } else if (isTouchToSeek && isTrackTouched(event)) {
                    isThumbOnDragging = true;
                    mThumbCenterX = event.getX();
                    if (mThumbCenterX < mLeft) {
                        mThumbCenterX = mLeft;
                    }
                    if (mThumbCenterX > mRight) {
                        mThumbCenterX = mRight;
                    }
                    mProgress = (mThumbCenterX - mLeft) * mDelta / mTrackLength + mMin;
                    invalidate();
                }

                dx = mThumbCenterX - event.getX();

                break;
            case MotionEvent.ACTION_MOVE:
                if (isThumbOnDragging) {
                    mThumbCenterX = event.getX() + dx;
                    if (mThumbCenterX < mLeft) {
                        mThumbCenterX = mLeft;
                    }
                    if (mThumbCenterX > mRight) {
                        mThumbCenterX = mRight;
                    }
                    mProgress = (mThumbCenterX - mLeft) * mDelta / mTrackLength + mMin;
                    invalidate();

                    if (mProgressListener != null) {
                        mProgressListener.onProgressChanged1(this, (int) getProgressFloat(), true);
                    }
                }

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                getParent().requestDisallowInterceptTouchEvent(false);

                if (isThumbOnDragging || isTouchToSeek) {
                    animate()
                            .setDuration(mAnimDuration)
                            .setStartDelay(!isThumbOnDragging && isTouchToSeek ? 300 : 0)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    isThumbOnDragging = false;
                                    invalidate();

                                    if (mProgressListener != null) {
                                        mProgressListener.onProgressChanged1(SignSeekBar.this,
                                                (int) getProgressFloat(), true);
                                    }
                                }

                                @Override
                                public void onAnimationCancel(Animator animation) {
                                    isThumbOnDragging = false;
                                    invalidate();
                                }
                            })
                            .start();
                }

                if (mProgressListener != null) {
                    mProgressListener.onStartTrackingTouch1(this);
                    mProgressListener.onStopTrackingTouch1(this);
                }
                break;
            default:
                break;
        }

        return isThumbOnDragging || isTouchToSeek || super.onTouchEvent(event);
    }

    /**
     * 计算新的透明度颜色
     *
     * @param color 旧颜色
     * @param ratio 透明度系数
     */
    public int getColorWithAlpha(int color, float ratio) {
        int newColor = 0;
        int alpha = Math.round(Color.alpha(color) * ratio);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        newColor = Color.argb(alpha, r, g, b);
        return newColor;
    }

    /**
     * 检测有效的拇指触摸
     */
    private boolean isThumbTouched(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }
        float mCircleR = isThumbOnDragging ? mThumbRadiusOnDragging : mThumbRadius;
        float x = mTrackLength / mDelta * (mProgress - mMin) + mLeft;
        float y = getMeasuredHeight() / 2f;
        return (event.getX() - x) * (event.getX() - x) + (event.getY() - y) * (event.getY() - y)
                <= (mLeft + mCircleR) * (mLeft + mCircleR);
    }

    /**
     * 检测轨道的有效接触
     */
    private boolean isTrackTouched(MotionEvent event) {
        return isEnabled() && event.getX() >= getPaddingLeft() && event.getX() <= getMeasuredWidth() - getPaddingRight()
                && event.getY() >= getPaddingTop() && event.getY() <= getMeasuredHeight() - getPaddingBottom();
    }

    public void setProgress1(float progress) {
        mProgress = progress;
        if (mProgressListener != null) {
            mProgressListener.onProgressChanged1(this, (int) getProgressFloat(), false);
        }
        postInvalidate();
    }

    @Override
    public int getProgress() {
        if (isSeekBySection && triggerSeekBySection) {
            float half = mSectionValue / 2;

            if (mProgress >= mPreSecValue) { // increasing
                if (mProgress >= mPreSecValue + half) {
                    mPreSecValue += mSectionValue;
                    return Math.round(mPreSecValue);
                } else {
                    return Math.round(mPreSecValue);
                }
            } else { // reducing
                if (mProgress >= mPreSecValue - half) {
                    return Math.round(mPreSecValue);
                } else {
                    mPreSecValue -= mSectionValue;
                    return Math.round(mPreSecValue);
                }
            }
        }

        return Math.round(mProgress);
    }

    public float getProgressFloat() {
        return formatFloat(mProgress);
    }

    void config(SignConfigBuilder builder) {
        mMin = builder.min;
        mMax = builder.max;
        mProgress = builder.progress;
        mTrackSize = builder.trackSize;
        mSecondTrackSize = builder.secondTrackSize;
        mThumbRadius = builder.thumbRadius;
        mThumbRadiusOnDragging = builder.thumbRadiusOnDragging;
        mTrackColor = builder.trackColor;
        mSecondTrackColor = builder.secondTrackColor;
        mThumbColor = builder.thumbColor;
        mSectionCount = builder.sectionCount;
        mAnimDuration = builder.animDuration;
        isTouchToSeek = builder.touchToSeek;
        isSeekBySection = builder.seekBySection;
        mThumbBgAlpha = mConfigBuilder.thumbBgAlpha;
        mThumbRatio = mConfigBuilder.thumbRatio;
        isShowThumbShadow = mConfigBuilder.showThumbShadow;

        initConfigByPriority();

        if (mProgressListener != null) {
            mProgressListener.onProgressChanged1(this, (int) getProgressFloat(), false);
        }

        mConfigBuilder = null;

        requestLayout();
    }


    private float formatFloat(float value) {
        BigDecimal bigDecimal = BigDecimal.valueOf(value);
        return bigDecimal.setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
    }

    public void setOnSeekBarChangeListener1(OnSeekBarChangeListener1 l) {
        mProgressListener = l;
    }

    //当进度级别已更改时通知客户端的回调
    public interface OnSeekBarChangeListener1 {

        //通知进度级别已更改
        void onProgressChanged1(SeekBar seekBar, int progress, boolean fromUser);

        //用户已开始触摸手势的通知
        void onStartTrackingTouch1(SeekBar seekBar);

        //用户已完成触摸手势的通知
        void onStopTrackingTouch1(SeekBar seekBar);

        void MY_ONTOUCHEVENT_ACTION_DOWN(boolean isOnTouch);
    }
}
