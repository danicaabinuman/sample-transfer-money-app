package com.unionbankph.corporate.fund_transfer.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SwiftBank(

    @SerialName("address_1")
    var address1: String? = null,

    @SerialName("address_2")
    var address2: String? = null,

    @SerialName("bank_name")
    var bankName: String? = null,

    @SerialName("city_name")
    var cityName: String? = null,

    @SerialName("country")
    var country: String? = null,

    @SerialName("location")
    var location: String? = null,

    @SerialName("swift_bic_code")
    var swiftBicCode: String? = null
)
