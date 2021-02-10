package com.unionbankph.corporate.app.common.widget.qrgenerator.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;

import com.unionbankph.corporate.R;

public class QrScannerView extends FrameLayout {
    private static final int MOVING_INTERVAL = 16;

    private final ImageView mMovingBar;
    private int mBarMargin;
    private int mMovingSpeed;

    private int mHeight;
    private int mCurPos;

    private final Runnable mMovingTask = new Runnable() {
        @Override
        public void run() {
            if (mHeight == 0) {
                mHeight = getHeight();
            }
            if (mHeight != 0) {
                mCurPos += mMovingSpeed;
                if (mCurPos >= mHeight - mBarMargin) {
                    mCurPos = mBarMargin;
                }
                LayoutParams params = (LayoutParams) mMovingBar.getLayoutParams();
                params.topMargin = mCurPos;
                mMovingBar.setLayoutParams(params);
            }

            postDelayed(this, MOVING_INTERVAL);
        }
    };

    public QrScannerView(Context context) {
        this(context, null, 0);
    }

    public QrScannerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public QrScannerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.getTheme()
                .obtainStyledAttributes(attrs, R.styleable.QrScannerView, 0, 0);

        int sideBg = a.getResourceId(R.styleable.QrScannerView_side_bg, 0);
        int movingBar = a.getResourceId(R.styleable.QrScannerView_moving_bar, 0);
        int barWidth = (int) a.getDimension(R.styleable.QrScannerView_bar_width, 0);
        int barHeight = (int) a.getDimension(R.styleable.QrScannerView_bar_height, 0);
        mMovingSpeed = a.getInteger(R.styleable.QrScannerView_moving_speed, 3);
        mBarMargin = (int) a.getDimension(R.styleable.QrScannerView_bar_margin, 10);
        mCurPos = mBarMargin;
        a.recycle();

        mMovingBar = new ImageView(context);
        addView(mMovingBar);
        LayoutParams params = new LayoutParams(
                barWidth == 0 ? ViewGroup.LayoutParams.WRAP_CONTENT : barWidth,
                barHeight == 0 ? ViewGroup.LayoutParams.WRAP_CONTENT : barHeight);
        params.gravity = Gravity.CENTER_HORIZONTAL;
        params.topMargin = mCurPos;
        mMovingBar.setLayoutParams(params);
        mMovingBar.setImageResource(movingBar);
        setBackgroundResource(sideBg);
    }

    public void setMovingBar(@DrawableRes int movingBar) {
        mMovingBar.setImageResource(movingBar);
    }

    public void setSideBg(@DrawableRes int sideBg) {
        setBackgroundResource(sideBg);
    }

    public void setMovingSpeed(int speed) {
        mMovingSpeed = speed;
    }

    public void setBarMargin(int margin) {
        mBarMargin = margin;
    }

    public void start() {
        post(mMovingTask);
    }

    public void stop() {
        removeCallbacks(mMovingTask);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mHeight = getHeight();
        start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stop();
    }
}