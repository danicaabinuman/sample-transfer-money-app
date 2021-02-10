package com.unionbankph.corporate.account.domain.form

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Created by herald25santos on 07/20/20
 */
@Serializable
data class GetAccountTransactionHistoryDetailsForm(
    @SerialName("id")
    var id: String? = null,
    @SerialName("referenceNumber")
    var referenceNumber: String? = null,
    @SerialName("serialNo")
    var serialNo: String? = null,
    @SerialName("transactionDate")
    var transactionDate: String? = null
)