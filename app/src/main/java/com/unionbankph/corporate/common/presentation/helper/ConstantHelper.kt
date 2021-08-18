package com.unionbankph.corporate.common.presentation.helper

import android.content.Context
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.approval.data.model.Transaction
import com.unionbankph.corporate.auth.data.model.RoleAccountPermissions
import com.unionbankph.corporate.branch.presentation.constant.BranchVisitStatusEnum
import com.unionbankph.corporate.common.data.model.ContextualClassStatus
import com.unionbankph.corporate.common.data.model.PermissionCollection
import com.unionbankph.corporate.common.presentation.constant.ChannelBankEnum
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.fund_transfer.data.form.BeneficiaryMasterForm
import com.unionbankph.corporate.fund_transfer.data.model.Beneficiary

class ConstantHelper {

    class Drawable {

        companion object {

            fun getGroupIconStatus(status: String?): Int {
                return when (status) {
                    Constant.STATUS_APPROVED -> R.drawable.ic_check_approved_green
                    Constant.STATUS_PENDING_NEXT_STEP -> R.drawable.ic_check_approved_green
                    Constant.STATUS_REJECTED -> R.drawable.ic_close_rejected_red
                    else -> 0
                }
            }

            fun getBatchIconStatus(status: ContextualClassStatus?): Int {
                return when (status?.contextualClass) {
                    Constant.ContextualClass.SUCCESS ->
                        R.drawable.ic_check_status_green
                    Constant.ContextualClass.DANGER ->
                        R.drawable.ic_warning_status_red
                    Constant.ContextualClass.WARNING ->
                        R.drawable.ic_warning_yellow
                    else -> 0
                }
            }

            fun getBackgroundStatusColor(status: String?): Int {
                return when (status) {
                    Constant.STATUS_UNTOUCHED -> R.drawable.bg_cardview_border_gray
                    Constant.STATUS_NOTIFIED,
                    Constant.STATUS_FOR_APPROVAL,
                    Constant.STATUS_IN_PROGRESS -> R.drawable.bg_cardview_border_yellow
                    Constant.STATUS_APPROVED,
                    Constant.STATUS_PENDING_NEXT_STEP -> R.drawable.bg_cardview_border_green
                    Constant.STATUS_REJECTED -> R.drawable.bg_cardview_border_red
                    else -> R.drawable.bg_cardview_border_gray
                }
            }

            fun getCorporateIconStatus(status: String?): Int {
                return when (status) {
                    Constant.STATUS_UNTOUCHED -> R.drawable.ic_eyeslash_gray
                    Constant.STATUS_FOR_APPROVAL,
                    Constant.STATUS_NOTIFIED -> R.drawable.ic_time_yellow
                    Constant.STATUS_APPROVED -> R.drawable.ic_thumb_green
                    Constant.STATUS_REJECTED -> R.drawable.ic_caution_red
                    else -> R.drawable.ic_eyeslash_gray
                }
            }

            fun getActivityLogBadge(status: ContextualClassStatus?): Int {
                return when (status?.contextualClass) {
                    Constant.ContextualClass.SUCCESS ->
                        R.drawable.circle_status_approved
                    Constant.ContextualClass.DANGER ->
                        R.drawable.circle_status_rejected
                    Constant.ContextualClass.WARNING ->
                        R.drawable.circle_status_pending
                    Constant.ContextualClass.INFO ->
                        R.drawable.circle_status_unseen
                    else -> R.drawable.circle_status_unseen
                }
            }

            fun getAccountTransactionType(status: ContextualClassStatus?): Int {
                return when {
                    status == null -> 0
                    status.contextualClass.equals(Constant.ContextualClass.DANGER) -> {
                        R.drawable.ic_account_minus_red
                    }
                    else -> {
                        R.drawable.ic_account_plus_green
                    }
                }
            }

            fun getDashboardActionIcon(action: String): Int {
                return when (action) {
                    Constant.DASHBOARD_ACTION_REQUEST_PAYMENT -> R.drawable.ic_dashboard_request_payment
                    Constant.DASHBOARD_ACTION_TRANSFER_FUNDS -> R.drawable.ic_dashboard_transfer_funds
                    Constant.DASHBOARD_ACTION_PAY_BILLS -> R.drawable.ic_dashboard_pay_bills
                    Constant.DASHBOARD_ACTION_DEPOSIT_CHECK -> R.drawable.ic_dashboard_deposit_check
                    Constant.DASHBOARD_ACTION_APPLY_LOAN -> R.drawable.ic_dashboard_apply_loan
                    Constant.DASHBOARD_ACTION_BRANCH_VISIT -> R.drawable.ic_dashboard_branch_visit
                    Constant.DASHBOARD_ACTION_MANAGE_EXPENSES -> R.drawable.ic_dashboard_manage_expenses
                    else -> R.drawable.ic_dashboard_more // Constant.DASHBOARD_ACTION_MORE
                }
            }
        }
    }

