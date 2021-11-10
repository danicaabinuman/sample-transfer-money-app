package com.unionbankph.corporate.user_creation.presentation


import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.user_creation.data.UserCreationForm
import com.unionbankph.corporate.user_creation.presentation.enter_name.UcEnterNameViewModel
import com.unionbankph.corporate.user_creation.presentation.enter_contact_info.UcEnterContactInfoViewModel
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

class UserCreationViewModel @Inject constructor() : BaseViewModel() {

    // Token from success otp
    private var otpSuccessToken: String = ""

    // Form
    private var openAccountForm = UserCreationForm()

    // Name
    var nameInput = Input()
    var hasNameInput = BehaviorSubject.create<Boolean>()

    inner class Input {
        var firstNameInput = BehaviorSubject.create<String>()
        var lastNameInput = BehaviorSubject.create<String>()
    }

    // Contact Info
    var contactInput = Input2()
    var hasContactInput = BehaviorSubject.create<Boolean>()

    inner class Input2 {
        var emailInput = BehaviorSubject.create<String>()
        var countryCodeInput = BehaviorSubject.create<String>()
        var mobileInput = BehaviorSubject.create<String>()
    }

    fun setExistingFormData(form: UserCreationForm) {
        form.let {
            nameInput.apply {
                firstNameInput.onNext(form.firstName ?: "")
                lastNameInput.onNext(form.lastName ?: "")
            }
            hasNameInput.onNext(true)
            contactInput.apply {
                emailInput.onNext(form.emailAddress ?: "")
                countryCodeInput.onNext(form.countryCodeId ?: "")
                mobileInput.onNext(form.mobileNumber ?: "")
            }
            hasContactInput.onNext(true)
        }
    }

    fun setOTPVerificationOTPToken(token: String) {
        otpSuccessToken = token
    }

    fun getOTPSuccessToken(): String {
        return otpSuccessToken
    }

    fun defaultForm() : UserCreationForm {
        return UserCreationForm().apply {
            firstName = nameInput.firstNameInput.value
            lastName = nameInput.lastNameInput.value
            emailAddress = contactInput.emailInput.value
            countryCodeId = contactInput.countryCodeInput.value
            mobileNumber = contactInput.mobileInput.value
        }
    }

    fun setNameInput(input: UcEnterNameViewModel.Input) {
        input.firstNameInput.value?.let { nameInput.firstNameInput.onNext(it) }
        input.lastNameInput.value?.let { nameInput.lastNameInput.onNext(it) }
        hasNameInput.onNext(true)
    }

    fun setContactInfo(input: UcEnterContactInfoViewModel.Input) {
        input.emailInput.value?.let { contactInput.emailInput.onNext(it) }
        input.countryCodeInput.value?.let { contactInput.countryCodeInput.onNext(it) }
        input.mobileInput.value?.let { contactInput.mobileInput.onNext(it) }
        hasContactInput.onNext(true)
    }

    var selectedAccount: String? = null
    
    fun setSelectedAccountType(type: String) {
        selectedAccount = type
    }

    fun clearCache() {
        _uiState.value = Event(UiState.Exit)
    }
}