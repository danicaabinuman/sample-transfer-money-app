package com.unionbankph.corporate.user_creation.presentation.enter_contact_info

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.auth.data.AuthGateway
import com.unionbankph.corporate.auth.data.model.Auth
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.user_creation.data.UserCreationForm
import com.unionbankph.corporate.user_creation.data.form.ValidateContactInfoForm
import com.unionbankph.corporate.user_creation.presentation.UserCreationViewModel
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import javax.inject.Inject

class UcEnterContactInfoViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val authGateway: AuthGateway
) : BaseViewModel() {

    val isLoadedScreen = BehaviorSubject.create<Boolean>()

    fun hasValidForm() = input.isValidFormInput.value ?: false

    var contactInfoForm = ValidateContactInfoForm()

    var userCreationForm = UserCreationForm()

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

    private val _navigateResult = MutableLiveData<Event<Auth>>()

    val navigateResult: LiveData<Event<Auth>> get() = _navigateResult

    fun onClickedNext(defaultForm: UserCreationForm) {
        val contactInfoForm = contactInfoForm.apply {
                firstName = defaultForm.firstName
                lastName = defaultForm.lastName
                email_address = input.emailInput.value
                mobile_number = input.mobileInput.value
                country_code_id = Integer.valueOf(input.countryCodeInput.value!!.replace("+",""))
            }
        enterContactInfo(contactInfoForm)
    }

    fun loadDefaultForm(defaultForm: UserCreationForm) {
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

    fun setExistingContactInfoInput(existingInput: UserCreationViewModel.Input2) {
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
        authGateway.userCreationValidateContact(validateContactInfoForm)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _uiState.value = Event(UiState.Loading) }
            .doFinally { _uiState.value = Event(UiState.Complete) }
            .subscribe(
                {
                    isLoadedScreen.onNext(true)
                    _navigateResult.value = Event(it)
                }, {
                    Timber.d(it, "OpenAccountContactValidity Failed")
                    _uiState.value = Event(UiState.Error(it))
                }).addTo(disposables)
    }
}