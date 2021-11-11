package com.unionbankph.corporate.loan.contactinformation

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.loan.applyloan.LoansViewState
import javax.inject.Inject

class ContactInformationViewModel @Inject
constructor(
    private val context: Context
) : BaseViewModel() {

    private val _contactInformation = MutableLiveData<List<ContactInformation>>()
    val contactInformation: LiveData<List<ContactInformation>> = _contactInformation

    private val _isSelect = MutableLiveData(false)
    val isSelect: LiveData<Boolean> = _isSelect

    init {

        generateCommonQuestions()
    }

    fun generateCommonQuestions() {
        _contactInformation.value = ContactInformation.generateContactInformation(context)
    }

    fun setLoanType(type: Int?) {

        type?.let { type ->
            _contactInformation.value?.let {
                it.forEachIndexed { index, contactInformation ->
                    if (index == type) {
                        if (contactInformation.isSelected == false) contactInformation.isSelected = !contactInformation.isSelected!!
                    } else {
                        contactInformation.isSelected = false
                    }
                }
                _isSelect.value = true
                _contactInformation.value = it
            }
        }
    }
}