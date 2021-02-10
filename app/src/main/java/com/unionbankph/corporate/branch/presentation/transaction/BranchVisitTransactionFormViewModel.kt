package com.unionbankph.corporate.branch.presentation.transaction

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.platform.bus.event.EventBus
import com.unionbankph.corporate.app.common.platform.bus.event.InputSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.branch.data.gateway.BranchVisitGateway
import com.unionbankph.corporate.branch.presentation.model.BranchTransaction
import com.unionbankph.corporate.branch.presentation.model.BranchTransactionForm
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import javax.inject.Inject

class BranchVisitTransactionFormViewModel @Inject constructor(
    private val eventBus: EventBus,
    private val schedulerProvider: SchedulerProvider,
    private val branchVisitGateway: BranchVisitGateway
) : BaseViewModel() {

    private val _branchVisitTransactionState = MutableLiveData<BranchVisitTransactionFormState>()

    private val branchTransactionFormMutableLiveData = MutableLiveData<BranchTransactionForm>()

    val transactionFormState: LiveData<BranchVisitTransactionFormState> get() = _branchVisitTransactionState

    val branchTransactionFormState: LiveData<BranchTransactionForm> get() = branchTransactionFormMutableLiveData

    fun addBranchTransaction(branchTransactionForm: BranchTransactionForm) {
        branchTransactionFormMutableLiveData.value = branchTransactionForm
        eventBus.inputSyncEvent.emmit(
            BaseEvent(
                InputSyncEvent.ACTION_ADD_BRANCH_TRANSACTION,
                JsonHelper.toJson(branchTransactionForm)
            )
        )
    }

    fun editBranchTransaction(branchTransactionForm: BranchTransactionForm, position: Int) {
        val branchTransaction = BranchTransaction()
        branchTransaction.branchTransactionForm = branchTransactionForm
        branchTransaction.position = position
        eventBus.inputSyncEvent.emmit(
            BaseEvent(
                InputSyncEvent.ACTION_EDIT_BRANCH_TRANSACTION,
                JsonHelper.toJson(branchTransaction)
            )
        )
        _branchVisitTransactionState.value = ShowBranchVisitTransactionEdit
    }
}

sealed class BranchVisitTransactionFormState

object ShowBranchVisitTransactionFormLoading : BranchVisitTransactionFormState()

object ShowBranchVisitTransactionFormEndlessLoading : BranchVisitTransactionFormState()

object ShowBranchVisitTransactionFormDismissLoading : BranchVisitTransactionFormState()

object ShowBranchVisitTransactionFormDismissEndlessLoading : BranchVisitTransactionFormState()

object ShowBranchVisitTransactionEdit : BranchVisitTransactionFormState()

data class ShowBranchVisitTransactionFormError(val throwable: Throwable) :
    BranchVisitTransactionFormState()
