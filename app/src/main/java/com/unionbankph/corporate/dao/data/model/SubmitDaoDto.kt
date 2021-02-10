package com.unionbankph.corporate.dao.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubmitDaoDto(
    @SerialName("user_reference_number")
    var userReferenceNumber: String? = null,
    @SerialName("amla_hit")
    var amlaHit: Boolean? = null,
    @SerialName("nfis_hit")
    var nfisHit: Boolean? = null,
    @SerialName("crr_hit")
    var crrHit: Boolean? = null,
    @SerialName("nationality_hit")
    var nationalityHit: Boolean? = null,
    @SerialName("business_name")
    var businessName: String? = null,
    @SerialName("prefered_branch")
    var preferedBranch: String? = null,
    @SerialName("prefered_branch_email")
    var preferedBranchEmail: String? = null
)
