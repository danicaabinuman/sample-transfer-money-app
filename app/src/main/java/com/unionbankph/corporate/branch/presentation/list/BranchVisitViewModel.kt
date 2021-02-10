package com.unionbankph.corporate.branch.presentation.list

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.util.AutoFormatUtil
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.branch.data.gateway.BranchVisitGateway
import com.unionbankph.corporate.branch.data.model.BranchVisitDto
import com.unionbankph.corporate.branch.data.model.Transaction
import com.unionbankph.corporate.branch.presentation.constant.BranchVisitStatusEnum
import com.unionbankph.corporate.branch.presentation.constant.BranchVisitTypeEnum
import com.unionbankph.corporate.branch.presentation.model.BranchVisit
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.common.presentation.constant.DateFormatEnum
import com.unionbankph.corporate.common.presentation.helper.ConstantHelper
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject

class BranchVisitViewModel @Inject constructor(
    private val context: Context,
    private val autoFormatUtil: AutoFormatUtil,
    private val viewUtil: ViewUtil,
    private val schedulerProvider: SchedulerProvider,
    private val branchVisitGateway: BranchVisitGateway
) : BaseViewModel() {

    private val _branchVisitState = MutableLiveData<BranchVisitState>()

    private val branchVisitsMutableLiveData =
        MutableLiveData<MutableList<BranchVisit>>()

    private val testBranchVisitsMutableLiveData =
        MutableLiveData<MutableList<BranchVisit>>()

    val state: LiveData<BranchVisitState> get() = _branchVisitState

    val branchVisitsLiveData: LiveData<MutableList<BranchVisit>> =
        branchVisitsMutableLiveData

    val testBranchVisitsLiveData: LiveData<MutableList<BranchVisit>> =
        testBranchVisitsMutableLiveData

    private var getCheckDepositsDisposable: Disposable? = null

    fun getBranchVisits(pageable: Pageable, isInitialLoading: Boolean) {
        getCheckDepositsDisposable?.dispose()
        getCheckDepositsDisposable = branchVisitGateway.getBranchVisits(pageable)
            .map {
                pageable.apply {
                    totalPageCount = it.totalPages
                    isLastPage = !it.hasNextPage
                    increasePage()
                }
                return@map mapBranchVisit(it.results)
            }
            .map { pageable.combineList(branchVisitsMutableLiveData.value, it) }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe {
                _branchVisitState.value = if (isInitialLoading) {
                    ShowBranchVisitLoading
                } else {
                    ShowBranchVisitEndlessLoading
                }
            }
            .doFinally {
                _branchVisitState.value = if (isInitialLoading) {
                    ShowBranchVisitDismissLoading
                } else {
                    ShowBranchVisitDismissEndlessLoading
                }
            }
            .subscribe(
                {
                    branchVisitsMutableLiveData.value = it
                }, {
                    Timber.e(it, "getBranchVisits failed")
                    if (!isInitialLoading) {
                        pageable.errorPagination(it.message)
                    } else {
                        _branchVisitState.value = ShowBranchVisitError(it)
                    }
                }
            )
        getCheckDepositsDisposable?.addTo(disposables)
    }

    private fun mapBranchVisit(
        data: MutableList<BranchVisitDto>
    ): MutableList<BranchVisit> {
        val branchVisits =
            mutableListOf<BranchVisit>()
        data.forEach { branchVisitIterate ->
            val transaction = branchVisitIterate.transactions.notNullable()[0]
            val isBatchTransaction = branchVisitIterate.transactions.notNullable().size > 1
            val batchSize = branchVisitIterate.transactions.notNullable().size
            val branchVisit =
                getBranchVisit(branchVisitIterate, transaction, isBatchTransaction, batchSize)
            branchVisits.add(branchVisit)
        }
        return branchVisits
    }

    private fun getBranchVisit(
        branchVisitDtoIterate: BranchVisitDto,
        transaction: Transaction,
        isBatchTransaction: Boolean,
        batchSize: Int
    ): BranchVisit {
        return BranchVisit().apply {
            id = branchVisitDtoIterate.id
            remarks = getRemarks(branchVisitDtoIterate, transaction)
            createdBy = ConstantHelper.Text.getRemarksCreated(
                context,
                branchVisitDtoIterate.client?.name,
                branchVisitDtoIterate.createdDate,
                viewUtil
            )
            isBatch = isBatchTransaction
            depositTo = if (isBatchTransaction) batchSize.toString() else {
                viewUtil.getAccountNumberFormat(transaction.detail?.accountNumber)
            }
            numberOfTransactions = batchSize
            transactionDate = viewUtil.getDateFormatByDateString(
                branchVisitDtoIterate.client?.transactionDate,
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
            amount = autoFormatUtil.formatWithTwoDecimalPlaces(
                getAmount(branchVisitDtoIterate.transactions.notNullable()),
                branchVisitDtoIterate.currency
            )
            status = if (branchVisitDtoIterate.status != null) {
                branchVisitDtoIterate.status?.let {
                    enumValueOf<BranchVisitStatusEnum>(
                        it.toUpperCase().replace(" ", "_")
                    )
                }
            } else {
                BranchVisitStatusEnum.UNKNOWN
            }
        }
    }

    private fun getRemarks(
        branchVisitDtoIterate: BranchVisitDto,
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

    fun getBranchVisitsTestData() {
        branchVisitGateway.getBranchVisitsTestData()
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                {
                    testBranchVisitsMutableLiveData.value = mapBranchVisit(it.results)
                }, {
                    Timber.e(it, "getBranchVisitsTestData failed")
                    _branchVisitState.value = ShowBranchVisitError(it)
                })
            .addTo(disposables)
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
}

sealed class BranchVisitState

object ShowBranchVisitLoading : BranchVisitState()

object ShowBranchVisitEndlessLoading : BranchVisitState()

object ShowBranchVisitDismissLoading : BranchVisitState()

object ShowBranchVisitDismissEndlessLoading : BranchVisitState()

data class ShowBranchVisitError(val throwable: Throwable) : BranchVisitState()
