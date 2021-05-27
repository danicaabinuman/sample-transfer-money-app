package com.unionbankph.corporate.payment_link.presentation.request_payment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.getDisposableSingleObserver
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.payment_link.domain.model.form.GeneratePaymentLinkForm
import com.unionbankph.corporate.payment_link.domain.model.response.GeneratePaymentLinkResponse
import com.unionbankph.corporate.payment_link.domain.usecase.GeneratePaymentLinkUseCase
import com.unionbankph.corporate.payment_link.presentation.payment_link_list.PaymentLinkListState
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import java.lang.Error
import javax.inject.Inject

class RequestForPaymentViewModel
@Inject constructor(
    private val generatePaymentLinkUseCase: GeneratePaymentLinkUseCase
) : BaseViewModel(){


    private val _linkDetailsResponse = MutableLiveData<GeneratePaymentLinkResponse>()

    val linkDetailsResponse: LiveData<GeneratePaymentLinkResponse>
        get() =
            _linkDetailsResponse

    val _linkDetailsState = MutableLiveData<RequestForPaymentLinkState>()

    fun preparePaymentLinkGeneration(amount: String, paymentFor: String, notes: String?, selectedExpiry: String, mobileNumber: String?){
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
//                    _uiState.value = Event(UiState.Error(it))
                    if (it.message.equals("Unable to generate new link, your merchant is currently disabled.", true)){
                        _linkDetailsState.value = ErrorMerchantDisabled(it)
                    } else {
                        _linkDetailsState.value = ShouldContinueGenerate(true)
                    }

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

}

sealed class RequestForPaymentLinkState

data class ShouldContinueGenerate(val shouldContinue: Boolean) : RequestForPaymentLinkState()

data class ErrorMerchantDisabled(val throwable: Throwable) : RequestForPaymentLinkState()