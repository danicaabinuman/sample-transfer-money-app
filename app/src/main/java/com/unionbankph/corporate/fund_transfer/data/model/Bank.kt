package com.unionbankph.corporate.fund_transfer.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Bank(

    @SerialName("bank")
    var bank: String? = null,

    @SerialName("swiftCode")
    var swiftCode: String? = null,

    @SerialName("city")
    var city: String? = null,

    @SerialName("address")
    var address: String? = null,

    @SerialName("location")
    var location: String? = null,

    @SerialName("instapayCode")
    var instapayCode: String? = null,

    @SerialName("pesonetCode")
    var pesonetCode: String? = null,

    @SerialName("pesonetBrstn")
    var pesonetBrstn: String? = null,

    @SerialName("pesonetBankCode")
    var pesonetBankCode: String? = null,

    @SerialName("pddtsFinacleCode")
    var pddtsFinacleCode: String? = null
)
