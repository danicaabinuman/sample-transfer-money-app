package com.unionbankph.corporate.dao.data.form

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PersonalInformationStepTwoForm(
    @SerialName("government_id")
    var governmentId: Int? = null,
    @SerialName("government_id_number")
    var governmentIdNumber: String? = null,
    @SerialName("date_of_birth")
    var dateOfBirth: String? = null,
    @SerialName("place_of_birth")
    var placeOfBirth: String? = null,
    @SerialName("mothers_maiden_name")
    var mothersMaidenName: String? = null,
    @SerialName("us_citizenship")
    var usCitizenship: Boolean? = null,
    @SerialName("us_record_type")
    var usRecordType: Int? = null,
    @SerialName("address_record")
    var addressRecord: String? = null
)