    class Color {

        companion object {

            fun getTextColor(status: ContextualClassStatus?): Int {
                return when (status?.contextualClass) {
                    Constant.ContextualClass.INACTIVE -> R.color.colorInActive
                    Constant.ContextualClass.SUCCESS -> R.color.colorSuccess
                    Constant.ContextualClass.DANGER -> R.color.colorDanger
                    Constant.ContextualClass.WARNING -> R.color.colorWarning
                    Constant.ContextualClass.INFO -> R.color.colorInfo
                    else -> R.color.colorInfo
                }
            }

            fun getTextColorBranchVisit(status: BranchVisitStatusEnum?): Int {
                return when (status) {
                    BranchVisitStatusEnum.TRANSACTION_SUCCESSFUL,
                    BranchVisitStatusEnum.APPOINTMENT_SET -> R.color.colorApprovedStatus
                    BranchVisitStatusEnum.EXPIRED,
                    BranchVisitStatusEnum.TRANSACTION_FAILED,
                    BranchVisitStatusEnum.APPOINTMENT_REJECTED,
                    BranchVisitStatusEnum.APPOINTMENT_ELAPSED -> R.color.colorRejectedStatus
                    BranchVisitStatusEnum.PENDING_APPROVAL -> R.color.colorPendingStatus
                    else -> R.color.colorDarkOverLay
                }
            }

            fun getStatusColor(status: String?): Int {
                return when (status) {
                    Constant.STATUS_UNTOUCHED -> R.color.colorUnSeenStatus
                    Constant.STATUS_NOTIFIED,
                    Constant.STATUS_FOR_APPROVAL,
                    Constant.STATUS_IN_PROGRESS -> R.color.colorApprovalHierarchyPending
                    Constant.STATUS_APPROVED -> R.color.colorApprovedStatus
                    Constant.STATUS_PENDING_NEXT_STEP -> R.color.colorApprovedStatus
                    Constant.STATUS_REJECTED -> R.color.colorRejectedStatus
                    else -> R.color.colorUnSeenStatus
                }
            }
        }
    }

    class Text {

