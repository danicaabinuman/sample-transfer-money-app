package com.unionbankph.corporate.auth.data.form

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EBankingResendOTPForm(

    @SerialName("request_id")
    var requestId: String? = null,

    @SerialName("migration_token")
    var migrationToken: String? = null
)
