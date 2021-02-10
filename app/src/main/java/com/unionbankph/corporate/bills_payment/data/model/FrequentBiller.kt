package com.unionbankph.corporate.bills_payment.data.model

import android.os.Parcelable
import com.unionbankph.corporate.account.data.model.Account
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class FrequentBiller(

    @SerialName("account_number")
    var accountNumber: String? = null,

    @SerialName("biller_name")
    var billerName: String? = null,

    @SerialName("code")
    var code: String? = null,

    @SerialName("created_by")
    var createdBy: String? = null,

    @SerialName("created_date")
    var createdDate: String? = null,

    @SerialName("fields")
    var fields: MutableList<Field> = mutableListOf(),

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
    var versionId: Int? = null,

    @SerialName("accounts")
    var accounts: MutableList<Account>? = null
) : Parcelable
