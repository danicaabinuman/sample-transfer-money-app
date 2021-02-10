package com.unionbankph.corporate.common.presentation.callback

import android.widget.Button
import android.widget.LinearLayout

interface OnConfirmationPageCallBack {
    fun onClickNegativeButtonDialog(data: String?, tag: String?) = Unit
    fun onClickPositiveButtonDialog(data: String?, tag: String?) = Unit
    fun onDataSetDialog(
        tag: String?,
        linearLayout: LinearLayout,
        buttonPositive: Button,
        buttonNegative: Button,
        data: String?,
        dynamicData: String?
    ) = Unit
}