        companion object {

            fun getRemarks(context: Context, transaction: Transaction?): String? {
                return if (transaction?.remarks == null || transaction.remarks == "") {
                    when {
                        transaction?.channel.equals(ChannelBankEnum.UBP_TO_UBP.value, true) ->
                            context.formatString(R.string.title_unionbank_account_transfer)
                        transaction?.channel.equals(ChannelBankEnum.PESONET.value, true) ->
                            context.formatString(
                                R.string.params_pesonet_transfer_to,
                                transaction?.beneficiaryName
                                    ?: transaction?.destinationAccountNumber.notNullable()
                            )
                        transaction?.channel.equals(ChannelBankEnum.SWIFT.value, true) ->
                            context.formatString(
                                R.string.params_swift_transfer_to,
                                transaction?.beneficiaryName
                                    ?: transaction?.destinationAccountNumber.notNullable()
                            )
                        transaction?.channel.equals(ChannelBankEnum.PDDTS.value, true) ->
                            context.formatString(
                                R.string.params_pddts_transfer_to,
                                transaction?.beneficiaryName
                                    ?: transaction?.destinationAccountNumber.notNullable()
                            )
                        transaction?.channel.equals(ChannelBankEnum.INSTAPAY.value, true) ->
                            context.formatString(
                                R.string.params_instapay_transfer_to,
                                transaction?.beneficiaryName
                                    ?: transaction?.destinationAccountNumber.notNullable()
                            )
                        transaction?.channel.equals(ChannelBankEnum.BILLS_PAYMENT.value, true) ->
                            context.formatString(
                                R.string.params_payment_to,
                                transaction?.billerName
                            )
                        transaction?.channel.equals(ChannelBankEnum.CHECK_WRITER.value, true) ->
                            context.formatString(
                                R.string.params_check_to,
                                transaction?.payeeName
                            )
                        transaction?.channel.equals(ChannelBankEnum.CASH_WITHDRAWAL.name, true) ->
                            context.formatString(
                                R.string.params_cash_withdrawal_from,
                                transaction?.sourceAccountNumber
                            )
                        else -> transaction?.channel ?: Constant.EMPTY
                    }
                } else {
                    transaction.remarks
                }
            }

            fun getRemarksCreated(
                context: Context,
                createdBy: String?,
                createdDate: String?,
                viewUtil: ViewUtil
            ): String {
                return String.format(
                    context.getString(R.string.params_created_by),
                    createdBy,
                    viewUtil.getDateFormatByDateString(
                        createdDate,
                        ViewUtil.DATE_FORMAT_ISO_WITHOUT_T,
                        ViewUtil.DATE_FORMAT_DEFAULT
                    )
                )
            }

            fun getChannelByChannelId(channelId: Int?): String {
                return when (channelId) {
                    ChannelBankEnum.UBP_TO_UBP.getChannelId() -> ChannelBankEnum.UBP_TO_UBP.value
                    ChannelBankEnum.PESONET.getChannelId() -> ChannelBankEnum.PESONET.value
                    ChannelBankEnum.PDDTS.getChannelId() -> ChannelBankEnum.PDDTS.value
                    ChannelBankEnum.SWIFT.getChannelId() -> ChannelBankEnum.SWIFT.value
                    ChannelBankEnum.INSTAPAY.getChannelId() -> ChannelBankEnum.INSTAPAY.value
                    ChannelBankEnum.BILLS_PAYMENT.getChannelId() -> ChannelBankEnum.BILLS_PAYMENT.value
                    else -> ChannelBankEnum.OTHER_BANKS.value
                }
            }

            fun getDashboardAccountButtonText(context: Context, accountSize: Int, isRefreshed: Boolean) : String {
                return when {
                    accountSize == 0 && !isRefreshed -> context.getString(R.string.action_add)
                    accountSize > 0 && !isRefreshed -> context.getString(R.string.action_view_all_account)
                    else -> ""
                }
            }
        }
    }

    class Object {

