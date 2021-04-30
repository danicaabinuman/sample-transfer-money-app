package com.unionbankph.corporate.payment_link.presentation.setup_payment_link

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.account.domain.form.GetAccountsBalances
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.getDisposableSingleObserver
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.app.dashboard.DashboardState
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.payment_link.domain.model.form.CreateMerchantForm
import com.unionbankph.corporate.payment_link.domain.model.response.CreateMerchantResponse
import com.unionbankph.corporate.payment_link.domain.usecase.CreateMerchantUseCase
import com.unionbankph.corporate.payment_link.domain.usecase.GetAccountsBalanceUseCase
import com.unionbankph.corporate.payment_link.domain.usecase.GetAccountsUseCase
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject

class SetupPaymentLinkViewModel
@Inject constructor(
    private val createMerchantUseCase: CreateMerchantUseCase,
    private val getAccountsUseCase: GetAccountsUseCase,
    private val getAccountsBalanceUseCase: GetAccountsBalanceUseCase
) : BaseViewModel() {

    private val _setupPaymentLinkState = MutableLiveData<SetupPaymentLinkState>()
    val setupPaymentLinkState: LiveData<SetupPaymentLinkState> get() = _setupPaymentLinkState

    private val _createMerchantResponse = MutableLiveData<CreateMerchantResponse>()
    val createMerchantResponse: LiveData<CreateMerchantResponse>
        get() =
            _createMerchantResponse


    private val _accounts = MutableLiveData<MutableList<Account>>()
    val accounts: LiveData<MutableList<Account>> = _accounts

    private val _soleAccount = MutableLiveData<Account>()
    val soleAccount: LiveData<Account> = _soleAccount

    private val _accountsBalances = MutableLiveData<MutableList<Account>>()
    val accountsBalances: LiveData<MutableList<Account>> = _accountsBalances

    fun createMerchant(form: CreateMerchantForm){

        createMerchantUseCase.execute(
            getDisposableSingleObserver(
                {
                    _createMerchantResponse.value = it
                }, {
                    Timber.e(it, "createMerchant")
                    _uiState.value = Event(UiState.Error(it))
                }
            ),
            doOnSubscribeEvent = {
                _uiState.value = Event(UiState.Loading)
            },
            doFinallyEvent = {
                _uiState.value = Event(UiState.Complete)
            },
            params = form
        ).addTo(disposables)
    }

    fun getAccounts() {
        getAccountsUseCase.execute(
            getDisposableSingleObserver(
                {
                    if(it.size == 1){
                        _soleAccount.value = it.first()
                        getAccountBalances(it)
                    }else if(it.size > 1){
                        //PASS TO NOMINATE SETTLEMENT ACTIVITY
                    }else{
                        _setupPaymentLinkState.value = ShowNoAvailableAccounts
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


}



sealed class SetupPaymentLinkState

object ShowNoAvailableAccounts : SetupPaymentLinkState()