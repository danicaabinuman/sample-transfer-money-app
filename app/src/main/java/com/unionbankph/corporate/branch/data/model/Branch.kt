package com.unionbankph.corporate.branch.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Branch(

    @SerialName("id")
    var id: String? = null,

    @SerialName("guid")
    var guid: String? = null,

    @SerialName("sol_id")
    var solId: String? = null,

    @SerialName("name")
    var name: String? = null,

    @SerialName("region")
    var region: String? = null,

    @SerialName("description")
    var description: String? = null,

    @SerialName("latitude")
    var latitude: String? = null,

    @SerialName("longtitude")
    var longtitude: String? = null,

    @SerialName("email")
    var email: String? = null,

    @SerialName("contact_number")
    var contactNumber: String? = null,

    @SerialName("open_time")
    var openTime: String? = null,

    @SerialName("closing_time")
    var closingTime: String? = null,

    @SerialName("open_saturday")
    var openSaturday: String? = null,

    @SerialName("open_sunday")
    var openSunday: String? = null,

    @SerialName("cash_type")
    var cashType: String? = null,

    @SerialName("address")
    var address: String? = null
)
