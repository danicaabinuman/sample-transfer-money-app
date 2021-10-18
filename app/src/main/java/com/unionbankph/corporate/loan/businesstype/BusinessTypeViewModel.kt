package com.unionbankph.corporate.loan.businesstype

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import javax.inject.Inject

class BusinessTypeViewModel @Inject
constructor(

) : BaseViewModel() {

    private val _businessType = MutableLiveData<Int>()
    val businessType: LiveData<Int> = _businessType

    private val _hasSelect = MutableLiveData(false)
    val hasSelect: LiveData<Boolean> = _hasSelect

    init {

    }

    fun setBusinessType(types: Int) {
        types?.let {
            _businessType.value = types
            _hasSelect.value = true
        }
    }


}