package com.unionbankph.corporate.mcd.presentation.confirmation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.mcd.data.form.CheckDepositForm
import com.unionbankph.corporate.mcd.data.gateway.CheckDepositGateway
import com.unionbankph.corporate.mcd.data.model.CheckDeposit
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class CheckDepositConfirmationViewModel @Inject constructor(
    private val checkDepositGateway: CheckDepositGateway
) : BaseViewModel() {

    private val _state = MutableLiveData<CheckDepositConfirmationState>()

    private val _checkDeposit = MutableLiveData<CheckDeposit>()

    val state: LiveData<CheckDepositConfirmationState> get() = _state

    val checkDeposit: LiveData<CheckDeposit> get() = _checkDeposit

    fun checkDeposit(checkDepositForm: CheckDepositForm) {
        checkDepositGateway.checkDeposit(checkDepositForm)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                _state.value = ShowCheckDepositConfirmationLoading
            }
            .doFinally {
                _state.value = ShowCheckDepositConfirmationDismissLoading
            }
            .subscribe(
                {
                    _checkDeposit.value = it
                }, {
                    Timber.e(it, "checkDeposit failed")
                    _state.value = ShowCheckDepositConfirmationError(it)
                }
            )
            .addTo(disposables)
    }
}

sealed class CheckDepositConfirmationState

object ShowCheckDepositConfirmationLoading : CheckDepositConfirmationState()

object ShowCheckDepositConfirmationDismissLoading : CheckDepositConfirmationState()

data class ShowCheckDepositConfirmationError(val throwable: Throwable) :
    CheckDepositConfirmationState()
