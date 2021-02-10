package com.unionbankph.corporate.account.data.model


import com.unionbankph.corporate.approval.data.model.Transaction
import com.unionbankph.corporate.bills_payment.data.model.Amount
import com.unionbankph.corporate.common.data.model.ContextualClassStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AccountTransactionHistoryDetails(
    @SerialName("account")
    var account: Account? = null,
    @SerialName("amount")
    var amount: Amount? = null,
    @SerialName("bills_payment")
    var billsPayment: BillsPayment? = null,
    @SerialName("check_number")
    var checkNumber: String? = null,
    @SerialName("description")
    var description: String? = null,
    @SerialName("portal_transaction")
    var portalTransaction: Transaction? = null,
    @SerialName("posting_date")
    var postingDate: String? = null,
    @SerialName("reference_number")
    var referenceNumber: String? = null,
    @SerialName("remittance")
    var remittance: String? = null,
    @SerialName("transaction_class")
    var transactionClass: ContextualClassStatus? = null,
    @SerialName("transaction_date")
    var transactionDate: String? = null,
    @SerialName("transaction_type")
    var transactionType: String? = null
)