package com.unionbankph.corporate.common.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class PermissionCollection(

    @SerialName("create_transaction_ubp")
    var hasAllowToCreateTransactionUBP: Boolean = false,

    @SerialName("create_transaction_pesonet")
    var hasAllowToCreateTransactionPesonet: Boolean = false,

    @SerialName("create_transaction_pddts")
    var hasAllowToCreateTransactionPDDTS: Boolean = false,

    @SerialName("create_transaction_swift")
    var hasAllowToCreateTransactionSwift: Boolean = false,

    @SerialName("create_transaction_instapay")
    var hasAllowToCreateTransactionInstapay: Boolean = false,

    @SerialName("create_transaction_eon")
    var hasAllowToCreateTransactionEon: Boolean = false,

    @SerialName("create_transaction_own")
    var hasAllowToCreateTransactionOwn: Boolean = false,

    @SerialName("create_transaction")
    var hasAllowToCreateTransaction: Boolean = false,

    @SerialName("create_transaction_adhoc")
    var hasAllowToCreateTransactionAdhoc: Boolean = false,

    @SerialName("create_transaction_beneficiary_master")
    var hasAllowToCreateTransactionBeneficiaryMaster: Boolean = false,

    @SerialName("view_transaction_view_details")
    var hasAllowToViewTransactionViewDetails: Boolean = false,

    @SerialName("view_transaction")
    var hasAllowToViewTransaction: Boolean = false,

    @SerialName("create_transaction_scheduled")
    var hasAllowToCreateTransactionScheduled: Boolean = false,

    @SerialName("view_channel")
    var hasAllowToViewChannel: Boolean = false,

    @SerialName("view_transaction_delete_scheduled")
    var hasAllowToViewTransactionDeleteScheduled: Boolean = false,

    @SerialName("create_beneficiary_master")
    var hasAllowToCreateBeneficiaryMaster: Boolean = false,

    @SerialName("view_beneficiary_master")
    var hasAllowToViewBeneficiaryMaster: Boolean = false,

    @SerialName("btr_view_transaction")
    var hasAllowToBTRViewTransaction: Boolean = false,

    @SerialName("btr_view_balance")
    var hasAllowToBTRViewBalance: Boolean = false,

    @SerialName("create_bills_payment")
    var hasAllowToCreateBillsPayment: Boolean = false,

    @SerialName("create_bills_payment_adhoc")
    var hasAllowToCreateBillsPaymentAdhoc: Boolean = false,

    @SerialName("scheduled_bills_payment")
    var hasAllowToScheduledBillsPayment: Boolean = false,

    @SerialName("delete_scheduled_bills_payment")
    var hasAllowToDeleteScheduledBillsPayment: Boolean = false,

    @SerialName("view_bills_payment_history")
    var hasAllowToViewBillsPaymentHistory: Boolean = false,

    @SerialName("create_bills_payment_frequent")
    var hasAllowToCreateBillsPaymentFrequent: Boolean = false,

    @SerialName("view_bills_payment_history_details")
    var hasAllowToViewBillsPaymentHistoryDetails: Boolean = false,

    @SerialName("create_frequent_biller")
    var hasAllowToCreateFrequentBiller: Boolean = false,

    @SerialName("delete_frequent_biller")
    var hasAllowToDeleteFrequentBiller: Boolean = false
) : Parcelable {

    fun getPermissionCollectionAllowAll(): PermissionCollection {
        return PermissionCollection(
            hasAllowToCreateTransactionUBP = true,
            hasAllowToCreateTransactionPesonet = true,
            hasAllowToCreateTransactionPDDTS = true,
            hasAllowToCreateTransactionSwift = true,
            hasAllowToCreateTransactionInstapay = true,
            hasAllowToCreateTransactionEon = true,
            hasAllowToCreateTransactionOwn = true,
            hasAllowToCreateTransaction = true,
            hasAllowToCreateTransactionAdhoc = true,
            hasAllowToCreateTransactionBeneficiaryMaster = true,
            hasAllowToViewTransactionViewDetails = true,
            hasAllowToViewTransaction = true,
            hasAllowToCreateTransactionScheduled = true,
            hasAllowToViewChannel = true,
            hasAllowToViewTransactionDeleteScheduled = true,
            hasAllowToCreateBeneficiaryMaster = true,
            hasAllowToViewBeneficiaryMaster = true,
            hasAllowToBTRViewTransaction = true,
            hasAllowToBTRViewBalance = true,
            hasAllowToCreateBillsPayment = true,
            hasAllowToCreateBillsPaymentAdhoc = true,
            hasAllowToScheduledBillsPayment = true,
            hasAllowToDeleteScheduledBillsPayment = true,
            hasAllowToViewBillsPaymentHistory = true,
            hasAllowToCreateBillsPaymentFrequent = true,
            hasAllowToViewBillsPaymentHistoryDetails = true,
            hasAllowToCreateFrequentBiller = true,
            hasAllowToDeleteFrequentBiller = true
        )
    }
}
