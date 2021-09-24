package com.unionbankph.corporate.payment_link.presentation.billing_details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.getDisposableSingleObserver
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.payment_link.domain.model.form.GeneratePaymentLinkForm
import com.unionbankph.corporate.payment_link.domain.model.response.GeneratePaymentLinkResponse
import com.unionbankph.corporate.payment_link.domain.model.response.GetPaymentLinkByReferenceIdResponse
import com.unionbankph.corporate.payment_link.domain.model.response.GetPaymentLogsResponse
import com.unionbankph.corporate.payment_link.domain.usecase.GeneratePaymentLinkUseCase
import com.unionbankph.corporate.payment_link.domain.usecase.GetBillingDetailsUseCase
import com.unionbankph.corporate.payment_link.domain.usecase.GetPaymentLinkByReferenceIdUseCase
import com.unionbankph.corporate.payment_link.domain.usecase.GetPaymentLogsUseCase
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject

class BillingDetailsViewModel @Inject constructor(
    private val getBillingDetailsUseCase: GetBillingDetailsUseCase
) : BaseViewModel()  {

//    private val _paymentLinkDetailsResponse = MutableLiveData<GetPaymentLinkByReferenceIdResponse>()
//
//    val paymentLinkDetailsResponse: LiveData<GetPaymentLinkByReferenceIdResponse>
//        get() =
//            _paymentLinkDetailsResponse
//
//    private val _paymentLogsResponse = MutableLiveData<GetPaymentLogsResponse>()
//
//    val paymentLogsResponse : LiveData<GetPaymentLogsResponse>
//        get() =
//            _paymentLogsResponse

    private val _state = MutableLiveData<BillingDetailsState>()

    val state: LiveData<BillingDetailsState> get() = _state

    fun initBundleData(referenceNumber: String) {
    Timber.e("initBundleData")
        getBillingDetailsUseCase.execute(
            getDisposableSingleObserver(
                {
                    Timber.e("getDisposableSingleObserver " + JsonHelper.toJson(it))
                    _state.value = it
                }, {
                    Timber.e(it, "getPaymentLinkByReferenceId")
                    _uiState.value = Event(UiState.Error(it))
                }
            ),
            doOnSubscribeEvent = {
                _uiState.value = Event(UiState.Loading)
            },
            doFinallyEvent = {
                _uiState.value = Event(UiState.Complete)
            },
            params = referenceNumber
        ).addTo(disposables)

    }
}