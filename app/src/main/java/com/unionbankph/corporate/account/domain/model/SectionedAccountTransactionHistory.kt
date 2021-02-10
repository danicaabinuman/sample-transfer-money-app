package com.unionbankph.corporate.account.domain.model

import com.unionbankph.corporate.account.data.model.Record
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Created by herald25santos on 07/23/20
 */
@Serializable
data class SectionedAccountTransactionHistory(
    @SerialName("header")
    var header: String? = null,
    @SerialName("records")
    var records: MutableList<Record>? = null
)