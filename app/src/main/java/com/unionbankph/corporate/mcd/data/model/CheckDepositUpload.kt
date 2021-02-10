package com.unionbankph.corporate.mcd.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CheckDepositUpload(
    @SerialName("id")
    var id: String? = null,
    @SerialName("front_key")
    var frontKey: String? = null,
    @SerialName("back_key")
    var backKey: String? = null,
    @SerialName("source_account")
    var sourceAccount: String? = null,
    @SerialName("source_account_name")
    var sourceAccountName: String? = null,
    @SerialName("brstn")
    var brstn: String? = null,
    @SerialName("issuer")
    var issuer: String? = null,
    @SerialName("check_number")
    var checkNumber: String? = null
)
