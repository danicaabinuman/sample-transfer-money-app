package com.unionbankph.corporate.app.common.widget.qrgenerator.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.unionbankph.corporate.R;


public class HoleContainer extends FrameLayout {
    private HoleView mHoleView;

    private boolean mInitialized = false;
    private int mOutsideColor;

    public HoleContainer(Context context) {
        this(context, null);
    }

    public HoleContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HoleContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.getTheme()
                .obtainStyledAttributes(attrs, R.styleable.QRContainer, 0, 0);
        mOutsideColor = a.getColor(R.styleable.QRContainer_outside_color, 0x80000000);
        a.recycle();
    }

    public void setOutsideColor(int outsideColor) {
        mOutsideColor = outsideColor;
        mHoleView.setColor(mOutsideColor);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (!mInitialized) {
            if (getChildCount() != 1) {
                throw new IllegalStateException("HoleContainer must have exactly one child!");
            }
            mHoleView = new HoleView(getContext());
            LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            mHoleView.setLayoutParams(params);
            addView(mHoleView);
            View child = getChildAt(0);
            mHoleView.setHole(mOutsideColor, child.getX(), child.getY(), child.getMeasuredWidth(),
                    child.getMeasuredHeight());

            mInitialized = true;
        }
    }
}