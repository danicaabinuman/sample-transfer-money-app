package com.unionbankph.corporate.app.common.widget.recyclerview

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.EpoxyRecyclerView

class BottomSheetRecyclerView(context: Context, p_attrs: AttributeSet) : EpoxyRecyclerView(
    context, p_attrs
) {

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (canScrollVertically(this)) {
            parent.requestDisallowInterceptTouchEvent(true)
        }
        return super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (canScrollVertically(this)) {
            parent.requestDisallowInterceptTouchEvent(true)
        }
        return super.onTouchEvent(ev)
    }

    private fun canScrollVertically(view: RecyclerView?): Boolean {
        var canScroll = false

        if (view != null && view.childCount > 0) {
            val layoutManager = LinearLayoutManager(context)
            val isOnTop =
                layoutManager.findFirstVisibleItemPosition() != 0 || view.getChildAt(0).top != 0
            val isAllItemsVisible =
                isOnTop && layoutManager.findLastVisibleItemPosition() == view.childCount

            if (isOnTop || isAllItemsVisible) {
                canScroll = true
            }
        }

        return canScroll
    }
}
