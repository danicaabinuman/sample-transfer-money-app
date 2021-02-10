package com.unionbankph.corporate.dao.data.form

import com.unionbankph.corporate.dao.data.model.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DaoForm(
    @SerialName("reference_number")
    var referenceNumber: String? = null,
    @SerialName("user_token")
    var userToken: String? = null,
    @SerialName("account_opening_tnc")
    var accountOpeningTnc: Boolean? = null,
    @SerialName("processing_remarks")
    var processingRemarks: String? = null,
    @SerialName("processor")
    var processor: String? = null,
    @SerialName("status")
    var status: String? = null,
    @SerialName("page")
    var page: Int? = null,
    @SerialName("private_policy")
    var privatePolicy: Boolean? = null,
    @SerialName("terms_and_condition")
    var termsAndCondition: Boolean? = null,
    @SerialName("scan_reference_id")
    var scanReferenceId: String? = null,
    @SerialName("jumio_id_type")
    var jumioIdType: String? = null,
    @SerialName("ownership_percentage")
    var ownershipPercentage: String? = null,
    @SerialName("funds_source")
    var fundsSource: String? = null,
    @SerialName("occupation")
    var occupation: String? = null,
    @SerialName("employment_status")
    var employmentStatus: String? = null,
    @SerialName("permanent_address")
    var permanentAddress: PermanentAddressForm? = null,
    @SerialName("present_address")
    var presentAddress: PresentAddressForm? = null,
    @SerialName("address_record")
    var addressRecord: String? = null,
    @SerialName("us")
    var us: US? = null,
    @SerialName("mothers_maiden_name")
    var mothersMaidenName: String? = null,
    @SerialName("birthplace")
    var birthplace: String? = null,
    @SerialName("birthdate")
    var birthdate: String? = null,
    @SerialName("government")
    var government: Government? = null,
    @SerialName("nationality")
    var nationality: String? = null,
    @SerialName("gender")
    var gender: String? = null,
    @SerialName("ultimate_beneficial_owner")
    var ultimateBeneficialOwner: Boolean? = null,
    @SerialName("allow")
    var allow: Allow? = null,
    @SerialName("authorized_signatory")
    var authorizedSignatory: Boolean? = null,
    @SerialName("job_title")
    var jobTitle: String? = null,
    @SerialName("mobile")
    var mobile: String? = null,
    @SerialName("email")
    var email: String? = null,
    @SerialName("name")
    var name: Name? = null,
    @SerialName("salutation")
    var salutation: String? = null,
    @SerialName("other")
    var other: Other? = null,
    @SerialName("civil_status")
    var civilStatus: String? = null,
    @SerialName("country_code")
    var countryCode: Int? = null,
    @SerialName("save_type")
    var saveType: String? = null
)
