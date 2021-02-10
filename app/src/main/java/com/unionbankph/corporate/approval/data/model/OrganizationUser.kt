package com.unionbankph.corporate.approval.data.model

import com.unionbankph.corporate.auth.data.model.CorporateUser

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrganizationUser(
    
    @SerialName("id")
    var id: String? = null,
    
    @SerialName("corporate_user")
    var corporateUser: CorporateUser? = null,
    
    var status: String? = null,
    
    var isCurrent: Boolean = false
)