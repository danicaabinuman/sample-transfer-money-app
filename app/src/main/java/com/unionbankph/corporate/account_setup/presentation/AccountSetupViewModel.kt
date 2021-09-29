package com.unionbankph.corporate.account_setup.presentation

import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.user_creation.presentation.enter_name.UcEnterNameViewModel
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

class AccountSetupViewModel @Inject constructor() : BaseViewModel() {

    val setupAccountType = BehaviorSubject.create<Int>()

    fun clearCache() {
        _uiState.value = Event(UiState.Exit)
    }
}