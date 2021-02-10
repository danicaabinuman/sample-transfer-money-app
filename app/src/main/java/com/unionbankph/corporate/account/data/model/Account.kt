package com.unionbankph.corporate.account.data.model

import android.os.Parcelable
import com.unionbankph.corporate.common.data.model.PermissionCollection
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Account(

    @SerialName("id")
    var id: Int? = null,

    @SerialName("status")
    var status: Status? = null,

    @SerialName("card")
    var card: Status? = null,

    @SerialName("currency")
    var currency: String? = null,

    @SerialName("headers")
    var headers: MutableList<Headers> = mutableListOf(),

    @SerialName("name")
    var name: String? = null,

    @SerialName("account_number")
    var accountNumber: String? = null,

    @SerialName("product_code")
    var productCode: String? = null,

    @SerialName("product_code_desc")
    var productCodeDesc: String? = null,

    @SerialName("product_type")
    var productType: String? = null,

    @SerialName("permission_collection")
    var permissionCollection: PermissionCollection = PermissionCollection(),

    @SerialName("isLoading")
    var isLoading: Boolean = false,

    @SerialName("isError")
    var isError: Boolean = false,

    @SerialName("isSelected")
    var isSelected: Boolean = false,

    @SerialName("isViewableBalance")
    var isViewableBalance: Boolean = true,

    @SerialName("isViewableCheckBox")
    var isViewableCheckBox: Boolean = true
): Parcelable
