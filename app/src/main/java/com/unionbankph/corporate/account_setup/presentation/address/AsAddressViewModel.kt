package com.unionbankph.corporate.account_setup.presentation.address

import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.settings.presentation.form.Selector
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

class AsAddressViewModel @Inject constructor() : BaseViewModel() {

    var input = Input()

    fun hasValidForm() = input.isValidFormInput.value ?: false

    inner class Input {
        val isValidFormInput = BehaviorSubject.create<Boolean>()
        val isSameAsPresentAddress = BehaviorSubject.createDefault(false)
        var line1Input = BehaviorSubject.create<String>()
        var line2Input = BehaviorSubject.create<String>()
        var cityInput = BehaviorSubject.create<Selector>()
        var regionInput = BehaviorSubject.create<Selector>()
        var zipInput = BehaviorSubject.create<String>()
        var line1PermanentInput = BehaviorSubject.create<String>()
        var line2PermanentInput = BehaviorSubject.create<String>()
        var cityPermanentInput = BehaviorSubject.create<Selector>()
        var regionPermanentInput = BehaviorSubject.create<Selector>()
        var zipPermanentInput = BehaviorSubject.create<String>()
    }

    fun onClickedNext() {

    }

    fun syncInputData() {

    }
}