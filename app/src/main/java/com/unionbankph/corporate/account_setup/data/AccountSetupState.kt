package com.unionbankph.corporate.account_setup.data

import kotlinx.serialization.Serializable

@Serializable
data class AccountSetupState(
    var businessType: Int? = null,
    var businessAccountType: Int? = null
)

@Serializable
data class ToolbarState(
    var isButtonShow: Boolean? = null,
    var buttonType: Int? = null,
    var backButtonType: Int? = null
)