        companion object {

            fun getBeneficiaryForm(
                context: Context,
                beneficiaryMaster: Beneficiary
            ): BeneficiaryMasterForm {
                val beneficiaryMasterForm = BeneficiaryMasterForm()
                beneficiaryMasterForm.beneficiaryId = beneficiaryMaster.id
                beneficiaryMasterForm.beneficiaryAddress = beneficiaryMaster.address
                beneficiaryMasterForm.beneficiaryBankAddress =
                    beneficiaryMaster.bankDetails?.address
                beneficiaryMasterForm.beneficiaryBankAccountNumber =
                    beneficiaryMaster.accountNumber
                beneficiaryMasterForm.beneficiaryBankName =
                    if (beneficiaryMaster.channelId == ChannelBankEnum.UBP_TO_UBP.getChannelId())
                        context.getString(R.string.value_receiving_bank_ubp)
                    else beneficiaryMaster.bankDetails?.name
                beneficiaryMasterForm.beneficiaryCode = beneficiaryMaster.code
                beneficiaryMasterForm.beneficiaryMobileNumber = beneficiaryMaster.mobileNumber
                beneficiaryMasterForm.beneficiaryName = beneficiaryMaster.name
                beneficiaryMasterForm.beneficiarySwiftCode =
                    beneficiaryMaster.swiftBankDetails?.swiftBicCode
                return beneficiaryMasterForm
            }

            fun getSwiftBeneficiaryForm(beneficiaryMaster: Beneficiary): BeneficiaryMasterForm {
                val beneficiaryMasterRequest =
                    BeneficiaryMasterForm()
                beneficiaryMasterRequest.beneficiaryId = beneficiaryMaster.id
                beneficiaryMasterRequest.beneficiaryAddress = beneficiaryMaster.address
                beneficiaryMasterRequest.beneficiaryBankAddress =
                    beneficiaryMaster.swiftBankDetails?.let {
                        if (it.address1 == null && it.address2 == null) {
                            Constant.EMPTY
                        } else {
                            it.address1.notNullable() + it.address2.notNullable()
                        }
                    }
                beneficiaryMasterRequest.beneficiaryBankAccountNumber =
                    beneficiaryMaster.accountNumber
                beneficiaryMasterRequest.beneficiaryBankName =
                    beneficiaryMaster.swiftBankDetails?.bankName
                beneficiaryMasterRequest.beneficiaryCode = beneficiaryMaster.code
                beneficiaryMasterRequest.beneficiaryMobileNumber = beneficiaryMaster.mobileNumber
                beneficiaryMasterRequest.beneficiaryName = beneficiaryMaster.name
                beneficiaryMasterRequest.beneficiarySwiftCode =
                    beneficiaryMaster.swiftBankDetails?.swiftBicCode
                return beneficiaryMasterRequest
            }

            fun getAccountPermission(
                accountId: Int?,
                roleAccountPermissions: MutableList<RoleAccountPermissions>
            ): PermissionCollection {
                val permissionCollection = PermissionCollection()
                roleAccountPermissions
                    .filter { it.accountId == accountId }
                    .forEach {
                        it.productPermissions
                            ?.filter { it.name == "Balance and Transaction Reporting" }
                            ?.forEach { productPermission ->
                                productPermission.permissions.forEach { permission ->
                                    when (permission.code) {
                                        Constant.Permissions.CODE_FT_CREATETRANSACTIONS_UNIONBANK
                                        -> permissionCollection.hasAllowToCreateTransactionUBP =
                                            true
                                        Constant.Permissions.CODE_FT_CREATETRANSACTIONS_PESONET
                                        -> permissionCollection.hasAllowToCreateTransactionPesonet =
                                            true
                                        Constant.Permissions.CODE_FT_CREATETRANSACTIONS_PDDTS
                                        -> permissionCollection.hasAllowToCreateTransactionPDDTS =
                                            true
                                        Constant.Permissions.CODE_FT_CREATETRANSACTIONS_SWIFT
                                        -> permissionCollection.hasAllowToCreateTransactionSwift =
                                            true
                                        Constant.Permissions.CODE_FT_CREATETRANSACTIONS_INSTAPAY
                                        -> permissionCollection.hasAllowToCreateTransactionInstapay =
                                            true
                                        Constant.Permissions.CODE_FT_CREATETRANSACTIONS_EON
                                        -> permissionCollection.hasAllowToCreateTransactionEon =
                                            true
                                        Constant.Permissions.CODE_FT_CREATETRANSACTIONS_OWN
                                        -> permissionCollection.hasAllowToCreateTransactionOwn =
                                            true
                                        Constant.Permissions.CODE_FT_CREATETRANSACTIONS
                                        -> permissionCollection.hasAllowToCreateTransaction = true
                                        Constant.Permissions.CODE_FT_CREATETRANSACTIONS_ADHOC
                                        -> permissionCollection.hasAllowToCreateTransactionAdhoc =
                                            true
                                        Constant.Permissions.CODE_FT_CREATETRANSACTIONS_BENEFICIARYMASTER
                                        -> permissionCollection.hasAllowToCreateTransactionBeneficiaryMaster =
                                            true
                                        Constant.Permissions.CODE_FT_VIEWTRANSACTIONS_VIEWDETAILS
                                        -> permissionCollection.hasAllowToViewTransactionViewDetails =
                                            true
                                        Constant.Permissions.CODE_FT_VIEWTRANSACTIONS
                                        -> permissionCollection.hasAllowToViewTransaction = true
                                        Constant.Permissions.CODE_FT_SCHEDULEDTRANSACTIONS
                                        -> permissionCollection.hasAllowToCreateTransactionScheduled =
                                            true
                                        Constant.Permissions.CODE_FT_DELETESCHEDULED
                                        -> permissionCollection.hasAllowToViewTransactionDeleteScheduled =
                                            true
                                        Constant.Permissions.CODE_FT_CHANNELS
                                        -> permissionCollection.hasAllowToViewChannel = true
                                        Constant.Permissions.CODE_BM_CREATEBENEFICIARYMASTER
                                        -> permissionCollection.hasAllowToCreateBeneficiaryMaster =
                                            true
                                        Constant.Permissions.CODE_BM_VIEWBENEFICIARYMASTER
                                        -> permissionCollection.hasAllowToViewBeneficiaryMaster =
                                            true
                                        Constant.Permissions.CODE_BTR_VIEWTRANSACTIONS
                                        -> permissionCollection.hasAllowToBTRViewTransaction = true
                                        Constant.Permissions.CODE_BTR_VIEWBALANCES
                                        -> permissionCollection.hasAllowToBTRViewBalance = true
                                        Constant.Permissions.CODE_BP_CREATEBILLSPAYMENT_ADHOC
                                        -> permissionCollection.hasAllowToCreateBillsPaymentAdhoc =
                                            true
                                        Constant.Permissions.CODE_BP_CREATEBILLSPAYMENT
                                        -> permissionCollection.hasAllowToCreateBillsPayment = true
                                        Constant.Permissions.CODE_BP_SCHEDULEDPAYMENTS
                                        -> permissionCollection.hasAllowToScheduledBillsPayment =
                                            true
                                        Constant.Permissions.CODE_BP_DELETESCHEDULEDPAYMENTS
                                        -> permissionCollection.hasAllowToDeleteScheduledBillsPayment =
                                            true
                                        Constant.Permissions.CODE_BP_VIEWBPHISTORY
                                        -> permissionCollection.hasAllowToViewBillsPaymentHistory =
                                            true
                                        Constant.Permissions.CODE_BP_CREATEBILLSPAYMENT_FREQUENT
                                        -> permissionCollection.hasAllowToCreateBillsPaymentFrequent =
                                            true
                                        Constant.Permissions.CODE_BP_VIEWBPHISTORY_DETAILS
                                        -> permissionCollection.hasAllowToViewBillsPaymentHistoryDetails =
                                            true
                                        Constant.Permissions.CODE_BP_CREATEFREQUENTBILLER
                                        -> permissionCollection.hasAllowToCreateFrequentBiller =
                                            true
                                        Constant.Permissions.CODE_BP_DELETEFREQUENTBILLER
                                        -> permissionCollection.hasAllowToDeleteFrequentBiller =
                                            true
                                    }
                                }
                            }
                    }
                return permissionCollection
            }
        }
    }

}

//private fuz
