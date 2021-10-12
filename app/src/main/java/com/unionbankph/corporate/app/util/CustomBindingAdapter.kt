package com.unionbankph.corporate.app.util

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.unionbankph.corporate.R


@BindingAdapter("setDrawable")
fun setDrawable(imageView: ImageView, source: Int?) {
    source?.let {
        Glide.with(imageView.context)
            .load(source)
            .into(imageView)
    }
}

@BindingAdapter("backgroundColor")
fun View.setBackgroundColor(source: Int?) {
    source?.let {
        if (source  % 2 == 0) {
            this.setBackgroundColor(this.context.getColor(R.color.dsColorExtraLightGray))
        } else {
            this.setBackgroundColor(this.context.getColor(R.color.colorDisableItem))
        }
    }
}

@BindingAdapter("backgroundColorTextView")
fun setBackgroundColorTextView(textView: TextView, source: Int?) {
    source?.let {
        if (source  % 2 == 0) {
            textView.setBackgroundColor(textView.context.getColor(R.color.colorDisableItem))
        } else {
            textView.setBackgroundColor(textView.context.getColor(R.color.dsColorExtraLightGray))
        }
    }
}

