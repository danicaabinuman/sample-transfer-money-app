package com.unionbankph.corporate.branch.presentation.confirmation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.branch.data.gateway.BranchVisitGateway
import com.unionbankph.corporate.branch.data.model.BranchVisitSubmitDto
import com.unionbankph.corporate.branch.presentation.constant.BranchVisitTypeEnum
import com.unionbankph.corporate.branch.presentation.model.BranchTransactionForm
import com.unionbankph.corporate.branch.presentation.model.BranchVisitConfirmationForm
import com.unionbankph.corporate.branch.presentation.model.Credits
import com.unionbankph.corporate.branch.presentation.model.Debits
import com.unionbankph.corporate.branch.presentation.model.Detail
import com.unionbankph.corporate.branch.presentation.model.Transaction
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject

class BranchVisitConfirmationViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val branchVisitGateway: BranchVisitGateway
) : BaseViewModel() {

    private val _branchVisitConfirmationState = MutableLiveData<BranchVisitConfirmationState>()

    private val transactionsMutableLiveData = MutableLiveData<MutableList<Transaction>>()

    private val branchVisitConfirmationFormMutableLiveData =
        MutableLiveData<BranchVisitConfirmationForm>()

    private val branchTransactionsFormMutableLiveData =
        MutableLiveData<MutableList<BranchTransactionForm>>()

    val transactions: LiveData<MutableList<Transaction>> get() = transactionsMutableLiveData

    val confirmationState: LiveData<BranchVisitConfirmationState> get() = _branchVisitConfirmationState

    val branchTransactionsForm: LiveData<MutableList<BranchTransactionForm>> get() = branchTransactionsFormMutableLiveData

    val branchVisitConfirmationForm: LiveData<BranchVisitConfirmationForm> get() = branchVisitConfirmationFormMutableLiveData

    init {
        branchTransactionsFormMutableLiveData.value = mutableListOf()
        transactionsMutableLiveData.value = mutableListOf()
    }

    fun setBranchTransactionsForm(branchTransactionsForm: MutableList<BranchTransactionForm>) {
        branchTransactionsFormMutableLiveData.value = branchTransactionsForm
    }

    fun setBranchVisitConfirmationForm(branchVisitConfirmationForm: BranchVisitConfirmationForm) {
        branchVisitConfirmationFormMutableLiveData.value = branchVisitConfirmationForm
    }

    fun submitBranchTransaction() {
        mapBranchVisitTransactionForm()
        branchVisitConfirmationForm.value?.let {
            branchVisitGateway.submitBranchVisitTransaction(
                it.branchSolId.notNullable(),
                it.branchName.notNullable(),
                it.branchAddress.notNullable(),
                it.transactionDate.notNullable(),
                it.remarks.notNullable(),
                transactions.value.notNullable()
            )
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .doOnSubscribe {
                    _branchVisitConfirmationState.value = ShowBranchVisitConfirmationLoading
                }
                .doFinally {
                    _branchVisitConfirmationState.value = ShowBranchVisitConfirmationDismissLoading
                }
                .subscribe(
                    {
                        _branchVisitConfirmationState.value = ShowBranchVisitConfirmationSubmitted(it)
                    }, {
                        Timber.e(it, "submitBranchTransaction failed")
                        _branchVisitConfirmationState.value = ShowBranchVisitConfirmationError(it)
                    }
                ).addTo(disposables)
        }
    }

    private fun mapBranchVisitTransactionForm() {
        branchTransactionsFormMutableLiveData.value?.let {
            it.forEach {
                val transaction = Transaction()
                transaction.type = it.type
                when (it.type) {
                    BranchVisitTypeEnum.CASH_DEPOSIT.value -> {
                        transaction.detail = Detail().apply {
                            amount = it.amount
                            remarks = it.remarks
                            accountNumber = branchVisitConfirmationForm.value?.depositTo
                        }
                        transactionsMutableLiveData.value?.add(transaction)
                    }
                    BranchVisitTypeEnum.CHECK_DEPOSIT_ON_US.value -> {
                        val existTransaction = transactionsMutableLiveData.value?.find {
                            it.type == BranchVisitTypeEnum.CHECK_DEPOSIT_ON_US.value
                        }
                        mapTransactionItem(existTransaction, it, transaction)
                    }
                    BranchVisitTypeEnum.CHECK_DEPOSIT_OFF_US.value -> {
                        val existTransaction = transactionsMutableLiveData.value?.find {
                            it.type == BranchVisitTypeEnum.CHECK_DEPOSIT_OFF_US.value
                        }
                        mapTransactionItem(existTransaction, it, transaction)
                    }
                }
            }
        }
    }

    private fun mapTransactionItem(
        existTransaction: Transaction?,
        branchTransactionForm: BranchTransactionForm,
        transaction: Transaction
    ) {
        if (existTransaction != null) {
            val debit = mapDebit(branchTransactionForm)
            var totalAmount = 0.00
            existTransaction.detail?.debits?.add(debit)
            existTransaction.detail?.debits?.forEach {
                totalAmount += it.amount?.toDouble() ?: 0.00
            }
            existTransaction.detail?.credits?.clear()
            existTransaction.detail?.credits?.add(
                Credits().apply {
                    amount = totalAmount.toString()
                    accountNumber = branchVisitConfirmationForm.value?.depositTo
                }
            )
        } else {
            transaction.detail = Detail().apply {
                debits = mutableListOf(mapDebit(branchTransactionForm))
                credits = mutableListOf(mapCredit(branchTransactionForm))
            }
            transactionsMutableLiveData.value?.add(transaction)
        }
    }

    private fun mapDebit(branchTransactionForm: BranchTransactionForm): Debits {
        return Debits().apply {
            amount = branchTransactionForm.amount
            checkDate = branchTransactionForm.checkDate
            checkNumber = branchTransactionForm.checkNumber
            accountNumber = branchTransactionForm.accountNumber
            remarks = branchTransactionForm.remarks
        }
    }

    private fun mapCredit(branchTransactionForm: BranchTransactionForm): Credits {
        return Credits().apply {
            amount = branchTransactionForm.amount
            accountNumber = branchVisitConfirmationForm.value?.depositTo
        }
    }
}

sealed class BranchVisitConfirmationState

object ShowBranchVisitConfirmationLoading : BranchVisitConfirmationState()

object ShowBranchVisitConfirmationDismissLoading : BranchVisitConfirmationState()

data class ShowBranchVisitConfirmationSubmitted(val branchVisitSubmitDto: BranchVisitSubmitDto) : BranchVisitConfirmationState()

data class ShowBranchVisitConfirmationError(val throwable: Throwable) : BranchVisitConfirmationState()
