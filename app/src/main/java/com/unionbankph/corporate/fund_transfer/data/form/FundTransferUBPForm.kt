package com.unionbankph.corporate.fund_transfer.data.form

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FundTransferUBPForm(

    @SerialName("transfer_date")
    var transferDate: String? = null,

    @SerialName("recurrence_type_id")
    var recurrenceTypeId: String? = null,

    @SerialName("frequency")
    var frequency: String? = null,

    @SerialName("beneficiary_master_id")
    var beneficiaryMasterId: Int? = null,

    @SerialName("beneficiary_master")
    var beneficiaryMasterForm: BeneficiaryMasterForm? = null,

    @SerialName("recurrence_end_date")
    var recurrenceEndDate: String? = null,

    @SerialName("sender_account_number")
    var senderAccountNumber: String? = null,

    @SerialName("receiver_account_number")
    var receiverAccountNumber: String? = null,

    @SerialName("amount")
    var amount: Double? = null,

    @SerialName("remarks")
    var remarks: String? = null,

    @SerialName("immediate")
    var immediate: Boolean? = null,

    @SerialName("organization_id")
    var organizationId: String? = null,

    @SerialName("channel_id")
    var channelId: String? = null,

    @SerialName("occurrences")
    var occurrences: Int? = null,

    @SerialName("occurrences_text")
    var occurrencesText: String? = null,

    @SerialName("reference_number")
    var referenceNumber: String? = null
)
