package com.unionbankph.corporate.account.domain.form

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Created by herald25santos on 07/20/20
 */
@Serializable
data class GetAccountsBalances(
    @SerialName("account_numbers")
    var ids: MutableList<Int>? = null
)