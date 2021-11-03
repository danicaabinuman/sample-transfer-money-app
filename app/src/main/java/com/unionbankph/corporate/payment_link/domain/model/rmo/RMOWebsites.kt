package com.unionbankph.corporate.payment_link.domain.model.rmo

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RMOWebsites(
    @SerialName("instagramUrl")
    var instagramUrl: String? = null,
    @SerialName("facebookUrl")
    var facebookUrl: String? = null,
    @SerialName("lazadaUrl")
    var lazadaUrl: String? = null,
    @SerialName("shopeeUrl")
    var shopeeUrl: String? = null,
    @SerialName("webpageUrl")
    var webpageUrl: String? = null

)