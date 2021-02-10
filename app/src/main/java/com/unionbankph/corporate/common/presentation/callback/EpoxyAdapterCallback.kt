package com.unionbankph.corporate.common.presentation.callback

import android.view.View

interface EpoxyAdapterCallback<T> {
    fun onClickItem(view: View, data: T, position: Int) = Unit
    fun onLongClickItem(view: View, data: T, position: Int) = Unit
    fun onTapToRetry() = Unit
}
