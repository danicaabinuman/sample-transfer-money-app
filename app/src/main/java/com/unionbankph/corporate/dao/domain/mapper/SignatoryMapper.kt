package com.unionbankph.corporate.dao.domain.mapper

import com.unionbankph.corporate.BuildConfig
import com.unionbankph.corporate.app.common.extension.convertDateToDesireFormat
import com.unionbankph.corporate.app.common.extension.notEmpty
import com.unionbankph.corporate.app.common.extension.nullable
import com.unionbankph.corporate.common.domain.mapper.Mapper
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.constant.DateFormatEnum
import com.unionbankph.corporate.dao.data.model.DaoDetailsDto
import com.unionbankph.corporate.dao.data.model.PermanentAddress
import com.unionbankph.corporate.dao.data.model.PresentAddress
import com.unionbankph.corporate.dao.domain.model.SignatoryDetail
import com.unionbankph.corporate.settings.data.gateway.SettingsGateway
import com.unionbankph.corporate.settings.presentation.form.Selector
import javax.inject.Inject

/**
 * Created by herald25santos on 5/14/20
 */
class SignatoryMapper
@Inject
constructor(
    private val settingsGateway: SettingsGateway
) : Mapper<SignatoryDetail, DaoDetailsDto>() {

    override fun map(from: DaoDetailsDto): SignatoryDetail {
        val signatoriesDetail = SignatoryDetail()
        mapSingleSelector(from, signatoriesDetail)
        from.let {
            signatoriesDetail.userToken = it.userToken
            signatoriesDetail.referenceNumber = it.referenceNumber
            signatoriesDetail.accountOpeningTnc = it.accountOpeningTnc
            signatoriesDetail.privatePolicy = it.privatePolicy
            signatoriesDetail.termsAndCondition = it.termsAndCondition
            signatoriesDetail.middleNameInput = it.name?.middle.nullable()
            signatoriesDetail.firstNameInput = it.name?.first.nullable()
            signatoriesDetail.middleNameInput = it.name?.middle.nullable()
            signatoriesDetail.lastNameInput = it.name?.last.nullable()
            signatoriesDetail.emailAddressInput = it.email.nullable()
            signatoriesDetail.countryCodeInput = it.countryCode ?: Constant.getDefaultCountryCode()
            signatoriesDetail.businessMobileNumberInput = it.mobile.nullable()
            signatoriesDetail.governmentIdNumberInput = it.government?.number.nullable()
            signatoriesDetail.dateOfBirthInput =
                it.birthdate.convertDateToDesireFormat(DateFormatEnum.DATE_FORMAT_DATE_SLASH)
            signatoriesDetail.placeOfBirthInput = it.birthplace.nullable()
            signatoriesDetail.usCitizenshipInput = it.us?.citizenship
            signatoriesDetail.usRecordInput = it.addressRecord.nullable()
            signatoriesDetail.allow = it.allow
            signatoriesDetail.permanentAddress =
                isPermanentSameWithPresentAddress(it.permanentAddress, it.presentAddress)
            signatoriesDetail.homeAddressInput = it.presentAddress?.unitOrHouseNo.nullable()
            signatoriesDetail.streetNameInput = it.presentAddress?.street.nullable()
            signatoriesDetail.villageBarangayInput = it.presentAddress?.barangay.nullable()
            signatoriesDetail.zipCodeInput = it.presentAddress?.zipCode.nullable()
            if (it.presentAddress?.country?.id != null
                && Constant.getDefaultCountryDao().id != it.presentAddress?.country?.id
            ) {
                signatoriesDetail.provinceInput = Selector(
                    id = it.presentAddress?.otherAddress?.province.nullable(),
                    value = it.presentAddress?.otherAddress?.province.nullable()
                )
                signatoriesDetail.cityInput = Selector(
                    id = it.presentAddress?.otherAddress?.city.nullable(),
                    value = it.presentAddress?.otherAddress?.city.nullable()
                )
            } else {
                signatoriesDetail.provinceInput = Selector(
                    id = it.presentAddress?.province?.id.nullable(),
                    value = it.presentAddress?.province?.value.nullable()
                )
                signatoriesDetail.cityInput = Selector(
                    id = it.presentAddress?.city?.id.nullable(),
                    value = it.presentAddress?.city?.value.nullable()
                )
            }
            signatoriesDetail.countryInput = Selector(
                id = it.presentAddress?.country?.id,
                value = it.presentAddress?.country?.name
            )
            signatoriesDetail.presentAddressOtherCity =
                it.presentAddress?.otherAddress?.city.nullable()
            signatoriesDetail.presentAddressOtherProvince =
                it.presentAddress?.otherAddress?.province.nullable()
            signatoriesDetail.homeAddressPermanentInput =
                it.permanentAddress?.unitOrHouseNo.nullable()
            signatoriesDetail.streetNamePermanentInput = it.permanentAddress?.street.nullable()
            signatoriesDetail.villageBarangayPermanentInput =
                it.permanentAddress?.barangay.nullable()
            if (it.permanentAddress?.country?.id != null
                && Constant.getDefaultCountryDao().id != it.permanentAddress?.country?.id
            ) {
                signatoriesDetail.provincePermanentInput = Selector(
                    id = it.permanentAddress?.otherAddress?.province.nullable(),
                    value = it.permanentAddress?.otherAddress?.province.nullable()
                )
                signatoriesDetail.cityPermanentInput = Selector(
                    id = it.permanentAddress?.otherAddress?.city.nullable(),
                    value = it.permanentAddress?.otherAddress?.city.nullable()
                )
            } else {
                signatoriesDetail.provincePermanentInput = Selector(
                    id = it.permanentAddress?.province?.id.nullable(),
                    value = it.permanentAddress?.province?.value.nullable()
                )
                signatoriesDetail.cityPermanentInput = Selector(
                    id = it.permanentAddress?.city?.id.nullable(),
                    value = it.permanentAddress?.city?.value.nullable()
                )
            }
            signatoriesDetail.zipCodePermanentInput = it.permanentAddress?.zipCode.nullable()
            signatoriesDetail.countryPermanentInput = Selector(
                id = it.permanentAddress?.country?.id,
                value = it.permanentAddress?.country?.name
            )
            signatoriesDetail.permanentAddressOtherCity =
                it.permanentAddress?.otherAddress?.city.nullable()
            signatoriesDetail.permanentAddressOtherProvince =
                it.permanentAddress?.otherAddress?.province.nullable()
            signatoriesDetail.occupationInput =
                Selector(id = it.occupation?.finacleCode, value = it.occupation?.name)
            signatoriesDetail.otherOccupationInput = it.other?.occupation.notEmpty()
            signatoriesDetail.percentOwnershipInput = it.ownershipPercentage.nullable()
            signatoriesDetail.apiSecretJumio = it.jumioApiSecret.nullable()
            signatoriesDetail.apiTokenJumio = it.jumioApiToken.nullable()
            signatoriesDetail.scanReferenceId = it.scanReferenceId.nullable()
            signatoriesDetail.idType = it.jumioIdType.nullable()
            signatoriesDetail.processor = it.processor.nullable()
            signatoriesDetail.employmentStatus = it.employmentStatus ?: "Employed"
            signatoriesDetail.ultimateBeneficialOwner = it.ultimateBeneficialOwner
            signatoriesDetail.authorizedSignatory = it.authorizedSignatory
            signatoriesDetail.jobTitle = it.jobTitle
            signatoriesDetail.status = it.status
            signatoriesDetail.processingRemarks = it.processingRemarks
            signatoriesDetail.unionBankOfficer = it.unionBankOfficer
            signatoriesDetail.page = it.page
            it.page?.let { page ->
                for (i in 0..page) {
                    when (i) {
                        0 -> {
                            signatoriesDetail.hasPersonalInformationOneCurrentInput = true
                        }
                        1 -> {
                            signatoriesDetail.hasPersonalInformationTwoCurrentInput = true
                        }
                        2 -> {
                            signatoriesDetail.hasPersonalInformationThreeCurrentInput = true
                        }
                        3 -> {
                            signatoriesDetail.hasPersonalInformationFourCurrentInput = true
                        }
                        4 -> {
                            signatoriesDetail.hasJumioVerificationCurrentInput = true
                        }
                        5 -> {
                            signatoriesDetail.hasSignatureCurrentInput = true
                        }
                        6 -> {
                            signatoriesDetail.imagePath = buildSignaturePath(it.referenceNumber)
                            signatoriesDetail.hasConfirmationCurrentInput = true
                        }
                    }
                }
            }
        }

        return signatoriesDetail
    }

    private fun buildSignaturePath(referenceNumber: String?) =
        BuildConfig.HOST + "api/${BuildConfig.CLIENT_API_VERSION}/dao/applications/signatories/signature?referenceNumber=$referenceNumber"

    override fun reverse(to: SignatoryDetail): DaoDetailsDto {
        return DaoDetailsDto()
    }

    private fun isPermanentSameWithPresentAddress(
        permanentAddress: PermanentAddress?,
        presentAddress: PresentAddress?
    ): Boolean {
        return permanentAddress?.barangay == presentAddress?.barangay
                && permanentAddress?.city?.id == presentAddress?.city?.id
                && permanentAddress?.province?.id == presentAddress?.province?.id
                && permanentAddress?.street == presentAddress?.street
                && permanentAddress?.unitOrHouseNo == presentAddress?.unitOrHouseNo
                && permanentAddress?.zipCode == presentAddress?.zipCode
                && permanentAddress?.country == presentAddress?.country
    }

    private fun mapSingleSelector(
        daoDetailsDto: DaoDetailsDto,
        signatoryDetail: SignatoryDetail
    ) {
        if (daoDetailsDto.salutation != null && daoDetailsDto.salutation != "") {
            settingsGateway.getSalutations()
                .map { selectors ->
                    val selector = selectors.find { daoDetailsDto.salutation == it.type }
                    return@map selector ?: Selector(
                        id = daoDetailsDto.salutation,
                        value = daoDetailsDto.salutation
                    )
                }
                .doOnSuccess { signatoryDetail.salutationInput = it }
                .subscribe()
        }
        if (daoDetailsDto.gender != null && daoDetailsDto.gender != "") {
            settingsGateway.getGenders()
                .map { selectors ->
                    val selector = selectors.find { daoDetailsDto.gender == it.id }
                    return@map selector ?: Selector(
                        id = daoDetailsDto.gender,
                        value = daoDetailsDto.gender
                    )
                }
                .doOnSuccess { signatoryDetail.genderInput = it }
                .subscribe()
        }
        if (daoDetailsDto.civilStatus != null && daoDetailsDto.civilStatus != "") {
            settingsGateway.getCivilStatuses()
                .map { selectors ->
                    val selector = selectors.find { daoDetailsDto.civilStatus == it.id }
                    return@map selector ?: Selector(
                        id = daoDetailsDto.civilStatus,
                        value = daoDetailsDto.civilStatus
                    )
                }
                .doOnSuccess { signatoryDetail.civilStatusInput = it }
                .subscribe()
        }
        if (daoDetailsDto.nationality != null) {
            settingsGateway.getNationality()
                .map { selectors ->
                    val selector = selectors.find { daoDetailsDto.nationality == it.id }
                    return@map selector ?: Selector(
                        id = daoDetailsDto.nationality,
                        value = daoDetailsDto.nationality
                    )
                }
                .doOnSuccess { signatoryDetail.nationalityInput = it }
                .subscribe()
        }
        if (daoDetailsDto.government?.id != null) {
            settingsGateway.getGovernmentIds()
                .map { selectors ->
                    val selector = selectors.find { daoDetailsDto.government?.id == it.id?.toInt() }
                    return@map selector ?: Selector(
                        id = daoDetailsDto.government?.id.toString(),
                        value = daoDetailsDto.government?.id.toString()
                    )
                }
                .doOnSuccess { signatoryDetail.governmentIdTypeInput = it }
                .subscribe()
        }
        if (daoDetailsDto.us?.recordType != null) {
            settingsGateway.getRecordTypes()
                .map { selectors ->
                    val selector = selectors.find { daoDetailsDto.us?.recordType == it.id }
                    return@map selector ?: Selector(
                        id = daoDetailsDto.us?.recordType,
                        value = daoDetailsDto.us?.recordType
                    )
                }
                .doOnSuccess { signatoryDetail.recordTypeInput = it }
                .subscribe()
        }
        if (daoDetailsDto.fundsSource != null && daoDetailsDto.fundsSource != "") {
            settingsGateway.getSourceOfFunds()
                .map { selectors ->
                    val selector = selectors.find { daoDetailsDto.fundsSource == it.id }
                    return@map selector ?: Selector(
                        id = daoDetailsDto.fundsSource,
                        value = daoDetailsDto.fundsSource
                    )
                }
                .doOnSuccess { signatoryDetail.sourceOfFundsInput = it }
                .subscribe()
        }
    }

}