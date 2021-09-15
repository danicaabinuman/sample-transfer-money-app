package com.unionbankph.corporate.open_account.presentation.enter_contact_info

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.auth.data.AuthGateway
import com.unionbankph.corporate.auth.data.model.Auth
import com.unionbankph.corporate.auth.data.model.ContactValidityResponse
import com.unionbankph.corporate.common.data.model.ApiError
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.dao.domain.interactor.ValidateNominatedUser
import com.unionbankph.corporate.dao.domain.model.DaoHit
import com.unionbankph.corporate.open_account.data.OpenAccountForm
import com.unionbankph.corporate.open_account.data.form.ValidateContactInfoForm
import com.unionbankph.corporate.open_account.presentation.OpenAccountViewModel
import com.unionbankph.corporate.settings.presentation.update_password.UpdatePasswordState
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import javax.inject.Inject

class OAEnterContactInfoViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val authGateway: AuthGateway
) : BaseViewModel() {

    val isLoadedScreen = BehaviorSubject.create<Boolean>()

    fun hasValidForm() = input.isValidFormInput.value ?: false

    var contactInfoForm = ValidateContactInfoForm()

    var userCreationForm = OpenAccountForm()

    var input = Input()

    private var currentInput = CurrentInput()

    inner class Input {
        val isValidFormInput = BehaviorSubject.create<Boolean>()
        var emailInput = BehaviorSubject.create<String>()
        var countryCodeInput = BehaviorSubject.create<String>()
        var mobileInput = BehaviorSubject.create<String>()

    }

    inner class CurrentInput {
        var emailInput: String? = ""
        var countryCodeInput: String? = ""
        var mobileInput: String? = ""
    }

    private val _navigateResult = MutableLiveData<Event<ContactValidityResponse>>()

    val navigateResult: LiveData<Event<ContactValidityResponse>> get() = _navigateResult

    private var _navigateNextStep = MutableLiveData<Event<Input>>()

    val navigateNextStep: LiveData<Event<Input>> get() = _navigateNextStep

    fun onClickedNext(defaultForm: OpenAccountForm) {
        val contactInfoForm = contactInfoForm.apply {
                firstName = defaultForm.firstName
                lastName = defaultForm.lastName
                email_address = input.emailInput.value
                mobile_number = input.mobileInput.value
                country_code_id = Integer.valueOf(input.countryCodeInput.value!!.replace("+",""))
            }
            enterContactInfo(contactInfoForm)
    }

    fun loadDefaultForm(defaultForm: OpenAccountForm) {
        this.userCreationForm = defaultForm
    }

    fun setPreTextValues(emailInput: String?, mobileInput: String?, countryCodeInput: String?) {
        emailInput?.let { input.emailInput.onNext(it) }
        countryCodeInput?.let { input.countryCodeInput.onNext(it) }
        mobileInput?.let { input.mobileInput.onNext(it) }
    }

    fun hasFormChanged(): Boolean {
        return input.emailInput.value != currentInput.emailInput
                || input.mobileInput.value != currentInput.mobileInput
                || input.countryCodeInput.value != currentInput.countryCodeInput
    }

    fun setExistingContactInfoInput(existingInput: OpenAccountViewModel.Input2) {
        existingInput.emailInput.value?.let {
            this.input.emailInput.onNext(it)
            currentInput.emailInput = it
        }
        existingInput.mobileInput.value?.let {
            this.input.mobileInput.onNext(it)
            currentInput.mobileInput = it
        }
        existingInput.countryCodeInput.value?.let {
            this.input.countryCodeInput.onNext(it)
            currentInput.countryCodeInput = it
        }
    }

    fun enterContactInfo(validateContactInfoForm: ValidateContactInfoForm) {
        authGateway.validateContactInfo(validateContactInfoForm)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _uiState.value = Event(UiState.Loading) }
            .doFinally { _uiState.value = Event(UiState.Complete) }
            .subscribe(
                {
                    isLoadedScreen.onNext(true)
                    _navigateNextStep.value = Event(input)
                    _navigateResult.value = Event(it)

                }, {
                    Timber.d(it, "OpenAccountContactValidity Failed")
                    _uiState.value = Event(UiState.Error(it))
                }).addTo(disposables)
    }


}