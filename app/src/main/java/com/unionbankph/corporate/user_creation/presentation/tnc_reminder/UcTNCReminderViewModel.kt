package com.unionbankph.corporate.user_creation.presentation.tnc_reminder

import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.user_creation.data.UserCreationForm
import com.unionbankph.corporate.user_creation.presentation.UserCreationViewModel
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

class UcTNCReminderViewModel @Inject constructor() : BaseViewModel() {
    val isLoadedScreen = BehaviorSubject.create<Boolean>()

    var input: Input = Input()
    var userCreationForm : UserCreationForm = UserCreationForm()

    fun loadName(userCreationForm: UserCreationForm) {
        this.userCreationForm = userCreationForm
    }

    inner class Input {
        var firstNameInput = BehaviorSubject.create<String>()
        var lastNameInput = BehaviorSubject.create<String>()
    }

    fun setName(input: UserCreationViewModel.Input) {
        this.input.firstNameInput = input.firstNameInput
        this.input.lastNameInput = input.lastNameInput
    }
}