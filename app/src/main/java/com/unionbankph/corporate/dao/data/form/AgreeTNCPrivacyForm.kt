package com.unionbankph.corporate.dao.data.form

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AgreeTNCPrivacyForm(
    @SerialName("has_agreed_to_private_policy")
    var hasAgreedToPrivatePolicy: Boolean? = null,
    @SerialName("has_agreed_to_terms_and_condition")
    var hasAgreedToTermsAndCondition: Boolean? = null
)
