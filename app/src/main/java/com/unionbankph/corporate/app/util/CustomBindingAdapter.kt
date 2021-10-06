package com.unionbankph.corporate.app.util

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide


@BindingAdapter("setDrawable")
fun setDrawable(imageView: ImageView, source: Int?) {
    source?.let {
        Glide.with(imageView.context)
            .load(source)
            .into(imageView)
    }
}