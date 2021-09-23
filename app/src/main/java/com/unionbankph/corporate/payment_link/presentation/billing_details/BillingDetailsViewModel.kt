package com.unionbankph.corporate.payment_link.presentation.billing_details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.getDisposableSingleObserver
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.payment_link.domain.model.form.GeneratePaymentLinkForm
import com.unionbankph.corporate.payment_link.domain.model.response.GeneratePaymentLinkResponse
import com.unionbankph.corporate.payment_link.domain.model.response.GetPaymentLinkByReferenceIdResponse
import com.unionbankph.corporate.payment_link.domain.model.response.GetPaymentLogsResponse
import com.unionbankph.corporate.payment_link.domain.usecase.GeneratePaymentLinkUseCase
import com.unionbankph.corporate.payment_link.domain.usecase.GetPaymentLinkByReferenceIdUseCase
import com.unionbankph.corporate.payment_link.domain.usecase.GetPaymentLogsUseCase
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject

class BillingDetailsViewModel @Inject constructor(
    private val getPaymentLinkByReferenceIdUseCase: GetPaymentLinkByReferenceIdUseCase,
    private val getPaymentLogsUseCase: GetPaymentLogsUseCase
) : BaseViewModel()  {

    private val _paymentLinkDetailsResponse = MutableLiveData<GetPaymentLinkByReferenceIdResponse>()

    val paymentLinkDetailsResponse: LiveData<GetPaymentLinkByReferenceIdResponse>
        get() =
            _paymentLinkDetailsResponse

    private val _paymentLogsResponse = MutableLiveData<GetPaymentLogsResponse>()

    val paymentLogsResponse : LiveData<GetPaymentLogsResponse>
        get() =
            _paymentLogsResponse

    fun initBundleData(referenceNumber: String) {

        getPaymentLinkByReferenceIdUseCase.execute(
            getDisposableSingleObserver(
                {
                    _paymentLinkDetailsResponse.value = it
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

        getPaymentLogsUseCase.execute(
            getDisposableSingleObserver(
                {
                    _paymentLogsResponse.value = it
                }, {
                    Timber.e(it, "getPaymentLogs")
                    _uiState.value = Event(UiState.Error(it))
                }
            ), doOnSubscribeEvent = {
                _uiState.value = Event(UiState.Loading)
            },
            doFinallyEvent = {
                _uiState.value = Event(UiState.Complete)
            }
        )

    }

}