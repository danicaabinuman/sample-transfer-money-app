package com.unionbankph.corporate.account_setup.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.account_setup.data.AccountSetupState
import com.unionbankph.corporate.account_setup.data.Address
import com.unionbankph.corporate.account_setup.data.PersonalInformation
import com.unionbankph.corporate.account_setup.data.ToolbarState
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import javax.inject.Inject

class AccountSetupViewModel @Inject constructor() : BaseViewModel() {

    private var _state = MutableLiveData<AccountSetupState>()
    val state : LiveData<AccountSetupState> get() = _state

    private var _toolbarState = MutableLiveData<ToolbarState>()
    val toolbarState: LiveData<ToolbarState> get() = _toolbarState

    init {
        _state.value = AccountSetupState(
            businessType = -1,
            businessAccountType = -1,
            debitCardType = null,
            hasPersonalInfoInput = false,
            personalInformation = null
        )

        _toolbarState.value = ToolbarState(
            isButtonShow = false,
            buttonType = AccountSetupActivity.BUTTON_SAVE_EXIT,
            backButtonType = AccountSetupActivity.BACK_ARROW_ICON
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

    fun setDebitCardType(debitCardType: Int) {
        _state.value = _state.value.also {
            it?.debitCardType = debitCardType
        }
    }

    fun showToolbarButton(isShow: Boolean) {
        _toolbarState.value = _toolbarState.value.also {
            it?.isButtonShow = isShow
        }
    }

    fun setToolbarButtonType(type: Int) {
        _toolbarState.value = _toolbarState.value.also {
            it?.buttonType = type
        }
    }

    fun setPersonalInfoInput(input: PersonalInformation) {
        _state.value = _state.value.also {
            it?.hasPersonalInfoInput = true
            it?.personalInformation = input
        }
    }

    fun setAddressInput(input: Address) {
        _state.value = _state.value.also {
            it?.hasAddressInput = true
            it?.address = input
        }
    }

    fun setCitizenship(citizenship: String) {
        _state.value = _state.value.also {
            it?.citizenship = citizenship
        }
    }

    fun clearCache() {
        _uiState.value = Event(UiState.Exit)
    }
}