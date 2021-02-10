package com.unionbankph.corporate.approval.data.model

import com.unionbankph.corporate.bills_payment.data.model.Fields
import com.unionbankph.corporate.common.data.model.ContextualClassStatus
import com.unionbankph.corporate.common.data.model.ServiceFee
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Transaction(
    
    @SerialName("id")
    var id: String? = null,
    
    @SerialName("frequency")
    var frequency: String? = null,
    
    @SerialName("channel")
    var channel: String? = null,
    
    @SerialName("remarks")
    var remarks: String? = null,
    
    @SerialName("currency")
    var currency: String? = null,
    
    @SerialName("purpose")
    var purpose: String? = null,
    
    @SerialName("fields")
    var fields: MutableList<Fields>? = null,
    
    @SerialName("immediate")
    var immediate: Boolean? = null,
    
    @SerialName("approved")
    var approved: Boolean? = null,
    
    @SerialName("maker")
    var maker: Boolean? = null,
    
    @SerialName("proposed_transaction_date")
    var proposedTransactionDate: String? = null,
    
    @SerialName("start_date")
    var startDate: String? = null,
    
    @SerialName("end_date")
    var endDate: String? = null,
    
    @SerialName("biller_name")
    var billerName: String? = null,
    
    @SerialName("file_name")
    var fileName: String? = null,
    
    @SerialName("beneficiary_code")
    var beneficiaryCode: String? = null,

    @SerialName("number_of_occurrences")
    var numberOfOccurrences: Int? = null,
    
    @SerialName("service_fee")
    var serviceFee: ServiceFee? = null,
    
    @SerialName("custom_service_fee")
    var customServiceFee: ServiceFee? = null,
    
    @SerialName("beneficiary_name")
    var beneficiaryName: String? = null,
    
    @SerialName("beneficiary_account_number")
    var beneficiaryAccountNumber: String? = null,
    
    @SerialName("beneficiary_address")
    var beneficiaryAddress: String? = null,
    
    @SerialName("transaction_status")
    var transactionStatus: ContextualClassStatus? = null,
    
    @SerialName("approval_status")
    var approvalStatus: String? = null,
    
    @SerialName("total_amount")
    var totalAmount: String? = null,
    
    @SerialName("created_by")
    var createdBy: String? = null,
    
    @SerialName("created_date")
    var createdDate: String? = null,
    
    @SerialName("posted_date")
    var postedDate: String? = null,
    
    @SerialName("source_account_number")
    var sourceAccountNumber: String? = null,
    
    @SerialName("destination_account_number")
    var destinationAccountNumber: String? = null,
    
    @SerialName("batch_type")
    var batchType: String? = null,
    
    @SerialName("approval_process_id")
    var approvalProcessId: String? = null,
    
    @SerialName("source_account_name")
    var sourceAccountName: String? = null,
    
    @SerialName("transaction_reference_id")
    var transactionReferenceId: String? = null,
    
    @SerialName("transaction_reference_number")
    var transactionReferenceNumber: String? = null,
    
    @SerialName("swift_code")
    var swiftCode: String? = null,
    
    @SerialName("bank_address")
    var bankAddress: String? = null,

    @SerialName("branch_address")
    var branchAddress: String? = null,

    @SerialName("branch_name")
    var branchName: String? = null,

    @SerialName("representative_name")
    var representativeName: String? = null,
    
    @SerialName("receiving_bank")
    var receivingBank: String? = null,
    
    @SerialName("source_account_type")
    var sourceAccountType: String? = null,
    
    @SerialName("number_of_transactions")
    var numberOfTransactions: String? = null,
    
    @SerialName("frequent_biller_version_id")
    var frequentBillerVersionId: String? = null,
    
    @SerialName("reason_for_rejection")
    var reasonForRejection: String? = null,

    @SerialName("reason_for_cancellation")
    var reasonForCancellation: String? = null,
    
    @SerialName("error_message")
    var errorMessage: String? = null,
    
    @SerialName("rejected_by")
    var rejectedBy: String? = null,

    @SerialName("cancelled_by")
    var cancelledBy: String? = null,
    
    @SerialName("transaction_type")
    var transactionType: String? = null,
    
    @SerialName("qr_content")
    var qrContent: String? = null,
    
    @SerialName("transaction_details")
    var transactionDetails: MutableList<TransactionDetails>? = null,
    
    @SerialName("check_type")
    var checkType: String? = null,
    
    @SerialName("ongoing")
    var ongoing: Boolean? = null,
    
    @SerialName("check_number")
    var checkNumber: String? = null,
    
    @SerialName("payee_name")
    var payeeName: String? = null,
    
    @SerialName("payee_tin")
    var payeeTIN: String? = null,
    
    @SerialName("payee_address")
    var payeeAddress: String? = null,
    
    @SerialName("printing_type")
    var printingType: String? = null,
    
    @SerialName("check_date")
    var checkDate: String? = null,

    @SerialName("branch_transaction_date")
    var branchTransactionDate: String? = null,
    
    @SerialName("checks")
    var checks: MutableList<Check>? = null,
    
    var hasSelected: Boolean = false
)