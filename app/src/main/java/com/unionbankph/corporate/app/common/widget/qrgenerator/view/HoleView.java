package com.unionbankph.corporate.app.common.widget.qrgenerator.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class HoleView extends View {

    private final Paint mPaint = new Paint();
    private final Path mPath = new Path();
    private boolean mInitialized = false;

    private float mHoleX;
    private float mHoleY;
    private int mHoleWidth;
    private int mHoleHeight;

    public HoleView(Context context) {
        this(context, null);
    }

    public HoleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HoleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);
        setLayerType(LAYER_TYPE_HARDWARE, mPaint);
    }

    public void setHole(int color, float holeX, float holeY, int holeWidth, int holeHeight) {
        mInitialized = true;

        mPaint.setColor(color);
        mHoleX = holeX;
        mHoleY = holeY;
        mHoleWidth = holeWidth;
        mHoleHeight = holeHeight;
        invalidate();
    }

    public void setColor(int color) {
        mPaint.setColor(color);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!mInitialized) {
            return;
        }

        int width = getWidth();
        int height = getHeight();

        mPath.reset();
        mPath.moveTo(0, 0);
        mPath.lineTo(width, 0);
        mPath.lineTo(width, mHoleY);
        mPath.lineTo(0, mHoleY);
        mPath.lineTo(0, 0);
        canvas.drawPath(mPath, mPaint);

        mPath.reset();
        mPath.moveTo(0, mHoleY);
        mPath.lineTo(mHoleX, mHoleY);
        mPath.lineTo(mHoleX, mHoleY + mHoleHeight);
        mPath.lineTo(0, mHoleY + mHoleHeight);
        mPath.lineTo(0, mHoleY);
        canvas.drawPath(mPath, mPaint);

        mPath.reset();
        mPath.moveTo(mHoleX + mHoleWidth, mHoleY);
        mPath.lineTo(width, mHoleY);
        mPath.lineTo(width, mHoleY + mHoleHeight);
        mPath.lineTo(mHoleX + mHoleWidth, mHoleY + mHoleHeight);
        mPath.lineTo(mHoleX + mHoleWidth, mHoleY);
        canvas.drawPath(mPath, mPaint);

        mPath.reset();
        mPath.moveTo(0, mHoleY + mHoleHeight);
        mPath.lineTo(width, mHoleY + mHoleHeight);
        mPath.lineTo(width, height);
        mPath.lineTo(0, height);
        mPath.lineTo(0, mHoleY + mHoleHeight);
        canvas.drawPath(mPath, mPaint);
    }
}
