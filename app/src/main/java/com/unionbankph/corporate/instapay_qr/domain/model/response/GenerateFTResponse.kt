package com.unionbankph.corporate.instapay_qr.domain.model.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class GenerateFTResponse(

    @SerialName ("uniqueId")
    var uniqueId: String? = null,

    @SerialName ("swiftCode")
    var swiftCode: String? = null,

    @SerialName ("hasAmount")
    var hasAmount: Boolean = false,

    @SerialName ("countryCode")
    var countryCode: String? = null,

    @SerialName ("currencyCode")
    var currencyCode: String? = null,

    @SerialName ("receivingBankId")
    var receivingBankId: String? = null,

    @SerialName ("beneficiaryName")
    var beneficiaryName: String? = null,

    @SerialName ("beneficiaryAccountNumber")
    var beneficiaryAccountNumber: String? = null,

    @SerialName ("amount")
    var amount: Double? = null,

    @SerialName ("bankData")
    var bankData: BankData? = null

) : Parcelable
