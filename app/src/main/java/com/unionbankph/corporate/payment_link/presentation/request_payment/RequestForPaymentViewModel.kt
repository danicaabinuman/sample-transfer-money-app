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
import com.unionbankph.corporate.payment_link.domain.usecase.*
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject

class RequestForPaymentViewModel
@Inject constructor(
    private val generatePaymentLinkUseCase: GeneratePaymentLinkUseCase,
    private val getAccountsUseCase: GetAccountsUseCase,
    private val getAccountsBalanceUseCase: GetAccountsBalanceUseCase,
    private val updateSettlementOnRequestPaymentUseCase: UpdateSettlementOnRequestPaymentUseCase,
    private val getDefaultMerchantSA: GetDefaultMerchantSAUseCase
) : BaseViewModel(){


    private val _linkDetailsResponse = MutableLiveData<GeneratePaymentLinkResponse>()

    val linkDetailsResponse: LiveData<GeneratePaymentLinkResponse>
        get() = _linkDetailsResponse
    val _linkDetailsState = MutableLiveData<RequestForPaymentLinkState>()

    private val _defaultMerchantSA = MutableLiveData<Account>()
    val defaultMerchantSA = _defaultMerchantSA

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

        val expiry = when (selectedExpiry) {
            "6 hours" -> 6
            "1 day" -> 24
            "2 days" -> 48
            "3 days" -> 72
            "7 days" -> 168
            else -> 12
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

            GeneratePaymentLinkForm().apply {
                this.totalAmount = amount.replace("PHP","").replace(",","").trim().toDouble()
                this.description = paymentFor
                this.notes = notes
                this.paymentLinkExpiry = expiry
                this.mobileNumber = finalMobileNumber  ?: ""
            }
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
                    _uiState.value = Event(UiState.Complete)

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

    fun getDefaultMerchantSettlementAccount() {
        getDefaultMerchantSA.execute(
            getDisposableSingleObserver(
                {

                    if (it.size == 1) {
                        _defaultMerchantSA.value = it.first()
                        _soleAccount.value = it.first()
                        val tempList = mutableListOf<Account>()
                        tempList.add(it.first())
                        getAccountBalances(tempList)
                    } else if (it.size > 1) {
                        _defaultMerchantSA.value = it.first()
                        _accounts.value = it
//                        val tempList = mutableListOf<Account>()
//                        tempList.addAl
                        getAccountBalances(it)
                    } else {
                        _linkDetailsState.value = ShowNoOtherAvailableAccounts
                    }
                }, {
                    _uiState.value = Event(UiState.Error(it))
                }
            ),
            doOnSubscribeEvent = {
                _uiState.value = Event(UiState.Loading)
            },
            doFinallyEvent = {
                _uiState.value = Event(UiState.Complete)
            }
        )
    }

    fun updateDefaultSettlementAccount(accountNumber: String){

        updateSettlementOnRequestPaymentUseCase.execute(
            getDisposableSingleObserver(
                {
                    _updateSettlementOnRequestPaymentResponse.value = it
                },{
                    _uiState.value = Event(UiState.Error(it))
                }
            ),
            doOnSubscribeEvent = {
                _uiState.value = Event(UiState.Loading)
            },
            doFinallyEvent = {
                _uiState.value = Event(UiState.Complete)
            },
            params = UpdateSettlementOnRequestPaymentForm(accountNumber = accountNumber)
        ).addTo(disposables)
    }

}

sealed class RequestForPaymentLinkState

object ShowNoOtherAvailableAccounts : RequestForPaymentLinkState()

object ShowTheApproverPermissionRequired : RequestForPaymentLinkState()

data class ShouldContinueGenerate(val shouldContinue: Boolean) : RequestForPaymentLinkState()

data class ErrorMerchantDisabled(val throwable: Throwable) : RequestForPaymentLinkState()