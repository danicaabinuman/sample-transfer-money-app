package com.unionbankph.corporate.user_creation.presentation.enter_name

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.user_creation.data.UserCreationForm
import com.unionbankph.corporate.user_creation.presentation.UserCreationViewModel
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

class UcEnterNameViewModel @Inject constructor() : BaseViewModel() {

    val isLoadedScreen = BehaviorSubject.create<Boolean>()

    fun hasValidForm() = input.isValidFormInput.value ?: false

    var openAccountForm = UserCreationForm()

    var input = Input()

    private var currentInput = CurrentInput()

    inner class Input {
        val isValidFormInput = BehaviorSubject.create<Boolean>()
        var firstNameInput = BehaviorSubject.create<String>()
        var lastNameInput = BehaviorSubject.create<String>()
    }

    inner class CurrentInput {
        var firstNameInput: String? = ""
        var lastNameInput: String? = ""
    }

    private var _navigateNextStep = MutableLiveData<Event<Input>>()

    val navigateNextStep: LiveData<Event<Input>> get() = _navigateNextStep

    fun onClickedNext() {
        isLoadedScreen.onNext(true)
        _navigateNextStep.value = Event(input)
    }

    fun loadDefaultForm(defaultForm: UserCreationForm) {
        this.openAccountForm = defaultForm
    }

    fun setPreTextValues(firstNameInput: String?, lastNameInput: String?) {
        firstNameInput?.let { input.firstNameInput.onNext(it) }
        lastNameInput?.let { input.lastNameInput.onNext(it) }
    }

    fun hasFormChanged(): Boolean {
        return input.firstNameInput.value != currentInput.firstNameInput
            || input.lastNameInput.value != currentInput.lastNameInput
    }

    fun setExistingNameInput(existingInput: UserCreationViewModel.Input) {
        existingInput.firstNameInput.value?.let {
            this.input.firstNameInput.onNext(it)
            currentInput.firstNameInput = it
        }
        existingInput.lastNameInput.value?.let {
            this.input.lastNameInput.onNext(it)
            currentInput.lastNameInput = it
        }
    }
}