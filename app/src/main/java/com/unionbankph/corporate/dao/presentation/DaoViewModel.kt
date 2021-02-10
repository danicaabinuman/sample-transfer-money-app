package com.unionbankph.corporate.dao.presentation

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.getDisposableSingleObserver
import com.unionbankph.corporate.app.common.extension.notEmpty
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.platform.bus.event.EventBus
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.dao.data.form.DaoForm
import com.unionbankph.corporate.dao.data.form.PermanentAddressForm
import com.unionbankph.corporate.dao.data.form.PresentAddressForm
import com.unionbankph.corporate.dao.data.model.Government
import com.unionbankph.corporate.dao.data.model.Name
import com.unionbankph.corporate.dao.data.model.Other
import com.unionbankph.corporate.dao.data.model.OtherAddress
import com.unionbankph.corporate.dao.data.model.US
import com.unionbankph.corporate.dao.domain.interactor.GetSignatoryDetailsFromCache
import com.unionbankph.corporate.dao.domain.model.SignatoryDetail
import com.unionbankph.corporate.dao.presentation.jumio_verification.DaoJumioVerificationViewModel
import com.unionbankph.corporate.dao.presentation.personal_info_1.DaoPersonalInformationStepOneViewModel
import com.unionbankph.corporate.dao.presentation.personal_info_2.DaoPersonalInformationStepTwoViewModel
import com.unionbankph.corporate.dao.presentation.personal_info_3.DaoPersonalInformationStepThreeViewModel
import com.unionbankph.corporate.dao.presentation.personal_info_4.DaoPersonalInformationStepFourViewModel
import com.unionbankph.corporate.dao.presentation.signature.DaoSignatureViewModel
import com.unionbankph.corporate.settings.presentation.form.Selector
import io.reactivex.Single
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import java.io.File
import javax.inject.Inject

/**
 * Created by herald25santos on 3/17/20
 */
class DaoViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val context: Context,
    private val getSignatoryDetailsFromCache: GetSignatoryDetailsFromCache
) : BaseViewModel() {

    private val _navigatePages = MutableLiveData<Event<NavigatePages>>()

    val navigatePages: LiveData<Event<NavigatePages>> get() = _navigatePages

    val isShowButton = BehaviorSubject.create<Boolean>()
    val isEnableButton = BehaviorSubject.create<Boolean>()
    val buttonName = BehaviorSubject.createDefault(context.formatString(R.string.action_next))

    val tokenDeepLink = BehaviorSubject.create<String>()
    val referenceNumber = BehaviorSubject.create<String>()

    // Personal Information 1
    var input1: Input1 = Input1()
    var hasEditPersonalInformationOneInput = BehaviorSubject.create<Boolean>()
    var hasPersonalInformationOneInput = BehaviorSubject.create<Boolean>()

    var apiTokenJumio = BehaviorSubject.create<String>()
    var apiSecretJumio = BehaviorSubject.create<String>()
    var signatoriesDetail = BehaviorSubject.create<SignatoryDetail>()

    var hasAgreedToPrivatePolicyInput = BehaviorSubject.createDefault(true)
    var hasAgreedToTermsAndConditionInput = BehaviorSubject.createDefault(true)
    var hasAgreedToAccountOpeningTncInput = BehaviorSubject.createDefault(false)

    inner class Input1 {
        var salutationInput = BehaviorSubject.createDefault(
            Selector(value = context.formatString(R.string.title_mr))
        )
        var firstNameInput = BehaviorSubject.create<String>()
        var middleNameInput = BehaviorSubject.create<String>()
        var lastNameInput = BehaviorSubject.create<String>()
        var emailAddressInput = BehaviorSubject.create<String>()
        var countryCodeInput =
            BehaviorSubject.createDefault(Constant.getDefaultCountryCode())
        var businessMobileNumberInput = BehaviorSubject.create<String>()
        var genderInput = BehaviorSubject.create<Selector>()
        var civilStatusInput = BehaviorSubject.create<Selector>()
    }

    // Personal Information 2
    var input2: Input2 = Input2()
    var hasEditPersonalInformationTwoInput = BehaviorSubject.create<Boolean>()
    var hasPersonalInformationTwoInput = BehaviorSubject.create<Boolean>()

    inner class Input2 {
        var governmentIdTypeInput =
            BehaviorSubject.createDefault(
                Selector(
                    id = "1",
                    value = context.formatString(R.string.title_tin)
                )
            )
        var governmentIdNumberInput = BehaviorSubject.create<String>()
        var dateOfBirthInput = BehaviorSubject.create<String>()
        var placeOfBirthInput = BehaviorSubject.create<String>()
        var nationalityInput = BehaviorSubject.create<Selector>()
        var usCitizenshipInput = BehaviorSubject.createDefault(false)
        var recordTypeInput = BehaviorSubject.create<Selector>()
        var usRecordInput = BehaviorSubject.create<String>()
    }

    // Personal Information 3
    var input3: Input3 = Input3()
    var hasEditPersonalInformationThreeInput = BehaviorSubject.create<Boolean>()
    var hasPersonalInformationThreeInput = BehaviorSubject.create<Boolean>()

    inner class Input3 {
        var permanentAddressInput = BehaviorSubject.createDefault(true)
        var homeAddressInput = BehaviorSubject.create<String>()
        var streetNameInput = BehaviorSubject.create<String>()
        var villageBarangayInput = BehaviorSubject.create<String>()
        var provinceInput = BehaviorSubject.create<Selector>()
        var cityInput = BehaviorSubject.create<Selector>()
        var zipCodeInput = BehaviorSubject.create<String>()
        var countryInput = BehaviorSubject.createDefault(Constant.getDefaultCountryDao())
        var homeAddressPermanentInput = BehaviorSubject.create<String>()
        var streetNamePermanentInput = BehaviorSubject.create<String>()
        var villageBarangayPermanentInput = BehaviorSubject.create<String>()
        var provincePermanentInput = BehaviorSubject.create<Selector>()
        var cityPermanentInput = BehaviorSubject.create<Selector>()
        var zipCodePermanentInput = BehaviorSubject.create<String>()
        var countryPermanentInput = BehaviorSubject.createDefault(Constant.getDefaultCountryDao())
    }

    // Personal Information 4
    var input4: Input4 = Input4()
    var hasEditPersonalInformationFourInput = BehaviorSubject.create<Boolean>()
    var hasPersonalInformationFourInput = BehaviorSubject.create<Boolean>()

    inner class Input4 {
        var occupationInput = BehaviorSubject.create<Selector>()
        var sourceOfFundsInput = BehaviorSubject.create<Selector>()
        var otherOccupationInput = BehaviorSubject.create<String>()
        var percentOwnershipInput = BehaviorSubject.create<String>()
    }

    var input5 = Input5()
    var hasEditJumioVerificationInput = BehaviorSubject.create<Boolean>()
    var hasJumioVerificationInput = BehaviorSubject.create<Boolean>()

    inner class Input5 {
        var idTypeInput = BehaviorSubject.create<String>()
        var scanReferenceInput = BehaviorSubject.create<String>()
    }

    var input6 = Input6()
    var hasEditSignatureInput = BehaviorSubject.create<Boolean>()
    var hasSignatureInput = BehaviorSubject.create<Boolean>()

    inner class Input6 {
        var file = BehaviorSubject.create<File>()
        var imagePath = BehaviorSubject.create<String>()
    }

    var hasEditConfirmationInput = BehaviorSubject.create<Boolean>()
    var hasConfirmationInput = BehaviorSubject.create<Boolean>()

    fun defaultDaoForm(): DaoForm {
        return DaoForm().apply {
            privatePolicy = hasAgreedToPrivatePolicyInput.value
            termsAndCondition = hasAgreedToTermsAndConditionInput.value
            accountOpeningTnc = hasAgreedToAccountOpeningTncInput.value
            countryCode = input1.countryCodeInput.value?.id
            mobile = input1.businessMobileNumberInput.value
            salutation = input1.salutationInput.value?.id
            email = input1.emailAddressInput.value
            gender = input1.genderInput.value?.id
            name = Name(
                input1.lastNameInput.value,
                input1.middleNameInput.value,
                input1.firstNameInput.value
            )
            civilStatus = input1.civilStatusInput.value?.id
            nationality = input2.nationalityInput.value?.id
            government = Government(
                input2.governmentIdTypeInput.value?.id?.toInt(),
                input2.governmentIdNumberInput.value
            )
            birthdate = input2.dateOfBirthInput.value
            birthplace = input2.placeOfBirthInput.value
            us = US(input2.recordTypeInput.value?.id ?: "0001", input2.usCitizenshipInput.value)
            mothersMaidenName = "-"
            addressRecord = input2.usRecordInput.value
            presentAddress = PresentAddressForm(
                input3.villageBarangayInput.value,
                if (Constant.getDefaultCountryDao().id != input3.countryInput.value?.id) {
                    null
                } else {
                    input3.cityInput.value?.id
                },
                if (Constant.getDefaultCountryDao().id != input3.countryInput.value?.id) {
                    null
                } else {
                    input3.provinceInput.value?.id
                },
                input3.streetNameInput.value,
                input3.homeAddressInput.value,
                input3.zipCodeInput.value,
                input3.countryInput.value?.id,
                OtherAddress(
                    signatoriesDetail.value?.presentAddressOtherCity,
                    signatoriesDetail.value?.presentAddressOtherProvince
                )
            )
            permanentAddress = PermanentAddressForm(
                input3.villageBarangayPermanentInput.value,
                if (Constant.getDefaultCountryDao().id != input3.countryPermanentInput.value?.id) {
                    null
                } else {
                    input3.cityPermanentInput.value?.id
                },
                if (Constant.getDefaultCountryDao().id != input3.countryPermanentInput.value?.id) {
                    null
                } else {
                    input3.provincePermanentInput.value?.id
                },
                input3.streetNamePermanentInput.value,
                input3.homeAddressPermanentInput.value,
                input3.zipCodePermanentInput.value,
                input3.countryPermanentInput.value?.id,
                OtherAddress(
                    signatoriesDetail.value?.permanentAddressOtherCity,
                    signatoriesDetail.value?.permanentAddressOtherProvince
                )
            )
            occupation = input4.occupationInput.value?.id
            other = Other(occupation = input4.otherOccupationInput.value.notEmpty())
            fundsSource = input4.sourceOfFundsInput.value?.id
            ownershipPercentage = input4.percentOwnershipInput.value
            scanReferenceId = input5.scanReferenceInput.value
            jumioIdType = input5.idTypeInput.value
            allow = signatoriesDetail.value?.allow
            processor = signatoriesDetail.value?.processor
            employmentStatus = signatoriesDetail.value?.employmentStatus
            ultimateBeneficialOwner = signatoriesDetail.value?.ultimateBeneficialOwner
            authorizedSignatory = signatoriesDetail.value?.authorizedSignatory
            jobTitle = signatoriesDetail.value?.jobTitle
            processingRemarks = signatoriesDetail.value?.processingRemarks
            status = signatoriesDetail.value?.status
            page = signatoriesDetail.value?.page
            saveType = "draft"
        }
    }

    fun getSignatoryDetailsFromCache() {
        getSignatoryDetailsFromCache.execute(
            getDisposableSingleObserver(
                {
                    setSignatoriesDetail(it)
                }, {
                    Timber.e(it, "getSignatoryDetailsFromCache")
                }
            )
        ).addTo(disposables)
    }

    fun setPersonalInformationStepOneInput(input: DaoPersonalInformationStepOneViewModel.Input) {
        input.salutationInput.value?.let { input1.salutationInput.onNext(it) }
        input.firstNameInput.value?.let { input1.firstNameInput.onNext(it) }
        input.middleNameInput.value?.let { input1.middleNameInput.onNext(it) }
        input.lastNameInput.value?.let { input1.lastNameInput.onNext(it) }
        input.emailAddressInput.value?.let { input1.emailAddressInput.onNext(it) }
        input.countryCodeInput.value?.let { input1.countryCodeInput.onNext(it) }
        input.businessMobileNumberInput.value?.let { input1.businessMobileNumberInput.onNext(it) }
        input.genderInput.value?.let { input1.genderInput.onNext(it) }
        input.civilStatusInput.value?.let { input1.civilStatusInput.onNext(it) }
        hasPersonalInformationOneInput.onNext(true)
    }

    fun setPersonalInformationStepTwoInput(input: DaoPersonalInformationStepTwoViewModel.Input) {
        input.governmentIdTypeInput.value?.let { input2.governmentIdTypeInput.onNext(it) }
        input.governmentIdNumberInput.value?.let { input2.governmentIdNumberInput.onNext(it) }
        input.dateOfBirthInput.value?.let { input2.dateOfBirthInput.onNext(it) }
        input.placeOfBirthInput.value?.let { input2.placeOfBirthInput.onNext(it) }
        input.nationalityInput.value?.let { input2.nationalityInput.onNext(it) }
        input.usCitizenshipInput.value?.let { input2.usCitizenshipInput.onNext(it) }
        input.recordTypeInput.value?.let { input2.recordTypeInput.onNext(it) }
        input.usRecordInput.value?.let { input2.usRecordInput.onNext(it) }
        hasPersonalInformationTwoInput.onNext(true)
    }

    fun setPersonalInformationStepThreeInput(input: DaoPersonalInformationStepThreeViewModel.Input) {
        input.permanentAddressInput.value?.let { permanentAddressInput ->
            input3.permanentAddressInput.onNext(permanentAddressInput)
            if (permanentAddressInput) {
                input.homeAddressInput.value?.let {
                    input3.homeAddressInput.onNext(it)
                    input3.homeAddressPermanentInput.onNext(it)
                }
                input.streetNameInput.value?.let {
                    input3.streetNameInput.onNext(it)
                    input3.streetNamePermanentInput.onNext(it)
                }
                input.villageBarangayInput.value?.let {
                    input3.villageBarangayInput.onNext(it)
                    input3.villageBarangayPermanentInput.onNext(it)
                }
                input.provinceInput.value?.let {
                    input3.provinceInput.onNext(it)
                    input3.provincePermanentInput.onNext(it)
                }
                input.cityInput.value?.let {
                    input3.cityInput.onNext(it)
                    input3.cityPermanentInput.onNext(it)
                }
                input.zipCodeInput.value?.let {
                    input3.zipCodeInput.onNext(it)
                    input3.zipCodePermanentInput.onNext(it)
                }
                input.countryInput.value?.let {
                    input3.countryInput.onNext(it)
                    input3.countryPermanentInput.onNext(it)
                }
            } else {
                input.homeAddressInput.value?.let { input3.homeAddressInput.onNext(it) }
                input.streetNameInput.value?.let { input3.streetNameInput.onNext(it) }
                input.villageBarangayInput.value?.let { input3.villageBarangayInput.onNext(it) }
                input.provinceInput.value?.let { input3.provinceInput.onNext(it) }
                input.cityInput.value?.let { input3.cityInput.onNext(it) }
                input.zipCodeInput.value?.let { input3.zipCodeInput.onNext(it) }
                input.countryInput.value?.let { input3.countryInput.onNext(it) }

                input.homeAddressPermanentInput.value?.let {
                    input3.homeAddressPermanentInput.onNext(
                        it
                    )
                }
                input.streetNamePermanentInput.value?.let {
                    input3.streetNamePermanentInput.onNext(
                        it
                    )
                }
                input.villageBarangayPermanentInput.value?.let {
                    input3.villageBarangayPermanentInput.onNext(
                        it
                    )
                }
                input.provincePermanentInput.value?.let { input3.provincePermanentInput.onNext(it) }
                input.cityPermanentInput.value?.let { input3.cityPermanentInput.onNext(it) }
                input.zipCodePermanentInput.value?.let { input3.zipCodePermanentInput.onNext(it) }
                input.countryPermanentInput.value?.let { input3.countryPermanentInput.onNext(it) }
            }
        }
        hasPersonalInformationThreeInput.onNext(true)
    }

    fun setPersonalInformationStepFourInput(input: DaoPersonalInformationStepFourViewModel.Input) {
        input.occupationInput.value?.let { input4.occupationInput.onNext(it) }
        input.otherOccupationInput.value?.let { input4.otherOccupationInput.onNext(it) }
        input.sourceOfFundsInput.value?.let { input4.sourceOfFundsInput.onNext(it) }
        input.percentOwnershipInput.value?.let { input4.percentOwnershipInput.onNext(it) }
        hasPersonalInformationFourInput.onNext(true)
    }

    fun setJumioVerificationInput(input: DaoJumioVerificationViewModel.Input) {
        input.idTypeInput.value?.let { input5.idTypeInput.onNext(it) }
        input.scanReferenceInput.value?.let { input5.scanReferenceInput.onNext(it) }
        hasJumioVerificationInput.onNext(true)
    }

    fun setSignatureInput(input: DaoSignatureViewModel.Input) {
        input.fileInput.value?.let { input6.file.onNext(it) }
        input.imagePath.value?.let { input6.imagePath.onNext(it) }
        hasSignatureInput.onNext(true)
    }

    fun setSignatoriesDetail(signatoryDetail: SignatoryDetail) {
        Single.fromCallable { signatoryDetail }
            .map { plotDetails(it) }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                {
                    _navigatePages.value = Event(it)
                }, {
                    Timber.e(it, "setSignatoriesDetail")
                    _uiState.value = Event(UiState.Error(it))
                }
            ).addTo(disposables)
    }

    private fun plotDetails(signatoryDetail: SignatoryDetail): NavigatePages {
        signatoryDetail.let {
            this.signatoriesDetail.onNext(it)
            it.referenceNumber?.let { value -> referenceNumber.onNext(value) }
            it.privatePolicy?.let { value -> hasAgreedToPrivatePolicyInput.onNext(value) }
            it.termsAndCondition?.let { value -> hasAgreedToTermsAndConditionInput.onNext(value) }
            it.accountOpeningTnc?.let { value -> hasAgreedToAccountOpeningTncInput.onNext(value) }

            it.salutationInput?.let { value -> input1.salutationInput.onNext(value) }
            it.firstNameInput?.let { value ->
                input1.firstNameInput.onNext(value)
                hasPersonalInformationOneInput.onNext(true)
            }
            it.middleNameInput?.let { value -> input1.middleNameInput.onNext(value) }
            it.lastNameInput?.let { value -> input1.lastNameInput.onNext(value) }
            it.emailAddressInput?.let { value -> input1.emailAddressInput.onNext(value) }
            it.countryCodeInput?.let { value -> input1.countryCodeInput.onNext(value) }
            it.businessMobileNumberInput?.let { value ->
                input1.businessMobileNumberInput.onNext(value)
            }
            it.genderInput?.let { value -> input1.genderInput.onNext(value) }
            it.civilStatusInput?.let { value -> input1.civilStatusInput.onNext(value) }

            it.governmentIdTypeInput?.let { value -> input2.governmentIdTypeInput.onNext(value) }
            it.governmentIdNumberInput?.let { value ->
                input2.governmentIdNumberInput.onNext(value)
                hasPersonalInformationTwoInput.onNext(true)
            }
            it.dateOfBirthInput?.let { value -> input2.dateOfBirthInput.onNext(value) }
            it.placeOfBirthInput?.let { value -> input2.placeOfBirthInput.onNext(value) }
            it.nationalityInput?.let { value -> input2.nationalityInput.onNext(value) }
            it.usCitizenshipInput?.let { value -> input2.usCitizenshipInput.onNext(value) }
            it.recordTypeInput?.let { value -> input2.recordTypeInput.onNext(value) }
            it.usRecordInput?.let { value -> input2.usRecordInput.onNext(value) }

            it.permanentAddress?.let { value -> input3.permanentAddressInput.onNext(value) }
            it.homeAddressInput?.let { value ->
                input3.homeAddressInput.onNext(value)
                hasPersonalInformationThreeInput.onNext(true)
            }
            it.streetNameInput?.let { value -> input3.streetNameInput.onNext(value) }
            it.villageBarangayInput?.let { value -> input3.villageBarangayInput.onNext(value) }
            it.provinceInput?.let { value -> input3.provinceInput.onNext(value) }
            it.cityInput?.let { value -> input3.cityInput.onNext(value) }
            it.zipCodeInput?.let { value -> input3.zipCodeInput.onNext(value) }
            it.countryInput?.let { value -> input3.countryInput.onNext(value) }
            it.homeAddressPermanentInput?.let { value ->
                input3.homeAddressPermanentInput.onNext(value)
            }
            it.streetNamePermanentInput?.let { value -> input3.streetNamePermanentInput.onNext(value) }
            it.villageBarangayPermanentInput?.let { value ->
                input3.villageBarangayPermanentInput.onNext(value)
            }
            it.provincePermanentInput?.let { value -> input3.provincePermanentInput.onNext(value) }
            it.cityPermanentInput?.let { value -> input3.cityPermanentInput.onNext(value) }
            it.zipCodePermanentInput?.let { value -> input3.zipCodePermanentInput.onNext(value) }
            it.countryPermanentInput?.let { value -> input3.countryPermanentInput.onNext(value) }

            it.otherOccupationInput?.let { value -> input4.otherOccupationInput.onNext(value) }
            it.occupationInput?.let { value -> input4.occupationInput.onNext(value) }
            it.sourceOfFundsInput?.let { value -> input4.sourceOfFundsInput.onNext(value) }
            it.percentOwnershipInput?.let { value ->
                hasPersonalInformationFourInput.onNext(true)
                input4.percentOwnershipInput.onNext(value)
            }

            it.apiTokenJumio?.let { value -> apiTokenJumio.onNext(value) }
            it.apiSecretJumio?.let { value -> apiSecretJumio.onNext(value) }
            it.scanReferenceId?.let { value ->
                input5.scanReferenceInput.onNext(value)
                hasJumioVerificationInput.onNext(true)
            }
            it.idType?.let { value -> input5.idTypeInput.onNext(value) }
            it.imagePath?.let { value ->
                input6.imagePath.onNext(value)
                hasSignatureInput.onNext(true)
            }
            it.hasPersonalInformationOneCurrentInput?.let { value ->
                hasEditPersonalInformationOneInput.onNext(value)
            }
            it.hasPersonalInformationTwoCurrentInput?.let { value ->
                hasEditPersonalInformationTwoInput.onNext(value)
            }
            it.hasPersonalInformationThreeCurrentInput?.let { value ->
                hasEditPersonalInformationThreeInput.onNext(value)
            }
            it.hasPersonalInformationFourCurrentInput?.let { value ->
                hasEditPersonalInformationFourInput.onNext(value)
            }
            it.hasJumioVerificationCurrentInput?.let { value ->
                hasEditJumioVerificationInput.onNext(value)
            }
            it.hasSignatureCurrentInput?.let { value ->
                hasEditSignatureInput.onNext(value)
            }
            it.hasConfirmationCurrentInput?.let { value ->
                hasEditConfirmationInput.onNext(value)
            }
            return NavigatePages(
                hasEditPersonalInformationOneInput.value.notNullable(),
                hasEditPersonalInformationTwoInput.value.notNullable(),
                hasEditPersonalInformationThreeInput.value.notNullable(),
                hasEditPersonalInformationFourInput.value.notNullable(),
                hasEditJumioVerificationInput.value.notNullable(),
                hasEditSignatureInput.value.notNullable(),
                hasEditConfirmationInput.value.notNullable()
            )
        }

    }

}
