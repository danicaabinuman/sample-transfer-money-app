package com.unionbankph.corporate.corporate.data.form

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ValidateAccountNumberForm(

    @SerialName("account_number")
    var accountNumber: String? = null
)
