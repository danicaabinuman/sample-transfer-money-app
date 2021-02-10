package com.unionbankph.corporate.fund_transfer.data.form

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BeneficiaryMasterForm(

    @SerialName("beneficiary_address")
    var beneficiaryAddress: String? = null,

    @SerialName("beneficiary_bank_account_number")
    var beneficiaryBankAccountNumber: String? = null,

    @SerialName("beneficiary_bank_address")
    var beneficiaryBankAddress: String? = null,

    @SerialName("beneficiary_bank_name")
    var beneficiaryBankName: String? = null,

    @SerialName("beneficiary_code")
    var beneficiaryCode: String? = null,

    @SerialName("beneficiary_id")
    var beneficiaryId: Int? = null,

    @SerialName("beneficiary_mobile_number")
    var beneficiaryMobileNumber: String? = null,

    @SerialName("beneficiary_name")
    var beneficiaryName: String? = null,

    @SerialName("beneficiary_swift_code")
    var beneficiarySwiftCode: String? = null
)
