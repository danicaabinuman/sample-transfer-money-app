package com.unionbankph.corporate.common.presentation.viewmodel.state

sealed class UiState {
    object Success : UiState()
    object Loading : UiState()
    object Complete : UiState()
    object Exit : UiState()
    class Error(val throwable: Throwable) : UiState()
}
