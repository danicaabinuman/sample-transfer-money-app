package com.unionbankph.corporate.dao.data.form

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PersonalInformationStepThreeForm(
    @SerialName("present_address_province")
    var presentAddressProvince: Int? = null,
    @SerialName("present_address_zip_code")
    var presentAddressZipCode: Int? = null,
    @SerialName("present_address_city")
    var presentAddressCity: Int? = null,
    @SerialName("present_address_barangay")
    var presentAddressBarangay: String? = null,
    @SerialName("present_address_unit_or_house_no")
    var presentAddressUnitOrHouseNo: String? = null,
    @SerialName("present_address_street")
    var presentAddressStreet: String? = null,
    @SerialName("permanent_address_province")
    var permanentAddressProvince: Int? = null,
    @SerialName("permanent_address_zip_code")
    var permanentAddressZipCode: Int? = null,
    @SerialName("permanent_address_city")
    var permanentAddressCity: Int? = null,
    @SerialName("permanent_address_barangay")
    var permanentAddressBarangay: String? = null,
    @SerialName("permanent_address_unit_or_house_no")
    var permanentAddressUnitOrHouseNo: String? = null,
    @SerialName("permanent_address_street")
    var permanentAddressStreet: String? = null
)
