package com.unionbankph.corporate.branch.presentation.detail

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.branch.data.gateway.BranchVisitGateway
import com.unionbankph.corporate.branch.data.model.Transaction
import com.unionbankph.corporate.branch.presentation.constant.BranchVisitStatusEnum
import com.unionbankph.corporate.branch.presentation.constant.BranchVisitTypeEnum
import com.unionbankph.corporate.branch.presentation.model.BranchTransactionForm
import com.unionbankph.corporate.branch.presentation.model.BranchVisit
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.common.presentation.constant.DateFormatEnum
import io.reactivex.Observable
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject

class BranchVisitDetailViewModel @Inject constructor(
    private val viewUtil: ViewUtil,
    private val context: Context,
    private val schedulerProvider: SchedulerProvider,
    private val branchVisitGateway: BranchVisitGateway
) : BaseViewModel() {

    private val branchVisitDetailState = MutableLiveData<BranchVisitDetailState>()

    private val branchVisitsMutableLiveData = MutableLiveData<BranchVisit>()

    private val branchTransactionsFormMutableLiveData =
        MutableLiveData<MutableList<BranchTransactionForm>>()

    val detailState: LiveData<BranchVisitDetailState> get() = branchVisitDetailState

    val branchVisitsLiveData: LiveData<BranchVisit> get() = branchVisitsMutableLiveData

    val branchTransactionsFormLiveData: LiveData<MutableList<BranchTransactionForm>>
        get() = branchTransactionsFormMutableLiveData

    fun getBranchVisit(id: String) {
        branchVisitGateway.getBranchVisit(id)
            .map { Pair(mapBranchVisit(it), mapBranchVisitToBranchTransactionForm(it)) }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe {
                branchVisitDetailState.value = ShowBranchVisitDetailLoading
            }
            .doFinally {
                branchVisitDetailState.value = ShowBranchVisitDetailDismissLoading
            }
            .subscribe(
                {
                    branchVisitsMutableLiveData.value = it.first
                    branchTransactionsFormMutableLiveData.value = it.second
                }, {
                    Timber.e(it, "getBranchVisit failed")
                    branchVisitDetailState.value = ShowBranchVisitDetailError(it)
                }
            )
            .addTo(disposables)
    }

    private fun mapBranchVisitToBranchTransactionForm(
        branchVisitDto: com.unionbankph.corporate.branch.data.model.BranchVisitDto
    ): MutableList<BranchTransactionForm> {
        val branchTransactionsForm = mutableListOf<BranchTransactionForm>()
        branchVisitDto.transactions?.let {
            val defaultCurrency = "PHP"
            Observable.fromIterable(it)
                .forEach { transaction ->
                    val branchTransactionForm = BranchTransactionForm()
                    branchTransactionForm.type = transaction.type
                    when (transaction.type) {
                        BranchVisitTypeEnum.CASH_DEPOSIT.value,
                        BranchVisitTypeEnum.CASH_WITHDRAW.value -> {
                            branchTransactionForm.amount = transaction.detail?.amount
                            branchTransactionForm.currency = defaultCurrency
                            branchTransactionForm.remarks = transaction.detail?.remarks
                            branchTransactionsForm.add(branchTransactionForm)
                        }
                        BranchVisitTypeEnum.CHECK_DEPOSIT_OFF_US.value,
                        BranchVisitTypeEnum.CHECK_DEPOSIT_ON_US.value -> {
                            Observable.fromIterable(transaction.detail?.debits)
                                .forEach {
                                    val debitBranchTransactionForm = BranchTransactionForm()
                                    debitBranchTransactionForm.type = transaction.type
                                    debitBranchTransactionForm.amount = it.amount
                                    debitBranchTransactionForm.currency = it.currency
                                    debitBranchTransactionForm.checkDate = it.checkDate
                                    debitBranchTransactionForm.accountNumber = it.accountNumber
                                    debitBranchTransactionForm.checkNumber = it.checkNumber
                                    debitBranchTransactionForm.remarks = it.remarks
                                    branchTransactionsForm.add(debitBranchTransactionForm)
                                }
                        }
                    }
                }
        }
        return branchTransactionsForm
    }

    private fun mapBranchVisit(
        branchVisitDto: com.unionbankph.corporate.branch.data.model.BranchVisitDto
    ): BranchVisit {
        val transaction = branchVisitDto.transactions.notNullable()[0]
        val isBatchTransaction = branchVisitDto.transactions.notNullable().size > 1
        val batchSize = branchVisitDto.transactions.notNullable().size
        return BranchVisit()
            .apply {
                id = branchVisitDto.id
                remarks = getRemarks(branchVisitDto, transaction)
                createdBy = branchVisitDto.client?.name
                createdDate = branchVisitDto.createdDate
                isBatch = isBatchTransaction
                depositTo = viewUtil.getAccountNumberFormat(transaction.detail?.accountNumber)
                numberOfTransactions = batchSize
                cashDepositSize =
                    getTotalSize(branchVisitDto.transactions, BranchVisitTypeEnum.CASH_DEPOSIT.value)
                checkDepositOnSize = getTotalSize(
                    branchVisitDto.transactions,
                    BranchVisitTypeEnum.CHECK_DEPOSIT_ON_US.value
                )
                checkDepositOffSize = getTotalSize(
                    branchVisitDto.transactions,
                    BranchVisitTypeEnum.CHECK_DEPOSIT_OFF_US.value
                )
                transactionDate = viewUtil.getDateFormatByDateString(
                    branchVisitDto.client?.transactionDate,
                    DateFormatEnum.DATE_FORMAT_ISO_DATE.value,
                    DateFormatEnum.DATE_FORMAT_DATE.value
                )
                channel = transaction.type?.let {
                    if (it.contains("withdraw")) {
                        context.formatString(R.string.title_cash_withdrawal)
                    } else {
                        context.formatString(R.string.title_deposit)
                    }
                }
                branchName = branchVisitDto.client?.branchName
                branchAddress = branchVisitDto.client?.branchAddress
                amount = getAmount(branchVisitDto.transactions.notNullable())
                totalAmountCashDeposit = getCashDepositTotalAmount(branchVisitDto.transactions)
                totalAmountCheckDepositOff =
                    getCheckDepositTotalAmount(branchVisitDto.transactions, false)
                totalAmountCheckDepositOn =
                    getCheckDepositTotalAmount(branchVisitDto.transactions, true)
                status = if (branchVisitDto.status != null) {
                    branchVisitDto.status?.let {
                        enumValueOf<BranchVisitStatusEnum>(
                            it.toUpperCase().replace(" ", "_")
                        )
                    }
                } else {
                    BranchVisitStatusEnum.UNKNOWN
                }
                referenceNumber = branchVisitDto.referenceId
            }
    }

    private fun getRemarks(
        branchVisitDtoIterate: com.unionbankph.corporate.branch.data.model.BranchVisitDto,
        transaction: Transaction
    ): String {
        return (branchVisitDtoIterate.client?.remarks
            ?: if (transaction.type == BranchVisitTypeEnum.CASH_WITHDRAW.value) {
                context.formatString(
                    R.string.params_withdraw_by,
                    branchVisitDtoIterate.client?.name
                )
            } else {
                context.formatString(
                    R.string.params_deposit_by,
                    branchVisitDtoIterate.client?.name
                )
            })
    }

    private fun getAmount(transactions: MutableList<Transaction>): String {
        var totalAmount = 0.00
        val transaction = transactions[0]
        if (transactions.size > 1) {
            transactions.forEach { transact ->
                if (transact.type == BranchVisitTypeEnum.CASH_WITHDRAW.value ||
                    transact.type == BranchVisitTypeEnum.CASH_DEPOSIT.value ||
                    transact.type == BranchVisitTypeEnum.CHECK_ENCASHMENT.value) {
                    transact.detail?.amount?.let { totalAmount += it.toDouble() }
                } else if (transaction.type == BranchVisitTypeEnum.CHECK_DEPOSIT_ON_US.value ||
                    transaction.type == BranchVisitTypeEnum.CHECK_DEPOSIT_OFF_US.value) {
                    transact.detail?.debits?.let {
                        it.forEach { debit ->
                            debit.amount?.let { amount -> totalAmount = amount.toDouble() }
                        }
                    }
                }
            }
        } else {
            if (transaction.type == BranchVisitTypeEnum.CASH_WITHDRAW.value ||
                transaction.type == BranchVisitTypeEnum.CASH_DEPOSIT.value ||
                transaction.type == BranchVisitTypeEnum.CHECK_ENCASHMENT.value) {
                transaction.detail?.amount?.let { totalAmount += it.toDouble() }
            } else if (transaction.type == BranchVisitTypeEnum.CHECK_DEPOSIT_ON_US.value ||
                transaction.type == BranchVisitTypeEnum.CHECK_DEPOSIT_OFF_US.value) {
                transaction.detail?.debits?.let {
                    it.forEach { debit ->
                        debit.amount?.let { amount -> totalAmount += amount.toDouble() }
                    }
                }
            }
        }
        return totalAmount.toString()
    }

    private fun getCashDepositTotalAmount(transactions: MutableList<Transaction>?): Double? {
        val defaultValue = 0.00
        transactions.let {
            return Observable.fromIterable(it)
                .filter { it.type == BranchVisitTypeEnum.CASH_DEPOSIT.value }
                .map { it.detail?.amount?.toDouble() }
                .reduce { value1: Double, value2: Double -> value1 + value2 }
                .blockingGet(defaultValue)
        }
    }

    private fun getTotalSize(
        transactions: MutableList<Transaction>?,
        filterType: String? = null
    ): Int {
        transactions.let {
            return Observable.fromIterable(it)
                .filter { filterBranchVisitType(it.type, filterType) }
                .map { it.detail?.debits }
                .count()
                .blockingGet().toInt()
        }
    }

    private fun getCheckDepositTotalAmount(
        transactions: MutableList<Transaction>?,
        isOnUs: Boolean
    ): Double? {
        val defaultValue = 0.00
        transactions.let {
            return Observable.fromIterable(it)
                .filter {
                    it.type == (if (isOnUs)
                        BranchVisitTypeEnum.CHECK_DEPOSIT_ON_US.value
                    else
                        BranchVisitTypeEnum.CHECK_DEPOSIT_OFF_US.value)
                }
                .map { it.detail?.credits?.get(0)?.amount?.toDouble() }
                .reduce { value1: Double, value2: Double -> value1 + value2 }
                .blockingGet(defaultValue)
        }
    }

    private fun filterBranchVisitType(givenType: String?, filterType: String?): Boolean {
        return if (filterType == null) {
            true
        } else filterType == givenType
    }
}

sealed class BranchVisitDetailState

object ShowBranchVisitDetailLoading : BranchVisitDetailState()

object ShowBranchVisitDetailDismissLoading : BranchVisitDetailState()

data class ShowBranchVisitDetailError(val throwable: Throwable) : BranchVisitDetailState()
