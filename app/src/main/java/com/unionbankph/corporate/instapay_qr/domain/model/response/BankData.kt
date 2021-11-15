package com.unionbankph.corporate.instapay_qr.domain.model.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class BankData(

    @SerialName("internalBankCode")
    var internalBankCode: String? = null,

    @SerialName ("bank")
    var bank: String? = null,

    @SerialName ("swiftCode")
    var swiftCode: String? = null,

    @SerialName ("bicfi")
    var bicfi: String? = null,

    @SerialName ("brstn")
    var brstn: String? = null,

    @SerialName ("bankCode")
    var bankCode: String? = null

) : Parcelable
