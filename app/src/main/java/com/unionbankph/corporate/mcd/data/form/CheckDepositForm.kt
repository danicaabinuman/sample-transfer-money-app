package com.unionbankph.corporate.mcd.data.form

import com.unionbankph.corporate.common.data.model.ServiceFee
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CheckDepositForm(

    @SerialName("id")
    var id: String? = null,

    @SerialName("issuer")
    var issuer: String? = null,

    @SerialName("brstn")
    var brstn: String? = null,

    @SerialName("source_account")
    var sourceAccount: String? = null,

    @SerialName("source_account_name")
    var sourceAccountName: String? = null,

    @SerialName("check_number")
    var checkNumber: String? = null,

    @SerialName("check_date")
    var checkDate: String? = null,

    @SerialName("check_amount")
    var checkAmount: String? = null,

    @SerialName("currency")
    var currency: String? = null,

    @SerialName("issuer_name")
    var issuerName: String? = null,

    @SerialName("service_fee")
    var serviceFee: ServiceFee? = null,

    @SerialName("target_account_name")
    var targetAccountName: String? = null,

    @SerialName("target_account")
    var targetAccount: String? = null,

    @SerialName("account_type")
    var accountType: String? = null,

    @SerialName("remarks")
    var remarks: String? = null,

    @SerialName("front_of_check_file_path")
    var frontOfCheckFilePath: String? = null,

    @SerialName("back_of_check_file_path")
    var backOfCheckFilePath: String? = null,

    @SerialName("front_key")
    var frontKey: String? = null,

    @SerialName("back_key")
    var backKey: String? = null
)
