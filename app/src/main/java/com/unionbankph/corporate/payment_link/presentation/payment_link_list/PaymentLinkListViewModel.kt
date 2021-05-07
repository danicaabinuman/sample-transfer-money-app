package com.unionbankph.corporate.payment_link.presentation.payment_link_list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.getDisposableSingleObserver
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.payment_link.domain.model.form.GetPaymentLinkListByReferenceNumberForm
import com.unionbankph.corporate.payment_link.domain.model.form.GetPaymentLinkListPaginatedForm
import com.unionbankph.corporate.payment_link.domain.model.response.GetPaymentLinkByReferenceIdResponse
import com.unionbankph.corporate.payment_link.domain.model.response.GetPaymentLinkListPaginatedResponse
import com.unionbankph.corporate.payment_link.domain.usecase.GetAllPaymentLinksByReferenceNumberUseCase
import com.unionbankph.corporate.payment_link.domain.usecase.GetAllPaymentLinksUseCase
import com.unionbankph.corporate.payment_link.domain.usecase.GetPaymentLinkByReferenceIdUseCase
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject

class PaymentLinkListViewModel
@Inject constructor(
        private val getAllPaymentLinksUseCase: GetAllPaymentLinksUseCase,
        private val getAllPaymentLinksByReferenceNumberUseCase: GetAllPaymentLinksByReferenceNumberUseCase,
        private val getPaymentLinkByReferenceIdUseCase: GetPaymentLinkByReferenceIdUseCase
) : BaseViewModel(){

    private var mCurrentPage = 1;
    private val _paymentLinkDetailsResponse = MutableLiveData<GetPaymentLinkByReferenceIdResponse>()

    val paymentLinkDetailsResponse: LiveData<GetPaymentLinkByReferenceIdResponse>
        get() =
            _paymentLinkDetailsResponse

    private val _paymentLinkListPaginatedResponse = MutableLiveData<GetPaymentLinkListPaginatedResponse>()
    val paymentLinkListPaginatedResponse: LiveData<GetPaymentLinkListPaginatedResponse> = _paymentLinkListPaginatedResponse

    private val _nextPaymentLinkListPaginatedResponse = MutableLiveData<GetPaymentLinkListPaginatedResponse>()
    val nextPaymentLinkListPaginatedResponse: LiveData<GetPaymentLinkListPaginatedResponse> = _nextPaymentLinkListPaginatedResponse


    fun getAllPaymentLinks() {
        mCurrentPage = 1
        getAllPaymentLinksUseCase.execute(
                getDisposableSingleObserver(
                        {
                            _paymentLinkListPaginatedResponse.value = it
                        }, {
                    Timber.e(it, "getAllPaymentLinks")
                    _uiState.value = Event(UiState.Error(it))
                }
                ),
                doOnSubscribeEvent = {
                    _uiState.value = Event(UiState.Loading)
                },
                doFinallyEvent = {
                    _uiState.value = Event(UiState.Complete)
                },
                params = GetPaymentLinkListPaginatedForm(mCurrentPage,10)
        ).addTo(disposables)
    }

    fun getAllNextPaymentLinks() {
        mCurrentPage++
        getAllPaymentLinksUseCase.execute(
                getDisposableSingleObserver(
                        {
                            if(it.totalCount!! > 0){
                                _nextPaymentLinkListPaginatedResponse.value = it
                            }else{
                                mCurrentPage--
                            }

                        }, {
                    Timber.e(it, "getAllNextPaymentLinks")
                    _uiState.value = Event(UiState.Error(it))
                }
                ),
                doOnSubscribeEvent = {
                    _uiState.value = Event(UiState.Loading)
                },
                doFinallyEvent = {
                    _uiState.value = Event(UiState.Complete)
                },
                params = GetPaymentLinkListPaginatedForm(mCurrentPage,10)
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

    fun getPaymentLinkDetails(referenceNumber: String){
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
    }
}