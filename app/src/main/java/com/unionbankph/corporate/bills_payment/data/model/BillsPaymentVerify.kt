package com.unionbankph.corporate.bills_payment.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BillsPaymentVerify(

    @SerialName("id")
    var id: String? = null,

    @SerialName("type")
    var type: String? = null,

    @SerialName("validity")
    val validity: Int? = null,

    @SerialName("request_id")
    var requestId: String? = null,

    @SerialName("mobile_number")
    var mobileNumber: String? = null,

    @SerialName("invalid_attempts")
    val invalidAttempts: Int? = null,

    @SerialName("approval_hierarchy")
    var approvalHierarchy: String? = null,

    @SerialName("frequency_end_date")
    var frequencyEndDate: String? = null,

    @SerialName("frequency_id")
    var frequencyId: String? = null,

    @SerialName("frequency_start_date")
    var frequencyStartDate: String? = null,

    @SerialName("immediate")
    var immediateX: Boolean? = null,

    @SerialName("is_immediate")
    var isImmediate: Boolean? = null,

    @SerialName("transactions")
    var transactions: MutableList<Transaction> = mutableListOf(),

    @SerialName("qr_content")
    var qrContent: String? = null,

    @SerialName("otp_type")
    var otpType: String? = null,

    @SerialName("created_by")
    var createdBy: String? = null,

    @SerialName("created_date")
    var createdDate: String? = null
)
