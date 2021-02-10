package com.unionbankph.corporate.common.presentation.callback

interface AccountAdapterCallback {
    fun onClickItem(account: String, position: Int) = Unit
    //fun onRequestAccountDetail(id: String, position: Int)
    fun onTapErrorRetry(id: String, position: Int) = Unit
    fun onTapToRetry() = Unit
}