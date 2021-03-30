package com.unionbankph.corporate.link_details.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.account.data.model.AccountTransactionHistoryDetails
import com.unionbankph.corporate.account.data.model.Record
import com.unionbankph.corporate.account.domain.form.GetAccountTransactionHistoryDetailsForm
import com.unionbankph.corporate.account.domain.interactor.GetAccountTransactionHistoryDetails
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.convertDateToDesireFormat
import com.unionbankph.corporate.app.common.extension.getDisposableSingleObserver
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.auth.data.model.Auth
import com.unionbankph.corporate.auth.presentation.login.*
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.common.presentation.constant.DateFormatEnum
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.link_details.data.LinkDetailsResponse
import com.unionbankph.corporate.link_details.data.form.LinkDetailsForm
import com.unionbankph.corporate.link_details.domain.interactor.GetPaymentLinkDetails
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import kotlinx.android.synthetic.main.activity_link_details.*
import timber.log.Timber
import javax.inject.Inject

class LinkDetailsViewModel
@Inject constructor(
    private val getPaymentLinkDetails: GetPaymentLinkDetails
) : BaseViewModel(){


    private val _linkDetailsResponse = MutableLiveData<LinkDetailsResponse>()

    val linkDetailsResponse: LiveData<LinkDetailsResponse>
        get() =
            _linkDetailsResponse

    private val _navigateViewTransaction = MutableLiveData<Event<String>>()

    val navigateViewTransaction: LiveData<Event<String>> get() = _navigateViewTransaction


    fun initBundleData(amount: String, paymentFor: String, notes: String?, selectedExpiry: String) {

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

        generateLinkDetails(
            LinkDetailsForm(
                amount.replace("PHP","").replace(",","").trim().toDouble(),
                paymentFor,
                notes,
                expiry,
                "09176770225",
                "Test Org 6247 2"
            )
        )

    }

    private fun generateLinkDetails(linkDetailsForm: LinkDetailsForm){

        getPaymentLinkDetails.execute(
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