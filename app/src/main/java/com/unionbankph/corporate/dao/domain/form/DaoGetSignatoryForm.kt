package com.unionbankph.corporate.dao.domain.form

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Created by herald25santos on 5/15/20
 */
@Serializable
data class DaoGetSignatoryForm(
    @SerialName("user_token")
    var userToken: String? = null,
    @SerialName("reference_number")
    var referenceNumber: String? = null
)