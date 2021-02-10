package com.unionbankph.corporate.fund_transfer.data.model

import com.unionbankph.corporate.approval.data.model.ApprovalHierarchy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FundTransferVerify(

    @SerialName("type")
    var type: String? = null,

    @SerialName("validity")
    var validity: Int? = null,

    @SerialName("request_id")
    var requestId: String? = null,

    @SerialName("mobile_number")
    var mobileNumber: String? = null,

    @SerialName("invalid_attempts")
    var invalidAttempts: Int? = null,

    @SerialName("id")
    var id: String? = null,

    @SerialName("transactions")
    var transactions: MutableList<TransactionVerify> = mutableListOf(),

    @SerialName("immediate")
    var immediate: Boolean? = null,

    @SerialName("frequency_start_date")
    var frequencyStartDate: String? = null,

    @SerialName("frequency_end_date")
    var frequencyEndDate: String? = null,

    @SerialName("frequency_id")
    var frequencyId: String? = null,

    @SerialName("batch_reference_id")
    var batchReferenceId: String? = null,

    @SerialName("recurrence_status")
    var recurrenceStatus: String? = null,

    @SerialName("approval_hierarchy")
    var approvalHierarchy: ApprovalHierarchy? = null,

    @SerialName("filename")
    var filename: String? = null,

    @SerialName("gl_reference_id")
    var glReferenceId: String? = null,

    @SerialName("qr_content")
    var qrContent: String? = null,

    @SerialName("otp_type")
    var otpType: String? = null,

    @SerialName("created_by")
    var createdBy: String? = null,

    @SerialName("created_date")
    var createdDate: String? = null
)
