package com.unionbankph.corporate.branch.presentation.summary

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.branch.data.gateway.BranchVisitGateway
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import javax.inject.Inject

class BranchVisitSummaryViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val branchVisitGateway: BranchVisitGateway
) : BaseViewModel() {

    private val branchVisitSummaryState = MutableLiveData<BranchVisitSummaryState>()

    val summaryState: LiveData<BranchVisitSummaryState> get() = branchVisitSummaryState
}

sealed class BranchVisitSummaryState

object ShowBranchVisitSummaryLoading : BranchVisitSummaryState()

object ShowBranchVisitSummaryDismissLoading : BranchVisitSummaryState()

data class ShowBranchVisitSummaryError(val throwable: Throwable) : BranchVisitSummaryState()
