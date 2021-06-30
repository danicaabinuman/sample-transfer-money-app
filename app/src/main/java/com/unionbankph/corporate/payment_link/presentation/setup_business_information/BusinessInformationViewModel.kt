package com.unionbankph.corporate.payment_link.presentation.setup_business_information

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.getDisposableSingleObserver
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.payment_link.domain.model.form.RMOBusinessInformationForm
import com.unionbankph.corporate.payment_link.domain.model.response.RMOBusinessInformationResponse
import com.unionbankph.corporate.payment_link.domain.usecase.RMOBusinessInformationUseCase
import com.unionbankph.corporate.payment_link.presentation.setup_payment_link.SetupPaymentLinkState
import com.unionbankph.corporate.payment_link.presentation.setup_payment_link.ShowHandleNotAvailable
import timber.log.Timber
import javax.inject.Inject

class BusinessInformationViewModel @Inject constructor(
    private val rmoBusinessInformationUseCase: RMOBusinessInformationUseCase
) : BaseViewModel() {

    private val _rmoBusinessInformationState = MutableLiveData<RMOBusinessInformationState>()
    val rmoBusinessInformationState: LiveData<RMOBusinessInformationState>
        get() =
            _rmoBusinessInformationState

    private val _rmoBusinessInformationResponse = MutableLiveData<RMOBusinessInformationResponse>()
    val rmoBusinessInformationResponse: LiveData<RMOBusinessInformationResponse>
        get() =
            _rmoBusinessInformationResponse

    fun submitBusinessInformation(form : RMOBusinessInformationForm){

        rmoBusinessInformationUseCase.execute(
            getDisposableSingleObserver(
                {
                    _rmoBusinessInformationResponse.value = it
                },{
                    Timber.e(it, "createMerchant Failed")
                    try {
                        if(it.message?.contains("This handle is no longer available. Please try another one.",true) == true){
                            _rmoBusinessInformationState.value = com.unionbankph.corporate.payment_link.presentation.setup_business_information.ShowHandleNotAvailable
                        }else{
                            _uiState.value = Event(UiState.Error(it))
                        }
                    } catch (e: Exception) {
                        _uiState.value = Event(UiState.Error(it))
                    }
                }
            )
        )

    }
}

sealed class RMOBusinessInformationState

object ShowHandleNotAvailable : RMOBusinessInformationState()