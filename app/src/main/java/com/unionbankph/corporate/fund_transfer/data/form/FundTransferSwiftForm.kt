package com.unionbankph.corporate.fund_transfer.data.form

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FundTransferSwiftForm(

    @SerialName("transfer_date")
    var transferDate: String? = null,

    @SerialName("recurrence_type_id")
    var recurrenceTypeId: String? = null,

    @SerialName("frequency")
    var frequency: String? = null,

    @SerialName("beneficiary_name")
    var beneficiaryName: String? = null,

    @SerialName("beneficiary_address")
    var beneficiaryAddress: String? = null,

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

    @SerialName("purpose")
    var purpose: String? = null,

    @SerialName("purpose_desc")
    var purposeDesc: String? = null,

    @SerialName("instructions")
    var instructions: String? = null,

    @SerialName("receiving_bank")
    var receivingBank: String? = null,

    @SerialName("receiving_bank_address")
    var receivingBankAddress: String? = null,

    @SerialName("immediate")
    var immediate: Boolean? = null,

    @SerialName("organization_id")
    var organizationId: String? = null,

    @SerialName("channel_id")
    var channelId: String? = null,

    @SerialName("swift_code")
    var swiftCode: String? = null,

    @SerialName("occurrences")
    var occurrences: Int? = null,

    @SerialName("occurrences_text")
    var occurrencesText: String? = null,

    @SerialName("reference_number")
    var referenceNumber: String? = null
)
