package com.unionbankph.corporate.payment_link.presentation.setup_payment_link.nominate_settlement_account

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.account.domain.form.GetAccountsBalances
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.getDisposableSingleObserver
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.payment_link.domain.usecase.GetAccountsBalanceUseCase
import com.unionbankph.corporate.payment_link.domain.usecase.GetAccountsBalanceUseCase_Factory
import com.unionbankph.corporate.payment_link.domain.usecase.GetAccountsUseCase
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class NominateSettlementViewModel @Inject constructor(
        private val getAccountsBalanceUseCase: GetAccountsBalanceUseCase
) : BaseViewModel()  {

    private val _accountsBalances = MutableLiveData<MutableList<Account>>()
    val accountsBalances: LiveData<MutableList<Account>> = _accountsBalances


    fun initBundleData(accountsJson: String) {

        //Convert JSON to MutableList<Account>
        Timber.d(accountsJson)
        //val data = convertHere
        //getAccountBalances(data)
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