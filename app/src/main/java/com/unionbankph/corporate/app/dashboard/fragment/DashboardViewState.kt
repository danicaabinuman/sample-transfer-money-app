package com.unionbankph.corporate.app.dashboard.fragment

import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.common.data.form.Pageable
import kotlinx.serialization.Serializable

@Serializable
data class DashboardViewState(
    var name: String? = null,
    var accountButtonText: String? = null,
    var actionList: MutableList<ActionItem> = mutableListOf(),
    var accounts: MutableList<Account> = mutableListOf()
)

@Serializable
data class ActionItem(
    var id: String? = null,
    var label: String? = null
)