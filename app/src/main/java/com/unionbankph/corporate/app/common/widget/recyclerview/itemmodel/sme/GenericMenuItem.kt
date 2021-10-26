package com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.sme

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class GenericMenuItem(
    var id: String? = null,
    var title: String? = null,
    var subtitle: String? = null,
    var action: String? = null,
    var src: String? = null,
    var isSelected: Boolean? = null,
    var isEnabled: Boolean? = null,
    var isVisible: Boolean? = null
) : Parcelable
