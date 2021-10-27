package com.unionbankph.corporate.payment_link.presentation.setup_business_information.business_information_forms

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.getDisposableSingleObserver
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.payment_link.domain.model.rmo.GetRMOBusinessInformationForm
import com.unionbankph.corporate.payment_link.domain.model.rmo.GetRMOBusinessInformationResponse
import com.unionbankph.corporate.payment_link.domain.model.rmo.RMOBusinessInformationForm
import com.unionbankph.corporate.payment_link.domain.model.rmo.RMOBusinessInformationResponse
import com.unionbankph.corporate.payment_link.domain.usecase.GetRMOBusinessInformationUseCase
import com.unionbankph.corporate.payment_link.domain.usecase.SubmitRMOBusinessInformationUseCase
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject

class BusinessInformationViewModel @Inject constructor(
    private val submitRmoBusinessInformationUseCase: SubmitRMOBusinessInformationUseCase,
    private val getRMOBusinessInformationUseCase: GetRMOBusinessInformationUseCase
) : BaseViewModel() {

    private val _rmoBusinessInformationState = MutableLiveData<RMOBusinessInformationState>()
    val rmoBusinessInformationState: LiveData<RMOBusinessInformationState>
        get() =
            _rmoBusinessInformationState

    private val _rmoBusinessInformationResponse = MutableLiveData<RMOBusinessInformationResponse>()
    val saveBusinessInformation: LiveData<RMOBusinessInformationResponse>
        get() =
            _rmoBusinessInformationResponse

    private val _getRMOBusinessInformationResponse = MutableLiveData<GetRMOBusinessInformationResponse>()
    val getRMOBusinessInformationResponse: LiveData<GetRMOBusinessInformationResponse>
        get() =
            _getRMOBusinessInformationResponse

    fun saveBusinessInfo(form : RMOBusinessInformationForm){
        submitRmoBusinessInformationUseCase.execute(
            singleObserver = getDisposableSingleObserver(
                {
                    _rmoBusinessInformationResponse.value = it
                },{
                    Timber.e(it, "Business Information Failed to Submit")
                    _uiState.value = Event(UiState.Error(it))

//                    try {
//                        if(it.message?.contains("This handle is no longer available. Please try another one.",true) == true){
//                            _rmoBusinessInformationState.value = ShowHandleNotAvailable
//                        }else{
//                            _uiState.value = Event(UiState.Error(it))
//                        }
//                    } catch (e: Exception) {
//                        _uiState.value = Event(UiState.Error(it))
//                    }
                }
            ),
            doOnSubscribeEvent = {
                _uiState.value = Event(UiState.Loading)
            },
            doFinallyEvent = {
                _uiState.value = Event(UiState.Complete)
            },
            params = form
        ).addTo(disposables)

    }

    fun getBusinessInformation(getRMOBusinessInformationResponse : GetRMOBusinessInformationForm){
        getRMOBusinessInformationUseCase.execute(
            getDisposableSingleObserver(
                {
                    _getRMOBusinessInformationResponse.value = it
                },{
                    _uiState.value = Event(UiState.Error(it))
                }
            ),
            doOnSubscribeEvent = {
                _uiState.value = Event(UiState.Loading)
            },
            doFinallyEvent = {
                _uiState.value = Event(UiState.Complete)
            },
            params = getRMOBusinessInformationResponse
        ).addTo(disposables)

    }

}

sealed class RMOBusinessInformationState

object ShowHandleNotAvailable : RMOBusinessInformationState()