package com.unionbankph.corporate.user_creation.presentation.enter_contact_info

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.user_creation.data.UserCreationForm
import com.unionbankph.corporate.user_creation.presentation.OpenAccountViewModel
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

class OAEnterContactInfoViewModel @Inject constructor() : BaseViewModel() {
    val isLoadedScreen = BehaviorSubject.create<Boolean>()

    fun hasValidForm() = input.isValidFormInput.value ?: false

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
        var countryCodeInput: String = ""
        var mobileInput: String? = ""
    }

    private var _navigateNextStep = MutableLiveData<Event<Input>>()

    val navigateNextStep: LiveData<Event<Input>> get() = _navigateNextStep

    fun onClickedNext() {
        isLoadedScreen.onNext(true)
        _navigateNextStep.value = Event(input)
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
}