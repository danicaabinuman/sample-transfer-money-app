package com.unionbankph.corporate.mcd.presentation.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.mcd.data.gateway.CheckDepositGateway
import com.unionbankph.corporate.mcd.data.model.CheckDeposit
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject

class CheckDepositDetailViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val checkDepositGateway: CheckDepositGateway
) : BaseViewModel() {

    private val _checkDepositState = MutableLiveData<CheckDepositDetailState>()

    val state: LiveData<CheckDepositDetailState> get() = _checkDepositState

    fun getCheckDeposit(id: String) {
        checkDepositGateway.getCheckDeposit(id)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe {
                _checkDepositState.value = ShowCheckDepositDetailLoading
            }
            .doFinally {
                _checkDepositState.value = ShowCheckDepositDetailDismissLoading
            }
            .subscribe(
                {
                    _checkDepositState.value = ShowCheckDepositDetailGetCheckDeposit(it)
                }, {
                    Timber.e(it, "getCheckDeposit failed")
                    _checkDepositState.value = ShowCheckDepositDetailError(it)
                })
            .addTo(disposables)
    }
}

sealed class CheckDepositDetailState

object ShowCheckDepositDetailLoading : CheckDepositDetailState()

object ShowCheckDepositDetailEndlessLoading : CheckDepositDetailState()

object ShowCheckDepositDetailDismissLoading : CheckDepositDetailState()

object ShowCheckDepositDetailDismissEndlessLoading : CheckDepositDetailState()

data class ShowCheckDepositDetailGetCheckDeposit(val data: CheckDeposit) :
    CheckDepositDetailState()

data class ShowCheckDepositDetailError(val throwable: Throwable) : CheckDepositDetailState()
