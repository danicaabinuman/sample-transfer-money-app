package com.unionbankph.corporate.branch.presentation.transactiondetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.branch.data.gateway.BranchVisitGateway
import com.unionbankph.corporate.branch.presentation.model.BranchTransactionForm
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import io.reactivex.Observable
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject


class BranchTransactionDetailViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val branchVisitGateway: BranchVisitGateway
) : BaseViewModel() {

    private val branchTransactionDetailMutableLiveData =
        MutableLiveData<BranchTransactionDetailState>()

    private val branchTransactionFormMutableLiveData =
        MutableLiveData<BranchTransactionForm>()

    val branchTransactionDetailLiveData: LiveData<BranchTransactionDetailState>
        get() = branchTransactionDetailMutableLiveData

    val branchTransactionFormLiveData: LiveData<BranchTransactionForm>
        get() = branchTransactionFormMutableLiveData

    fun setBranchTransactionForm(branchTransactionFormString: String) {
        Observable.just(branchTransactionFormString)
            .map { JsonHelper.fromJson<BranchTransactionForm>(it) }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                {
                    branchTransactionFormMutableLiveData.value = it
                }, {
                    Timber.e(it, "setBranchTransactionForm failed")
                    branchTransactionDetailMutableLiveData.value =
                        ShowBranchTransactionDetailError(it)
                }
            ).addTo(disposables)
    }

}

sealed class BranchTransactionDetailState

object ShowBranchTransactionDetailLoading : BranchTransactionDetailState()

object ShowBranchTransactionDetailDismissLoading : BranchTransactionDetailState()

data class ShowBranchTransactionDetailError(val throwable: Throwable) : BranchTransactionDetailState()

