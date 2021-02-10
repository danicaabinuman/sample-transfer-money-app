package com.unionbankph.corporate.bills_payment.data.model

import com.unionbankph.corporate.account.data.model.Account
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreationFrequentBillerDto(

    @SerialName("account_number")
    var accountNumber: String? = null,

    @SerialName("accounts")
    var accounts: MutableList<Account> = mutableListOf(),

    @SerialName("biller_name")
    var billerName: String? = null,

    @SerialName("code")
    var code: String? = null,

    @SerialName("created_by")
    var createdBy: String? = null,

    @SerialName("created_date")
    var createdDate: String? = null,

    @SerialName("fields")
    var fields: MutableList<Fields> = mutableListOf(),

    @SerialName("id")
    var id: String? = null,

    @SerialName("name")
    var name: String? = null,

    @SerialName("service_id")
    var serviceId: String? = null,

    @SerialName("short_name")
    var shortName: String? = null,

    @SerialName("success_message")
    var successMessage: String? = null,

    @SerialName("version_id")
    var versionId: Int? = null
)
