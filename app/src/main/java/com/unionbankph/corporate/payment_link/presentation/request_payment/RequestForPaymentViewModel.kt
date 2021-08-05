package com.unionbankph.corporate.payment_link.presentation.request_payment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.account.domain.form.GetAccountsBalances
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.getDisposableSingleObserver
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.payment_link.domain.model.form.GeneratePaymentLinkForm
import com.unionbankph.corporate.payment_link.domain.model.form.UpdateSettlementOnRequestPaymentForm
import com.unionbankph.corporate.payment_link.domain.model.response.GeneratePaymentLinkResponse
import com.unionbankph.corporate.payment_link.domain.model.response.UpdateSettlementOnRequestPaymentResponse
import com.unionbankph.corporate.payment_link.domain.usecase.GeneratePaymentLinkUseCase
import com.unionbankph.corporate.payment_link.domain.usecase.GetAccountsBalanceUseCase
import com.unionbankph.corporate.payment_link.domain.usecase.GetAccountsUseCase
import com.unionbankph.corporate.payment_link.domain.usecase.UpdateSettlementOnRequestPaymentUseCase
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import java.lang.Exception
import javax.inject.Inject

class RequestForPaymentViewModel
@Inject constructor(
    private val generatePaymentLinkUseCase: GeneratePaymentLinkUseCase,
    private val getAccountsUseCase: GetAccountsUseCase,
    private val getAccountsBalanceUseCase: GetAccountsBalanceUseCase,
    private val updateSettlementOnRequestPaymentUseCase: UpdateSettlementOnRequestPaymentUseCase
) : BaseViewModel(){


    private val _linkDetailsResponse = MutableLiveData<GeneratePaymentLinkResponse>()

    val linkDetailsResponse: LiveData<GeneratePaymentLinkResponse>
        get() = _linkDetailsResponse
    val _linkDetailsState = MutableLiveData<RequestForPaymentLinkState>()


    private val _accounts = MutableLiveData<MutableList<Account>>()
    val accounts: LiveData<MutableList<Account>> = _accounts

    private val _soleAccount = MutableLiveData<Account>()
    val soleAccount: LiveData<Account> = _soleAccount

    private val _accountsBalances = MutableLiveData<MutableList<Account>>()
    val accountsBalances: LiveData<MutableList<Account>> = _accountsBalances

    private val _updateSettlementOnRequestPaymentResponse = MutableLiveData<UpdateSettlementOnRequestPaymentResponse>()
    val updateSettlementOnRequestPaymentResponse: LiveData<UpdateSettlementOnRequestPaymentResponse>
    get() = _updateSettlementOnRequestPaymentResponse

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



    fun getAccounts() {
        getAccountsUseCase.execute(
            getDisposableSingleObserver(
                {
                    if(it.results.size == 1){
                        _soleAccount.value = it.results.first()
                        val tempList = mutableListOf<Account>()
                        tempList.add(it.results.first())
                        getAccountBalances(tempList)
                    }else if(it.results.size > 1){
                        _accounts.value = it.results
                    }else{
                        _linkDetailsState.value = ShowNoOtherAvailableAccounts
                    }
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
            params = null
        ).addTo(disposables)
    }

    private fun getAccountBalances(accounts: MutableList<Account>) {
        var accountNumberList : MutableList<Int> = arrayListOf()
        accounts.forEach{
            it.id?.let {
                    it1 -> accountNumberList.add(it1)
            }
        }
        getAccountsBalanceUseCase.execute(
            getDisposableSingleObserver(
                {
                    _accountsBalances.value = it
                }, {
                    Timber.e(it, "getAccounts Balances")
                    _uiState.value = Event(UiState.Error(it))
                }
            ),
            doOnSubscribeEvent = {
                _uiState.value = Event(UiState.Loading)
            },
            doFinallyEvent = {
                _uiState.value = Event(UiState.Complete)
            },
            params = GetAccountsBalances(
                accountNumberList
            )
        ).addTo(disposables)
    }

    fun prepareSettlement(accountNo: String){
        updateSettlementOnRequestPayment(
            UpdateSettlementOnRequestPaymentForm(
                accountNo
            )
        )
    }

    private fun updateSettlementOnRequestPayment(updateSettlementOnRequestPaymentForm: UpdateSettlementOnRequestPaymentForm){

        updateSettlementOnRequestPaymentUseCase.execute(
            getDisposableSingleObserver(
                {
                    _updateSettlementOnRequestPaymentResponse.value = it
                },{
                    Timber.e(it, "update Settlement Failed")
                    try{
                        if(it.message?.contains("Invalid source account", true) == true){

                        }else{
                            _uiState.value = Event(UiState.Error(it))
                        }
                    } catch (e: Exception) {
                        _uiState.value = Event(UiState.Error(it))
                    }
                }
            ),
            doOnSubscribeEvent = {
                _uiState.value = Event(UiState.Loading)
            },
            doFinallyEvent = {
                _uiState.value = Event(UiState.Complete)
            },
            params = updateSettlementOnRequestPaymentForm
        ).addTo(disposables)
    }

}

sealed class RequestForPaymentLinkState

object ShowNoOtherAvailableAccounts : RequestForPaymentLinkState()

object ShowTheApproverPermissionRequired : RequestForPaymentLinkState()

data class ShouldContinueGenerate(val shouldContinue: Boolean) : RequestForPaymentLinkState()

data class ErrorMerchantDisabled(val throwable: Throwable) : RequestForPaymentLinkState()