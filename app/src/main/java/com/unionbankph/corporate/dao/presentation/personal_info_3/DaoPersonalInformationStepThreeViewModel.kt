package com.unionbankph.corporate.dao.presentation.personal_info_3

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.getDisposableSingleObserver
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.dao.data.form.DaoForm
import com.unionbankph.corporate.dao.data.form.PermanentAddressForm
import com.unionbankph.corporate.dao.data.form.PresentAddressForm
import com.unionbankph.corporate.dao.data.model.OtherAddress
import com.unionbankph.corporate.dao.domain.form.GetCityForm
import com.unionbankph.corporate.dao.domain.interactor.GetCityById
import com.unionbankph.corporate.dao.domain.interactor.GetProvinceById
import com.unionbankph.corporate.dao.domain.interactor.SubmitDao
import com.unionbankph.corporate.dao.domain.model.DaoHit
import com.unionbankph.corporate.dao.presentation.DaoViewModel
import com.unionbankph.corporate.settings.presentation.form.Selector
import com.unionbankph.corporate.settings.presentation.single_selector.SingleSelectorTypeEnum
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

/**
 * Created by herald25santos on 3/17/20
 */
class DaoPersonalInformationStepThreeViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val context: Context,
    private val submitDao: SubmitDao,
    private val getProvinceById: GetProvinceById,
    private val getCityById: GetCityById
) : BaseViewModel() {

    val isLoadedScreen = BehaviorSubject.create<Boolean>()

    val isEditMode = BehaviorSubject.create<Boolean>()

    var input: Input = Input()

    var output: Output = Output()

    var daoForm = DaoForm()

    private var currentInput: CurrentInput = CurrentInput()

    inner class Input {
        val isValidFormInput = BehaviorSubject.create<Boolean>()
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

    inner class CurrentInput {
        var permanentAddressInput: Boolean? = input.permanentAddressInput.value
        var homeAddressInput: String? = ""
        var streetNameInput: String? = ""
        var villageBarangayInput: String? = ""
        var provinceInput: String? = null
        var cityInput: String? = null
        var zipCodeInput: String? = ""
        var countryInput: String? = input.countryInput.value?.id
        var homeAddressPermanentInput: String? = ""
        var streetNamePermanentInput: String? = ""
        var villageBarangayPermanentInput: String? = ""
        var provincePermanentInput: String? = null
        var cityPermanentInput: String? = null
        var zipCodePermanentInput: String? = ""
        var countryPermanentInput: String? = input.countryPermanentInput.value?.id
    }

    inner class Output {
        var _loadingField = MutableLiveData<Event<SingleSelectorTypeEnum>>()
        val loadingField: LiveData<Event<SingleSelectorTypeEnum>> = _loadingField
        var _dismissLoadingField = MutableLiveData<Event<SingleSelectorTypeEnum>>()
        val dismissLoadingField: LiveData<Event<SingleSelectorTypeEnum>> = _dismissLoadingField

        var _loadingPermanentField = MutableLiveData<Event<SingleSelectorTypeEnum>>()
        val loadingPermanentField: LiveData<Event<SingleSelectorTypeEnum>> = _loadingPermanentField
        var _dismissLoadingPermanentField = MutableLiveData<Event<SingleSelectorTypeEnum>>()
        val dismissLoadingPermanentField: LiveData<Event<SingleSelectorTypeEnum>> =
            _dismissLoadingPermanentField
    }

    private val _navigateNextStep = MutableLiveData<Event<Input>>()

    val navigateNextStep: LiveData<Event<Input>> get() = _navigateNextStep

    private val _navigateResult = MutableLiveData<Event<DaoHit>>()

    val navigateResult: LiveData<Event<DaoHit>> get() = _navigateResult

    fun hasValidForm() = input.isValidFormInput.value ?: false

    fun loadDaoForm(daoForm: DaoForm) {
        this.daoForm = daoForm
    }

    fun setPreTextValues(
        provinceInput: String?,
        cityInput: String?,
        homeAddressInput: String?,
        streetNameInput: String?,
        villageBarangayInput: String?,
        zipCodeInput: String?,
        provincePermanentInput: String?,
        cityPermanentInput: String?,
        homeAddressPermanentInput: String?,
        streetNamePermanentInput: String?,
        villageBarangayPermanentInput: String?,
        zipCodePermanentInput: String?
    ) {
        input.countryInput.value?.let { countryInput ->
            if (!isPHCountry(countryInput.id)) {
                provinceInput?.let { input.provinceInput.onNext(Selector(id = it, value = it)) }
                cityInput?.let { input.cityInput.onNext(Selector(id = it, value = it)) }
            }
        }
        input.countryPermanentInput.value?.let { countryPermanentInput ->
            if (!isPHCountry(countryPermanentInput.id)) {
                provincePermanentInput?.let {
                    input.provincePermanentInput.onNext(
                        Selector(id = it, value = it)
                    )
                }
                cityPermanentInput?.let {
                    input.cityPermanentInput.onNext(
                        Selector(id = it, value = it)
                    )
                }
            }
        }
        homeAddressInput?.let { input.homeAddressInput.onNext(it) }
        streetNameInput?.let { input.streetNameInput.onNext(it) }
        villageBarangayInput?.let { input.villageBarangayInput.onNext(it) }
        zipCodeInput?.let { input.zipCodeInput.onNext(it) }
        homeAddressPermanentInput?.let { input.homeAddressPermanentInput.onNext(it) }
        streetNamePermanentInput?.let { input.streetNamePermanentInput.onNext(it) }
        villageBarangayPermanentInput?.let { input.villageBarangayPermanentInput.onNext(it) }
        zipCodePermanentInput?.let { input.zipCodePermanentInput.onNext(it) }
    }

    fun setExistingPersonalInformationStepThree(input: DaoViewModel.Input3) {
        input.permanentAddressInput.value?.let { permanentAddressInput ->
            currentInput.permanentAddressInput = permanentAddressInput
            this.input.permanentAddressInput.onNext(permanentAddressInput)
            input.homeAddressInput.value?.let {
                currentInput.homeAddressInput = it
                this.input.homeAddressInput.onNext(it)
            }
            input.streetNameInput.value?.let {
                currentInput.streetNameInput = it
                this.input.streetNameInput.onNext(it)
            }
            input.villageBarangayInput.value?.let {
                currentInput.villageBarangayInput = it
                this.input.villageBarangayInput.onNext(it)
            }
            input.provinceInput.value?.let {
                currentInput.provinceInput = it.id
                this.input.provinceInput.onNext(it)
            }
            input.cityInput.value?.let {
                currentInput.cityInput = it.id
                this.input.cityInput.onNext(it)
            }
            input.zipCodeInput.value?.let {
                currentInput.zipCodeInput = it
                this.input.zipCodeInput.onNext(it)
            }
            input.countryInput.value?.let {
                currentInput.countryInput = it.id
                this.input.countryInput.onNext(it)
            }
            if (!permanentAddressInput) {
                input.homeAddressPermanentInput.value?.let {
                    currentInput.homeAddressPermanentInput = it
                    this.input.homeAddressPermanentInput.onNext(it)
                }
                input.streetNamePermanentInput.value?.let {
                    currentInput.streetNamePermanentInput = it
                    this.input.streetNamePermanentInput.onNext(it)
                }
                input.villageBarangayPermanentInput.value?.let {
                    currentInput.villageBarangayPermanentInput = it
                    this.input.villageBarangayPermanentInput.onNext(it)
                }
                input.provincePermanentInput.value?.let {
                    currentInput.provincePermanentInput = it.id
                    this.input.provincePermanentInput.onNext(it)
                }
                input.cityPermanentInput.value?.let {
                    currentInput.cityPermanentInput = it.id
                    this.input.cityPermanentInput.onNext(it)
                }
                input.zipCodePermanentInput.value?.let {
                    currentInput.zipCodePermanentInput = it
                    this.input.zipCodePermanentInput.onNext(it)
                }
                input.countryPermanentInput.value?.let {
                    currentInput.countryPermanentInput = it.id
                    this.input.countryPermanentInput.onNext(it)
                }
            }
        }
    }

    private fun requestRemotes(singleSelectorEnum: SingleSelectorTypeEnum, param: String?) {
        val interactor =
            if (singleSelectorEnum == SingleSelectorTypeEnum.PROVINCE_PERMANENT ||
                singleSelectorEnum == SingleSelectorTypeEnum.PROVINCE
            ) {
                getProvinceById
            } else {
                getCityById
            }
        val isPermanentField = singleSelectorEnum == SingleSelectorTypeEnum.PROVINCE_PERMANENT
                || singleSelectorEnum == SingleSelectorTypeEnum.CITY_PERMANENT
        interactor.execute(
            getDisposableSingleObserver(
                {
                    parseDetailById(singleSelectorEnum, it)
                }, {
                    _uiState.value = Event(UiState.Error(it))
                }
            ),
            doOnSubscribeEvent = {
                if (isPermanentField) {
                    output._loadingPermanentField.value = Event(singleSelectorEnum)
                } else {
                    output._loadingField.value = Event(singleSelectorEnum)
                }
            },
            doFinallyEvent = {
                if (isPermanentField) {
                    output._dismissLoadingPermanentField.value = Event(singleSelectorEnum)
                } else {
                    output._dismissLoadingField.value = Event(singleSelectorEnum)
                }
            },
            params = param
        ).addTo(disposables)
    }

    private fun parseDetailById(
        singleSelectorEnum: SingleSelectorTypeEnum,
        it: Selector
    ) {
        when (singleSelectorEnum) {
            SingleSelectorTypeEnum.PROVINCE -> {
                this.input.provinceInput.onNext(it)
                val cityId = input.cityInput.value?.id
                val paramGetCity = JsonHelper.toJson(GetCityForm(it.id, cityId))
                requestRemotes(SingleSelectorTypeEnum.CITY, paramGetCity)
            }
            SingleSelectorTypeEnum.CITY -> {
                this.input.cityInput.onNext(it)
            }
            SingleSelectorTypeEnum.PROVINCE_PERMANENT -> {
                this.input.provincePermanentInput.onNext(it)
                val cityId = input.cityPermanentInput.value?.id
                val paramGetCity = JsonHelper.toJson(GetCityForm(it.id, cityId))
                requestRemotes(SingleSelectorTypeEnum.CITY_PERMANENT, paramGetCity)
            }
            SingleSelectorTypeEnum.CITY_PERMANENT -> {
                this.input.cityPermanentInput.onNext(it)
            }
        }
    }

    fun hasFormChanged(): Boolean {
        return if (currentInput.permanentAddressInput == false) {
            input.villageBarangayInput.value != currentInput.villageBarangayInput
                    || input.cityInput.value?.id != currentInput.cityInput
                    || input.provinceInput.value?.id != currentInput.provinceInput
                    || input.streetNameInput.value != currentInput.streetNameInput
                    || input.homeAddressInput.value != currentInput.homeAddressInput
                    || input.zipCodeInput.value != currentInput.zipCodeInput
                    || input.countryInput.value?.id != currentInput.countryInput
                    || input.villageBarangayPermanentInput.value != currentInput.villageBarangayPermanentInput
                    || input.cityPermanentInput.value?.id != currentInput.cityPermanentInput
                    || input.provincePermanentInput.value?.id != currentInput.provincePermanentInput
                    || input.streetNamePermanentInput.value != currentInput.streetNamePermanentInput
                    || input.homeAddressPermanentInput.value != currentInput.homeAddressPermanentInput
                    || input.zipCodePermanentInput.value != currentInput.zipCodePermanentInput
                    || input.countryPermanentInput.value?.id != currentInput.countryPermanentInput
        } else {
            input.villageBarangayInput.value != currentInput.villageBarangayInput
                    || input.cityInput.value?.id != currentInput.cityInput
                    || input.provinceInput.value?.id != currentInput.provinceInput
                    || input.streetNameInput.value != currentInput.streetNameInput
                    || input.homeAddressInput.value != currentInput.homeAddressInput
                    || input.zipCodeInput.value != currentInput.zipCodeInput
                    || input.countryInput.value?.id != currentInput.countryInput
        }
    }

    fun onClickedNext() {
        if (!hasFormChanged()) {
            isLoadedScreen.onNext(true)
            _navigateNextStep.value = Event(input)
        } else {
            val daoForm = daoForm.apply {
                presentAddress = PresentAddressForm(
                    input.villageBarangayInput.value,
                    if (!isPHCountry(input.countryInput.value?.id)) {
                        null
                    } else {
                        input.cityInput.value?.id
                    },
                    if (!isPHCountry(input.countryInput.value?.id)) {
                        null
                    } else {
                        input.provinceInput.value?.id
                    },
                    input.streetNameInput.value,
                    input.homeAddressInput.value,
                    input.zipCodeInput.value,
                    input.countryInput.value?.id,
                    if (!isPHCountry(input.countryInput.value?.id)) {
                        OtherAddress(input.cityInput.value?.id, input.provinceInput.value?.id)
                    } else {
                        daoForm.presentAddress?.otherAddress
                    }
                )
                if (input.permanentAddressInput.value == true) {
                    permanentAddress = PermanentAddressForm(
                        input.villageBarangayInput.value,
                        if (!isPHCountry(input.countryInput.value?.id)) {
                            null
                        } else {
                            input.cityInput.value?.id
                        },
                        if (!isPHCountry(input.countryInput.value?.id)) {
                            null
                        } else {
                            input.provinceInput.value?.id
                        },
                        input.streetNameInput.value,
                        input.homeAddressInput.value,
                        input.zipCodeInput.value,
                        input.countryInput.value?.id,
                        if (!isPHCountry(input.countryInput.value?.id)) {
                            OtherAddress(input.cityInput.value?.id, input.provinceInput.value?.id)
                        } else {
                            daoForm.presentAddress?.otherAddress
                        }
                    )
                } else {
                    permanentAddress = PermanentAddressForm(
                        input.villageBarangayPermanentInput.value,
                        if (!isPHCountry(input.countryPermanentInput.value?.id)) {
                            null
                        } else {
                            input.cityPermanentInput.value?.id
                        },
                        if (!isPHCountry(input.countryPermanentInput.value?.id)) {
                            null
                        } else {
                            input.provincePermanentInput.value?.id
                        },
                        input.streetNamePermanentInput.value,
                        input.homeAddressPermanentInput.value,
                        input.zipCodePermanentInput.value,
                        input.countryPermanentInput.value?.id,
                        if (!isPHCountry(input.countryPermanentInput.value?.id)) {
                            OtherAddress(
                                input.countryPermanentInput.value?.id,
                                input.provincePermanentInput.value?.id
                            )
                        } else {
                            daoForm.permanentAddress?.otherAddress
                        }
                    )
                }
                page?.let {
                    page = if (it > 2) it else 3
                }
            }
            submitApi(daoForm)
        }
    }

    private fun submitApi(daoForm: DaoForm) {
        submitDao.execute(
            getDisposableSingleObserver(
                {
                    updateCurrentInputs()
                    isLoadedScreen.onNext(true)
                    if (it.isHit) {
                        _navigateResult.value = Event(it)
                    } else {
                        _navigateNextStep.value = Event(input)
                    }
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

    private fun isPHCountry(country: String?) = country == Constant.getDefaultCountryDao().id

    private fun updateCurrentInputs() {
        currentInput.apply {
            permanentAddressInput = input.permanentAddressInput.value
            homeAddressInput = input.homeAddressInput.value
            streetNameInput = input.streetNameInput.value
            villageBarangayInput = input.villageBarangayInput.value
            provinceInput = input.provinceInput.value?.id
            cityInput = input.cityInput.value?.id
            zipCodeInput = input.zipCodeInput.value
            countryInput = input.countryInput.value?.id
            homeAddressPermanentInput = input.homeAddressPermanentInput.value
            streetNamePermanentInput = input.streetNamePermanentInput.value
            villageBarangayPermanentInput = input.villageBarangayPermanentInput.value
            provincePermanentInput = input.provincePermanentInput.value?.id
            cityPermanentInput = input.cityPermanentInput.value?.id
            zipCodePermanentInput = input.zipCodePermanentInput.value
            countryPermanentInput = input.countryPermanentInput.value?.id
        }
    }
}
