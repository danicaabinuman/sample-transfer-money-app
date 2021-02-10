package com.unionbankph.corporate.bills_payment.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FrequentBillerSectioned(

    @SerialName("title_header")
    var titleHeader: String? = null,

    @SerialName("frequent_billers")
    var frequentBillers: MutableList<FrequentBiller> = mutableListOf()
)
