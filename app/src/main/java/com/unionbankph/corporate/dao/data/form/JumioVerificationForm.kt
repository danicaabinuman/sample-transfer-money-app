package com.unionbankph.corporate.dao.data.form

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JumioVerificationForm(
    @SerialName("scan_reference_id")
    var scanReferenceId: String? = null
)
