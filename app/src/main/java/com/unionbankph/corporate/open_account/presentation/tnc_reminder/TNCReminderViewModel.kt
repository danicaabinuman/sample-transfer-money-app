package com.unionbankph.corporate.open_account.presentation.tnc_reminder

import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.open_account.data.OpenAccountForm
import com.unionbankph.corporate.open_account.presentation.OpenAccountViewModel
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

class TNCReminderViewModel @Inject constructor() : BaseViewModel() {
    val isLoadedScreen = BehaviorSubject.create<Boolean>()

    var input: Input = Input()
    var openAccountForm : OpenAccountForm = OpenAccountForm()

    fun loadName(openAccountForm: OpenAccountForm) {
        this.openAccountForm = openAccountForm
    }

    inner class Input {
        var firstNameInput = BehaviorSubject.create<String>()
        var lastNameInput = BehaviorSubject.create<String>()
    }

    fun setName(input: OpenAccountViewModel.Input) {
        this.input.firstNameInput = input.firstNameInput
        this.input.lastNameInput = input.lastNameInput
    }
}