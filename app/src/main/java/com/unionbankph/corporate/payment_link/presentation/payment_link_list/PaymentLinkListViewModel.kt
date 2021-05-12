package com.unionbankph.corporate.payment_link.presentation.payment_link_list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.getDisposableSingleObserver
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.app.dashboard.DashboardState
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.payment_link.domain.model.form.GetPaymentLinkListByReferenceNumberForm
import com.unionbankph.corporate.payment_link.domain.model.form.GetPaymentLinkListPaginatedForm
import com.unionbankph.corporate.payment_link.domain.model.response.GetPaymentLinkByReferenceIdResponse
import com.unionbankph.corporate.payment_link.domain.model.response.GetPaymentLinkListPaginatedResponse
import com.unionbankph.corporate.payment_link.domain.usecase.*
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

    private val _searchPaymentLinkListPaginatedResponse = MutableLiveData<GetPaymentLinkListPaginatedResponse>()
    val searchPaymentLinkListPaginatedResponse: LiveData<GetPaymentLinkListPaginatedResponse> = _searchPaymentLinkListPaginatedResponse

    private val _nextPaymentLinkListPaginatedResponse = MutableLiveData<GetPaymentLinkListPaginatedResponse>()
    val nextPaymentLinkListPaginatedResponse: LiveData<GetPaymentLinkListPaginatedResponse> = _nextPaymentLinkListPaginatedResponse

    private val _paymentLinkListState = MutableLiveData<PaymentLinkListState>()

    val paymentLinkListState: LiveData<PaymentLinkListState> get() = _paymentLinkListState

    fun getAllPaymentLinks() {
        mCurrentPage = 1
        getAllPaymentLinksUseCase.execute(
                getDisposableSingleObserver(
                        {
                            if(it.data != null){
                                if(it.data!!.isNotEmpty()){
                                    _paymentLinkListPaginatedResponse.value = it
                                    _paymentLinkListState.value = ShouldShowRecyclerView(true)
                                    _paymentLinkListState.value = ShouldShowNoAvailablePaymentLinks(false)
                                }else{
                                    _paymentLinkListState.value = ShouldShowRecyclerView(false)
                                    _paymentLinkListState.value = ShouldShowNoAvailablePaymentLinks(true)
                                }
                            }else{
                                _paymentLinkListState.value = ShouldShowRecyclerView(false)
                                _paymentLinkListState.value = ShouldShowNoAvailablePaymentLinks(true)
                            }
                        }, {
                    Timber.e(it, "getAllPaymentLinks")
                    _paymentLinkListState.value = Error(it)
                }
                ),
                doOnSubscribeEvent = {
                    _paymentLinkListState.value = ShouldShowProgressLoading(true)
                    _paymentLinkListState.value = ShouldShowRecyclerView(false)
                    _paymentLinkListState.value = ShouldShowLazyLoading(false)
                    _paymentLinkListState.value = ShouldShowNoAvailablePaymentLinks(false)
                },
                doFinallyEvent = {
                    _paymentLinkListState.value = ShouldShowProgressLoading(false)
                },
                params = GetPaymentLinkListPaginatedForm(mCurrentPage,10)
        ).addTo(disposables)
    }

    fun getAllNextPaymentLinks() {
        mCurrentPage++
        getAllPaymentLinksUseCase.execute(
                getDisposableSingleObserver(
                        {
                            if(it.data != null){
                                if(it.data!!.isNotEmpty()){
                                    _nextPaymentLinkListPaginatedResponse.value = it
                                }else{
                                    mCurrentPage--
                                    _paymentLinkListState.value = NoMoreAvailablePaymentLinks
                                }
                            }else{
                                mCurrentPage--
                                _paymentLinkListState.value = NoMoreAvailablePaymentLinks
                            }
                        }, {
                    Timber.e(it, "getAllNextPaymentLinks")
                    _paymentLinkListState.value = Error(it)
                }
                ),
                doOnSubscribeEvent = {
                    _paymentLinkListState.value = ShouldShowProgressLoading(false)
                    _paymentLinkListState.value = ShouldShowRecyclerView(true)
                    _paymentLinkListState.value = ShouldShowLazyLoading(true)
                    _paymentLinkListState.value = ShouldShowNoAvailablePaymentLinks(false)
                },
                doFinallyEvent = {
                    _paymentLinkListState.value = ShouldShowLazyLoading(false)
                },
                params = GetPaymentLinkListPaginatedForm(mCurrentPage,10)
        ).addTo(disposables)
    }

    fun doSearch(query: String) {
        getAllPaymentLinksByReferenceNumberUseCase.execute(
                getDisposableSingleObserver(
                        {

                            if(it.data != null){
                                if(it.data!!.isNotEmpty()){
                                    _searchPaymentLinkListPaginatedResponse.value = it
                                }else{
                                    mCurrentPage--
                                    _paymentLinkListState.value = ShouldShowNoAvailablePaymentLinks(true)
                                    _paymentLinkListState.value = ShouldShowRecyclerView(false)
                                }
                            }else{
                                mCurrentPage--
                                _paymentLinkListState.value = ShouldShowNoAvailablePaymentLinks(true)
                                _paymentLinkListState.value = ShouldShowRecyclerView(false)
                            }

                        }, {
                    Timber.e(it, "doSearch")
                    if(it.message.equals("Merchant does not have any payment links.",true)){
                        _paymentLinkListState.value = ShouldShowNoAvailablePaymentLinks(true)
                        _paymentLinkListState.value = ShouldShowRecyclerView(false)
                    }else{
                        _paymentLinkListState.value = Error(it)
                        _paymentLinkListState.value = ShouldShowRecyclerView(true)
                    }
                }
                ),
                doOnSubscribeEvent = {
                    _paymentLinkListState.value = ShouldShowProgressLoading(true)
                    _paymentLinkListState.value = ShouldShowRecyclerView(true)
                    _paymentLinkListState.value = ShouldShowLazyLoading(false)
                    _paymentLinkListState.value = ShouldShowNoAvailablePaymentLinks(false)
                },
                doFinallyEvent = {
                    _paymentLinkListState.value = ShouldShowProgressLoading(false)
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
                    _paymentLinkListState.value = Error(it)
                }
                ),
                doOnSubscribeEvent = {
                    _paymentLinkListState.value = ShouldShowProgressLoading(true)
                    _paymentLinkListState.value = ShouldShowRecyclerView(true)
                    _paymentLinkListState.value = ShouldShowLazyLoading(false)
                    _paymentLinkListState.value = ShouldShowNoAvailablePaymentLinks(false)
                },
                doFinallyEvent = {
                    _paymentLinkListState.value = ShouldShowProgressLoading(false)
                },
                params = referenceNumber
        ).addTo(disposables)
    }

}

sealed class PaymentLinkListState

data class ShouldShowProgressLoading(val shouldShow: Boolean) : PaymentLinkListState()

data class ShouldShowLazyLoading(val shouldShow: Boolean) : PaymentLinkListState()

data class ShouldShowRecyclerView(val shouldShow: Boolean) : PaymentLinkListState()

data class ShouldShowNoAvailablePaymentLinks(val shouldShow: Boolean) : PaymentLinkListState()

object NoMoreAvailablePaymentLinks : PaymentLinkListState()

data class Error(val throwable: Throwable) : PaymentLinkListState()
