package com.unionbankph.corporate.account_setup.presentation.address

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.account_setup.data.Address
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.settings.presentation.form.Selector
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import javax.inject.Inject

class AsAddressViewModel @Inject constructor() : BaseViewModel() {

    private val _state = MutableLiveData<Event<Address>>()
    val state: LiveData<Event<Address>> get() = _state

    var input = Input()

    private var address = Address()

    fun hasValidForm() = input.isValidFormInput.value ?: false

    inner class Input {
        val isValidFormInput = BehaviorSubject.create<Boolean>()
        val isSameAsPresentAddress = BehaviorSubject.createDefault(false)
        var line1Input = BehaviorSubject.create<String>()
        var line2Input = BehaviorSubject.create<String>()
        var cityInput = BehaviorSubject.create<Selector>()
        var regionInput = BehaviorSubject.create<Selector>()
        var zipInput = BehaviorSubject.create<String>()
        var permanentLine1Input = BehaviorSubject.create<String>()
        var permanentLine2Input = BehaviorSubject.create<String>()
        var permanentCityInput = BehaviorSubject.create<Selector>()
        var permanentRegionInput = BehaviorSubject.create<Selector>()
        var permanentZipInput = BehaviorSubject.create<String>()
    }

    fun onClickedNext() {
        _state.value = Event(address)
    }

    fun populateFieldsWithExisting(address: Address) {
        this.address = address
        address.let {
            syncInputData(
                isSameAsPermanentAddress = it.isSameAsPresentAddress,
                line1Input = it.line1,
                line2Input = it.line2,
                regionInput = it.region,
                cityInput = it.city,
                zipCodeInput = it.zipCode,
                permanentLine1Input = it.permanentLine1,
                permanentLine2Input = it.permanentLine2,
                permanentRegionInput = it.permanentRegion,
                permanentCityInput = it.permanentCity,
                permanentZipCodeInput = it.permanentZipCode
            )
        }
    }

    fun syncInputData (
        isSameAsPermanentAddress: Boolean?,
        line1Input: String?,
        line2Input: String?,
        regionInput: Selector?,
        cityInput: Selector?,
        zipCodeInput: String?,
        permanentLine1Input: String?,
        permanentLine2Input: String?,
        permanentRegionInput: Selector?,
        permanentCityInput: Selector?,
        permanentZipCodeInput: String?,
    ) {
        isSameAsPermanentAddress?.let {
            address.isSameAsPresentAddress = it
            input.isSameAsPresentAddress.onNext(it)
        }
        line1Input?.let {
            address.line1 = it
            input.line1Input.onNext(it)
        }
        line2Input?.let {
            address.line2 = it
            input.line2Input.onNext(it)
        }
        regionInput?.let {
            address.region = it
            input.regionInput.onNext(it)
        }
        cityInput?.let {
            address.city = it
            input.cityInput.onNext(it)
        }
        zipCodeInput?.let {
            address.zipCode = it
            input.zipInput.onNext(it)
        }

        if (isSameAsPermanentAddress != null && isSameAsPermanentAddress) {
            address.permanentLine1 = line1Input
            address.permanentLine2 = line2Input
            address.permanentRegion = regionInput
            address.permanentCity = cityInput
            address.permanentZipCode = zipCodeInput
        } else {
            permanentLine1Input?.let {
                address.permanentLine1 = it
                input.permanentLine1Input.onNext(it)
            }
            permanentLine2Input?.let {
                address.permanentLine2 = it
                input.permanentLine2Input.onNext(it)
            }
            permanentRegionInput?.let {
                address.permanentRegion = it
                input.permanentRegionInput.onNext(it)
            }
            permanentCityInput?.let {
                address.permanentCity = it
                input.permanentCityInput.onNext(it)
            }
            permanentZipCodeInput?.let {
                address.permanentZipCode = it
                input.permanentZipInput.onNext(it)
            }
        }
    }
}