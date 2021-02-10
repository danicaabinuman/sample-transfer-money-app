package com.unionbankph.corporate.auth.data.form

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ECredMergeAccountForm(

    @SerialName("username")
    var username: String? = null,

    @SerialName("password")
    var password: String? = null
)
