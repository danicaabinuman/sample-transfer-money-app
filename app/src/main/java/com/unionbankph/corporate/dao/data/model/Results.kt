package com.unionbankph.corporate.dao.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Created by herald25santos on 07/07/20
 */
@Serializable
data class Results(
    @SerialName("email_address")
    var emailAddress: String? = null,
    @SerialName("mobile_number")
    var mobileNumber: String? = null,
    @SerialName("country_code_id")
    var countryCodeId: String? = null,
    @SerialName("with_error")
    var withError: Boolean? = null,
    @SerialName("email_address_error_message")
    var emailAddressErrorMessage: String? = null,
    @SerialName("mobile_number_error_message")
    var mobileNumberErrorMessage: String? = null
)