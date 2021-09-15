package com.unionbankph.corporate.open_account.presentation


import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.open_account.data.Name
import com.unionbankph.corporate.open_account.data.OpenAccountForm
import com.unionbankph.corporate.open_account.presentation.enter_name.OAEnterNameViewModel
import com.unionbankph.corporate.open_account.presentation.enter_contact_info.OAEnterContactInfoViewModel
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

class OpenAccountViewModel @Inject constructor() : BaseViewModel() {

    // Form
    private var openAccountForm = OpenAccountForm()

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

    fun defaultForm() : OpenAccountForm {
        return OpenAccountForm().apply {
            firstName = nameInput.firstNameInput.value
            lastName = nameInput.lastNameInput.value
            email_address = contactInput.emailInput.value
            country_code_id = contactInput.countryCodeInput.value
            mobile_number = contactInput.mobileInput.value
        }
    }

    fun setNameInput(input: OAEnterNameViewModel.Input) {
        input.firstNameInput.value?.let { nameInput.firstNameInput.onNext(it) }
        input.lastNameInput.value?.let { nameInput.lastNameInput.onNext(it) }
        hasNameInput.onNext(true)
    }

    fun setContactInfo(input: OAEnterContactInfoViewModel.Input) {
        input.emailInput.value?.let { contactInput.emailInput.onNext(it) }
        input.countryCodeInput.value?.let { contactInput.countryCodeInput.onNext(it) }
        input.mobileInput.value?.let { contactInput.mobileInput.onNext(it) }
        hasContactInput.onNext(true)
    }

    fun clearCache() {
        nameInput = Input()
        hasNameInput.onNext(false)
    }
}