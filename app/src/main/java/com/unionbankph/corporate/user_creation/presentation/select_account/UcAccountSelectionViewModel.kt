package com.unionbankph.corporate.user_creation.presentation.select_account

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.common.presentation.constant.Constant
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

class UcAccountSelectionViewModel @Inject constructor() :
    BaseViewModel() {

    private val _state = MutableLiveData<Event<String>>()
    val state : LiveData<Event<String>> get() = _state

    var selectedAccountInput = BehaviorSubject.create<String>()

    fun noOpenAccount() {
        _state.value = Event(Constant.SelectedAccountType.NO_OPEN_ACCOUNT)
    }

    fun yesUnionBankAccount() {
        _state.value = Event(Constant.SelectedAccountType.YES_UNIONBANK_ACCOUNT)
    }

    fun continueExistingAccount() {
        _state.value = Event(Constant.SelectedAccountType.CONTINUE_EXISTING_ACCOUNT)
    }
}