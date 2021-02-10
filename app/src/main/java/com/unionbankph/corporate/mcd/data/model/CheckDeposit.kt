package com.unionbankph.corporate.mcd.data.model

import com.unionbankph.corporate.bills_payment.data.model.Amount
import com.unionbankph.corporate.common.data.model.ContextualClassStatus
import com.unionbankph.corporate.common.data.model.ServiceFee
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CheckDeposit(

    @SerialName("id")
    var id: String? = null,

    @SerialName("issuer")
    var issuer: String? = null,

    @SerialName("source_account")
    var sourceAccount: String? = null,

    @SerialName("reference_number")
    var referenceNumber: String? = null,

    @SerialName("source_account_name")
    var sourceAccountName: String? = null,

    @SerialName("check_number")
    var checkNumber: String? = null,

    @SerialName("check_date")
    var checkDate: String? = null,

    @SerialName("check_amount")
    var checkAmount: Amount? = null,

    @SerialName("currency")
    var currency: String? = null,

    @SerialName("service_fee")
    var serviceFee: ServiceFee? = null,

    @SerialName("custom_service_fee")
    var customServiceFee: ServiceFee? = null,

    @SerialName("target_account_name")
    var targetAccountName: String? = null,

    @SerialName("target_account")
    var targetAccount: String? = null,

    @SerialName("source_account_type")
    var sourceAccountType: String? = null,

    @SerialName("front_path")
    var frontPath: String? = null,

    @SerialName("back_path")
    var backPath: String? = null,

    @SerialName("created_date")
    var createdDate: String? = null,

    @SerialName("created_by")
    var createdBy: String? = null,

    @SerialName("qr_content")
    var qrContent: String? = null,

    @SerialName("contextual_status")
    var status: ContextualClassStatus? = null,

    @SerialName("reason_for_rejection")
    var reasonForRejection: String? = null,

    @SerialName("remarks")
    var remarks: String? = null
)
