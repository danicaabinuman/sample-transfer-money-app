package com.unionbankph.corporate.user_creation.presentation

import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.user_creation.data.Name
import com.unionbankph.corporate.user_creation.data.UserCreationForm
import com.unionbankph.corporate.user_creation.presentation.enter_name.OAEnterNameViewModel
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

class OpenAccountViewModel @Inject constructor() : BaseViewModel() {

    // Name
    var nameInput = Input()
    var hasNameInput = BehaviorSubject.create<Boolean>()

    inner class Input {
        var firstNameInput = BehaviorSubject.create<String>()
        var lastNameInput = BehaviorSubject.create<String>()
    }

    fun defaultForm() : UserCreationForm {
        return UserCreationForm().apply {
            name = Name(
                firstName = nameInput.firstNameInput.value,
                lastName = nameInput.lastNameInput.value
            )
        }
    }

    fun setNameInput(input: OAEnterNameViewModel.Input) {
        input.firstNameInput.value?.let { nameInput.firstNameInput.onNext(it) }
        input.lastNameInput.value?.let { nameInput.lastNameInput.onNext(it) }
        hasNameInput.onNext(true)
    }
}