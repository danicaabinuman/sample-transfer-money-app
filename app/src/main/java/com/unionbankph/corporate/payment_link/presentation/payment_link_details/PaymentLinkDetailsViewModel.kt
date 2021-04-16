package com.unionbankph.corporate.payment_link.presentation.payment_link_details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.getDisposableSingleObserver
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.payment_link.domain.model.form.GeneratePaymentLinkForm
import com.unionbankph.corporate.payment_link.domain.model.response.GeneratePaymentLinkResponse
import com.unionbankph.corporate.payment_link.domain.usecase.GeneratePaymentLinkUseCase
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject

class LinkDetailsViewModel
@Inject constructor(
    private val generatePaymentLinkUseCase: GeneratePaymentLinkUseCase
) : BaseViewModel() {


    private val _linkDetailsResponse = MutableLiveData<GeneratePaymentLinkResponse>()

    val linkDetailsResponse: LiveData<GeneratePaymentLinkResponse>
        get() =
            _linkDetailsResponse

    private val _navigateViewTransaction = MutableLiveData<Event<String>>()

    val navigateViewTransaction: LiveData<Event<String>> get() = _navigateViewTransaction


    fun initBundleData(
        amount: String,
        paymentFor: String,
        notes: String?,
        selectedExpiry: String,
        mobileNumber: String
    ) {

        var expiry = 12

        if (selectedExpiry.equals("6 hours", true)) {
            expiry = 6
        } else if (selectedExpiry.equals("12 hours", true)) {
            expiry = 12
        } else if (selectedExpiry.equals("1 day", true)) {
            expiry = 24
        } else if (selectedExpiry.equals("2 days", true)) {
            expiry = 48
        } else if (selectedExpiry.equals("3 days", true)) {
            expiry = 72
        } else if (selectedExpiry.equals("7 days", true)) {
            expiry = 168
        }

        var finalMobileNumber = mobileNumber
        if (!mobileNumber.first().equals('0', true)) {
            finalMobileNumber = "0$mobileNumber"
        }

        generateLinkDetails(
            GeneratePaymentLinkForm(
                amount.replace("PHP", "").replace(",", "").trim().toDouble(),
                paymentFor,
                notes,
                expiry,
                finalMobileNumber,
                null
            )
        )

    }

    private fun generateLinkDetails(linkDetailsForm: GeneratePaymentLinkForm) {

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


}