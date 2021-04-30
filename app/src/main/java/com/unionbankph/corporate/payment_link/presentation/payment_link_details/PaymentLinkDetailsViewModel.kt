package com.unionbankph.corporate.payment_link.presentation.payment_link_details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.getDisposableSingleObserver
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.payment_link.domain.model.form.GeneratePaymentLinkForm
import com.unionbankph.corporate.payment_link.domain.model.response.GeneratePaymentLinkResponse
import com.unionbankph.corporate.payment_link.domain.model.response.GetPaymentLinkByReferenceIdResponse
import com.unionbankph.corporate.payment_link.domain.model.response.PutPaymentLinkStatusResponse
import com.unionbankph.corporate.payment_link.domain.usecase.ArchivePaymentLinkUseCase
import com.unionbankph.corporate.payment_link.domain.usecase.GeneratePaymentLinkUseCase
import com.unionbankph.corporate.payment_link.domain.usecase.GetPaymentLinkByReferenceIdUseCase
import com.unionbankph.corporate.payment_link.domain.usecase.MarkAsUnpaidPaymentLinkUseCase
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject

class LinkDetailsViewModel
@Inject constructor(
    private val generatePaymentLinkUseCase: GeneratePaymentLinkUseCase,
    private val archivePaymentLinkUseCase: ArchivePaymentLinkUseCase,
    private val markAsUnpaidPaymentLinkUseCase: MarkAsUnpaidPaymentLinkUseCase,
    private val getPaymentLinkByReferenceIdUseCase: GetPaymentLinkByReferenceIdUseCase
) : BaseViewModel(){


    private val _linkDetailsResponse = MutableLiveData<GeneratePaymentLinkResponse>()

    val linkDetailsResponse: LiveData<GeneratePaymentLinkResponse>
        get() =
            _linkDetailsResponse


    private val _archivePaymentLinkResponse = MutableLiveData<PutPaymentLinkStatusResponse>()

    val archivePaymentLinkResponse: LiveData<PutPaymentLinkStatusResponse>
        get() =
            _archivePaymentLinkResponse

    private val _navigateViewTransaction = MutableLiveData<Event<String>>()

    val navigateViewTransaction: LiveData<Event<String>> get() = _navigateViewTransaction


    private val _markAsUnpaidPaymentLinkResponse = MutableLiveData<PutPaymentLinkStatusResponse>()

    val markAsUnpaidPaymentLinkResponse: LiveData<PutPaymentLinkStatusResponse>
        get() =
            _markAsUnpaidPaymentLinkResponse


    fun initBundleData(amount: String, paymentFor: String, notes: String?, selectedExpiry: String, mobileNumber: String?) {

        var expiry = 12

        if(selectedExpiry.equals("6 hours",true)){
            expiry = 6
        }else if (selectedExpiry.equals("12 hours", true)){
            expiry = 12
        }else if (selectedExpiry.equals("1 day", true)){
            expiry = 24
        }else if (selectedExpiry.equals("2 days", true)){
            expiry = 48
        }else if (selectedExpiry.equals("3 days", true)){
            expiry = 72
        }else if (selectedExpiry.equals("7 days", true)){
            expiry = 168
        }

        var finalMobileNumber : String? = null
        mobileNumber?.let {
            if(mobileNumber.isNotEmpty() && mobileNumber.length < 11){
                if(!mobileNumber.first().equals('0',true)){
                    finalMobileNumber = "0$mobileNumber"
                }else{
                    finalMobileNumber = mobileNumber
                }
            }
        }


        generateLinkDetails(
            GeneratePaymentLinkForm(
                amount.replace("PHP","").replace(",","").trim().toDouble(),
                paymentFor,
                notes,
                expiry,
                finalMobileNumber,
                null,
                    null
            )
        )

    }

    private fun generateLinkDetails(linkDetailsForm: GeneratePaymentLinkForm){

        generatePaymentLinkUseCase.execute(
            getDisposableSingleObserver(
                {
                    _linkDetailsResponse.value = it
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
            params = linkDetailsForm
        ).addTo(disposables)
    }



    fun getPaymentLinkDetailsThenArchive(referenceId: String){
        getPaymentLinkByReferenceIdUseCase.execute(
                getDisposableSingleObserver(
                        {
                            archive(it.transactionId!!)
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

    fun getPaymentLinkDetailsThenMarkAsUnpaid(referenceId: String){
        getPaymentLinkByReferenceIdUseCase.execute(
            getDisposableSingleObserver(
                {
                    markAsUnpaid(it.transactionId!!)
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

    private fun archive(transactionId: String){
        archivePaymentLinkUseCase.execute(
                getDisposableSingleObserver(
                        {
                            _archivePaymentLinkResponse.value = it
                        }, {
                    Timber.e(it, "putPaymentLinkStatus")
                    _uiState.value = Event(UiState.Error(it))
                }
                ),
                doOnSubscribeEvent = {
                    _uiState.value = Event(UiState.Loading)
                },
                doFinallyEvent = {
                    _uiState.value = Event(UiState.Complete)
                },
                params = transactionId
        ).addTo(disposables)
    }

    private fun markAsUnpaid(transactionId: String){
        markAsUnpaidPaymentLinkUseCase.execute(
            getDisposableSingleObserver(
                {
                    _markAsUnpaidPaymentLinkResponse.value = it
                }, {
                    Timber.e(it, "putPaymentLinkStatus")
                    _uiState.value = Event(UiState.Error(it))
                }
            ),
            doOnSubscribeEvent = {
                _uiState.value = Event(UiState.Loading)
            },
            doFinallyEvent = {
                _uiState.value = Event(UiState.Complete)
            },
            params = transactionId
        ).addTo(disposables)
    }


}