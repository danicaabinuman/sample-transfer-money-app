package com.unionbankph.corporate.account_setup.data

import kotlinx.serialization.Serializable

@Serializable
data class AccountSetupState(
    var businessType: Int? = null,
    var businessAccountType: Int? = null
)