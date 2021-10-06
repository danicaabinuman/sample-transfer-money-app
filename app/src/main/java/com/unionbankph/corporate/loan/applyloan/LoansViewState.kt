package com.unionbankph.corporate.loan.applyloan

import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.common.data.form.Pageable
import kotlinx.serialization.Serializable

@Serializable
data class LoansViewState(
    var isScreenRefreshed: Boolean = true,
    var hasInitialFetchError: Boolean = true,
    var errorMessage: String? = null,
)
