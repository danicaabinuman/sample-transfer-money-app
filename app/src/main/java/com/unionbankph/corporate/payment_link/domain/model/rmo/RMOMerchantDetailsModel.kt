package com.unionbankph.corporate.payment_link.domain.model.rmo

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RMOMerchantDetailsModel(
    @SerialName("businessType")
    var businessType: String? = null,
    @SerialName("product")
    var storeProduct: String? = null,
    @SerialName("yearsInBusiness")
    var yearsInBusiness: Int? = null,
    @SerialName("numberOfBranches")
    var numberOfBranches: Int? = null,
    @SerialName("physicalStore")
    var physicalStore: String? = null,
    @SerialName("websites")
    var websites: RMOWebsites? = null,
    @SerialName("photos")
    var photos: String? = null,
    @SerialName("status")
    var status: String? = null
)
