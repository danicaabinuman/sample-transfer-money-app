package com.unionbankph.corporate.auth.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CorporateUser(

    @SerialName("id")
    val id: String? = null,

    @SerialName("salutation")
    val salutation: String? = null,

    @SerialName("email_address")
    val emailAddress: String? = null,

    @SerialName("first_name")
    val firstName: String? = null,

    @SerialName("last_name")
    val lastName: String? = null,

    @SerialName("country_code")
    var countryCode: CountryCode? = null,

    @SerialName("mobile_number")
    var mobileNumber: String? = null,

    @SerialName("activation_id")
    val activationId: String? = null,

    @SerialName("pending_email_address")
    val pendingEmailAddress: String? = null,

    @SerialName("secret_token")
    val secretToken: String? = null
)
