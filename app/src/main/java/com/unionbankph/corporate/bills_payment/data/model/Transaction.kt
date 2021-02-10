package com.unionbankph.corporate.bills_payment.data.model

import com.unionbankph.corporate.common.data.model.ContextualClassStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Transaction(

    @SerialName("amount")
    var amount: Amount? = null,

    @SerialName("batch_type")
    var batchType: String? = null,

    @SerialName("biller_id")
    var billerId: String? = null,

    @SerialName("biller_name")
    var billerName: String? = null,

    @SerialName("file_name")
    var fileName: String? = null,

    @SerialName("biller_service_id")
    var billerServiceId: String? = null,

    @SerialName("destination")
    var destination: String? = null,

    @SerialName("error_message")
    var errorMessage: String? = null,

    @SerialName("frequent_biller_version_id")
    var frequentBillerVersionId: String? = null,

    @SerialName("id")
    var id: String? = null,

    @SerialName("other_info")
    var otherInfo: MutableList<OtherInfo>? = null,

    @SerialName("particular")
    var particular: String? = null,

    @SerialName("payment_date")
    var paymentDate: String? = null,

    @SerialName("reference_number")
    var referenceNumber: String? = null,

    @SerialName("references")
    var references: MutableList<Reference>? = null,

    @SerialName("remarks")
    var remarks: String? = null,

    @SerialName("request_id")
    var requestId: String? = null,

    @SerialName("source")
    var source: String? = null,

    @SerialName("status")
    var status: ContextualClassStatus? = null,

    @SerialName("transaction_reference_id")
    var transactionReferenceId: String? = null,

    @SerialName("tran_date")
    var tranDate: String? = null,

    @SerialName("tran_id")
    var tranId: String? = null,

    @SerialName("qr_content")
    var qrContent: String? = null
)
