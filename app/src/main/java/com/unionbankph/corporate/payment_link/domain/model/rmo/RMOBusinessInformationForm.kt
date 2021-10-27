package com.unionbankph.corporate.payment_link.domain.model.rmo

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Parcelize
@Serializable
data class RMOBusinessInformationForm(
    @SerialName("businessType")
    var businessType: String,
    @SerialName("product")
    var storeProduct: String,
    @SerialName("status")
    var infoStatus: String,
    @SerialName("yearsInBusiness")
    var yearsInBusiness: Int,
    @SerialName("numberOfEmployees")
    var numberOfEmployees: Int,
    @SerialName("numberOfBranches")
    var numberOfBranches: Int
//    @SerialName("physicalStore")
//    var physicalStore: String,
//    @SerialName("website")
//    var website: String,
//    @SerialName("lazadaUrl")
//    var lazadaUrl: String,
//    @SerialName("shopeeUrl")
//    var shopeeUrl: String,
//    @SerialName("facebookUrl")
//    var facebookUrl: String,
//    @SerialName("instagramUrl")
//    var instagramUrl: String
//    @SerialName("imageUrl1")
//    var imageUrl1: String,
//    @SerialName("imageUrl2")
//    var imageUrl2: String,
//    @SerialName("imageUrl3")
//    var imageUrl3: String,
//    @SerialName("imageUrl4")
//    var imageUrl4: String,
//    @SerialName("imageUrl5")
//    var imageUrl5: String,
//    @SerialName("imageUrl6")
//    var imageUrl6: String
//    @SerialName("photos")
//    var imageUrls: ArrayList<String>
) : Parcelable