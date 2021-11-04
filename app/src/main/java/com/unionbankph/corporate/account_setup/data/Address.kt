package com.unionbankph.corporate.account_setup.data

import com.unionbankph.corporate.settings.presentation.form.Selector
import kotlinx.serialization.Serializable

@Serializable
data class Address(
    var isSameAsPresentAddress: Boolean? = null,
    var line1: String? = null,
    var line2: String?  = null,
    var region: Selector?  = null,
    var city: Selector?  = null,
    var zipCode: String?  = null,
    var permanentLine1: String? = null,
    var permanentLine2: String?  = null,
    var permanentRegion: Selector?  = null,
    var permanentCity: Selector?  = null,
    var permanentZipCode: String?  = null
)