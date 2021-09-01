package com.unionbankph.corporate.fund_transfer.data.form

import kotlinx.serialization.*

@Serializable
data class BeneficiaryForm(

    @SerialName("name")
    var name: String? = null,

    @SerialName("nickname")
    var nickName: String? = null,

    @SerialName("address")
    var address: String? = null,

    @SerialName("email_address")
    var emailAddress: String? = null,

    @SerialName("code")
    var code: String? = null,

    @SerialName("swift_code")
    var swiftCode: String? = null,

    @SerialName("instapay_code")
    var instapayCode: String? = null,

    @SerialName("bank_name")
    var bankName: String? = null,

    @SerialName("account_number")
    var accountNumber: String? = null,

    @SerialName("bank_address")
    var bankAddress: String? = null,

    @SerialName("channel_id")
    var channelId: Int? = null,

    @SerialName("country_code_id")
    var countryCodeId: Int? = null,

    @SerialName("mobile_number")
    var mobileNumber: String? = null,

    @SerialName("organization_id")
    var organizationId: String? = null,

    @SerialName("brstn_code")
    var brstnCode: String? = null,

    @SerialName("firm_code")
    var firmCode: String? = null,

    @SerialName("all_accounts_selected")
    var allAccountsSelected: Boolean? = null,

    @SerialName("account_ids")
    var accountIds: MutableList<String>? = null,

    @SerialName("excluded_account_ids")
    var excludedAccountIds: MutableList<Int> = mutableListOf()
)
