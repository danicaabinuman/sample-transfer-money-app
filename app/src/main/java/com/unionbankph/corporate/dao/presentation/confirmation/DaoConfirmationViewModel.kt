package com.unionbankph.corporate.dao.presentation.confirmation

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.getDisposableSingleObserver
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.dao.data.form.DaoForm
import com.unionbankph.corporate.dao.domain.interactor.ClearDaoCache
import com.unionbankph.corporate.dao.domain.interactor.SubmitDao
import com.unionbankph.corporate.dao.domain.model.DaoHit
import com.unionbankph.corporate.dao.presentation.DaoViewModel
import com.unionbankph.corporate.settings.presentation.form.Selector
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import java.io.File
import javax.inject.Inject

/**
 * Created by herald25santos on 3/17/20
 */
class DaoConfirmationViewModel @Inject constructor(
    private val context: Context,
    private val submitDao: SubmitDao,
    private val clearDaoCache: ClearDaoCache
) : BaseViewModel() {

    val isLoadedScreen = BehaviorSubject.create<Boolean>()

    val isCheckedTNC = BehaviorSubject.createDefault(false)

    val homeAddress = BehaviorSubject.create<String>()

    private val _navigateNextStep = MutableLiveData<Event<DaoHit>>()

    val navigateNextStep: LiveData<Event<DaoHit>> get() = _navigateNextStep

    var daoForm = DaoForm()

    var input: Input = Input()

    inner class Input {
        // Personal Info 1
        var salutationInput = BehaviorSubject.createDefault(
            Selector(id = "1", value = context.formatString(R.string.title_mr))
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

        // Personal Info 2
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
        var usCitizenshipInput = BehaviorSubject.createDefault(true)
        var recordTypeInput = BehaviorSubject.create<Selector>()
        var usRecordInput = BehaviorSubject.create<String>()

        // Personal Info 3
        var permanentAddressInput = BehaviorSubject.createDefault(true)
        var homeAddressInput = BehaviorSubject.create<String>()
        var streetNameInput = BehaviorSubject.create<String>()
        var villageBarangayInput = BehaviorSubject.create<String>()
        var provinceInput = BehaviorSubject.create<Selector>()
        var cityInput = BehaviorSubject.create<Selector>()
        var countryInput = BehaviorSubject.createDefault(Constant.getDefaultCountryDao())
        var homeAddressPermanentInput = BehaviorSubject.create<String>()
        var streetNamePermanentInput = BehaviorSubject.create<String>()
        var villageBarangayPermanentInput = BehaviorSubject.create<String>()
        var provincePermanentInput = BehaviorSubject.create<Selector>()
        var cityPermanentInput = BehaviorSubject.create<Selector>()
        var countryPermanentInput = BehaviorSubject.createDefault(Constant.getDefaultCountryDao())

        // Personal Info 4
        var occupationInput = BehaviorSubject.create<Selector>()
        var otherOccupationInput = BehaviorSubject.create<String>()
        var sourceOfFundsInput = BehaviorSubject.create<Selector>()
        var percentOwnershipInput = BehaviorSubject.create<String>()

        // Jumio Verification
        var idTypeInput = BehaviorSubject.create<String>()
        var scanReferenceInput = BehaviorSubject.create<String>()

        // Signature
        var fileInput = BehaviorSubject.create<File>()
        var imagePath = BehaviorSubject.create<String>()
    }

    fun setExistingPersonalInformationStepOne(input: DaoViewModel.Input1) {
        this.input.salutationInput = input.salutationInput
        this.input.firstNameInput = input.firstNameInput
        this.input.middleNameInput = input.middleNameInput
        this.input.lastNameInput = input.lastNameInput
        this.input.emailAddressInput = input.emailAddressInput
        this.input.countryCodeInput = input.countryCodeInput
        this.input.businessMobileNumberInput = input.businessMobileNumberInput
        this.input.genderInput = input.genderInput
        this.input.civilStatusInput = input.civilStatusInput
    }

    fun setExistingPersonalInformationStepTwo(input: DaoViewModel.Input2) {
        this.input.governmentIdTypeInput = input.governmentIdTypeInput
        this.input.governmentIdNumberInput = input.governmentIdNumberInput
        this.input.dateOfBirthInput = input.dateOfBirthInput
        this.input.placeOfBirthInput = input.placeOfBirthInput
        this.input.nationalityInput = input.nationalityInput
        this.input.usCitizenshipInput = input.usCitizenshipInput
        this.input.recordTypeInput = input.recordTypeInput
        this.input.usRecordInput = input.usRecordInput
    }

    fun loadDaoForm(daoForm: DaoForm) {
        this.daoForm = daoForm
    }

    fun setExistingPersonalInformationStepThree(input: DaoViewModel.Input3) {
        input.homeAddressInput.value?.let { this.input.homeAddressInput.onNext(it) }
        input.streetNameInput.value?.let { this.input.streetNameInput.onNext(it) }
        input.villageBarangayInput.value?.let { this.input.villageBarangayInput.onNext(it) }
        input.provinceInput.value?.let { this.input.provinceInput.onNext(it) }
        input.cityInput.value?.let { this.input.cityInput.onNext(it) }
        input.countryInput.value?.let { this.input.countryInput.onNext(it) }
        input.permanentAddressInput.value?.let { this.input.permanentAddressInput.onNext(it) }
        input.homeAddressPermanentInput.value?.let { this.input.homeAddressPermanentInput.onNext(it) }
        input.streetNamePermanentInput.value?.let { this.input.streetNamePermanentInput.onNext(it) }
        input.villageBarangayPermanentInput.value?.let {
            this.input.villageBarangayPermanentInput.onNext(it)
        }
        input.provincePermanentInput.value?.let { this.input.provincePermanentInput.onNext(it) }
        input.cityPermanentInput.value?.let { this.input.cityPermanentInput.onNext(it) }
        input.countryPermanentInput.value?.let { this.input.countryPermanentInput.onNext(it) }
        input.permanentAddressInput.value?.let {
            this.input.permanentAddressInput.onNext(it)
            if (!it) {
                this.homeAddress.onNext(
                    "${input.homeAddressPermanentInput.value.notNullable()} " +
                            "${input.streetNamePermanentInput.value.notNullable()} " +
                            "${input.villageBarangayPermanentInput.value.notNullable()} " +
                            "${input.cityPermanentInput.value?.value.notNullable()}, " +
                            input.provincePermanentInput.value?.value.notNullable()
                )
            } else {
                this.homeAddress.onNext(
                    "${input.homeAddressInput.value.notNullable()} " +
                            "${input.streetNameInput.value.notNullable()} " +
                            "${input.villageBarangayInput.value.notNullable()} " +
                            "${input.cityInput.value?.value.notNullable()}, " +
                            input.provinceInput.value?.value.notNullable()
                )
            }
        }
    }

    fun setExistingPersonalInformationStepFour(input: DaoViewModel.Input4) {
        this.input.occupationInput = input.occupationInput
        this.input.otherOccupationInput = input.otherOccupationInput
        this.input.sourceOfFundsInput = input.sourceOfFundsInput
        this.input.percentOwnershipInput = input.percentOwnershipInput
    }

    fun setExistingJumioVerification(input: DaoViewModel.Input5) {
        this.input.idTypeInput = input.idTypeInput
        this.input.scanReferenceInput = input.scanReferenceInput
    }

    fun setExistingSignature(input: DaoViewModel.Input6) {
        input.file.value?.let {  this.input.fileInput.onNext(it)  }
        input.imagePath.value?.let {  this.input.imagePath.onNext(it)  }
    }

    fun onClickedNext() {
        val daoForm = daoForm.apply {
            saveType = "final"
            accountOpeningTnc = isCheckedTNC.value
            page?.let {
                page = if (it > 6) page else 7
            }
        }
        submitDao.execute(
            getDisposableSingleObserver(
                {
                    isLoadedScreen.onNext(true)
                    _navigateNextStep.value = Event(it)
                }, {
                    _uiState.value = Event(UiState.Error(it))
                }
            ),
            params = daoForm,
            doOnSubscribeEvent = {
                _uiState.value = Event(UiState.Loading)
            },
            doFinallyEvent = {
                _uiState.value = Event(UiState.Complete)
            }
        ).addTo(disposables)
    }

    fun clearDaoCache() {
        clearDaoCache.execute()
            .subscribe(
                {
                    _uiState.value = Event(UiState.Exit)
                }, {
                    Timber.e(it, "clearDaoCache")
                    _uiState.value = Event(UiState.Error(it))
                }
            ).addTo(disposables)
    }

}
