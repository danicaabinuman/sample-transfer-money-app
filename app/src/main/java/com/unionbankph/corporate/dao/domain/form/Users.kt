package com.unionbankph.corporate.dao.domain.form

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Created by herald25santos on 07/07/20
 */
@Serializable
data class Users(
    @SerialName("email_address")
    var emailAddress: String? = null,
    @SerialName("mobile_number")
    var mobileNumber: String? = null,
    @SerialName("country_code_id")
    var countryCodeId: Int? = null
)