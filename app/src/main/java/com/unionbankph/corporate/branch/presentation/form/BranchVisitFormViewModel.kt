package com.unionbankph.corporate.branch.presentation.form

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.branch.data.gateway.BranchVisitGateway
import com.unionbankph.corporate.branch.data.model.Branch
import com.unionbankph.corporate.branch.presentation.constant.BranchVisitTypeEnum
import com.unionbankph.corporate.branch.presentation.model.BranchTransactionForm
import com.unionbankph.corporate.branch.presentation.model.BranchVisitConfirmationForm
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import io.reactivex.Observable
import javax.inject.Inject

class BranchVisitFormViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val branchVisitGateway: BranchVisitGateway
) : BaseViewModel() {

    private val _branchVisitFormState = MutableLiveData<BranchVisitFormState>()

    private val branchMutableLiveData = MutableLiveData<Branch>()

    private val branchTransactionsFormMutableLiveData =
        MutableLiveData<MutableList<BranchTransactionForm>>()

    val branchVisitFormState: LiveData<BranchVisitFormState> get() = _branchVisitFormState

    val branchTransactionsForm: LiveData<MutableList<BranchTransactionForm>> get() = branchTransactionsFormMutableLiveData

    val branch: LiveData<Branch> get() = branchMutableLiveData

    init {
        branchTransactionsFormMutableLiveData.value = mutableListOf()
    }

    fun addBranchTransactionForm(branchTransactionForm: BranchTransactionForm) {
        branchTransactionsFormMutableLiveData.value?.add(branchTransactionForm)
    }

    fun setBranchTransactions(branchTransactionsForm: MutableList<BranchTransactionForm>) {
        branchTransactionsFormMutableLiveData.value = branchTransactionsForm
    }

    fun clearBranchTransactionForm() {
        branchTransactionsFormMutableLiveData.value = mutableListOf()
    }

    fun setBranch(branch: Branch) {
        branchMutableLiveData.value = branch
    }

    fun proceedToConfirmation(
        transactionDate: String,
        depositTo: String,
        channel: String,
        remarks: String
    ) {
        val branchVisitConfirmationForm = BranchVisitConfirmationForm()
        branchTransactionsFormMutableLiveData.value?.let {
            branchVisitConfirmationForm.transactionDate = transactionDate
            branchVisitConfirmationForm.isBatch = it.size > 1
            branchVisitConfirmationForm.branchSolId = branch.value?.solId
            branchVisitConfirmationForm.branchName = branch.value?.name
            branchVisitConfirmationForm.branchAddress = branch.value?.address
            branchVisitConfirmationForm.depositTo = depositTo
            branchVisitConfirmationForm.channel = channel
            branchVisitConfirmationForm.remarks = remarks
            branchVisitConfirmationForm.totalAmount = getTotalAmount()
            branchVisitConfirmationForm.totalAmountCashDeposit =
                getTotalAmount(BranchVisitTypeEnum.CASH_DEPOSIT.value)
            branchVisitConfirmationForm.totalAmountCheckDepositOn =
                getTotalAmount(BranchVisitTypeEnum.CHECK_DEPOSIT_ON_US.value)
            branchVisitConfirmationForm.totalAmountCheckDepositOff =
                getTotalAmount(BranchVisitTypeEnum.CHECK_DEPOSIT_OFF_US.value)
            branchVisitConfirmationForm.numberOfTransactions = it.size
            branchVisitConfirmationForm.cashDepositSize =
                getTotalSize(BranchVisitTypeEnum.CASH_DEPOSIT.value)
            branchVisitConfirmationForm.checkDepositOffSize =
                getTotalSize(BranchVisitTypeEnum.CHECK_DEPOSIT_OFF_US.value)
            branchVisitConfirmationForm.checkDepositOnSize =
                getTotalSize(BranchVisitTypeEnum.CHECK_DEPOSIT_ON_US.value)
            _branchVisitFormState.value =
                ShowBranchVisitFormProceedToConfirmation(branchVisitConfirmationForm)
        }
    }

    private fun getTotalAmount(filterType: String? = null): Double? {
        val defaultValue = 0.00
        branchTransactionsFormMutableLiveData.value?.let {
            return Observable.fromIterable(it)
                .filter { filterBranchVisitType(it.type, filterType) }
                .map { it.amount?.toDouble() }
                .reduce { value1: Double, value2: Double -> value1 + value2 }
                .blockingGet(defaultValue)
        }
        return defaultValue
    }

    private fun getTotalSize(filterType: String? = null): Int {
        branchTransactionsFormMutableLiveData.value?.let {
            return Observable.fromIterable(it)
                .filter { filterBranchVisitType(it.type, filterType) }
                .count()
                .blockingGet().toInt()
        }
        return 0
    }

    private fun filterBranchVisitType(givenType: String?, filterType: String?): Boolean {
        return if (filterType == null) {
            true
        } else filterType == givenType
    }
}

sealed class BranchVisitFormState

object ShowBranchVisitFormLoading : BranchVisitFormState()

object ShowBranchVisitFormEndlessLoading : BranchVisitFormState()

object ShowBranchVisitFormDismissLoading : BranchVisitFormState()

object ShowBranchVisitFormDismissEndlessLoading : BranchVisitFormState()

data class ShowBranchVisitFormProceedToConfirmation(
    val branchVisitConfirmationForm: BranchVisitConfirmationForm
) : BranchVisitFormState()

data class ShowBranchVisitFormError(val throwable: Throwable) : BranchVisitFormState()
