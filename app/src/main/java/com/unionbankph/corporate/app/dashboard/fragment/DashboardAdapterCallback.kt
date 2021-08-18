package com.unionbankph.corporate.app.dashboard.fragment

interface DashboardAdapterCallback {
    fun onDashboardActionEmit(actionId: String, isEnabled: Boolean)
    fun onContinueAccountSetup()
}