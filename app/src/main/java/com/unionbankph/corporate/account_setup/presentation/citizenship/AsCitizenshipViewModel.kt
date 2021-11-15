package com.unionbankph.corporate.account_setup.presentation.citizenship

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.common.presentation.constant.Constant
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

class AsCitizenshipViewModel @Inject constructor()
    : BaseViewModel() {

    private val _state = MutableLiveData<Event<String>>()
    val state : LiveData<Event<String>> get() = _state

    var citizenshipInput = BehaviorSubject.create<String>()

    fun isFilipino() {
        _state.value = Event(Constant.Citizenship.FILIPINO)
    }

    fun isNonFilipino() {
        _state.value = Event(Constant.Citizenship.NON_FILIPINO)
    }
}