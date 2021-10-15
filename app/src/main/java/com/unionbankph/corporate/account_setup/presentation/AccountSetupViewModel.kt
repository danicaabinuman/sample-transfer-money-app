package com.unionbankph.corporate.account_setup.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.account_setup.data.AccountSetupState
import com.unionbankph.corporate.account_setup.data.DebitCardState
import com.unionbankph.corporate.account_setup.data.ToolbarState
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.app.dashboard.fragment.DashboardViewState
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import javax.inject.Inject

class AccountSetupViewModel @Inject constructor() : BaseViewModel() {

    private var _state = MutableLiveData<AccountSetupState>()
    val state : LiveData<AccountSetupState> get() = _state

    private var _toolbarState = MutableLiveData<ToolbarState>()
    val toolbarState: LiveData<ToolbarState> get() = _toolbarState

    var _debitCardState = MutableLiveData<DebitCardState>()
    val debitCardState: LiveData<DebitCardState> get() = _debitCardState

    init {
        _state.value = AccountSetupState(
            businessType = -1,
            businessAccountType = -1,
            debitCardType = -1
        )

        _toolbarState.value = ToolbarState(
            isButtonShow = false,
            buttonType = AccountSetupActivity.BUTTON_SAVE_EXIT,
            backButtonType = AccountSetupActivity.BACK_ARROW
        )

        _debitCardState.value = DebitCardState(
            lastCardSelected = -1,
            cards = mutableListOf(),

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

    fun clearCache() {
        _uiState.value = Event(UiState.Exit)
    }
}