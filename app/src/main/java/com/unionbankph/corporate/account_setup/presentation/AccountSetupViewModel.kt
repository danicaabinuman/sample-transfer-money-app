package com.unionbankph.corporate.account_setup.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.account_setup.data.AccountSetupState
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import javax.inject.Inject

class AccountSetupViewModel @Inject constructor() : BaseViewModel() {

    private var _state = MutableLiveData<AccountSetupState>()
    val state : LiveData<AccountSetupState> get() = _state

    init {
        _state.value = AccountSetupState(
            businessType = -1,
            businessAccountType = -1
        )
    }

    fun setBusinessType(businessType: Int) {
        _state.value = _state.value.also {
            it?.businessType = businessType
        }
    }

    fun setBusinessAccountType(accountType: Int) {
        _state.value = _state.value.also {
            it?.businessAccountType = accountType
        }
    }

    fun clearCache() {
        _uiState.value = Event(UiState.Exit)
    }
}