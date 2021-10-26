package com.unionbankph.corporate.account_setup.data

import com.unionbankph.corporate.settings.presentation.form.Selector
import kotlinx.serialization.Serializable

@Serializable
data class PersonalInfoInput(
    var firstName: String? = null,
    var middleName: String?  = null,
    var lastName: String?  = null,
    var mobile: String? = null,
    var email: String? = null,
    var gender: Selector? = null,
    var civilStatus: Selector? = null,
    var tin: String? = null,
    var dateOfBirth: String? = null,
    var placeOfBirth: String? = null,
    var nationality: String? = null,
    var notUsCitizen: Boolean = true
)