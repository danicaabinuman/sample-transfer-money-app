package com.unionbankph.corporate.auth.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Details(
    @SerialName("business_name")
    var businessName: String? = null,
    @SerialName("preferred_branch")
    var preferredBranch: String? = null,
    @SerialName("preferred_branch_email")
    var preferredBranchEmail: String? = null
)
