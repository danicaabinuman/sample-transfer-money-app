package com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.sme.generic_item_1

import kotlinx.serialization.Serializable

@Serializable
data class GenericItemModel(
    var id: String? = null,
    var label: String? = null,
    var caption: String? = null,
    var src: String? = null,
    var isEnabled: Boolean = true,
    var action: String? = null
)