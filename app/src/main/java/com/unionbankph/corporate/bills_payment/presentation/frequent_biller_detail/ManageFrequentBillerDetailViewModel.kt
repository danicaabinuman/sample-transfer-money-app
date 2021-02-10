package com.unionbankph.corporate.bills_payment.presentation.frequent_biller_detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.bills_payment.data.gateway.BillsPaymentGateway
import com.unionbankph.corporate.bills_payment.data.model.FrequentBiller
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject

class ManageFrequentBillerDetailViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val billsPaymentGateway: BillsPaymentGateway
) : BaseViewModel() {

    private val _frequentBillerDetailState = MutableLiveData<FrequentBillerDetailState>()

    val state: LiveData<FrequentBillerDetailState> get() = _frequentBillerDetailState

    fun getBeneficiaryDetail(id: String) {
        billsPaymentGateway.getFrequentBillerDetail(id)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _frequentBillerDetailState.value = ShowFrequentBillerDetailLoading }
            .doOnSuccess {
                _frequentBillerDetailState.value = ShowFrequentBillerDetailDismissLoading
            }
            .subscribe(
                {
                    _frequentBillerDetailState.value =
                        ShowBeneficiaryDetailGetFrequentBillerDetail(it)
                }, {
                    Timber.e(it, "getBeneficiaryDetail Failed")
                    _frequentBillerDetailState.value = ShowFrequentBillerDetailError(it)
                })
            .addTo(disposables)
    }

    fun deleteFrequentBiller(id: String) {
        billsPaymentGateway.deleteFrequentBiller(id)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe {
                _frequentBillerDetailState.value = ShowFrequentBillerDetailProgressBarLoading
            }
            .doFinally {
                _frequentBillerDetailState.value = ShowFrequentBillerDetailDismissProgressBarLoading
            }
            .subscribe(
                {
                    _frequentBillerDetailState.value =
                        ShowFrequentBillerDetailDeleteFrequentBiller(it.message ?: "")
                }, {
                    Timber.e(it, "deleteFrequentBiller Failed")
                    _frequentBillerDetailState.value = ShowFrequentBillerDetailError(it)
                })
            .addTo(disposables)
    }
}

sealed class FrequentBillerDetailState

object ShowFrequentBillerDetailProgressBarLoading : FrequentBillerDetailState()

object ShowFrequentBillerDetailDismissProgressBarLoading : FrequentBillerDetailState()

object ShowFrequentBillerDetailLoading : FrequentBillerDetailState()

object ShowFrequentBillerDetailDismissLoading : FrequentBillerDetailState()

data class ShowBeneficiaryDetailGetFrequentBillerDetail(
    val data: FrequentBiller
) : FrequentBillerDetailState()

data class ShowFrequentBillerDetailDeleteFrequentBiller(val message: String) :
    FrequentBillerDetailState()

data class ShowFrequentBillerDetailError(val throwable: Throwable) : FrequentBillerDetailState()
