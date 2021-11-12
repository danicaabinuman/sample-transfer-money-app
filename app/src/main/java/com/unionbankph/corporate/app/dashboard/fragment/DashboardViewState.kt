package com.unionbankph.corporate.app.dashboard.fragment

import android.content.Context
import com.unionbankph.corporate.R
import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.sme.GenericMenuItem
import com.unionbankph.corporate.common.presentation.constant.Constant
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

@Serializable
data class BannerCardItem(
    var id: String? = null,
    var header: String? = null,
    var body: String? = null,
    var footer: String? = null,
    var src: String? = null,
    var action: String? = null
) {
    companion object {

        fun generateBannerItems(context: Context): List<BannerCardItem> {
            return mutableListOf(
                BannerCardItem(
                    id = Constant.Banner.BUSINESS_PROFILE,
                    header = context.getString(R.string.banner_business_profile_title),
                    body = context.getString(R.string.banner_business_profile_body),
                    footer = context.getString(R.string.banner_business_profile_footer),
                    src = Constant.Banner.BUSINESS_PROFILE,
                    action = Constant.Banner.BUSINESS_PROFILE
                ),
                BannerCardItem(
                    id = Constant.Banner.LEARN_MORE,
                    header = context.getString(R.string.banner_learn_more_title),
                    body = context.getString(R.string.banner_learn_more_body),
                    footer = context.getString(R.string.banner_learn_more_footer),
                    src = Constant.Banner.LEARN_MORE,
                    action = Constant.Banner.LEARN_MORE
                )
            )
        }
    }
}