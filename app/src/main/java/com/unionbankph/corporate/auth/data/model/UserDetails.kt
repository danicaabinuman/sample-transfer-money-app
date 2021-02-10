package com.unionbankph.corporate.auth.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDetails(

    @SerialName("token")
    val token: Token? = null,

    @SerialName("role")
    val role: Role? = null,

    @SerialName("corporate_user")
    val corporateUser: CorporateUser? = null,

    @SerialName("approval_groups")
    val approvalGroups: MutableList<Int>? = null,

    @SerialName("is_policy_agreed")
    var isPolicyAgreed: Boolean? = null,

    @SerialName("is_trusted")
    var isTrusted: Boolean? = null,

    @SerialName("read_mcd_terms")
    var readMcdTerms: Boolean? = null
)
