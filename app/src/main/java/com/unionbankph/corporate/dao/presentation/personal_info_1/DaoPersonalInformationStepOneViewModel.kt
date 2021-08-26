package com.unionbankph.corporate.dao.presentation.personal_info_1

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.getDisposableSingleObserver
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.dao.data.form.DaoForm
import com.unionbankph.corporate.dao.data.model.Name
import com.unionbankph.corporate.dao.data.model.Results
import com.unionbankph.corporate.dao.domain.form.Users
import com.unionbankph.corporate.dao.domain.form.ValidateNominatedUserForm
import com.unionbankph.corporate.dao.domain.interactor.ClearDaoCache
import com.unionbankph.corporate.dao.domain.interactor.SubmitDao
import com.unionbankph.corporate.dao.domain.interactor.ValidateNominatedUser
import com.unionbankph.corporate.dao.domain.model.DaoHit
import com.unionbankph.corporate.dao.presentation.DaoViewModel
import com.unionbankph.corporate.settings.presentation.form.Selector
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by herald25santos on 3/17/20
 */
class DaoPersonalInformationStepOneViewModel @Inject constructor(
    private val context: Context,
    private val submitDao: SubmitDao,
    private val validateNominatedUser: ValidateNominatedUser,
    private val clearDaoCache: ClearDaoCache
) : BaseViewModel() {

    val isLoadedScreen = BehaviorSubject.create<Boolean>()

    val isEditMode = BehaviorSubject.createDefault(false)

    var input: Input = Input()

    var daoForm = DaoForm()

    private var currentInput: CurrentInput = CurrentInput()

    inner class Input {
        val isValidFormInput = BehaviorSubject.create<Boolean>()
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
    }

    inner class CurrentInput {
        var salutationInput: String? = null
        var firstNameInput: String? = null
        var middleNameInput: String? = null
        var lastNameInput: String? = null
        var emailAddressInput: String? = null
        var countryCodeInput: String? = null
        var businessMobileNumberInput: String? = null
        var genderInput: String? = null
        var civilStatusInput: String? = null
    }

    private val _navigateNextStep = MutableLiveData<Event<Input>>()

    val navigateNextStep: LiveData<Event<Input>> get() = _navigateNextStep

    private val _navigateResult = MutableLiveData<Event<DaoHit>>()

    val navigateResult: LiveData<Event<DaoHit>> get() = _navigateResult

    private val _hasValidationError = MutableLiveData<Event<Results>>()

    val hasValidationError: LiveData<Event<Results>> get() = _hasValidationError

    fun hasValidForm() = input.isValidFormInput.value ?: false

    fun loadDaoForm(daoForm: DaoForm) {
        this.daoForm = daoForm
    }

    fun setPreTextValues(
        firstNameInput: String?,
        middleNameInput: String?,
        lastNameInput: String?,
        emailAddressInput: String?,
        businessMobileNumberInput: String?
    ) {
        firstNameInput?.let { input.firstNameInput.onNext(it) }
        middleNameInput?.let { input.middleNameInput.onNext(it) }
        lastNameInput?.let { input.lastNameInput.onNext(it) }
        emailAddressInput?.let { input.emailAddressInput.onNext(it) }
        businessMobileNumberInput?.let { input.businessMobileNumberInput.onNext(it) }
    }

    fun setExistingPersonalInformationStepOne(input: DaoViewModel.Input1) {
        input.salutationInput.value?.let {
            currentInput.salutationInput = it.id
            this.input.salutationInput.onNext(it)
        }
        input.firstNameInput.value?.let {
            currentInput.firstNameInput = it
            this.input.firstNameInput.onNext(it)
        }
        input.middleNameInput.value?.let {
            currentInput.middleNameInput = it
            this.input.middleNameInput.onNext(it)
        }
        input.lastNameInput.value?.let {
            currentInput.lastNameInput = it
            this.input.lastNameInput.onNext(it)
        }
        input.emailAddressInput.value?.let {
            currentInput.emailAddressInput = it
            this.input.emailAddressInput.onNext(it)
        }
        input.countryCodeInput.value?.let {
            currentInput.countryCodeInput = it.id.toString()
            this.input.countryCodeInput.onNext(it)
        }
        input.businessMobileNumberInput.value?.let {
            currentInput.businessMobileNumberInput = it
            this.input.businessMobileNumberInput.onNext(it)
        }
        input.genderInput.value?.let {
            currentInput.genderInput = it.id
            this.input.genderInput.onNext(it)
        }
        input.civilStatusInput.value?.let {
            currentInput.civilStatusInput = it.id
            this.input.civilStatusInput.onNext(it)
        }
    }

    private fun hasFormChanged(): Boolean {
        return input.salutationInput.value?.id.toString() != currentInput.salutationInput
                || input.firstNameInput.value != currentInput.firstNameInput
                || input.middleNameInput.value != currentInput.middleNameInput
                || input.lastNameInput.value != currentInput.lastNameInput
                || input.emailAddressInput.value != currentInput.emailAddressInput
                || input.countryCodeInput.value?.id.toString() != currentInput.countryCodeInput
                || input.businessMobileNumberInput.value != currentInput.businessMobileNumberInput
                || input.genderInput.value?.id != currentInput.genderInput
                || input.civilStatusInput.value?.id != currentInput.civilStatusInput
    }

    fun onClickedNext() {
        if (!hasFormChanged()) {
            isLoadedScreen.onNext(true)
            _navigateNextStep.value = Event(input)
        } else {
            val daoForm = daoForm.apply {
                salutation = input.salutationInput.value?.id
                countryCode = input.countryCodeInput.value?.id
                mobile = input.businessMobileNumberInput.value
                email = input.emailAddressInput.value
                name = Name(
                    input.lastNameInput.value,
                    input.middleNameInput.value,
                    input.firstNameInput.value
                )
                gender = input.genderInput.value?.id
                civilStatus = input.civilStatusInput.value?.id
                page?.let {
                    page = if (it > 0) it else 1
                }
            }
            validateNominatedUser.execute(
                getDisposableSingleObserver(
                    {
                        if (!it.isNullOrEmpty()) {
                            val results = it[0]
                            if (results.withError == true) {
                                _uiState.value = Event(UiState.Complete)
                                _hasValidationError.value = Event(results)
                            } else {
                                submitDao(daoForm)
                            }
                        } else {
                            _uiState.value = Event(UiState.Complete)
                        }
                    }, {
                        _uiState.value = Event(UiState.Complete)
                        _uiState.value = Event(UiState.Error(it))
                    }
                ),
                doOnSubscribeEvent = {
                    _uiState.value = Event(UiState.Loading)
                },
                params = ValidateNominatedUserForm(
                    users = mutableListOf(Users(daoForm.email, daoForm.mobile, daoForm.countryCode))
                )
            ).addTo(disposables)
        }
    }

    private fun submitDao(daoForm: DaoForm) {
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
            doFinallyEvent = {
                _uiState.value = Event(UiState.Complete)
            },
            params = daoForm
        ).addTo(disposables)
    }

    private fun updateCurrentInputs() {
        currentInput.apply {
            salutationInput = input.salutationInput.value?.id.toString()
            firstNameInput = input.firstNameInput.value
            middleNameInput = input.middleNameInput.value
            lastNameInput = input.lastNameInput.value
            emailAddressInput = input.emailAddressInput.value
            countryCodeInput = input.countryCodeInput.value?.id.toString()
            businessMobileNumberInput = input.businessMobileNumberInput.value
            genderInput = input.genderInput.value?.id
            civilStatusInput = input.civilStatusInput.value?.id
        }
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
