package com.unionbankph.corporate.app.dashboard.fragment

interface DashboardAdapterCallback {
    fun onDashboardActionEmit(id: String)
    fun onAccountClickItem(account: String, position: Int) = Unit
    //fun onRequestAccountDetail(id: String, position: Int)
    fun onTapErrorRetry(id: String, position: Int) = Unit
    fun onTapToRetry() = Unit
}