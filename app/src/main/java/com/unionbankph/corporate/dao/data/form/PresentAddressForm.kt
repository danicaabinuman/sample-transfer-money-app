package com.unionbankph.corporate.dao.data.form

import com.unionbankph.corporate.dao.data.model.OtherAddress
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Created by herald25santos on 5/13/20
 */
@Serializable
data class PresentAddressForm(
    @SerialName("barangay")
    var barangay: String? = null,
    @SerialName("city")
    var city: String? = null,
    @SerialName("province")
    var province: String? = null,
    @SerialName("street")
    var street: String? = null,
    @SerialName("unit_or_house_no")
    var unitOrHouseNo: String? = null,
    @SerialName("zip_code")
    var zipCode: String? = null,
    @SerialName("country")
    var country: String? = null,
    @SerialName("other")
    var otherAddress: OtherAddress? = null
)