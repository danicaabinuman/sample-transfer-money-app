package com.unionbankph.corporate.app.dashboard.fragment

import android.os.Parcelable
import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.sme.chip.GenericItem
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
data class DashboardViewState(
    var name: String? = null,
    var accountButtonText: String? = null,
    var isScreenRefreshed: Boolean = true,
    var hasInitialFetchError: Boolean = true,
    var errorMessage: String? = null,

    var actionList: MutableList<ActionItem> = mutableListOf(),
    var accounts: MutableList<Account> = mutableListOf()
)

@Parcelize
@Serializable
data class ActionItem(
    var id: String? = null,
    var label: String? = null,
    var caption: String? = null,
    var src: String? = null,
    var action: String? = null,
    var isVisible: Boolean = false,
    var isEnabled: Boolean = true
) : Parcelable

@Serializable
data class MoreBottomSheetState(
    var lastFilterSelected: Int? = 0,
    var filters: MutableList<GenericItem> = mutableListOf(),
    var actions: MutableList<GenericItem> = mutableListOf()
)