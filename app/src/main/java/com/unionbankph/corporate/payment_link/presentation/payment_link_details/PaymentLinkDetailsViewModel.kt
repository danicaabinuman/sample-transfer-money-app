package com.unionbankph.corporate.payment_link.presentation.payment_link_details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.getDisposableSingleObserver
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.payment_link.domain.model.form.PutPaymentLinkStatusContainerForm
import com.unionbankph.corporate.payment_link.domain.model.form.PutPaymentLinkStatusForm
import com.unionbankph.corporate.payment_link.domain.model.response.PutPaymentLinkStatusResponse
import com.unionbankph.corporate.payment_link.domain.usecase.PutPaymentLinkStatusUseCase
import com.unionbankph.corporate.payment_link.domain.usecase.GetPaymentLinkByReferenceIdUseCase
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject

class LinkDetailsViewModel
@Inject constructor(
    private val putPaymentLinkStatusUseCase: PutPaymentLinkStatusUseCase,
    private val getPaymentLinkByReferenceIdUseCase: GetPaymentLinkByReferenceIdUseCase
) : BaseViewModel(){



    private val _putPaymentLinkStatusResponse = MutableLiveData<PutPaymentLinkStatusResponse>()

    val putPaymentLinkStatusResponse: LiveData<PutPaymentLinkStatusResponse>
        get() = _putPaymentLinkStatusResponse

    fun getPaymentLinkDetailsThenPut(referenceId: String, putStatus: String){
        getPaymentLinkByReferenceIdUseCase.execute(
                getDisposableSingleObserver(
                        {
                            proceedWithPut(
                                PutPaymentLinkStatusContainerForm(
                                    it.transactionId!!,
                                    PutPaymentLinkStatusForm(
                                        putStatus
                                    )
                                )
                            )
                        }, {
                    Timber.e(it, "getPaymentLinkDetails")
                    _uiState.value = Event(UiState.Error(it))
                }
                ),
                doOnSubscribeEvent = {
                    _uiState.value = Event(UiState.Loading)
                },
                doFinallyEvent = {
                    _uiState.value = Event(UiState.Complete)
                },
                params = referenceId
        ).addTo(disposables)
    }

    private fun proceedWithPut(putPaymentLinkStatusContainerForm: PutPaymentLinkStatusContainerForm){
        putPaymentLinkStatusUseCase.execute(
            getDisposableSingleObserver(
                {
                    _putPaymentLinkStatusResponse.value = it
                }, {
                    _uiState.value = Event(UiState.Error(it))
                }
            ),
            doOnSubscribeEvent = {
                _uiState.value = Event(UiState.Loading)
            },
            doFinallyEvent = {
                _uiState.value = Event(UiState.Complete)
            },
            params = putPaymentLinkStatusContainerForm
        ).addTo(disposables)
    }

    companion object{
        const val STATUS_ARCHIVED = "archived"
        const val STATUS_PAID = "paid"
        const val STATUS_UNPAID = "unpaid"
        const val STATUS_PENDING = "pending"
        const val STATUS_EXPIRED = "expired"
    }


}