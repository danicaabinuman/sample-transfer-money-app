package com.unionbankph.corporate.branch.presentation.transactionlist

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.platform.bus.event.ActionSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.EventBus
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.branch.presentation.model.BranchTransactionForm
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import io.reactivex.Observable
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class BranchTransactionViewModel @Inject constructor(
    private val eventBus: EventBus,
    private val schedulerProvider: SchedulerProvider,
    private val context: Context
) : BaseViewModel() {

    private val _branchTransactionState = MutableLiveData<BranchTransactionState>()

    private val selectionMutableLiveData = MutableLiveData<Boolean>()

    private val branchTransactionsFormMutableLiveData =
        MutableLiveData<MutableList<BranchTransactionForm>>()

    val branchTransactionState: LiveData<BranchTransactionState> get() = _branchTransactionState

    val branchTransactionsForm: LiveData<MutableList<BranchTransactionForm>> get() = branchTransactionsFormMutableLiveData

    val isSelection: LiveData<Boolean> get() = selectionMutableLiveData

    init {
        selectionMutableLiveData.value = false
        branchTransactionsFormMutableLiveData.value = mutableListOf()
    }

    fun setSelectedItem(branchTransactionForm: BranchTransactionForm, position: Int) {
        branchTransactionsFormMutableLiveData.value?.get(position)?.isSelected =
            !branchTransactionForm.isSelected
    }

    fun setBranchTransactions(branchTransactionsString: String) {
        Observable.just(branchTransactionsString)
            .map {
                JsonHelper.fromListJson<BranchTransactionForm>(it)
            }
            .delay(
                context.resources.getInteger(R.integer.time_loading_parse_delay).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe {
                _branchTransactionState.value = ShowBranchTransactionLoading
            }.doFinally {
                _branchTransactionState.value = ShowBranchTransactionDismissLoading
            }.subscribe(
                {
                    branchTransactionsFormMutableLiveData.value = it
                }, {
                    Timber.e(it, "setBranchTransactions failed")
                    _branchTransactionState.value = ShowBranchTransactionError(it)
                }
            ).addTo(disposables)
    }

    fun activateSelection(isSelection: Boolean) {
        selectionMutableLiveData.value = isSelection
    }

    fun getSelection(): Boolean {
        return isSelection.value ?: false
    }

    fun editBranchTransactionByPosition(
        position: Int,
        editedBranchTransactionForm: BranchTransactionForm
    ) {
        branchTransactionsFormMutableLiveData.value?.let {
            it.forEachIndexed { index, _ ->
                if (index == position) {
                    it[index] = editedBranchTransactionForm
                }
            }
        }
        _branchTransactionState.value = ShowBranchTransactionEditedItem
    }

    fun selectAll() {
        branchTransactionsFormMutableLiveData.value?.let {
            Observable.fromIterable(it)
                .filter { !it.isSelected }
                .forEach {
                    it.isSelected = true
                }
        }
    }

    fun deSelectAll() {
        branchTransactionsFormMutableLiveData.value?.let {
            Observable.fromIterable(it)
                .filter { it.isSelected }
                .forEach {
                    it.isSelected = false
                }
        }
    }

    fun isSelectedAll(): Boolean {
        branchTransactionsFormMutableLiveData.value?.let {
            return getSelectedCount() == it.size
        }
        return false
    }

    fun getSelectedCount(): Int {
        return branchTransactionsFormMutableLiveData.value?.let {
            it.filter { it.isSelected }.size
        } ?: 0
    }

    fun deleteItem(position: Int) {
        branchTransactionsFormMutableLiveData.value?.removeAt(position)
        publishEventBus()
    }

    fun clearSelection() {
        branchTransactionsFormMutableLiveData.value?.let {
            it.filter {
                it.isSelected
            }.forEach {
                it.isSelected = false
            }
        }
    }

    fun deleteSelectedItems() {
        branchTransactionsFormMutableLiveData.value?.let {
            it.removeAll { it.isSelected }
        }
        publishEventBus()
    }

    private fun publishEventBus() {
        eventBus.actionSyncEvent.emmit(
            BaseEvent(
                ActionSyncEvent.ACTION_UPDATE_BRANCH_TRANSACTION_LIST,
                JsonHelper.toJson(branchTransactionsFormMutableLiveData.value)
            )
        )
    }

}

sealed class BranchTransactionState

object ShowBranchTransactionLoading : BranchTransactionState()

object ShowBranchTransactionEndlessLoading : BranchTransactionState()

object ShowBranchTransactionDismissLoading : BranchTransactionState()

object ShowBranchTransactionDismissEndlessLoading : BranchTransactionState()

object ShowBranchTransactionEditedItem : BranchTransactionState()

data class ShowBranchTransactionError(val throwable: Throwable) : BranchTransactionState()

