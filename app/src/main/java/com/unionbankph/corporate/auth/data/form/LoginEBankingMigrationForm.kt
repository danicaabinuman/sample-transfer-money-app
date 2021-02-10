package com.unionbankph.corporate.auth.data.form

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginEBankingMigrationForm(

    @SerialName("corp_id")
    val corpId: String? = null,

    @SerialName("user_id")
    val userId: String? = null,

    @SerialName("password")
    val password: String? = null
)
