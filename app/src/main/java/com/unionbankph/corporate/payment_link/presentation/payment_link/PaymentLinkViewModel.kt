package com.unionbankph.corporate.payment_link.presentation.payment_link

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.getDisposableSingleObserver
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.payment_link.domain.model.form.GetPaymentLinkListByReferenceNumberForm
import com.unionbankph.corporate.payment_link.domain.model.form.GetPaymentLinkListPaginatedForm
import com.unionbankph.corporate.payment_link.domain.model.response.GetPaymentLinkListPaginatedResponse
import com.unionbankph.corporate.payment_link.domain.usecase.GetAllPaymentLinksByReferenceNumberUseCase
import com.unionbankph.corporate.payment_link.domain.usecase.GetAllPaymentLinksUseCase
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject

class PaymentLinkViewModel
@Inject constructor(
        private val getAllPaymentLinksUseCase: GetAllPaymentLinksUseCase,
        private val getAllPaymentLinksByReferenceNumberUseCase: GetAllPaymentLinksByReferenceNumberUseCase
) : BaseViewModel(){


    private val _paymentLinkListPaginatedResponse = MutableLiveData<GetPaymentLinkListPaginatedResponse>()
    val paymentLinkListPaginatedResponse: LiveData<GetPaymentLinkListPaginatedResponse> = _paymentLinkListPaginatedResponse


    fun getAllPaymentLinks() {
        getAllPaymentLinksUseCase.execute(
                getDisposableSingleObserver(
                        {
                            _paymentLinkListPaginatedResponse.value = it
                        }, {
                    Timber.e(it, "getAccounts")
                    _uiState.value = Event(UiState.Error(it))
                }
                ),
                doOnSubscribeEvent = {
                    _uiState.value = Event(UiState.Loading)
                },
                doFinallyEvent = {
                    _uiState.value = Event(UiState.Complete)
                },
                params = GetPaymentLinkListPaginatedForm(1,10)
        ).addTo(disposables)
    }

    fun doSearch(query: String) {
        getAllPaymentLinksByReferenceNumberUseCase.execute(
                getDisposableSingleObserver(
                        {
                            _paymentLinkListPaginatedResponse.value = it
                        }, {
                    Timber.e(it, "doSearch")
                    _uiState.value = Event(UiState.Error(it))
                }
                ),
                doOnSubscribeEvent = {
                    _uiState.value = Event(UiState.Loading)
                },
                doFinallyEvent = {
                    _uiState.value = Event(UiState.Complete)
                },
                params = GetPaymentLinkListByReferenceNumberForm(1,10, query )
        ).addTo(disposables)
    }
}