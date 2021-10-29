package com.unionbankph.corporate.account_setup.presentation.citizenship

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.platform.events.Event
import javax.inject.Inject

class AsCitizenshipViewModel @Inject constructor()
    : BaseViewModel() {

    private val _state = MutableLiveData<Event<String>>()
    val state : LiveData<Event<String>> get() = _state

    fun onClickedNext(citizenship: String) {
        _state.value = Event(citizenship)
    }
}