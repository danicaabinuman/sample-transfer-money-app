package com.unionbankph.corporate.ebilling.domain.form

import android.os.Parcelable
import com.unionbankph.corporate.account.data.model.Account
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Created by herald on 10/28/20
 */
@Parcelize
@Serializable
data class EBillingForm(
    @SerialName("deposit_to")
    var depositTo: Account? = null,
    @SerialName("currency")
    var currency: String? = null,
    @SerialName("amount")
    var amount: Double? = null,
    @SerialName("qr_code_path")
    var qrCodePath: String? = null
) : Parcelable