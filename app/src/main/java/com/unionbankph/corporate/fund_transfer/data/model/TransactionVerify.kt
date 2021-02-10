package com.unionbankph.corporate.fund_transfer.data.model

import com.unionbankph.corporate.common.data.model.ContextualClassStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TransactionVerify(

    @SerialName("amount")
    var amount: String? = null,

    @SerialName("currency")
    var currency: String? = null,

    @SerialName("status")
    var status: ContextualClassStatus? = null,

    @SerialName("purpose")
    var purpose: String? = null,

    @SerialName("receiver_account_number")
    var receiverAccountNumber: String? = null,

    @SerialName("sender_account_number")
    var senderAccountNumber: String? = null,

    @SerialName("transfer_date")
    var transferDate: String? = null,

    @SerialName("channel_id")
    var channelId: String? = null,

    @SerialName("request_id")
    var requestId: String? = null,

    @SerialName("transaction_reference_id")
    var transactionReferenceId: String? = null,

    @SerialName("beneficiary_name")
    var beneficiaryName: String? = null,

    @SerialName("swift_code")
    var swiftCode: String? = null,

    @SerialName("receiving_bank")
    var receivingBank: String? = null,

    @SerialName("remarks")
    var remarks: String? = null,

    @SerialName("error_message")
    var errorMessage: String? = null,

    @SerialName("fund_transfer_upload_detail_id")
    var fundTransferUploadDetailId: String? = null,

    @SerialName("upload_status")
    var uploadStatus: ContextualClassStatus? = null
)
