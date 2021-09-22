package com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.sme.chip

import kotlinx.serialization.Serializable

@Serializable
data class GenericItem(
    var id: String? = null,
    var title: String? = null,
    var subtitle: String? = null,
    var action: String? = null,
    var src: String? = null,
    var isSelected: Boolean? = null,
    var isEnabled: Boolean? = null
)
