package com.unionbankph.corporate.dao.data.form

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PersonalInformationStepFourForm(
    @SerialName("occupation")
    var occupation: Int? = null,
    @SerialName("other_occupation")
    var otherOccupation: String? = null,
    @SerialName("source_of_funds")
    var sourceOfFunds: Int? = null,
    @SerialName("percentage_of_ownership")
    var percentageOfOwnership: Int? = null
)
