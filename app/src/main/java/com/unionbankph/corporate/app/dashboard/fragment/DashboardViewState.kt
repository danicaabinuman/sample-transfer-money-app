package com.unionbankph.corporate.app.dashboard.fragment

import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.sme.GenericMenuItem
import kotlinx.serialization.Serializable

@Serializable
data class DashboardViewState(
    var name: String? = null,
    var accountButtonText: String? = null,
    var isScreenRefreshed: Boolean = true,
    var hasInitialFetchError: Boolean = true,
    var isOnTrialMode: Boolean = true,
    var hasLoans: Boolean = false,
    var hasEarnings: Boolean = false,
    var errorMessage: String? = null,

    var megaMenuList: MutableList<GenericMenuItem> = mutableListOf(),
    var accounts: MutableList<Account> = mutableListOf()
)

@Serializable
data class MegaMenuBottomSheetState(
    var lastFilterSelected: Int? = 0,
    var filters: MutableList<GenericMenuItem> = mutableListOf(),
    var menus: MutableList<GenericMenuItem> = mutableListOf()
)

@Serializable
data class FeatureCardItem(
    var id: String? = null,
    var featureTitle: String? = null,
    var cardTitle: String? = null,
    var cardContent: String? = null,
    var cardFooter: String? = null,
    var action: String? = null
)