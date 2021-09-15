package com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.sme.chip

import kotlinx.serialization.Serializable

@Serializable
data class ChipItemModel(
    var id: String? = null,
    var label: String? = null,
    var action: String? = null,
    var isSelected: Boolean = false,
    var isEnabled: Boolean = true
)
