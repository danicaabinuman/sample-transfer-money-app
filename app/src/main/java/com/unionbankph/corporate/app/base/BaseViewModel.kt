package com.unionbankph.corporate.app.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import io.reactivex.disposables.CompositeDisposable

abstract class BaseViewModel : ViewModel() {

    val _uiState = MutableLiveData<Event<UiState>>()

    val uiState: LiveData<Event<UiState>> get() = _uiState

    val disposables = CompositeDisposable()

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
        disposables.dispose()
    }
}
