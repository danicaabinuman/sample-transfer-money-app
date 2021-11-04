package com.unionbankph.corporate.account_setup.presentation.business_info

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.account_setup.data.BusinessInformation
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.settings.presentation.form.Selector
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

class AsBusinessInformationViewModel @Inject constructor()
    : BaseViewModel() {

    private val _state = MutableLiveData<Event<BusinessInformation>>()
    val state: LiveData<Event<BusinessInformation>> get() = _state

    var input = Input()

    private var businessInformation = BusinessInformation()

    fun hasValidForm() = input.isValidFormInput.value ?: false

    inner class Input {
        val isValidFormInput = BehaviorSubject.create<Boolean>()
        var businessName = BehaviorSubject.create<String>()
        var line1Input = BehaviorSubject.create<String>()
        var line2Input = BehaviorSubject.create<String>()
        var cityInput = BehaviorSubject.create<Selector>()
        var regionInput = BehaviorSubject.create<Selector>()
        var zipInput = BehaviorSubject.create<String>()
    }

    fun onClickedNext() {
        _state.value = Event(businessInformation)
    }

    fun setExistingInput(businessInformation: BusinessInformation) {

    }

    fun syncInputs() {

    }
}