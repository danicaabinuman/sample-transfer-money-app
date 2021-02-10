package com.unionbankph.corporate.branch.presentation.channel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.branch.data.gateway.BranchVisitGateway
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import javax.inject.Inject

class BranchVisitChannelViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val branchVisitGateway: BranchVisitGateway
) : BaseViewModel() {

    private val _branchVisitChannelState = MutableLiveData<BranchVisitChannelState>()

    val channelState: LiveData<BranchVisitChannelState> get() = _branchVisitChannelState
}

sealed class BranchVisitChannelState

object ShowBranchVisitChannelLoading : BranchVisitChannelState()

object ShowBranchVisitChannelEndlessLoading : BranchVisitChannelState()

object ShowBranchVisitChannelDismissLoading : BranchVisitChannelState()

object ShowBranchVisitChannelDismissEndlessLoading : BranchVisitChannelState()

data class ShowBranchVisitChannelError(val throwable: Throwable) : BranchVisitChannelState()
