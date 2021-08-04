package com.unionbankph.corporate.payment_link.presentation.setup_payment_link.card_acceptance_option

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.account.domain.form.GetAccountsBalances
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.getDisposableSingleObserver
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.payment_link.domain.usecase.GetAccountsBalanceUseCase
import com.unionbankph.corporate.payment_link.domain.usecase.GetAccountsUseCase
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import javax.inject.Inject

class NotNowCardPaymentsViewModel @Inject constructor(

    private val getAccountsUseCase: GetAccountsUseCase,
    private val getAccountsBalanceUseCase: GetAccountsBalanceUseCase

) : BaseViewModel() {

    private val _eligibleAccount = MutableLiveData<AccountState>()
    val eligibleAccount: LiveData<AccountState> = _eligibleAccount

    private val _accountWithBalance = MutableLiveData<Account>()
    val accountWithBalance: LiveData<Account> = _accountWithBalance

    fun getAccounts() {
        getAccountsUseCase.execute(
            getDisposableSingleObserver({
                if (it.results.size == 1) {
                    getAccountBalance(it.results.first())
                } else if (it.results.size > 1) {
                    _eligibleAccount.value = ShowHasMultipleAccount(it.results)
                } else {
                    _eligibleAccount.value = ShowNoEligibleAccounts
                }
            }, {
                _uiState.value = Event(UiState.Error(it))
            }),
            doOnSubscribeEvent = {
                _uiState.value = Event(UiState.Loading)
            },
            doFinallyEvent = {
                _uiState.value = Event(UiState.Complete)
            },
            params = null
        ).addTo(disposables)
    }

    private fun getAccountBalance(account: Account) {
        val accountNumberList : MutableList<Int> = arrayListOf()
        accountNumberList.add(account.id!!)

        getAccountsBalanceUseCase.execute(
            getDisposableSingleObserver(
                {
                    _eligibleAccount.value = ShowHasSoleAccount(it.first())
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
            params = GetAccountsBalances(
                accountNumberList
            )
        ).addTo(disposables)
    }
}

sealed class AccountState

data class ShowHasSoleAccount(val account: Account) : AccountState()

data class ShowHasMultipleAccount(val accounts: MutableList<Account>) : AccountState()

object ShowNoEligibleAccounts : AccountState()