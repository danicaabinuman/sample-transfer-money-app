package com.unionbankph.corporate.dao.domain.model

import com.unionbankph.corporate.auth.data.model.CountryCode
import com.unionbankph.corporate.dao.data.model.Allow
import com.unionbankph.corporate.settings.presentation.form.Selector

/**
 * Created by herald25santos on 4/22/20
 */
data class SignatoryDetail(
    // Personal Info 1
    var userToken: String? = null,
    var referenceNumber: String? = null,
    var accountOpeningTnc: Boolean? = null,
    var privatePolicy: Boolean? = null,
    var termsAndCondition: Boolean? = null,
    var salutationInput: Selector? = null,
    var firstNameInput: String? = null,
    var middleNameInput: String? = null,
    var lastNameInput: String? = null,
    var emailAddressInput: String? = null,
    var countryCodeInput: CountryCode? = null,
    var businessMobileNumberInput: String? = null,
    var genderInput: Selector? = null,
    var civilStatusInput: Selector? = null,
    var nationalityInput: Selector? = null,
    var governmentIdTypeInput: Selector? = null,
    var governmentIdNumberInput: String? = null,
    var dateOfBirthInput: String? = null,
    var placeOfBirthInput: String? = null,
    var usCitizenshipInput: Boolean? = false,
    var recordTypeInput: Selector? = null,
    var usRecordInput: String? = null,
    // Personal Info 2
    var permanentAddress: Boolean? = null,
    var homeAddressInput: String? = null,
    var streetNameInput: String? = null,
    var villageBarangayInput: String? = null,
    var provinceInput: Selector? = null,
    var cityInput: Selector? = null,
    var zipCodeInput: String? = null,
    var countryInput: Selector? = null,
    var homeAddressPermanentInput: String? = null,
    var streetNamePermanentInput: String? = null,
    var villageBarangayPermanentInput: String? = null,
    var provincePermanentInput: Selector? = null,
    var cityPermanentInput: Selector? = null,
    var zipCodePermanentInput: String? = null,
    var countryPermanentInput: Selector? = null,
    // Personal Info 3
    var occupationInput: Selector? = null,
    var otherOccupationInput: String? = null,
    var sourceOfFundsInput: Selector? = null,
    var percentOwnershipInput: String? = null,
    // Jumio
    var scanReferenceId: String? = null,
    var idType: String? = null,
    var apiSecretJumio: String? = null,
    var apiTokenJumio: String? = null,
    // Extras
    var allow: Allow? = null,
    var processor: String? = null,
    var status: String? = null,
    var employmentStatus: String? = null,
    var ultimateBeneficialOwner: Boolean? = null,
    var authorizedSignatory: Boolean? = null,
    var jobTitle: String? = null,
    var processingRemarks: String? = null,
    var permanentAddressOtherCity: String? = null,
    var permanentAddressOtherProvince: String? = null,
    var presentAddressOtherCity: String? = null,
    var presentAddressOtherProvince: String? = null,
    var page: Int? = null,
    var imagePath: String? = null,
    var hasPersonalInformationOneCurrentInput: Boolean? = null,
    var hasPersonalInformationTwoCurrentInput: Boolean? = null,
    var hasPersonalInformationThreeCurrentInput: Boolean? = null,
    var hasPersonalInformationFourCurrentInput: Boolean? = null,
    var hasJumioVerificationCurrentInput: Boolean? = null,
    var hasSignatureCurrentInput: Boolean? = null,
    var hasConfirmationCurrentInput: Boolean? = null
)
