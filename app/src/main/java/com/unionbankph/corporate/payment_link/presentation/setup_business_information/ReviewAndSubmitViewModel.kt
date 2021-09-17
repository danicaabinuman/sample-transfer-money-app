package com.unionbankph.corporate.payment_link.presentation.setup_business_information

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.dashboard.DashboardState
import javax.inject.Inject

class ReviewAndSubmitViewModel
@Inject constructor()
    : BaseViewModel() {

    private val _reviewAndSubmitState = MutableLiveData<ReviewAndSubmitState>()

    val reviewAndSubmitState: LiveData<ReviewAndSubmitState> get() = _reviewAndSubmitState

    fun navigateBackToBusinessInfo(fromWhatTab: String){

    }
        companion object{
            const val FROM_BUSINESS_INFO_BUTTON = "from_business_info_button"
            const val FROM_OTHER_BUSINESS_INFO_BUTTON = "from_other_business_info_button"
        }
}

sealed class ReviewAndSubmitState

data class ShowBusinessInfo(val fromWhatTab: String) : ReviewAndSubmitState()