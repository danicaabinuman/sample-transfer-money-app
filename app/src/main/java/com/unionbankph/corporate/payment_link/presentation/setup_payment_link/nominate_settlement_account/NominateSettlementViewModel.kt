package com.unionbankph.corporate.payment_link.presentation.setup_payment_link.nominate_settlement_account

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class NominateSettlementViewModel @Inject constructor(
) : BaseViewModel()  {

    private val _settlementAccounts =
            MutableLiveData<MutableList<Account>>()

    val settlementAccounts: LiveData<MutableList<Account>> get() = _settlementAccounts

    fun initBundleData(settlementAccounts: String?) {

        Observable.just(settlementAccounts)
                .map { JsonHelper.fromJson<MutableList<Account>>(it) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            _settlementAccounts.value = it
                        }, {
                    Timber.e(it, "setupRecord")
                }
                ).addTo(disposables)
    }
}