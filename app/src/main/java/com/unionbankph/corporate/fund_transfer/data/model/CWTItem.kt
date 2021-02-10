package com.unionbankph.corporate.fund_transfer.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CWTItem(

    @SerialName("title")
    var title: String? = null,

    @SerialName("cwt_body")
    var cwtBody: MutableList<CWTDetail> = mutableListOf()
)
