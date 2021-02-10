package com.unionbankph.corporate.fund_transfer.data.model

import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.auth.data.model.CountryCode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreationBeneficiaryDto(

    @SerialName("account_number")
    var accountNumber: String? = null,

    @SerialName("accounts")
    var accounts: MutableList<Account> = mutableListOf(),

    @SerialName("address")
    var address: String? = null,

    @SerialName("bank_details")
    var bankDetails: BankDetails? = null,

    @SerialName("channel_id")
    var channelId: Int? = null,

    @SerialName("code")
    var code: String? = null,

    @SerialName("country_code_id")
    var countryCodeId: String? = null,

    @SerialName("country_code")
    var countryCode: CountryCode? = null,

    @SerialName("created_by")
    var createdBy: String? = null,

    @SerialName("created_date")
    var createdDate: String? = null,

    @SerialName("email_address")
    var emailAddress: String? = null,

    @SerialName("id")
    var id: String? = null,

    @SerialName("mobile_number")
    var mobileNumber: String? = null,

    @SerialName("modified_by")
    var modifiedBy: String? = null,

    @SerialName("modified_date")
    var modifiedDate: String? = null,

    @SerialName("name")
    var name: String? = null,

    @SerialName("nickname")
    var nickname: String? = null,

    @SerialName("organization_id")
    var organizationId: String? = null,

    @SerialName("instapay_code")
    var instapayCode: String? = null,

    @SerialName("swift_bank_details")
    var swiftBankDetails: SwiftBankDetails? = null
)
