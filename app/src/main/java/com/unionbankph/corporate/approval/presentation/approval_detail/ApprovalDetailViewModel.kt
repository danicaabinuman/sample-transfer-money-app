package com.unionbankph.corporate.approval.presentation.approval_detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.account.data.gateway.AccountGateway
import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.notEmpty
import com.unionbankph.corporate.app.common.platform.bus.data.DataBus
import com.unionbankph.corporate.approval.data.model.ApprovalForm
import com.unionbankph.corporate.approval.data.model.ApprovalHierarchyDto
import com.unionbankph.corporate.approval.data.model.ApprovalProcessOrganizationUser
import com.unionbankph.corporate.approval.data.model.OrganizationUser
import com.unionbankph.corporate.approval.data.model.Transaction
import com.unionbankph.corporate.approval.domain.gateway.ApprovalGateway
import com.unionbankph.corporate.auth.data.model.UserDetails
import com.unionbankph.corporate.bills_payment.data.form.CancelBillsPaymentTransactionForm
import com.unionbankph.corporate.bills_payment.data.gateway.BillsPaymentGateway
import com.unionbankph.corporate.branch.data.model.BranchVisitDto
import com.unionbankph.corporate.common.data.model.ContextualClassStatus
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.common.presentation.constant.ChannelBankEnum
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.constant.TransactionStatusEnum
import com.unionbankph.corporate.corporate.domain.gateway.CorporateGateway
import com.unionbankph.corporate.fund_transfer.data.form.CancelFundTransferTransactionForm
import com.unionbankph.corporate.fund_transfer.data.gateway.FundTransferGateway
import com.unionbankph.corporate.fund_transfer.data.model.ScheduledTransferDeletionForm
import com.unionbankph.corporate.settings.data.gateway.SettingsGateway
import com.unionbankph.corporate.settings.domain.constant.FeaturesEnum
import io.reactivex.Observable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.zipWith
import io.reactivex.subjects.BehaviorSubject
import kotlinx.serialization.enumFromName
import timber.log.Timber
import javax.inject.Inject


class ApprovalDetailViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val corporateGateway: CorporateGateway,
    private val approvalGateway: ApprovalGateway,
    private val billsPaymentGateway: BillsPaymentGateway,
    private val fundTransferGateway: FundTransferGateway,
    private val accountGateway: AccountGateway,
    private val settingsGateway: SettingsGateway,
    private val dataBus: DataBus
) : BaseViewModel() {

    val isValidReasonForRejection = BehaviorSubject.createDefault(false)

    private val _approvalState = MutableLiveData<ApprovalDetailState>()
    private val hierarchyMutableLiveData = MutableLiveData<ApprovalHierarchyDto>()
    val state: LiveData<ApprovalDetailState> = _approvalState
    val hierarchyLiveData: LiveData<ApprovalHierarchyDto> = hierarchyMutableLiveData

    val isShowHelpIcon = BehaviorSubject.createDefault(false)

    val isFromApproveOrReject = BehaviorSubject.createDefault(false)

    val contextualClassFeatureToggle = BehaviorSubject.create<Boolean>()

    init {
        isEnabledFeature()
    }

    fun getApprovalHierarchy(id: String, status: String? = "") {
        approvalGateway.getApprovalHierarchy(id, status)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _approvalState.value = ShowApprovalDetailLoading }
            .doFinally { _approvalState.value = ShowApprovalDetailDismissLoading }
            .subscribe(
                {
                    hierarchyMutableLiveData.value = it
                }, {
                    Timber.e(it, "getApprovalHierarchy failed")
                    _approvalState.value = ShowApprovalDetailError(it)
                })
            .addTo(disposables)
    }

    fun getFundTransferDetail(
        id: String,
        loading: ApprovalDetailState,
        dismissLoading: ApprovalDetailState
    ) {
        fundTransferGateway.getFundTransferDetail(id)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _approvalState.value = loading }
            .doFinally { _approvalState.value = dismissLoading }
            .subscribe(
                {
                    isShowHelpIcon.onNext(true)
                    _approvalState.value = ShowApprovalFundTransferDetail(it)
                }, {
                    Timber.e(it, "Get account failed")
                    _approvalState.value = ShowApprovalDetailError(it)
                })
            .addTo(disposables)
    }

    fun getBillsPaymentDetail(
        id: String,
        loading: ApprovalDetailState,
        dismissLoading: ApprovalDetailState
    ) {
        billsPaymentGateway.getBillsPaymentDetail(id)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _approvalState.value = loading }
            .doFinally { _approvalState.value = dismissLoading }
            .subscribe(
                {
                    isShowHelpIcon.onNext(true)
                    _approvalState.value = ShowApprovalBillsPaymentDetail(it)
                }, {
                    Timber.e(it, "Get account failed")
                    _approvalState.value = ShowApprovalDetailError(it)
                })
            .addTo(disposables)
    }

    fun getCheckWriterDetails(
        id: String,
        loading: ApprovalDetailState,
        dismissLoading: ApprovalDetailState
    ) {
        approvalGateway.getCheckWriterDetails(id)
            .map {
                if (it.id == null) {
                    mapTransactionDetails(it)
                } else {
                    it
                }
            }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _approvalState.value = loading }
            .doFinally { _approvalState.value = dismissLoading }
            .subscribe(
                {
                    isShowHelpIcon.onNext(true)
                    _approvalState.value = ShowApprovalCheckWriterDetail(it)
                }, {
                    Timber.e(it, "getCheckWriterDetails failed")
                    _approvalState.value = ShowApprovalDetailError(it)
                })
            .addTo(disposables)
    }

    fun getCashWithdrawalDetails(
        id: String,
        loading: ApprovalDetailState,
        dismissLoading: ApprovalDetailState,
        createdBy: String?
    ) {
        approvalGateway.getCashWithdrawalDetails(id)
            .zipWith(accountGateway.getAccountsPermission())
            .map { mapCashWithdrawalDetails(it.first, it.second, createdBy) }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _approvalState.value = loading }
            .doFinally { _approvalState.value = dismissLoading }
            .subscribe(
                {
                    isShowHelpIcon.onNext(true)
                    _approvalState.value = ShowApprovalBillsPaymentDetail(it)
                }, {
                    Timber.e(it, "getCashWithdrawalDetails failed")
                    _approvalState.value = ShowApprovalDetailError(it)
                })
            .addTo(disposables)
    }

    fun approval(
        approvalType: String,
        reasonForRejection: String? = null,
        batchIds: MutableList<String>,
        checkWriterBatchIds: MutableList<String>
    ) {
        approvalGateway.approval(
            ApprovalForm(approvalType, reasonForRejection, batchIds, checkWriterBatchIds)
        )
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _approvalState.value = ShowApprovalRequestLoading }
            .doFinally { _approvalState.value = ShowApprovalRequestDismissLoading }
            .subscribe(
                {
                    isFromApproveOrReject.onNext(true)
                    dataBus.approvalBatchIdDataBus.emmit(it!!)
                }, {
                    Timber.e(it, "Approval failed")
                    _approvalState.value = ShowApprovalDetailErrorSubmit(it)
                })
            .addTo(disposables)

    }

    fun cancelPendingApprovalTransaction(
        id: String,
        reasonForRejection: String,
        transactionType: String?
    ) {
        if (transactionType == ChannelBankEnum.BILLS_PAYMENT.value) {
            val cancelBillsPaymentTransactionForm = CancelBillsPaymentTransactionForm().apply {
                batchId = id
                this.reasonForRejection = reasonForRejection
            }
            billsPaymentGateway.cancelBillsPaymentTransaction(cancelBillsPaymentTransactionForm)
        } else {
            val cancelFundTransferTransactionForm = CancelFundTransferTransactionForm().apply {
                batchId = id
                this.reasonForCancellation = reasonForRejection
            }
            fundTransferGateway.cancelFundTransferTransaction(cancelFundTransferTransactionForm)
        }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _approvalState.value = ShowApprovalRequestLoading }
            .doFinally { _approvalState.value = ShowApprovalRequestDismissLoading }
            .subscribe(
                {
                    _approvalState.value = ShowApprovalDetailCancelTransaction
                }, {
                    Timber.e(it, "Approval failed")
                    _approvalState.value = ShowApprovalDetailErrorSubmit(it)
                })
            .addTo(disposables)

    }

    fun getUserDetails() {
        corporateGateway.getUserDetails()
            .subscribeOn(schedulerProvider.newThread())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                {
                    Timber.d(it.toString())
                    _approvalState.value =
                        ShowApprovalDetailGetUserDetails(
                            it
                        )
                }, {
                    Timber.e(it, "getUserDetails failed")
                    _approvalState.value = ShowApprovalDetailError(it)
                })
            .addTo(disposables)
    }

    fun deleteScheduledTransfer(id: Long, reasonForCancellation: String) {
        val scheduledTransferDeletionForm =
            ScheduledTransferDeletionForm(mutableListOf(id), reasonForCancellation)
        fundTransferGateway.deleteScheduledTransfer(scheduledTransferDeletionForm)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _approvalState.value = ShowApprovalRequestLoading }
            .doFinally { _approvalState.value = ShowApprovalRequestDismissLoading }
            .subscribe(
                {
                    _approvalState.value =
                        ShowApprovalDetailDeleteScheduledTransfer(it.message ?: "")
                }, {
                    Timber.e(it, "deleteBeneficiary Failed")
                    _approvalState.value = ShowApprovalDetailErrorSubmit(it)
                })
            .addTo(disposables)
    }

    private fun mapCashWithdrawalDetails(
        branchVisitDto: BranchVisitDto,
        accounts: MutableList<Account>,
        createdBy: String?
    ): Transaction {
        val transaction = Transaction()
        val detail = branchVisitDto.transactions?.get(0)?.detail
        transaction.id = branchVisitDto.id
        transaction.channel = ChannelBankEnum.CASH_WITHDRAWAL.name
        transaction.totalAmount = detail?.amount
        transaction.batchType = Constant.TYPE_SINGLE
        transaction.currency = branchVisitDto.currency
        transaction.transactionReferenceId = branchVisitDto.referenceId
        transaction.approvalProcessId = branchVisitDto.approvalProcessId
        transaction.createdBy = createdBy
        transaction.createdDate = branchVisitDto.createdDate
        transaction.branchTransactionDate = branchVisitDto.client?.transactionDate
        transaction.remarks = branchVisitDto.client?.remarks
        transaction.branchAddress = branchVisitDto.client?.branchAddress
        transaction.branchName = branchVisitDto.client?.branchName
        val account = accounts.find {
            it.accountNumber == detail?.accountNumber
        }
        transaction.sourceAccountName = account?.name
        transaction.sourceAccountNumber = account?.accountNumber
        transaction.sourceAccountType = account?.productCodeDesc

        val contextualClassStatus = when (branchVisitDto.status) {
            TransactionStatusEnum.PENDING_APPROVAL.name -> {
                Constant.ContextualClass.WARNING
            }
            TransactionStatusEnum.APPOINTMENT_ELAPSED.name,
            TransactionStatusEnum.APPOINTMENT_REJECTED.name,
            TransactionStatusEnum.TRANSACTION_FAILED.name,
            TransactionStatusEnum.EXPIRED.name,
            TransactionStatusEnum.CANCELLED.name -> {
                Constant.ContextualClass.DANGER
            }
            TransactionStatusEnum.TRANSACTION_PARTIALLY_SUCCESSFUL.name,
            TransactionStatusEnum.APPOINTMENT_SET.name,
            TransactionStatusEnum.TRANSACTION_SUCCESSFUL.name -> {
                Constant.ContextualClass.SUCCESS
            }
            else -> {
                Constant.ContextualClass.INFO
            }
        }
        val description =
            enumFromName(TransactionStatusEnum::class, branchVisitDto.status.notEmpty())

        transaction.transactionStatus = ContextualClassStatus(
            type = branchVisitDto.status,
            description = description.value,
            contextualClass = contextualClassStatus
        )
        val representativeName =
            "${detail?.representativeName?.firstName} ${detail?.representativeName?.lastName}"
        transaction.representativeName = representativeName

        return transaction
    }

    private fun mapTransactionDetails(transaction: Transaction): Transaction {
        transaction.id = transaction.transactionReferenceNumber
        transaction.channel = ChannelBankEnum.CHECK_WRITER.value
        transaction.serviceFee = if (transaction.serviceFee?.value == "0") {
            null
        } else {
            transaction.serviceFee
        }
        Observable.fromIterable(transaction.transactionDetails)
            .forEach {
                when (it.header) {
                    "remarks" -> transaction.remarks = it.value
                    "batch_type" -> transaction.batchType = it.value
                    "Amount" -> transaction.totalAmount = it.value
                    "Check Date" -> transaction.checkDate = it.value
                    "Check TypeCheck Type" -> transaction.checkType = it.value
                    "Payee Name" -> transaction.payeeName = it.value
                    "File Name" -> transaction.fileName = it.value
                    "number_of_transactions" -> transaction.numberOfTransactions = it.value
                }
            }.addTo(disposables)
        return transaction
    }

    private fun isEnabledFeature() {
        settingsGateway.isEnabledFeature(FeaturesEnum.TRANSACTION_CONTEXTUAL)
            .subscribeOn(schedulerProvider.newThread())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                {
                    contextualClassFeatureToggle.onNext(it)
                }, {
                    Timber.e(it, "isEnabledFeature failed")
                }
            ).addTo(disposables)
    }

}

sealed class ApprovalDetailState

data class ShowApprovalFundTransferDetail(val item: Transaction) : ApprovalDetailState()

data class ShowApprovalBillsPaymentDetail(val item: Transaction) : ApprovalDetailState()

data class ShowApprovalCheckWriterDetail(val item: Transaction) : ApprovalDetailState()

data class ShowApprovalDetailGetUserDetails(val userDetails: UserDetails) : ApprovalDetailState()

data class ShowApprovalDetailDeleteScheduledTransfer(val message: String) : ApprovalDetailState()

object ShowApprovalDetailCancelTransaction : ApprovalDetailState()

data class ShowApprovalDetailError(val throwable: Throwable) : ApprovalDetailState()

data class ShowApprovalDetailErrorSubmit(val throwable: Throwable) : ApprovalDetailState()

object ShowApprovalDetailInitialLoading : ApprovalDetailState()

object ShowApprovalDetailLoading : ApprovalDetailState()

object ShowApprovalDetailInitialDismissLoading : ApprovalDetailState()

object ShowApprovalDetailDismissLoading : ApprovalDetailState()

object ShowApprovalRequestLoading : ApprovalDetailState()

object ShowApprovalRequestDismissLoading : ApprovalDetailState()

