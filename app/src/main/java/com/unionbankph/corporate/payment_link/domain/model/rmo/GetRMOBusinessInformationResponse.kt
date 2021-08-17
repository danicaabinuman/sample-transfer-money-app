package com.unionbankph.corporate.payment_link.domain.model.rmo

import com.unionbankph.corporate.payment_link.domain.model.rmo.RMOMerchantDetailsModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetRMOBusinessInformationResponse (
    @SerialName("message")
    var message: String? = null,
    @SerialName("organizationId")
    var organizationId: String? = null,
    @SerialName("corporateId")
    var corporateId: String? = null,
    @SerialName("merchantId")
    var merchantId: String? = null,
    @SerialName("createdDate")
    var createdDate: String? = null,
    @SerialName("modifiedDate")
    var modifiedDate: String? = null,
    @SerialName("merchantDetails")
    var merchantDetails: RMOMerchantDetailsModel? = null
)